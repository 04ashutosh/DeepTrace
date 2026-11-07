package io.deeptrace.repository;

import io.deeptrace.entity.PipelineCheckpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PipelineCheckpointRepository extends JpaRepository<PipelineCheckpoint, UUID> {
    Optional<PipelineCheckpoint> findByPipelineName(String pipelineName);
}