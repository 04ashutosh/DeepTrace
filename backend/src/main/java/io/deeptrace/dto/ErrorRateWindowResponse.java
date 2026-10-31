package io.deeptrace.dto;

import java.time.OffsetDateTime;

public record ErrorRateWindowResponse(
        String serviceName,
        OffsetDateTime windowStart,
        Long totalRequests,
        Long errors5xx,
        Long errors4xx,
        Double errorRate5xxPct
) {}