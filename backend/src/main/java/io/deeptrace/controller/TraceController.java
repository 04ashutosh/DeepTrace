package io.deeptrace.controller;

import io.deeptrace.dto.TraceTreeResponse;
import io.deeptrace.entity.SpanEntity;
import io.deeptrace.service.TraceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/traces")
public class TraceController {

    private final TraceService traceService;

    public TraceController(TraceService traceService) {
        this.traceService = traceService;
    }

    @GetMapping("/{traceId}")
    public ResponseEntity<TraceTreeResponse> getTraceTree(@PathVariable String traceId) {
        return traceService.getTraceTree(traceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/spans")
    public ResponseEntity<SpanEntity> ingestSpan(@RequestBody SpanEntity span) {
        return ResponseEntity.ok(traceService.recordSpan(span));
    }
}
