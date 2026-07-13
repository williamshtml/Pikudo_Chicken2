package com.pikudo.repository.catalog;

import com.pikudo.entity.catalog.ModifierGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ModifierGroupRepository extends JpaRepository<ModifierGroup, Long>, JpaSpecificationExecutor<ModifierGroup> {

    boolean existsBySlug(String slug);

    Optional<ModifierGroup> findBySlug(String slug);
}
