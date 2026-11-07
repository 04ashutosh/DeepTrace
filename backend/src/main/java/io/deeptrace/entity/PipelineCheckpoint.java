package io.deeptrace.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pipeline_checkpoints", schema = "metadata")
public class PipelineCheckpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "checkpoint_id")
    private UUID checkpointId;

    @Column(name = "pipeline_name", nullable = false, unique = true, length = 100)
    private String pipelineName;

    @Column(name = "high_watermark", nullable = false)
    private Instant highWatermark;

    @Column(name = "records_processed")
    private Long recordsProcessed = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public PipelineCheckpoint() {}

    public PipelineCheckpoint(String pipelineName, Instant highWatermark, Long recordsProcessed) {
        this.pipelineName = pipelineName;
        this.highWatermark = highWatermark;
        this.recordsProcessed = recordsProcessed;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getCheckpointId() { return checkpointId; }
    public void setCheckpointId(UUID checkpointId) { this.checkpointId = checkpointId; }

    public String getPipelineName() { return pipelineName; }
    public void setPipelineName(String pipelineName) { this.pipelineName = pipelineName; }

    public Instant getHighWatermark() { return highWatermark; }
    public void setHighWatermark(Instant highWatermark) { this.highWatermark = highWatermark; }

    public Long getRecordsProcessed() { return recordsProcessed; }
    public void setRecordsProcessed(Long recordsProcessed) { this.recordsProcessed = recordsProcessed; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}