package com.pikudo.restaurant.entity.sunat;

public enum SunatSubmissionStatus {
    PENDING,
    PROCESSING,
    ACCEPTED,
    ACCEPTED_WITH_OBSERVATION,
    REJECTED,
    FAILED_RETRYABLE,
    FAILED_FINAL
}
