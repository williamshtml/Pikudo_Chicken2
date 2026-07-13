package com.pikudo.repository.sunat;

import com.pikudo.entity.sunat.SunatSubmissionJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SunatSubmissionJobRepository extends JpaRepository<SunatSubmissionJob, UUID> {
}
