package io.deeptrace.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "spans", schema = "observability")
public class SpanEntity {

    @Id
    @Column(name = "span_id", length = 64)
    private String spanId;

    @Column(name = "trace_id", nullable = false, length = 64)
    private String traceId;

    @Column(name = "parent_span_id", length = 64)
    private String parentSpanId;

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Column(name = "operation_name", nullable = false, length = 100)
    private String operationName;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Column(name = "duration_ms", nullable = false)
    private Double durationMs;

    @Column(name = "status_code", nullable = false)
    private Integer statusCode;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "attributes", columnDefinition = "jsonb")
    private String attributes = "{}";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public SpanEntity() {}

    public SpanEntity(String spanId, String traceId, String parentSpanId, String serviceName,
                      String operationName, Instant startTime, Instant endTime,
                      Double durationMs, Integer statusCode, String errorMessage) {
        this.spanId = spanId;
        this.traceId = traceId;
        this.parentSpanId = parentSpanId;
        this.serviceName = serviceName;
        this.operationName = operationName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMs = durationMs;
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
        this.attributes = "{}";
        this.createdAt = Instant.now();
    }

    public String getSpanId() { return spanId; }
    public void setSpanId(String spanId) { this.spanId = spanId; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public String getParentSpanId() { return parentSpanId; }
    public void setParentSpanId(String parentSpanId) { this.parentSpanId = parentSpanId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getOperationName() { return operationName; }
    public void setOperationName(String operationName) { this.operationName = operationName; }

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }

    public Double getDurationMs() { return durationMs; }
    public void setDurationMs(Double durationMs) { this.durationMs = durationMs; }

    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getAttributes() { return attributes; }
    public void setAttributes(String attributes) { this.attributes = attributes; }

    public Instant getCreatedAt() { return createdAt; }
}