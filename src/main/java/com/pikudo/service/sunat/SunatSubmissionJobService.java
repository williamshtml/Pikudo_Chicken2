package com.pikudo.service.sunat;

import com.pikudo.entity.Comprobante;
import com.pikudo.entity.NotaCredito;
import com.pikudo.entity.NotaDebito;
import com.pikudo.entity.sunat.SunatSubmissionJob;
import com.pikudo.entity.sunat.SunatSubmissionStatus;

import java.util.List;
import java.util.UUID;

public interface SunatSubmissionJobService {

    void enqueue(Comprobante comprobante);

    void enqueue(NotaCredito notaCredito);

    void enqueue(NotaDebito notaDebito);

    List<SunatSubmissionJob> list(SunatSubmissionStatus status);

    SunatSubmissionJob retry(UUID id);

    int processPending();
}
