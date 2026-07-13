package com.pikudo.restaurant.service.delivery;

import com.pikudo.restaurant.dto.delivery.DeliveryAssignRequestDTO;
import com.pikudo.restaurant.dto.delivery.DeliveryCreateRequestDTO;
import com.pikudo.restaurant.dto.delivery.DeliveryLocationRequestDTO;
import com.pikudo.restaurant.dto.delivery.DeliveryLocationResponseDTO;
import com.pikudo.restaurant.dto.delivery.DeliveryResponseDTO;
import com.pikudo.restaurant.dto.delivery.DeliveryStatusRequestDTO;
import com.pikudo.restaurant.dto.delivery.PublicTrackingResponseDTO;
import com.pikudo.restaurant.entity.delivery.DeliveryStatus;
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
