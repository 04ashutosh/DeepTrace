package io.deeptrace.dto;

import java.util.List;

public record TraceTreeResponse(
        String traceId,
        Double totalDurationMs,
        int spanCount,
        String rootCauseSpanId,
        String rootCauseService,
        List<SpanNodeDto> rootSpans
) {}