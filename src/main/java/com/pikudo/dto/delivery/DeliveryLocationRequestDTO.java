package com.pikudo.dto.delivery;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class DeliveryLocationRequestDTO {
    @NotNull
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private BigDecimal latitude;

    @NotNull
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private BigDecimal longitude;

    private BigDecimal accuracyMeters;
    private BigDecimal speedMetersPerSecond;
    private BigDecimal headingDegrees;
    private LocalDateTime recordedAt;
}
