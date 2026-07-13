package com.pikudo.restaurant.repository.sunat;

import com.pikudo.restaurant.entity.sunat.SunatSubmissionJob;
import com.pikudo.restaurant.entity.sunat.SunatSubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SunatSubmissionJobRepository extends JpaRepository<SunatSubmissionJob, UUID> {

    List<SunatSubmissionJob> findTop25ByStatusInAndNextRetryAtBeforeOrderByFechaCreacionAsc(
            List<SunatSubmissionStatus> statuses,
            LocalDateTime nextRetryAt
    );

    List<SunatSubmissionJob> findTop25ByStatusInAndNextRetryAtIsNullOrderByFechaCreacionAsc(
            List<SunatSubmissionStatus> statuses
    );

    List<SunatSubmissionJob> findByStatusOrderByFechaCreacionDesc(SunatSubmissionStatus status);
}
