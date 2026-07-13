package com.pikudo.restaurant.repository.catalog;

import com.pikudo.restaurant.entity.catalog.ComboComponent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComboComponentRepository extends JpaRepository<ComboComponent, Long> {

    boolean existsByComboProductIdAndComponentVariantId(Long comboProductId, Long componentVariantId);

    List<ComboComponent> findByComboProductIdOrderByOrdenAscIdAsc(Long comboProductId);

    Optional<ComboComponent> findByIdAndComboProductId(Long id, Long comboProductId);
}
