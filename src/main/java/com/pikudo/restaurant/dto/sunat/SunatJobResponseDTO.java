package com.pikudo.restaurant.dto.sunat;

import com.pikudo.restaurant.entity.sunat.SunatSubmissionStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record SunatJobResponseDTO(
        UUID id,
        Long comprobanteId,
        Long notaCreditoId,
        Long notaDebitoId,
        String documentType,
        SunatSubmissionStatus status,
        Integer attempts,
        LocalDateTime nextRetryAt,
        String lastError,
        String responseCode,
        String responseDescription,
        LocalDateTime sentAt,
        LocalDateTime acceptedAt,
        LocalDateTime fechaCreacion
) {
}
