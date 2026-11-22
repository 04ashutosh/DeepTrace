package io.deeptrace.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record SpanNodeDto(
        String spanId,
        String traceId,
        String parentSpanId,
        String serviceName,
        String operationName,
        Instant startTime,
        Instant endTime,
        Double durationMs,
        Integer statusCode,
        String errorMessage,
        boolean isRootCause,
        boolean isCriticalPath,
        List<SpanNodeDto> children
) {
    public SpanNodeDto withCriticalPath(boolean criticalPath) {
        return new SpanNodeDto(
                spanId, traceId, parentSpanId, serviceName, operationName,
                startTime, endTime, durationMs, statusCode, errorMessage,
                isRootCause, criticalPath, children
        );
    }

    public SpanNodeDto withRootCause(boolean rootCause) {
        return new SpanNodeDto(
                spanId, traceId, parentSpanId, serviceName, operationName,
                startTime, endTime, durationMs, statusCode, errorMessage,
                rootCause, isCriticalPath, children
        );
    }
}