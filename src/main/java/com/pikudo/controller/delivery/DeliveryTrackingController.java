package com.pikudo.controller.delivery;

import com.pikudo.dto.delivery.DeliveryAssignRequestDTO;
import com.pikudo.dto.delivery.DeliveryCreateRequestDTO;
import com.pikudo.dto.delivery.DeliveryLocationRequestDTO;
import com.pikudo.dto.delivery.DeliveryLocationResponseDTO;
import com.pikudo.dto.delivery.DeliveryResponseDTO;
import com.pikudo.dto.delivery.DeliveryStatusRequestDTO;
import com.pikudo.dto.delivery.PublicTrackingResponseDTO;
import com.pikudo.entity.delivery.DeliveryStatus;
import com.pikudo.service.delivery.DeliveryTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DeliveryTrackingController {

    private final DeliveryTrackingService service;

    @PostMapping("/api/v1/orders/{id}/delivery")
    public ResponseEntity<DeliveryResponseDTO> createForOrder(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) DeliveryCreateRequestDTO request
    ) {
        return ResponseEntity.ok(service.createForOrder(id, request));
    }

    @GetMapping("/api/v1/deliveries")
    public ResponseEntity<Page<DeliveryResponseDTO>> list(
            @RequestParam(required = false) DeliveryStatus status,
            @RequestParam(required = false) Long driverId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.list(status, driverId, pageable));
    }

    @GetMapping("/api/v1/deliveries/{id}")
    public ResponseEntity<DeliveryResponseDTO> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping("/api/v1/deliveries/{id}/assign")
    public ResponseEntity<DeliveryResponseDTO> assign(
            @PathVariable UUID id,
            @Valid @RequestBody DeliveryAssignRequestDTO request
    ) {
        return ResponseEntity.ok(service.assign(id, request));
    }

    @PostMapping("/api/v1/deliveries/{id}/accept")
    public ResponseEntity<DeliveryResponseDTO> accept(@PathVariable UUID id) {
        return ResponseEntity.ok(service.accept(id));
    }

    @PostMapping("/api/v1/deliveries/{id}/reject")
    public ResponseEntity<DeliveryResponseDTO> reject(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) DeliveryStatusRequestDTO request
    ) {
        return ResponseEntity.ok(service.reject(id, request));
    }

    @PostMapping("/api/v1/deliveries/{id}/pickup")
    public ResponseEntity<DeliveryResponseDTO> pickup(@PathVariable UUID id) {
        return ResponseEntity.ok(service.pickup(id));
    }

    @PostMapping("/api/v1/deliveries/{id}/status")
    public ResponseEntity<DeliveryResponseDTO> status(
            @PathVariable UUID id,
            @Valid @RequestBody DeliveryStatusRequestDTO request
    ) {
        return ResponseEntity.ok(service.changeStatus(id, request));
    }

    @PostMapping("/api/v1/deliveries/{id}/locations")
    public ResponseEntity<DeliveryLocationResponseDTO> location(
            @PathVariable UUID id,
            @Valid @RequestBody DeliveryLocationRequestDTO request
    ) {
        return ResponseEntity.ok(service.reportLocation(id, request));
    }

    @GetMapping("/api/v1/deliveries/{id}/last-location")
    public ResponseEntity<DeliveryLocationResponseDTO> lastLocation(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getLastLocation(id));
    }

    @GetMapping("/api/v1/orders/tracking/{trackingCode}")
    public ResponseEntity<PublicTrackingResponseDTO> publicTracking(@PathVariable String trackingCode) {
        return ResponseEntity.ok(service.publicTracking(trackingCode));
    }
}
