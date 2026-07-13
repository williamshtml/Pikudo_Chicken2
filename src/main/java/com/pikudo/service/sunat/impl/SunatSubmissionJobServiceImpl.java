package com.pikudo.service.sunat.impl;

import com.pikudo.entity.Comprobante;
import com.pikudo.entity.NotaCredito;
import com.pikudo.entity.sunat.SunatSubmissionJob;
import com.pikudo.entity.sunat.SunatSubmissionStatus;
import com.pikudo.repository.sunat.SunatSubmissionJobRepository;
import com.pikudo.service.sunat.SunatSubmissionJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SunatSubmissionJobServiceImpl implements SunatSubmissionJobService {

    private final SunatSubmissionJobRepository repository;

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
}
