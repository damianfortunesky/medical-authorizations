package com.damian.medicalauthorization.shared.logging;

import org.slf4j.MDC;

import java.util.Optional;

public final class CorrelationIdHolder {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
    private static final String UNKNOWN = "N/A";

    private CorrelationIdHolder() {
    }

    public static String getOrDefault() {
        return Optional.ofNullable(MDC.get(CORRELATION_ID_MDC_KEY)).orElse(UNKNOWN);
    }
}
