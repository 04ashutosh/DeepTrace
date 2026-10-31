package io.deeptrace.repository;

import io.deeptrace.entity.DatasetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface DatasetRepository extends JpaRepository<DatasetEntity, UUID> {
    Optional<DatasetEntity> findByName(String name);
}