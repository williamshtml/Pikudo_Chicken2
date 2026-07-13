package com.pikudo.entity;

import com.pikudo.entity.storage.StorageFile;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "notas_credito", indexes = {
    @Index(name = "idx_nc_serie_correlativo", columnList = "serie, correlativo", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotaCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El comprobante es obligatorio")
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "comprobante_id", nullable = false, unique = true)
    private Comprobante comprobante;

    @NotBlank
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String serie = "NC01";

    @NotBlank
    @Column(nullable = false, length = 20)
    private String correlativo;

    @NotBlank(message = "El motivo de anulación es obligatorio")
    @Column(nullable = false, length = 255)
    private String motivo;

    @NotNull
    @Column(name = "monto_devuelto", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoDevuelto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_emisor_id")
    private Usuario usuarioEmisor;

    @Builder.Default
    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision = LocalDateTime.now();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_sunat", length = 30)
    private EstadoSunat estadoSunat = EstadoSunat.NO_ENVIADO;

    @Column(name = "hash_sunat", length = 100)
    private String hashSunat;

    @Column(name = "mensaje_sunat", length = 500)
    private String mensajeSunat;

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
