package io.deeptrace.repository;

import io.deeptrace.entity.SpanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpanRepository extends JpaRepository<SpanEntity, String> {
    List<SpanEntity> findByTraceIdOrderByStartTimeAsc(String traceId);
    List<SpanEntity> findByServiceNameOrderByStartTimeDesc(String serviceName);
}