package com.pikudo.entity;

import com.pikudo.entity.storage.StorageFile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
        name = "notas_debito",
        uniqueConstraints = @UniqueConstraint(name = "ux_nd_serie_correlativo", columnNames = {"serie", "correlativo"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotaDebito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "comprobante_id", nullable = false)
    private Comprobante comprobante;

    @NotBlank
    @Column(nullable = false, length = 10)
    private String serie;

    @NotBlank
    @Column(nullable = false, length = 20)
    private String correlativo;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String motivo;

    @NotNull
    @Column(name = "monto_adicional", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoAdicional;

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

    @PrePersist
    void prePersist() {
        if (fechaEmision == null) {
            fechaEmision = LocalDateTime.now();
        }
    }
}
