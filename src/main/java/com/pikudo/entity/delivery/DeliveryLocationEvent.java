package com.pikudo.entity.delivery;

import com.pikudo.entity.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_location_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryLocationEvent {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Usuario driver;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "accuracy_meters", precision = 8, scale = 2)
    private BigDecimal accuracyMeters;

    @Column(name = "speed_meters_per_second", precision = 8, scale = 2)
    private BigDecimal speedMetersPerSecond;

    @Column(name = "heading_degrees", precision = 6, scale = 2)
    private BigDecimal headingDegrees;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @Builder.Default
    @Column(nullable = false, length = 30)
    private String source = "MOBILE";

    @PrePersist
    void prePersist() {
        if (receivedAt == null) {
            receivedAt = LocalDateTime.now();
        }
        if (recordedAt == null) {
            recordedAt = receivedAt;
        }
    }
}
