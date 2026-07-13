package com.pikudo.repository.delivery;

import com.pikudo.entity.delivery.DeliveryStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeliveryStatusHistoryRepository extends JpaRepository<DeliveryStatusHistory, UUID> {
    List<DeliveryStatusHistory> findByDeliveryIdOrderByFechaCreacionAsc(UUID deliveryId);
}
