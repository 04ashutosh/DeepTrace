package io.deeptrace.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DatasetResponse(
        UUID id,
        String name,
        String urn,
        String layer,
        String format,
        String storageLocation,
        Long recordCount,
        OffsetDateTime lastSuccessfulRunAt,
        OffsetDateTime createdAt
) {}