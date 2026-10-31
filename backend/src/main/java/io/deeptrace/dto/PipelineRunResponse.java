package io.deeptrace.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PipelineRunResponse(
        UUID id,
        UUID runUuid,
        String pipelineName,
        String status,
        String inputFile,
        Long recordsRead,
        Long recordsAccepted,
        Long recordsRejected,
        Double rejectionRatePct,
        Long durationMs,
        OffsetDateTime startedAt,
        OffsetDateTime completedAt,
        String errorMessage
) {}