package com.pikudo.repository.catalog;

import com.pikudo.entity.catalog.Modifier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModifierRepository extends JpaRepository<Modifier, Long> {

    boolean existsByGroupIdAndSlug(Long groupId, String slug);

    List<Modifier> findByGroupIdOrderByOrdenAscIdAsc(Long groupId);
}
