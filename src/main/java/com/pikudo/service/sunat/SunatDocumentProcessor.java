package com.pikudo.service.sunat;

import com.pikudo.entity.sunat.SunatSubmissionJob;

public interface SunatDocumentProcessor {

    void process(SunatSubmissionJob job);
}
