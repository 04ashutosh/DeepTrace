package io.deeptrace.controller;

import io.deeptrace.dto.PipelineRunResponse;
import io.deeptrace.service.PipelineRunService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/runs")
@Tag(name = "Pipeline Runs", description = "Pipeline Execution and Audit Tracking API")
public class PipelineRunController {
    private final PipelineRunService service;

    public PipelineRunController(PipelineRunService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all historical pipeline runs")
    public ResponseEntity<List<PipelineRunResponse>> getAllRuns() {
        return ResponseEntity.ok(service.getAllRuns());
    }

    @GetMapping("/{runUuid}")
    @Operation(summary = "Get pipeline run audit record by run UUID")
    public ResponseEntity<PipelineRunResponse> getRunByUuid(@PathVariable UUID runUuid) {
        return service.getRunByUuid(runUuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}