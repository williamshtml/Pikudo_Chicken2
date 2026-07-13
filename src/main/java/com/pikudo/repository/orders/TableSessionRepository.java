package com.pikudo.repository.orders;

import com.pikudo.entity.orders.TableSession;
import com.pikudo.entity.orders.TableSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TableSessionRepository extends JpaRepository<TableSession, Long> {

    Optional<TableSession> findFirstByMesaIdAndStatusOrderByOpenedAtDesc(Long mesaId, TableSessionStatus status);

    List<TableSession> findByStatusOrderByOpenedAtDesc(TableSessionStatus status);

    boolean existsByMesaIdAndStatus(Long mesaId, TableSessionStatus status);
}
