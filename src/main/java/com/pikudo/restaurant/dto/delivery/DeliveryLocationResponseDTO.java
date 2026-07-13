package com.pikudo.restaurant.dto.delivery;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record DeliveryLocationResponseDTO(
        UUID deliveryId,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime recordedAt,
        Integer etaMinutes,
        Integer distanceMeters
) {
}
