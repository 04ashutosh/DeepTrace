package io.deeptrace.service;

import io.deeptrace.dto.SpanNodeDto;
import io.deeptrace.dto.TraceTreeResponse;
import io.deeptrace.entity.SpanEntity;
import io.deeptrace.repository.SpanRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TraceService {

    private final SpanRepository spanRepository;

    public TraceService(SpanRepository spanRepository) {
        this.spanRepository = spanRepository;
    }

    public Optional<TraceTreeResponse> getTraceTree(String traceId) {
        List<SpanEntity> spans = spanRepository.findByTraceIdOrderByStartTimeAsc(traceId);
        if (spans.isEmpty()) {
            return Optional.empty();
        }

        // 1. Identify root cause span: first/deepest error span (statusCode >= 400 or error_message present)
        SpanEntity rootCause = null;
        for (SpanEntity span : spans) {
            if (span.getStatusCode() >= 400 || (span.getErrorMessage() != null && !span.getErrorMessage().isBlank())) {
                rootCause = span;
                // Prefer the callee error over parent caller error (traverse downwards)
            }
        }

        String rootCauseSpanId = rootCause != null ? rootCause.getSpanId() : null;
        String rootCauseService = rootCause != null ? rootCause.getServiceName() : null;

        // 2. Build adjacency mapping: parentSpanId -> children
        Map<String, List<SpanEntity>> parentToChildren = new HashMap<>();
        List<SpanEntity> roots = new ArrayList<>();

        for (SpanEntity span : spans) {
            if (span.getParentSpanId() == null || span.getParentSpanId().isBlank()) {
                roots.add(span);
            } else {
                parentToChildren.computeIfAbsent(span.getParentSpanId(), k -> new ArrayList<>()).add(span);
            }
        }

        // If no explicit root found, pick the earliest span as root
        if (roots.isEmpty()) {
            roots.add(spans.get(0));
        }

        // 3. Assemble recursive tree nodes
        List<SpanNodeDto> rootDtos = new ArrayList<>();
        double totalDurationMs = 0.0;

        for (SpanEntity root : roots) {
            totalDurationMs = Math.max(totalDurationMs, root.getDurationMs());
            rootDtos.add(buildNode(root, parentToChildren, rootCauseSpanId));
        }

        return Optional.of(new TraceTreeResponse(
                traceId,
                totalDurationMs,
                spans.size(),
                rootCauseSpanId,
                rootCauseService,
                rootDtos
        ));
    }

    private SpanNodeDto buildNode(SpanEntity span, Map<String, List<SpanEntity>> childrenMap, String rootCauseSpanId) {
        List<SpanEntity> childEntities = childrenMap.getOrDefault(span.getSpanId(), Collections.emptyList());
        List<SpanNodeDto> childDtos = new ArrayList<>();

        // Find child with maximum duration for critical path
        SpanEntity criticalChild = null;
        double maxChildDuration = -1.0;

        for (SpanEntity child : childEntities) {
            if (child.getDurationMs() > maxChildDuration) {
                maxChildDuration = child.getDurationMs();
                criticalChild = child;
            }
        }

        for (SpanEntity child : childEntities) {
            SpanNodeDto childDto = buildNode(child, childrenMap, rootCauseSpanId);
            if (child.equals(criticalChild)) {
                childDto = childDto.withCriticalPath(true);
            }
            childDtos.add(childDto);
        }

        boolean isRootCause = span.getSpanId().equals(rootCauseSpanId);
        return new SpanNodeDto(
                span.getSpanId(),
                span.getTraceId(),
                span.getParentSpanId(),
                span.getServiceName(),
                span.getOperationName(),
                span.getStartTime(),
                span.getEndTime(),
                span.getDurationMs(),
                span.getStatusCode(),
                span.getErrorMessage(),
                isRootCause,
                false, // set on parent traversal
                childDtos
        );
    }

    public SpanEntity recordSpan(SpanEntity span) {
        return spanRepository.save(span);
    }
}