package io.deeptrace.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "pipeline_runs")
public class PipelineRunEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id", nullable = false)
    private PipelineEntity pipeline;

    @Column(name = "run_uuid", nullable = false, unique = true)
    private UUID runUuid;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "input_file", length = 512)
    private String inputFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_dataset_id")
    private DatasetEntity targetDataset;

    @Column(name = "records_read")
    private Long recordsRead = 0L;

    @Column(name = "records_accepted")
    private Long recordsAccepted = 0L;

    @Column(name = "records_rejected")
    private Long recordsRejected = 0L;

    @Column(name = "records_written")
    private Long recordsWritten = 0L;

    @Column(name = "duration_ms")
    private Long durationMs = 0L;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt = OffsetDateTime.now();

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    public UUID getId() { return id; }
    public PipelineEntity getPipeline() { return pipeline; }
    public void setPipeline(PipelineEntity pipeline) { this.pipeline = pipeline; }
    public UUID getRunUuid() { return runUuid; }
    public void setRunUuid(UUID runUuid) { this.runUuid = runUuid; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getInputFile() { return inputFile; }
    public void setInputFile(String inputFile) { this.inputFile = inputFile; }
    public DatasetEntity getTargetDataset() { return targetDataset; }
    public void setTargetDataset(DatasetEntity targetDataset) { this.targetDataset = targetDataset; }
    public Long getRecordsRead() { return recordsRead; }
    public void setRecordsRead(Long recordsRead) { this.recordsRead = recordsRead; }
    public Long getRecordsAccepted() { return recordsAccepted; }
    public void setRecordsAccepted(Long recordsAccepted) { this.recordsAccepted = recordsAccepted; }
    public Long getRecordsRejected() { return recordsRejected; }
    public void setRecordsRejected(Long recordsRejected) { this.recordsRejected = recordsRejected; }
    public Long getRecordsWritten() { return recordsWritten; }
    public void setRecordsWritten(Long recordsWritten) { this.recordsWritten = recordsWritten; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
}