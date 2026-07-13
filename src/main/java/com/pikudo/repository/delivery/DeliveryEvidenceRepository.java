package com.pikudo.repository.delivery;

import com.pikudo.entity.delivery.DeliveryEvidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeliveryEvidenceRepository extends JpaRepository<DeliveryEvidence, UUID> {
}
