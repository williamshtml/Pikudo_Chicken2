package com.pikudo.service.delivery.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pikudo.config.properties.TrackingProperties;
import com.pikudo.dto.delivery.DeliveryAssignRequestDTO;
import com.pikudo.dto.delivery.DeliveryCreateRequestDTO;
import com.pikudo.dto.delivery.DeliveryLocationRequestDTO;
import com.pikudo.dto.delivery.DeliveryLocationResponseDTO;
import com.pikudo.dto.delivery.DeliveryResponseDTO;
import com.pikudo.dto.delivery.DeliveryStatusRequestDTO;
import com.pikudo.dto.delivery.PublicTrackingResponseDTO;
import com.pikudo.entity.EstadoPedido;
import com.pikudo.entity.Pedido;
import com.pikudo.entity.Usuario;
import com.pikudo.entity.delivery.Delivery;
import com.pikudo.entity.delivery.DeliveryLocationEvent;
import com.pikudo.entity.delivery.DeliveryStatus;
import com.pikudo.entity.delivery.DeliveryStatusHistory;
import com.pikudo.entity.orders.OrderOperationalStatus;
import com.pikudo.entity.orders.OrderServiceType;
import com.pikudo.exception.BusinessException;
import com.pikudo.exception.ResourceNotFoundException;
import com.pikudo.repository.PedidoRepository;
import com.pikudo.repository.UsuarioRepository;
import com.pikudo.repository.delivery.DeliveryLocationEventRepository;
import com.pikudo.repository.delivery.DeliveryRepository;
import com.pikudo.repository.delivery.DeliveryStatusHistoryRepository;
import com.pikudo.service.delivery.DeliveryTrackingService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryTrackingServiceImpl implements DeliveryTrackingService {

    private static final List<DeliveryStatus> ACTIVE_TRACKING_STATUSES = List.of(
            DeliveryStatus.ACCEPTED,
            DeliveryStatus.PICKED_UP,
            DeliveryStatus.ON_DELIVERY,
            DeliveryStatus.NEAR_CUSTOMER
    );

    private final DeliveryRepository deliveryRepository;
    private final DeliveryStatusHistoryRepository historyRepository;
    private final DeliveryLocationEventRepository locationRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final StringRedisTemplate redisTemplate;
    private final TrackingProperties trackingProperties;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public DeliveryResponseDTO createForOrder(Long orderId, DeliveryCreateRequestDTO request) {
        Pedido pedido = pedidoRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + orderId));
        if (pedido.getServiceType() != OrderServiceType.DELIVERY) {
            throw new BusinessException("Solo pedidos DELIVERY pueden crear entrega.");
        }
        if (deliveryRepository.existsByPedidoId(orderId)) {
            throw new BusinessException("El pedido ya tiene una entrega creada.");
        }
        if (!List.of(OrderOperationalStatus.ACCEPTED, OrderOperationalStatus.IN_PREPARATION,
                OrderOperationalStatus.READY, OrderOperationalStatus.ASSIGNED).contains(pedido.getEstadoOperativo())) {
            throw new BusinessException("El pedido no esta en un estado asignable para delivery.");
        }

        Usuario user = currentUser();
        Delivery delivery = Delivery.builder()
                .pedido(pedido)
                .status(DeliveryStatus.CREATED)
                .trackingCode(pedido.getTrackingCode())
                .destinationAddress(pedido.getDireccion())
                .destinationReference(request != null ? request.getDestinationReference() : null)
                .customerPhone(pedido.getTelefonoCliente())
                .createdBy(user)
                .build();
        Delivery saved = deliveryRepository.save(delivery);
        record(saved, null, DeliveryStatus.CREATED, user, "Entrega creada");
        return toResponse(saved, true);
    }

    @Override
    public Page<DeliveryResponseDTO> list(DeliveryStatus status, Long driverId, Pageable pageable) {
        return deliveryRepository.findAll(filters(status, driverId), pageable).map(delivery -> toResponse(delivery, true));
    }

    @Override
    public DeliveryResponseDTO get(UUID id) {
        return toResponse(find(id), true);
    }

    @Override
    @Transactional
    public DeliveryResponseDTO assign(UUID id, DeliveryAssignRequestDTO request) {
        Delivery delivery = find(id);
        Usuario driver = usuarioRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Repartidor no encontrado: " + request.getDriverId()));
        delivery.setDriver(driver);
        delivery.setAssignedAt(LocalDateTime.now());
        transition(delivery, DeliveryStatus.ASSIGNED, currentUser(), "Repartidor asignado");
        Pedido pedido = delivery.getPedido();
        pedido.setRepartidor(driver);
        pedido.setEstadoOperativo(OrderOperationalStatus.ASSIGNED);
        return toResponse(delivery, true);
    }

    @Override
    @Transactional
    public DeliveryResponseDTO accept(UUID id) {
        Delivery delivery = find(id);
        requireAssignedDriver(delivery);
        delivery.setAcceptedAt(LocalDateTime.now());
        transition(delivery, DeliveryStatus.ACCEPTED, currentUser(), "Entrega aceptada");
        delivery.getPedido().setEstadoOperativo(OrderOperationalStatus.ASSIGNED);
        return toResponse(delivery, true);
    }

    @Override
    @Transactional
    public DeliveryResponseDTO reject(UUID id, DeliveryStatusRequestDTO request) {
        Delivery delivery = find(id);
        requireAssignedDriver(delivery);
        transition(delivery, DeliveryStatus.REJECTED, currentUser(), request != null ? request.getReason() : "Entrega rechazada");
        return toResponse(delivery, true);
    }

    @Override
    @Transactional
    public DeliveryResponseDTO pickup(UUID id) {
        Delivery delivery = find(id);
        requireAssignedDriver(delivery);
        delivery.setPickedUpAt(LocalDateTime.now());
        transition(delivery, DeliveryStatus.PICKED_UP, currentUser(), "Pedido recogido");
        delivery.getPedido().setEstadoOperativo(OrderOperationalStatus.ON_DELIVERY);
        delivery.getPedido().setEstado(EstadoPedido.ON_DELIVERY);
        return toResponse(delivery, true);
    }

    @Override
    @Transactional
    public DeliveryResponseDTO changeStatus(UUID id, DeliveryStatusRequestDTO request) {
        Delivery delivery = find(id);
        Usuario user = currentUser();
        DeliveryStatus status = request.getStatus();
        if (ACTIVE_TRACKING_STATUSES.contains(status)) {
            requireAssignedDriver(delivery);
        }
        if (status == DeliveryStatus.DELIVERED) {
            delivery.setDeliveredAt(LocalDateTime.now());
            delivery.getPedido().setEstadoOperativo(OrderOperationalStatus.DELIVERED);
            delivery.getPedido().setEstado(EstadoPedido.DELIVERED);
        } else if (status == DeliveryStatus.CANCELLED) {
            delivery.setCancelledAt(LocalDateTime.now());
            delivery.getPedido().setEstadoOperativo(OrderOperationalStatus.CANCELLED);
            delivery.getPedido().setEstado(EstadoPedido.CANCELLED);
        } else if (status == DeliveryStatus.NEAR_CUSTOMER) {
            delivery.getPedido().setEstadoOperativo(OrderOperationalStatus.NEAR_CUSTOMER);
        } else if (status == DeliveryStatus.ON_DELIVERY) {
            delivery.getPedido().setEstadoOperativo(OrderOperationalStatus.ON_DELIVERY);
        }
        transition(delivery, status, user, request.getReason());
        return toResponse(delivery, true);
    }

    @Override
    @Transactional
    public DeliveryLocationResponseDTO reportLocation(UUID id, DeliveryLocationRequestDTO request) {
        Delivery delivery = find(id);
        requireAssignedDriver(delivery);
        return reportLocation(delivery, request, currentUser());
    }

    @Override
    @Transactional
    public DeliveryLocationResponseDTO reportActiveDriverLocation(Long driverId, DeliveryLocationRequestDTO request) {
        Delivery delivery = findActiveForDriver(driverId);
        return reportLocation(delivery, request, delivery.getDriver());
    }

    private DeliveryLocationResponseDTO reportLocation(Delivery delivery, DeliveryLocationRequestDTO request, Usuario driver) {
        if (!ACTIVE_TRACKING_STATUSES.contains(delivery.getStatus())) {
            throw new BusinessException("No se acepta GPS si la entrega no esta activa.");
        }
        if (delivery.getLastLocationAt() != null
                && delivery.getLastLocationAt().plusSeconds(trackingProperties.getMinIntervalSeconds()).isAfter(LocalDateTime.now())) {
            throw new BusinessException("Frecuencia GPS demasiado alta.");
        }

        LocalDateTime recordedAt = request.getRecordedAt() != null ? request.getRecordedAt() : LocalDateTime.now();
        DeliveryLocationEvent event = locationRepository.save(DeliveryLocationEvent.builder()
                .delivery(delivery)
                .driver(driver)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .accuracyMeters(request.getAccuracyMeters())
                .speedMetersPerSecond(request.getSpeedMetersPerSecond())
                .headingDegrees(request.getHeadingDegrees())
                .recordedAt(recordedAt)
                .build());
        delivery.setCurrentLatitude(event.getLatitude());
        delivery.setCurrentLongitude(event.getLongitude());
        delivery.setLastLocationAt(recordedAt);
        saveLastLocation(delivery);
        return toLocation(delivery, true);
    }

    @Override
    public DeliveryLocationResponseDTO getLastLocation(UUID id) {
        Delivery delivery = find(id);
        return toLocation(delivery, true);
    }

    @Override
    public PublicTrackingResponseDTO publicTracking(String trackingCode) {
        Delivery delivery = deliveryRepository.findByTrackingCode(trackingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking no encontrado"));
        return PublicTrackingResponseDTO.builder()
                .trackingCode(delivery.getTrackingCode())
                .orderCode(delivery.getPedido().getOrderCode())
                .deliveryStatus(delivery.getStatus())
                .orderStatus(delivery.getPedido().getEstadoOperativo().name())
                .progressPercent(progress(delivery.getStatus()))
                .etaMinutes(delivery.getEtaMinutes())
                .distanceMeters(delivery.getDistanceMeters())
                .nearCustomer(delivery.getStatus() == DeliveryStatus.NEAR_CUSTOMER)
                .build();
    }

    public Delivery findActiveForDriver(Long driverId) {
        return deliveryRepository.findFirstByDriverIdAndStatusInOrderByFechaCreacionDesc(driverId, ACTIVE_TRACKING_STATUSES)
                .orElseThrow(() -> new BusinessException("El repartidor no tiene una entrega activa."));
    }

    private Delivery find(UUID id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrega no encontrada: " + id));
    }

    private void transition(Delivery delivery, DeliveryStatus status, Usuario user, String reason) {
        DeliveryStatus previous = delivery.getStatus();
        delivery.setStatus(status);
        record(delivery, previous, status, user, reason);
    }

    private void record(Delivery delivery, DeliveryStatus previous, DeliveryStatus current, Usuario user, String reason) {
        historyRepository.save(DeliveryStatusHistory.builder()
                .delivery(delivery)
                .previousStatus(previous)
                .newStatus(current)
                .changedBy(user)
                .reason(reason)
                .build());
    }

    private void requireAssignedDriver(Delivery delivery) {
        Usuario user = currentUser();
        if (delivery.getDriver() == null || !delivery.getDriver().getId().equals(user.getId())) {
            throw new BusinessException("Solo el repartidor asignado puede operar esta entrega.");
        }
    }

    private void saveLastLocation(Delivery delivery) {
        try {
            Map<String, Object> payload = Map.of(
                    "deliveryId", delivery.getId().toString(),
                    "latitude", delivery.getCurrentLatitude(),
                    "longitude", delivery.getCurrentLongitude(),
                    "recordedAt", delivery.getLastLocationAt().toString()
            );
            redisTemplate.opsForValue().set(
                    redisKey(delivery.getId()),
                    objectMapper.writeValueAsString(payload),
                    Duration.ofSeconds(trackingProperties.getLocationTtlSeconds())
            );
        } catch (Exception e) {
            throw new BusinessException("No se pudo guardar la ultima ubicacion en Redis: " + e.getMessage());
        }
    }

    private String redisKey(UUID deliveryId) {
        return "delivery:last-location:" + deliveryId;
    }

    private Specification<Delivery> filters(DeliveryStatus status, Long driverId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (driverId != null) {
                predicates.add(criteriaBuilder.equal(root.get("driver").get("id"), driverId));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Usuario currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new BusinessException("No hay usuario autenticado.");
        }
        return usuarioRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario de sesion no encontrado"));
    }

    private DeliveryResponseDTO toResponse(Delivery delivery, boolean preciseLocation) {
        Usuario driver = delivery.getDriver();
        Pedido pedido = delivery.getPedido();
        return DeliveryResponseDTO.builder()
                .id(delivery.getId())
                .orderId(pedido.getId())
                .orderCode(pedido.getOrderCode())
                .trackingCode(delivery.getTrackingCode())
                .driverId(driver != null ? driver.getId() : null)
                .driverUsername(driver != null ? driver.getUsername() : null)
                .status(delivery.getStatus())
                .destinationAddress(delivery.getDestinationAddress())
                .destinationReference(delivery.getDestinationReference())
                .customerPhone(delivery.getCustomerPhone())
                .latitude(preciseLocation ? delivery.getCurrentLatitude() : null)
                .longitude(preciseLocation ? delivery.getCurrentLongitude() : null)
                .lastLocationAt(delivery.getLastLocationAt())
                .etaMinutes(delivery.getEtaMinutes())
                .distanceMeters(delivery.getDistanceMeters())
                .fechaCreacion(delivery.getFechaCreacion())
                .build();
    }

    private DeliveryLocationResponseDTO toLocation(Delivery delivery, boolean precise) {
        return DeliveryLocationResponseDTO.builder()
                .deliveryId(delivery.getId())
                .latitude(precise ? delivery.getCurrentLatitude() : null)
                .longitude(precise ? delivery.getCurrentLongitude() : null)
                .recordedAt(delivery.getLastLocationAt())
                .etaMinutes(delivery.getEtaMinutes())
                .distanceMeters(delivery.getDistanceMeters())
                .build();
    }

    private int progress(DeliveryStatus status) {
        return switch (status) {
            case CREATED -> 10;
            case ASSIGNED -> 20;
            case ACCEPTED -> 35;
            case PICKED_UP -> 55;
            case ON_DELIVERY -> 75;
            case NEAR_CUSTOMER -> 90;
            case DELIVERED -> 100;
            case REJECTED, CANCELLED -> 0;
        };
    }
}
