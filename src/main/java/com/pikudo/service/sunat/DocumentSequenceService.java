package com.pikudo.service.sunat;

import com.pikudo.entity.TipoComprobante;

public interface DocumentSequenceService {

    String nextCorrelativo(TipoComprobante documentType, String serie);
}
