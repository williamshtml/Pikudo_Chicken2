package com.pikudo.restaurant.service.sunat.impl;

import com.pikudo.restaurant.config.properties.SunatProperties;
import com.pikudo.restaurant.entity.Comprobante;
import com.pikudo.restaurant.entity.EstadoSunat;
import com.pikudo.restaurant.entity.NotaCredito;
import com.pikudo.restaurant.entity.NotaDebito;
import com.pikudo.restaurant.entity.Pedido;
import com.pikudo.restaurant.entity.sunat.SunatSubmissionJob;
import com.pikudo.restaurant.entity.sunat.SunatSubmissionStatus;
import com.pikudo.restaurant.service.storage.StoragePurpose;
import com.pikudo.restaurant.service.storage.StorageService;
import com.pikudo.restaurant.service.storage.StorageUploadRequest;
import com.pikudo.restaurant.service.storage.StoredFile;
import com.pikudo.restaurant.service.sunat.SunatDocumentProcessor;
import io.github.project.openubl.xbuilder.signature.CertificateDetails;
import io.github.project.openubl.xbuilder.signature.CertificateDetailsFactory;
import io.github.project.openubl.xbuilder.signature.XMLSigner;
import io.github.project.openubl.xsender.company.CompanyCredentials;
import io.github.project.openubl.xsender.files.ZipFile;
import io.github.project.openubl.xsender.sunat.BillServiceDestination;
import io.github.project.openubl.xsender.camel.utils.CamelUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class SunatDocumentProcessorImpl implements SunatDocumentProcessor {

    private final SunatProperties properties;
    private final StorageService storageService;

    @Override
    @Transactional
    public void process(SunatSubmissionJob job) {
        if (!properties.isEnabled()) {
            job.setLastError("SUNAT deshabilitado por configuracion");
            return;
        }

        try {
            String xml = unsignedXml(job);
            byte[] signedXml = sign(xml);
            String filename = documentFilename(job) + ".xml";
            StoredFile xmlFile = storageService.upload(new StorageUploadRequest(
                    new ByteArrayInputStream(signedXml),
                    filename,
                    "application/xml",
                    signedXml.length,
                    StoragePurpose.SUNAT_DOCUMENT,
                    "sunat",
                    documentOwnerId(job),
                    null
            ));

            attachXml(job, xmlFile.id());
            markSent(job, signedXml);
            if (shouldSend()) {
                prepareXSenderPayload(signedXml, filename);
                markRetry(job, "Envio SUNAT/OSE requiere validacion sandbox con credenciales reales antes de activar respuesta final.");
            } else {
                markAccepted(job, "0", "XML firmado y almacenado. Envio omitido por modo disabled.");
            }
        } catch (Exception e) {
            markRetry(job, e.getMessage());
        }
    }

    private boolean shouldSend() {
        return !"disabled".equalsIgnoreCase(properties.getMode());
    }

    private byte[] sign(String xml) throws Exception {
        byte[] pfx = Base64.getDecoder().decode(properties.getPfxBase64());
        CertificateDetails details = CertificateDetailsFactory.create(new ByteArrayInputStream(pfx), properties.getPfxPassword());
        Document signed = XMLSigner.signXML(xml, "signatureKG", details.getX509Certificate(), details.getPrivateKey());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        var transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.transform(new DOMSource(signed), new StreamResult(out));
        return out.toByteArray();
    }

    private void prepareXSenderPayload(byte[] signedXml, String filename) {
        ZipFile zipFile = ZipFile.builder()
                .filename(filename.replace(".xml", ".zip"))
                .file(signedXml)
                .build();
        BillServiceDestination destination = BillServiceDestination.builder()
                .url(resolveEndpoint())
                .build();
        CompanyCredentials credentials = CompanyCredentials.builder()
                .username(properties.getSolUsername())
                .password(properties.getSolPassword())
                .build();
        CamelUtils.getBillServiceCamelData(zipFile, destination, credentials);
    }

    private String resolveEndpoint() {
        return "prod".equalsIgnoreCase(properties.getMode()) ? properties.getEndpointProd() : properties.getEndpointBeta();
    }

    private String unsignedXml(SunatSubmissionJob job) {
        if (job.getComprobante() != null) {
            return invoiceXml(job.getComprobante());
        }
        if (job.getNotaCredito() != null) {
            return creditNoteXml(job.getNotaCredito());
        }
        return debitNoteXml(job.getNotaDebito());
    }

    private String invoiceXml(Comprobante c) {
        Pedido pedido = c.getPedido();
        String typeCode = switch (c.getTipoComprobante()) {
            case FACTURA -> "01";
            default -> "03";
        };
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <Invoice xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2"
                         xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"
                         xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"
                         xmlns:ext="urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2">
                  <ext:UBLExtensions><ext:UBLExtension><ext:ExtensionContent/></ext:UBLExtension></ext:UBLExtensions>
                  <cbc:UBLVersionID>2.1</cbc:UBLVersionID>
                  <cbc:CustomizationID>2.0</cbc:CustomizationID>
                  <cbc:ID>%s-%s</cbc:ID>
                  <cbc:IssueDate>%s</cbc:IssueDate>
                  <cbc:InvoiceTypeCode>%s</cbc:InvoiceTypeCode>
                  <cbc:DocumentCurrencyCode>PEN</cbc:DocumentCurrencyCode>
                  <cac:AccountingSupplierParty><cac:Party><cac:PartyIdentification><cbc:ID>%s</cbc:ID></cac:PartyIdentification></cac:Party></cac:AccountingSupplierParty>
                  <cac:AccountingCustomerParty><cac:Party><cac:PartyIdentification><cbc:ID>%s</cbc:ID></cac:PartyIdentification><cac:PartyLegalEntity><cbc:RegistrationName>%s</cbc:RegistrationName></cac:PartyLegalEntity></cac:Party></cac:AccountingCustomerParty>
                  <cac:LegalMonetaryTotal><cbc:PayableAmount currencyID="PEN">%s</cbc:PayableAmount></cac:LegalMonetaryTotal>
                  <cac:InvoiceLine><cbc:ID>1</cbc:ID><cbc:InvoicedQuantity unitCode="NIU">1</cbc:InvoicedQuantity><cbc:LineExtensionAmount currencyID="PEN">%s</cbc:LineExtensionAmount><cac:Item><cbc:Description>Pedido %s</cbc:Description></cac:Item></cac:InvoiceLine>
                </Invoice>
                """.formatted(
                esc(c.getSerie()), esc(c.getCorrelativo()),
                c.getFechaEmision().toLocalDate(),
                typeCode,
                esc(properties.getRuc()),
                esc(c.getNumeroDocumentoCliente() != null ? c.getNumeroDocumentoCliente() : "00000000"),
                esc(c.getClienteNombreSnapshot() != null ? c.getClienteNombreSnapshot() : "PUBLICO_GENERAL"),
                money(c.getMontoTotal()),
                money(c.getMontoNeto()),
                pedido != null ? pedido.getId() : c.getId()
        );
    }

    private String creditNoteXml(NotaCredito n) {
        return noteXml("CreditNote", n.getSerie(), n.getCorrelativo(), n.getFechaEmision(), n.getMontoDevuelto(), "07");
    }

    private String debitNoteXml(NotaDebito n) {
        return noteXml("DebitNote", n.getSerie(), n.getCorrelativo(), n.getFechaEmision(), n.getMontoAdicional(), "08");
    }

    private String noteXml(String root, String serie, String correlativo, LocalDateTime fecha, BigDecimal total, String typeCode) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <%s xmlns="urn:oasis:names:specification:ubl:schema:xsd:%s-2"
                    xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"
                    xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"
                    xmlns:ext="urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2">
                  <ext:UBLExtensions><ext:UBLExtension><ext:ExtensionContent/></ext:UBLExtension></ext:UBLExtensions>
                  <cbc:UBLVersionID>2.1</cbc:UBLVersionID>
                  <cbc:CustomizationID>2.0</cbc:CustomizationID>
                  <cbc:ID>%s-%s</cbc:ID>
                  <cbc:IssueDate>%s</cbc:IssueDate>
                  <cbc:DocumentCurrencyCode>PEN</cbc:DocumentCurrencyCode>
                  <cbc:Note>Documento tributario generado por Pikudo</cbc:Note>
                  <cac:TaxTotal><cbc:TaxAmount currencyID="PEN">0.00</cbc:TaxAmount></cac:TaxTotal>
                  <cac:LegalMonetaryTotal><cbc:PayableAmount currencyID="PEN">%s</cbc:PayableAmount></cac:LegalMonetaryTotal>
                </%s>
                """.formatted(root, root, esc(serie), esc(correlativo), fecha.toLocalDate(), money(total), root);
    }

    private void attachXml(SunatSubmissionJob job, java.util.UUID storageId) {
        job.setLastError(null);
        if (job.getComprobante() != null) {
            job.getComprobante().setXmlStorageFile(com.pikudo.restaurant.entity.storage.StorageFile.builder().id(storageId).build());
        } else if (job.getNotaCredito() != null) {
            job.getNotaCredito().setXmlStorageFile(com.pikudo.restaurant.entity.storage.StorageFile.builder().id(storageId).build());
        } else if (job.getNotaDebito() != null) {
            job.getNotaDebito().setXmlStorageFile(com.pikudo.restaurant.entity.storage.StorageFile.builder().id(storageId).build());
        }
    }

    private void markSent(SunatSubmissionJob job, byte[] signedXml) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        String hash = sha256(signedXml);
        job.setAttempts(job.getAttempts() + 1);
        job.setSunatSentAt(now);
        if (job.getComprobante() != null) {
            job.getComprobante().setSunatAttempts(job.getAttempts());
            job.getComprobante().setSunatSentAt(now);
            job.getComprobante().setHashSunat(hash);
            job.getComprobante().setEstadoSunat(EstadoSunat.PENDIENTE);
        }
        if (job.getNotaCredito() != null) {
            job.getNotaCredito().setSunatAttempts(job.getAttempts());
            job.getNotaCredito().setSunatSentAt(now);
            job.getNotaCredito().setHashSunat(hash);
            job.getNotaCredito().setEstadoSunat(EstadoSunat.PENDIENTE);
        }
        if (job.getNotaDebito() != null) {
            job.getNotaDebito().setSunatAttempts(job.getAttempts());
            job.getNotaDebito().setSunatSentAt(now);
            job.getNotaDebito().setHashSunat(hash);
            job.getNotaDebito().setEstadoSunat(EstadoSunat.PENDIENTE);
        }
    }

    private void markAccepted(SunatSubmissionJob job, String code, String description) {
        LocalDateTime now = LocalDateTime.now();
        job.setStatus(SunatSubmissionStatus.ACCEPTED);
        job.setSunatResponseCode(code);
        job.setSunatResponseDescription(description);
        job.setSunatAcceptedAt(now);
        updateDocumentSunat(job, EstadoSunat.ACEPTADO, code, description, now);
    }

    private void markRetry(SunatSubmissionJob job, String message) {
        job.setStatus(job.getAttempts() >= properties.getMaxAttempts()
                ? SunatSubmissionStatus.FAILED_FINAL
                : SunatSubmissionStatus.FAILED_RETRYABLE);
        job.setLastError(message);
        job.setNextRetryAt(LocalDateTime.now().plusMinutes(properties.getRetryDelayMinutes()));
        updateDocumentSunat(job, EstadoSunat.PENDIENTE, null, message, null);
    }

    private void updateDocumentSunat(SunatSubmissionJob job, EstadoSunat estado, String code, String description, LocalDateTime acceptedAt) {
        if (job.getComprobante() != null) {
            job.getComprobante().setEstadoSunat(estado);
            job.getComprobante().setMensajeSunat(description);
            job.getComprobante().setSunatResponseCode(code);
            job.getComprobante().setSunatResponseDescription(description);
            job.getComprobante().setSunatAcceptedAt(acceptedAt);
        }
        if (job.getNotaCredito() != null) {
            job.getNotaCredito().setEstadoSunat(estado);
            job.getNotaCredito().setMensajeSunat(description);
            job.getNotaCredito().setSunatResponseCode(code);
            job.getNotaCredito().setSunatResponseDescription(description);
            job.getNotaCredito().setSunatAcceptedAt(acceptedAt);
        }
        if (job.getNotaDebito() != null) {
            job.getNotaDebito().setEstadoSunat(estado);
            job.getNotaDebito().setMensajeSunat(description);
            job.getNotaDebito().setSunatResponseCode(code);
            job.getNotaDebito().setSunatResponseDescription(description);
            job.getNotaDebito().setSunatAcceptedAt(acceptedAt);
        }
    }

    private String documentFilename(SunatSubmissionJob job) {
        if (job.getComprobante() != null) {
            return properties.getRuc() + "-" + job.getComprobante().getSerie() + "-" + job.getComprobante().getCorrelativo();
        }
        if (job.getNotaCredito() != null) {
            return properties.getRuc() + "-" + job.getNotaCredito().getSerie() + "-" + job.getNotaCredito().getCorrelativo();
        }
        return properties.getRuc() + "-" + job.getNotaDebito().getSerie() + "-" + job.getNotaDebito().getCorrelativo();
    }

    private String documentOwnerId(SunatSubmissionJob job) {
        if (job.getComprobante() != null) {
            return String.valueOf(job.getComprobante().getId());
        }
        if (job.getNotaCredito() != null) {
            return "NC-" + job.getNotaCredito().getId();
        }
        return "ND-" + job.getNotaDebito().getId();
    }

    private String sha256(byte[] bytes) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
        StringBuilder out = new StringBuilder();
        for (byte b : digest) {
            out.append(String.format("%02x", b));
        }
        return out.toString();
    }

    private String money(BigDecimal value) {
        return value == null ? "0.00" : value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private String esc(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
