package com.pikudo.restaurant.repository.delivery;

import com.pikudo.restaurant.entity.delivery.Delivery;
import com.pikudo.restaurant.entity.delivery.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID>, JpaSpecificationExecutor<Delivery> {
    Optional<Delivery> findByPedidoId(Long pedidoId);
    Optional<Delivery> findByTrackingCode(String trackingCode);
    Optional<Delivery> findFirstByDriverIdAndStatusInOrderByFechaCreacionDesc(Long driverId, List<DeliveryStatus> statuses);
    boolean existsByPedidoId(Long pedidoId);
}
