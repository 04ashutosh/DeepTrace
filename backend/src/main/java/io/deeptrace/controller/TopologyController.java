package io.deeptrace.controller;

import io.deeptrace.entity.ServiceDependencyEntity;
import io.deeptrace.service.TopologyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/topology")
public class TopologyController {

    private final TopologyService topologyService;

    public TopologyController(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @GetMapping
    public ResponseEntity<List<ServiceDependencyEntity>> getAllDependencies() {
        return ResponseEntity.ok(topologyService.getAllDependencies());
    }

    @GetMapping("/graph")
    public ResponseEntity<Map<String, Object>> getTopologyGraph() {
        return ResponseEntity.ok(topologyService.getTopologyGraph());
    }

    @GetMapping("/bottlenecks")
    public ResponseEntity<List<ServiceDependencyEntity>> getBottlenecks(
            @RequestParam(defaultValue = "0.05") double minErrorRate,
            @RequestParam(defaultValue = "200.0") double minP95LatencyMs) {
        return ResponseEntity.ok(topologyService.getBottlenecks(minErrorRate, minP95LatencyMs));
    }
}
