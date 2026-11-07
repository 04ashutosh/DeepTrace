package io.deeptrace.controller;

import io.deeptrace.entity.PipelineCheckpoint;
import io.deeptrace.repository.PipelineCheckpointRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/checkpoints")
public class CheckpointController {

    private final PipelineCheckpointRepository checkpointRepository;

    public CheckpointController(PipelineCheckpointRepository checkpointRepository) {
        this.checkpointRepository = checkpointRepository;
    }

    @GetMapping
    public ResponseEntity<List<PipelineCheckpoint>> getAllCheckpoints() {
        return ResponseEntity.ok(checkpointRepository.findAll());
    }

    @GetMapping("/{pipelineName}")
    public ResponseEntity<PipelineCheckpoint> getCheckpoint(@PathVariable String pipelineName) {
        return checkpointRepository.findByPipelineName(pipelineName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PipelineCheckpoint> upsertCheckpoint(@RequestBody Map<String, Object> payload) {
        String pipelineName = (String) payload.get("pipelineName");
        String highWatermarkStr = (String) payload.get("highWatermark");
        Instant highWatermark = Instant.parse(highWatermarkStr);
        Long recordsProcessed = payload.containsKey("recordsProcessed")
                ? ((Number) payload.get("recordsProcessed")).longValue()
                : 0L;

        PipelineCheckpoint checkpoint = checkpointRepository.findByPipelineName(pipelineName)
                .map(existing -> {
                    existing.setHighWatermark(highWatermark);
                    existing.setRecordsProcessed(existing.getRecordsProcessed() + recordsProcessed);
                    existing.setUpdatedAt(Instant.now());
                    return existing;
                })
                .orElseGet(() -> new PipelineCheckpoint(pipelineName, highWatermark, recordsProcessed));

        return ResponseEntity.ok(checkpointRepository.save(checkpoint));
    }
}