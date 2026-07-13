package com.pikudo.restaurant.service.sunat.impl;

import com.pikudo.restaurant.entity.Comprobante;
import com.pikudo.restaurant.entity.NotaCredito;
import com.pikudo.restaurant.entity.NotaDebito;
import com.pikudo.restaurant.entity.sunat.SunatSubmissionJob;
import com.pikudo.restaurant.entity.sunat.SunatSubmissionStatus;
import com.pikudo.restaurant.exception.ResourceNotFoundException;
import com.pikudo.restaurant.repository.sunat.SunatSubmissionJobRepository;
import com.pikudo.restaurant.service.sunat.SunatDocumentProcessor;
import com.pikudo.restaurant.service.sunat.SunatSubmissionJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SunatSubmissionJobServiceImpl implements SunatSubmissionJobService {

    private final SunatSubmissionJobRepository repository;
    private final SunatDocumentProcessor processor;

    @Override
    @Transactional
    public void enqueue(Comprobante comprobante) {
        repository.save(SunatSubmissionJob.builder()
                .comprobante(comprobante)
                .documentType(comprobante.getTipoComprobante().name())
                .status(SunatSubmissionStatus.PENDING)
                .build());
    }

    @Override
    @Transactional
    public void enqueue(NotaCredito notaCredito) {
        repository.save(SunatSubmissionJob.builder()
                .notaCredito(notaCredito)
                .documentType("NOTA_CREDITO")
                .status(SunatSubmissionStatus.PENDING)
                .build());
    }

    @Override
    @Transactional
    public void enqueue(NotaDebito notaDebito) {
        repository.save(SunatSubmissionJob.builder()
                .notaDebito(notaDebito)
                .documentType("NOTA_DEBITO")
                .status(SunatSubmissionStatus.PENDING)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SunatSubmissionJob> list(SunatSubmissionStatus status) {
        if (status != null) {
            return repository.findByStatusOrderByFechaCreacionDesc(status);
        }
        return repository.findAll();
    }

    @Override
    @Transactional
    public SunatSubmissionJob retry(UUID id) {
        SunatSubmissionJob job = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job SUNAT no encontrado: " + id));
        job.setStatus(SunatSubmissionStatus.PENDING);
        job.setNextRetryAt(null);
        job.setLastError(null);
        return job;
    }

    @Override
    @Transactional
    public int processPending() {
        List<SunatSubmissionStatus> retryable = List.of(SunatSubmissionStatus.PENDING, SunatSubmissionStatus.FAILED_RETRYABLE);
        List<SunatSubmissionJob> jobs = new ArrayList<>();
        jobs.addAll(repository.findTop25ByStatusInAndNextRetryAtIsNullOrderByFechaCreacionAsc(retryable));
        jobs.addAll(repository.findTop25ByStatusInAndNextRetryAtBeforeOrderByFechaCreacionAsc(retryable, LocalDateTime.now()));
        int processed = 0;
        for (SunatSubmissionJob job : jobs.stream().distinct().limit(25).toList()) {
            job.setStatus(SunatSubmissionStatus.PROCESSING);
            processor.process(job);
            processed++;
        }
        return processed;
    }
}
