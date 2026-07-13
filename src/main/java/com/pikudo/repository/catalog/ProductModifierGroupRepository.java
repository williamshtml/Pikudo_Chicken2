package com.pikudo.repository.catalog;

import com.pikudo.entity.catalog.ProductModifierGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductModifierGroupRepository extends JpaRepository<ProductModifierGroup, Long> {

    boolean existsByProductoIdAndGroupId(Long productId, Long groupId);

    List<ProductModifierGroup> findByProductoIdOrderByOrdenAscIdAsc(Long productId);

    Optional<ProductModifierGroup> findByProductoIdAndGroupId(Long productId, Long groupId);
}
