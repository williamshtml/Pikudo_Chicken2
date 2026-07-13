package com.pikudo.restaurant.repository.delivery;

import com.pikudo.restaurant.entity.delivery.DeliveryLocationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeliveryLocationEventRepository extends JpaRepository<DeliveryLocationEvent, UUID> {
    List<DeliveryLocationEvent> findTop100ByDeliveryIdOrderByRecordedAtDesc(UUID deliveryId);
}
