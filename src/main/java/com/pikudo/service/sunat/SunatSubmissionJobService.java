package com.pikudo.service.sunat;

import com.pikudo.entity.Comprobante;
import com.pikudo.entity.NotaCredito;

public interface SunatSubmissionJobService {

    void enqueue(Comprobante comprobante);

    void enqueue(NotaCredito notaCredito);
}
