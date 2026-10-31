package io.deeptrace.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "datasets")
public class DatasetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 128)
    private String name;

    @Column(nullable = false, unique = true, length = 256)
    private String urn;

    @Column(nullable = false, length = 32)
    private String layer;

    @Column(nullable = false, length = 32)
    private String format;

    @Column(name = "storage_location", nullable = false, columnDefinition = "TEXT")
    private String storageLocation;

    @Column(name = "schema_definition", nullable = false, columnDefinition = "JSONB")
    private String schemaDefinition;

    @Column(name = "record_count")
    private Long recordCount = 0L;

    @Column(name = "last_successful_run_at")
    private OffsetDateTime lastSuccessfulRunAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUrn() { return urn; }
    public void setUrn(String urn) { this.urn = urn; }
    public String getLayer() { return layer; }
    public void setLayer(String layer) { this.layer = layer; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
    public String getSchemaDefinition() { return schemaDefinition; }
    public void setSchemaDefinition(String schemaDefinition) { this.schemaDefinition = schemaDefinition; }
    public Long getRecordCount() { return recordCount; }
    public void setRecordCount(Long recordCount) { this.recordCount = recordCount; }
    public OffsetDateTime getLastSuccessfulRunAt() { return lastSuccessfulRunAt; }
    public void setLastSuccessfulRunAt(OffsetDateTime lastSuccessfulRunAt) { this.lastSuccessfulRunAt = lastSuccessfulRunAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}