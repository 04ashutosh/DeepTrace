package io.deeptrace.repository;

import io.deeptrace.entity.PipelineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PipelineRepository extends JpaRepository<PipelineEntity, UUID> {
    Optional<PipelineEntity> findByName(String name);
}