package io.deeptrace.service;

import io.deeptrace.dto.PipelineRunResponse;
import io.deeptrace.entity.PipelineRunEntity;
import io.deeptrace.repository.PipelineRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PipelineRunService {
    private final PipelineRunRepository repository;

    public PipelineRunService(PipelineRunRepository repository) {
        this.repository = repository;
    }

    public List<PipelineRunResponse> getAllRuns() {
        return repository.findAllWithPipeline().stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<PipelineRunResponse> getRunByUuid(UUID runUuid) {
        return repository.findByRunUuid(runUuid).map(this::toResponse);
    }

    private PipelineRunResponse toResponse(PipelineRunEntity entity) {
        Double rejectionRate = 0.0;
        if (entity.getRecordsRead() != null && entity.getRecordsRead() > 0) {
            rejectionRate = Math.round(((double) entity.getRecordsRejected() / entity.getRecordsRead() * 100.0) * 100.0) / 100.0;
        }

        return new PipelineRunResponse(
                entity.getId(),
                entity.getRunUuid(),
                entity.getPipeline() != null ? entity.getPipeline().getName() : "UNKNOWN",
                entity.getStatus(),
                entity.getInputFile(),
                entity.getRecordsRead(),
                entity.getRecordsAccepted(),
                entity.getRecordsRejected(),
                rejectionRate,
                entity.getDurationMs(),
                entity.getStartedAt(),
                entity.getCompletedAt(),
                entity.getErrorMessage()
        );
    }
}