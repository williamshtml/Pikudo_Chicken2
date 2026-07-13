package com.pikudo.restaurant.service.sunat;

import com.pikudo.restaurant.entity.TipoComprobante;

public interface DocumentSequenceService {

    String nextCorrelativo(TipoComprobante documentType, String serie);
}
