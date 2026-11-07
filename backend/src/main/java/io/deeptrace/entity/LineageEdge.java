package io.deeptrace.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lineage_edges", schema = "metadata")
public class LineageEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "edge_id")
    private UUID edgeId;

    @Column(name = "source_dataset", nullable = false, length = 100)
    private String sourceDataset;

    @Column(name = "target_dataset", nullable = false, length = 100)
    private String targetDataset;

    @Column(name = "transformation_type", nullable = false, length = 50)
    private String transformationType;

    @Column(name = "job_name", nullable = false, length = 100)
    private String jobName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public LineageEdge() {}

    public LineageEdge(String sourceDataset, String targetDataset, String transformationType, String jobName) {
        this.sourceDataset = sourceDataset;
        this.targetDataset = targetDataset;
        this.transformationType = transformationType;
        this.jobName = jobName;
        this.createdAt = Instant.now();
    }

    public UUID getEdgeId() { return edgeId; }
    public void setEdgeId(UUID edgeId) { this.edgeId = edgeId; }

    public String getSourceDataset() { return sourceDataset; }
    public void setSourceDataset(String sourceDataset) { this.sourceDataset = sourceDataset; }

    public String getTargetDataset() { return targetDataset; }
    public void setTargetDataset(String targetDataset) { this.targetDataset = targetDataset; }

    public String getTransformationType() { return transformationType; }
    public void setTransformationType(String transformationType) { this.transformationType = transformationType; }

    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }

    public Instant getCreatedAt() { return createdAt; }
}