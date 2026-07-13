package com.pikudo.restaurant.entity.sunat;

import com.pikudo.restaurant.entity.Comprobante;
import com.pikudo.restaurant.entity.NotaCredito;
import com.pikudo.restaurant.entity.NotaDebito;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sunat_submission_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SunatSubmissionJob {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprobante_id")
    private Comprobante comprobante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nota_credito_id")
    private NotaCredito notaCredito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nota_debito_id")
    private NotaDebito notaDebito;

    @Column(name = "document_type", nullable = false, length = 30)
    private String documentType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SunatSubmissionStatus status = SunatSubmissionStatus.PENDING;

    @Builder.Default
    @Column(nullable = false)
    private Integer attempts = 0;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "sunat_response_code", length = 40)
    private String sunatResponseCode;

    @Column(name = "sunat_response_description")
    private String sunatResponseDescription;

    @Column(name = "sunat_sent_at")
    private LocalDateTime sunatSentAt;

    @Column(name = "sunat_accepted_at")
    private LocalDateTime sunatAcceptedAt;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    @PreUpdate
    void preUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
}
