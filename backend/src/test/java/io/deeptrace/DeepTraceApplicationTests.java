package io.deeptrace;

import io.deeptrace.dto.PipelineRunResponse;
import io.deeptrace.dto.ServiceMetricsResponse;
import io.deeptrace.entity.DatasetEntity;
import io.deeptrace.entity.PipelineEntity;
import io.deeptrace.repository.DatasetRepository;
import io.deeptrace.repository.PipelineRepository;
import io.deeptrace.repository.PipelineRunRepository;
import io.deeptrace.service.AnalyticsService;
import io.deeptrace.service.DatasetService;
import io.deeptrace.service.PipelineRunService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DeepTraceApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private PipelineRepository pipelineRepository;

    @Autowired
    private PipelineRunRepository pipelineRunRepository;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private PipelineRunService pipelineRunService;

    @Autowired
    private AnalyticsService analyticsService;

    @Test
    @DisplayName("Application Context Loads and Critical Beans Are Registered")
    void contextLoads() {
        assertNotNull(applicationContext, "ApplicationContext should not be null");
        assertNotNull(datasetRepository, "DatasetRepository should be loaded");
        assertNotNull(pipelineRepository, "PipelineRepository should be loaded");
        assertNotNull(pipelineRunRepository, "PipelineRunRepository should be loaded");
        assertNotNull(datasetService, "DatasetService should be loaded");
        assertNotNull(pipelineRunService, "PipelineRunService should be loaded");
        assertNotNull(analyticsService, "AnalyticsService should be loaded");
    }

    @Test
    @DisplayName("Seeded Metadata Schema (Dataset & Pipeline) Is Present in PostgreSQL")
    void testSeededMetadataIsPresent() {
        Optional<DatasetEntity> datasetOpt = datasetRepository.findByName("silver_http_events");
        assertTrue(datasetOpt.isPresent(), "Dataset 'silver_http_events' should be seeded by Flyway V1");
        DatasetEntity dataset = datasetOpt.get();
        assertEquals("SILVER", dataset.getLayer());
        assertEquals("POSTGRES_TABLE", dataset.getFormat());
        assertEquals("public.silver_http_events", dataset.getStorageLocation());

        Optional<PipelineEntity> pipelineOpt = pipelineRepository.findByName("http_log_pipeline");
        assertTrue(pipelineOpt.isPresent(), "Pipeline 'http_log_pipeline' should be seeded by Flyway V1");
        PipelineEntity pipeline = pipelineOpt.get();
        assertEquals("MANUAL", pipeline.getScheduleType());
        assertTrue(pipeline.getIsActive());
    }

    @Test
    @DisplayName("Pipeline Runs Verify Ingestion Audit Invariant: Read == Accepted + Rejected")
    void testPipelineRunsAuditInvariant() {
        List<PipelineRunResponse> runs = pipelineRunService.getAllRuns();
        assertNotNull(runs, "Runs list should not be null");
        
        // If ingestion has been run, verify each run's audit invariant
        for (PipelineRunResponse run : runs) {
            assertEquals("SUCCEEDED", run.status(), "Completed runs should have SUCCEEDED status");
            assertEquals(
                run.recordsRead(),
                run.recordsAccepted() + run.recordsRejected(),
                "Audit Invariant violated: recordsRead must equal recordsAccepted + recordsRejected"
            );
            assertTrue(run.durationMs() >= 0, "Duration in milliseconds should be non-negative");
        }
    }

    @Test
    @DisplayName("Analytical Percentiles (p50, p95, p99) and Quarantine Breakdowns Execute Accurately")
    void testAnalyticsQueriesExecution() {
        List<ServiceMetricsResponse> metrics = analyticsService.getServiceMetrics();
        assertNotNull(metrics, "Service metrics list should not be null");

        for (ServiceMetricsResponse metric : metrics) {
            assertNotNull(metric.serviceName(), "Service name should not be null");
            assertTrue(metric.totalRequests() > 0, "Request count should be greater than zero");
            assertTrue(metric.p50LatencyMs() >= 0, "p50 latency should be non-negative");
            assertTrue(metric.p95LatencyMs() >= metric.p50LatencyMs(), "p95 latency should be >= p50 latency");
            assertTrue(metric.p99LatencyMs() >= metric.p95LatencyMs(), "p99 latency should be >= p95 latency");
        }

        List<Map<String, Object>> quarantine = analyticsService.getQuarantineBreakdown();
        assertNotNull(quarantine, "Quarantine breakdown should not be null");
    }
}
