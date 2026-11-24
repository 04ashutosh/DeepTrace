package io.deeptrace.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "incidents", schema = "observability")
public class IncidentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "incident_id")
    private UUID incidentId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Column(name = "trace_id", nullable = false, length = 64)
    private String traceId;

    @Column(name = "severity", nullable = false, length = 20)
    private String severity = "HIGH";

    @Column(name = "status", nullable = false, length = 30)
    private String status = "OPEN";

    @Column(name = "root_cause")
    private String rootCause;

    @Column(name = "mitigation_steps")
    private String mitigationSteps;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    public IncidentEntity() {}

    public IncidentEntity(String title, String serviceName, String traceId, String severity) {
        this.title = title;
        this.serviceName = serviceName;
        this.traceId = traceId;
        this.severity = severity;
        this.status = "OPEN";
        this.createdAt = Instant.now();
    }

    public UUID getIncidentId() { return incidentId; }
    public void setIncidentId(UUID incidentId) { this.incidentId = incidentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRootCause() { return rootCause; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }

    public String getMitigationSteps() { return mitigationSteps; }
    public void setMitigationSteps(String mitigationSteps) { this.mitigationSteps = mitigationSteps; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
}