package io.deeptrace.controller;

import io.deeptrace.entity.LineageEdge;
import io.deeptrace.service.LineageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/lineage")
public class LineageController {

    private final LineageService lineageService;

    public LineageController(LineageService lineageService) {
        this.lineageService = lineageService;
    }

    @GetMapping
    public ResponseEntity<List<LineageEdge>> getAllEdges() {
        return ResponseEntity.ok(lineageService.getAllEdges());
    }

    @GetMapping("/graph")
    public ResponseEntity<Map<String, Object>> getLineageGraph() {
        return ResponseEntity.ok(lineageService.getGraph());
    }

    @PostMapping
    public ResponseEntity<LineageEdge> recordEdge(@RequestBody Map<String, String> payload) {
        LineageEdge edge = lineageService.recordEdge(
                payload.get("sourceDataset"),
                payload.get("targetDataset"),
                payload.get("transformationType"),
                payload.get("jobName")
        );
        return ResponseEntity.ok(edge);
    }
}