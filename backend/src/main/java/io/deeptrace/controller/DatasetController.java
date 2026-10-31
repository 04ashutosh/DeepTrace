package io.deeptrace.controller;

import io.deeptrace.dto.DatasetResponse;
import io.deeptrace.service.DatasetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/datasets")
@Tag(name = "Datasets", description = "Lakehouse Catalog Registry API")
public class DatasetController {
    private final DatasetService service;

    public DatasetController(DatasetService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all registered datasets")
    public ResponseEntity<List<DatasetResponse>> getAllDatasets() {
        return ResponseEntity.ok(service.getAllDatasets());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get dataset details by UUID")
    public ResponseEntity<DatasetResponse> getDatasetById(@PathVariable UUID id) {
        return service.getDatasetById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}