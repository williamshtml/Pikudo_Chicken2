package com.pikudo.restaurant.dto.delivery;

import com.pikudo.restaurant.entity.delivery.DeliveryStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record DeliveryResponseDTO(
        UUID id,
        Long orderId,
        String orderCode,
        String trackingCode,
        Long driverId,
        String driverUsername,
        DeliveryStatus status,
        String destinationAddress,
        String destinationReference,
        String customerPhone,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime lastLocationAt,
        Integer etaMinutes,
        Integer distanceMeters,
        LocalDateTime fechaCreacion
) {
}
