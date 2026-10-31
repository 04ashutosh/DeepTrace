package io.deeptrace.service;

import io.deeptrace.dto.DatasetResponse;
import io.deeptrace.entity.DatasetEntity;
import io.deeptrace.repository.DatasetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DatasetService {
    private final DatasetRepository repository;

    public DatasetService(DatasetRepository repository) {
        this.repository = repository;
    }

    public List<DatasetResponse> getAllDatasets() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<DatasetResponse> getDatasetById(UUID id) {
        return repository.findById(id).map(this::toResponse);
    }

    private DatasetResponse toResponse(DatasetEntity entity) {
        return new DatasetResponse(
                entity.getId(),
                entity.getName(),
                entity.getUrn(),
                entity.getLayer(),
                entity.getFormat(),
                entity.getStorageLocation(),
                entity.getRecordCount(),
                entity.getLastSuccessfulRunAt(),
                entity.getCreatedAt()
        );
    }
}