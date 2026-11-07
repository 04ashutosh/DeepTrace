package io.deeptrace.repository;

import io.deeptrace.entity.LineageEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LineageEdgeRepository extends JpaRepository<LineageEdge, UUID> {
    List<LineageEdge> findBySourceDataset(String sourceDataset);
    List<LineageEdge> findByTargetDataset(String targetDataset);
}