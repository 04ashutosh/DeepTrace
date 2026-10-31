package io.deeptrace.repository;

import io.deeptrace.entity.PipelineRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PipelineRunRepository extends JpaRepository<PipelineRunEntity, UUID> {
    Optional<PipelineRunEntity> findByRunUuid(UUID runUuid);

    @Query("SELECT r FROM PipelineRunEntity r JOIN FETCH r.pipeline ORDER BY r.startedAt DESC")
    List<PipelineRunEntity> findAllWithPipeline();
}