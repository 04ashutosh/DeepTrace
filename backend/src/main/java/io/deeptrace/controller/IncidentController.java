package io.deeptrace.controller;

import io.deeptrace.entity.IncidentEntity;
import io.deeptrace.service.IncidentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    public ResponseEntity<List<IncidentEntity>> getAllIncidents() {
        return ResponseEntity.ok(incidentService.getAllIncidents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentEntity> getIncident(@PathVariable UUID id) {
        return incidentService.getIncident(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<IncidentEntity> createIncident(@RequestBody IncidentEntity incident) {
        return ResponseEntity.ok(incidentService.createIncident(incident));
    }

    @GetMapping(value = "/{id}/investigate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter investigateStream(@PathVariable UUID id) {
        return incidentService.streamInvestigation(id);
    }
}