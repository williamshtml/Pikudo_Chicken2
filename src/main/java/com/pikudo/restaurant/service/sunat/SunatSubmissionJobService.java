package com.pikudo.restaurant.service.sunat;

import com.pikudo.restaurant.entity.Comprobante;
import com.pikudo.restaurant.entity.NotaCredito;
import com.pikudo.restaurant.entity.NotaDebito;
import com.pikudo.restaurant.entity.sunat.SunatSubmissionJob;
import com.pikudo.restaurant.entity.sunat.SunatSubmissionStatus;

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
