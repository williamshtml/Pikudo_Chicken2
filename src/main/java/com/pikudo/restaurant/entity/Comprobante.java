package com.pikudo.restaurant.entity;

import com.pikudo.restaurant.entity.caja.TransaccionPago;
import com.pikudo.restaurant.entity.storage.StorageFile;
import jakarta.persistence.Index;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comprobantes", indexes = {
    @Index(name = "idx_serie_correlativo", columnList = "serie, correlativo", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comprobante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El pedido es obligatorio")
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @NotNull(message = "El tipo de comprobante es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comprobante", nullable = false, length = 20)
    private TipoComprobante tipoComprobante;

    @NotBlank(message = "La serie es obligatoria")
    @Column(nullable = false, length = 10)
    private String serie;

    @NotBlank(message = "El número correlativo es obligatorio")
    @Column(nullable = false, length = 20)
    private String correlativo;

    @NotNull(message = "El monto neto es obligatorio")
    @PositiveOrZero(message = "El monto neto no puede ser negativo")
    @Column(name = "monto_neto", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoNeto;

    @NotNull(message = "El IGV es obligatorio")
    @PositiveOrZero(message = "El IGV no puede ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal igv;

    @NotNull(message = "El monto total es obligatorio")
    @PositiveOrZero(message = "El monto total no puede ser negativo")
    @Column(name = "monto_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;

    @Pattern(regexp = "^[0-9]{11}$", message = "El RUC debe tener exactamente 11 dígitos numéricos")
    @Column(length = 11)
    private String ruc;

    @Column(name = "razon_social", length = 150)
    private String razonSocial;

    @Builder.Default
    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision = LocalDateTime.now();

    @Builder.Default
    @OneToMany(mappedBy = "comprobante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransaccionPago> pagos = new ArrayList<>();

    // --- Estado interno del comprobante (emitido / anulado) ---
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoComprobante estado = EstadoComprobante.EMITIDO;

    // --- Integración SUNAT (se completa cuando se conecte el proveedor) ---
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_sunat", length = 30)
    private EstadoSunat estadoSunat = EstadoSunat.NO_ENVIADO;

    @Column(name = "hash_sunat", length = 100)
    private String hashSunat;

    @Column(name = "mensaje_sunat", length = 500)
    private String mensajeSunat;

    @Column(name = "url_pdf_sunat", length = 500)
    private String urlPdfSunat;

    // --- Datos del cliente (algunos proveedores piden DNI incluso en boleta) ---
    @Column(name = "tipo_documento_cliente", length = 10)
    private String tipoDocumentoCliente;

    @Column(name = "numero_documento_cliente", length = 15)
    private String numeroDocumentoCliente;

    @Column(name = "direccion_cliente", length = 255)
    private String direccionCliente;

    @Column(name = "cliente_nombre_snapshot", length = 180)
    private String clienteNombreSnapshot;

    @Column(name = "document_folder_type", length = 40)
    private String documentFolderType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "xml_storage_file_id")
    private StorageFile xmlStorageFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cdr_storage_file_id")
    private StorageFile cdrStorageFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdf_storage_file_id")
    private StorageFile pdfStorageFile;

    @Builder.Default
    @Column(name = "sunat_attempts", nullable = false)
    private Integer sunatAttempts = 0;

    @Column(name = "sunat_next_retry_at")
    private LocalDateTime sunatNextRetryAt;

    @Column(name = "sunat_last_error")
    private String sunatLastError;

    @Column(name = "sunat_ticket", length = 120)
    private String sunatTicket;

    @Column(name = "sunat_response_code", length = 40)
    private String sunatResponseCode;

    @Column(name = "sunat_response_description")
    private String sunatResponseDescription;

    @Column(name = "sunat_sent_at")
    private LocalDateTime sunatSentAt;

    @Column(name = "sunat_accepted_at")
    private LocalDateTime sunatAcceptedAt;
}
