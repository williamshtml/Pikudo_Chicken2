package com.pikudo.dto.delivery;

import com.pikudo.entity.delivery.DeliveryStatus;
import lombok.Builder;

@Builder
public record PublicTrackingResponseDTO(
        String trackingCode,
        String orderCode,
        DeliveryStatus deliveryStatus,
        String orderStatus,
        Integer progressPercent,
        Integer etaMinutes,
        Integer distanceMeters,
        boolean nearCustomer
) {
}
