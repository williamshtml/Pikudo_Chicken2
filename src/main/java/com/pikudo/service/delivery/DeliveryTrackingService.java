package com.pikudo.service.delivery;

import com.pikudo.dto.delivery.DeliveryAssignRequestDTO;
import com.pikudo.dto.delivery.DeliveryCreateRequestDTO;
import com.pikudo.dto.delivery.DeliveryLocationRequestDTO;
import com.pikudo.dto.delivery.DeliveryLocationResponseDTO;
import com.pikudo.dto.delivery.DeliveryResponseDTO;
import com.pikudo.dto.delivery.DeliveryStatusRequestDTO;
import com.pikudo.dto.delivery.PublicTrackingResponseDTO;
import com.pikudo.entity.delivery.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DeliveryTrackingService {
    DeliveryResponseDTO createForOrder(Long orderId, DeliveryCreateRequestDTO request);
    Page<DeliveryResponseDTO> list(DeliveryStatus status, Long driverId, Pageable pageable);
    DeliveryResponseDTO get(UUID id);
    DeliveryResponseDTO assign(UUID id, DeliveryAssignRequestDTO request);
    DeliveryResponseDTO accept(UUID id);
    DeliveryResponseDTO reject(UUID id, DeliveryStatusRequestDTO request);
    DeliveryResponseDTO pickup(UUID id);
    DeliveryResponseDTO changeStatus(UUID id, DeliveryStatusRequestDTO request);
    DeliveryLocationResponseDTO reportLocation(UUID id, DeliveryLocationRequestDTO request);
    DeliveryLocationResponseDTO reportActiveDriverLocation(Long driverId, DeliveryLocationRequestDTO request);
    DeliveryLocationResponseDTO getLastLocation(UUID id);
    PublicTrackingResponseDTO publicTracking(String trackingCode);
}
