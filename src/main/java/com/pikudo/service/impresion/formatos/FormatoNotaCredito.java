package com.pikudo.service.impresion.formatos;

import com.pikudo.entity.NotaCredito;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Arma el contenido del ticket de nota de credito (anulacion de un comprobante).
 * Se imprime en la impresora de CAJA, igual que boleta/factura/precuenta.
 *
 * NOTA: al igual que boleta/factura, no incluye QR de SUNAT todavia
 * (pendiente de la decision de negocio sobre OSE/PSE).
 */
@Component
public class FormatoNotaCredito {

    private static final byte[] INIT = {0x1B, 0x40};
    private static final byte[] BOLD_ON = {0x1B, 0x45, 0x01};
    private static final byte[] BOLD_OFF = {0x1B, 0x45, 0x00};
    private static final byte[] CENTER = {0x1B, 0x61, 0x01};
    private static final byte[] LEFT = {0x1B, 0x61, 0x00};
    private static final byte[] DOUBLE_SIZE = {0x1D, 0x21, 0x11};
    private static final byte[] NORMAL_SIZE = {0x1D, 0x21, 0x00};
    private static final byte[] CUT = {0x1D, 0x56, 0x41, 0x00};

    public byte[] construir(NotaCredito notaCredito) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write(INIT);
            out.write(CENTER);
            out.write(BOLD_ON);
            escribir(out, "PIKUDO CHICKEN\n");
            out.write(NORMAL_SIZE);
            out.write(BOLD_OFF);
            escribir(out, "\n");

            out.write(DOUBLE_SIZE);
            out.write(BOLD_ON);
            escribir(out, "NOTA DE CREDITO\n");
            out.write(NORMAL_SIZE);
            escribir(out, notaCredito.getSerie() + "-" + notaCredito.getCorrelativo() + "\n");
            out.write(BOLD_OFF);
            escribir(out, "\n");

            out.write(LEFT);
            escribir(out, "Fecha: " + notaCredito.getFechaEmision() + "\n");

            var comprobante = notaCredito.getComprobante();
            escribir(out, "Anula comprobante: " + comprobante.getSerie() + "-" + comprobante.getCorrelativo() + "\n");
            escribir(out, "Pedido original: #" + comprobante.getPedido().getId() + "\n");

            if (notaCredito.getUsuarioEmisor() != null) {
                escribir(out, "Autorizado por: " + notaCredito.getUsuarioEmisor().getUsername() + "\n");
            }

            escribir(out, "--------------------------------\n");
            escribir(out, "Motivo:\n" + notaCredito.getMotivo() + "\n");
            escribir(out, "--------------------------------\n");

            out.write(BOLD_ON);
            escribir(out, "Monto devuelto: S/ " + notaCredito.getMontoDevuelto() + "\n");
            out.write(BOLD_OFF);

            // TODO: cuando se integre un OSE/PSE, imprimir aqui el codigo QR de la nota de credito

            escribir(out, "\n");
            out.write(CENTER);
            escribir(out, "Documento de anulacion interno\n\n\n");
            out.write(CUT);
        } catch (IOException e) {
            throw new RuntimeException("Error al construir nota de credito", e);
        }
        return out.toByteArray();
    }

    private void escribir(ByteArrayOutputStream out, String texto) throws IOException {
        out.write(texto.getBytes(StandardCharsets.UTF_8));
    }
}