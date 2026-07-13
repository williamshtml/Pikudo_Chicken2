package com.pikudo.restaurant.service.sunat;

import com.pikudo.restaurant.entity.sunat.SunatSubmissionJob;

public interface SunatDocumentProcessor {

    void process(SunatSubmissionJob job);
}
