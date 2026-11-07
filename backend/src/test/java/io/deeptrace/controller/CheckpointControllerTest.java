package io.deeptrace.controller;

import io.deeptrace.entity.PipelineCheckpoint;
import io.deeptrace.repository.PipelineCheckpointRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CheckpointController.class)
public class CheckpointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PipelineCheckpointRepository checkpointRepository;

    @Test
    void shouldReturnAllCheckpoints() throws Exception {
        PipelineCheckpoint checkpoint = new PipelineCheckpoint(
                "spark_http_events_incremental",
                Instant.parse("2026-10-31T12:00:00Z"),
                5000L
        );
        when(checkpointRepository.findAll()).thenReturn(List.of(checkpoint));

        mockMvc.perform(get("/api/v1/checkpoints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].pipelineName", is("spark_http_events_incremental")))
                .andExpect(jsonPath("$[0].recordsProcessed", is(5000)));
    }

    @Test
    void shouldReturnCheckpointByName() throws Exception {
        PipelineCheckpoint checkpoint = new PipelineCheckpoint(
                "spark_http_events_incremental",
                Instant.parse("2026-10-31T12:00:00Z"),
                5000L
        );
        when(checkpointRepository.findByPipelineName("spark_http_events_incremental"))
                .thenReturn(Optional.of(checkpoint));

        mockMvc.perform(get("/api/v1/checkpoints/spark_http_events_incremental"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pipelineName", is("spark_http_events_incremental")))
                .andExpect(jsonPath("$.recordsProcessed", is(5000)));
    }

    @Test
    void shouldUpsertCheckpoint() throws Exception {
        PipelineCheckpoint checkpoint = new PipelineCheckpoint(
                "spark_http_events_incremental",
                Instant.parse("2026-10-31T12:00:00Z"),
                5000L
        );
        when(checkpointRepository.findByPipelineName("spark_http_events_incremental"))
                .thenReturn(Optional.empty());
        when(checkpointRepository.save(any(PipelineCheckpoint.class))).thenReturn(checkpoint);

        String jsonPayload = """
            {
                "pipelineName": "spark_http_events_incremental",
                "highWatermark": "2026-10-31T12:00:00Z",
                "recordsProcessed": 5000
            }
        """;

        mockMvc.perform(post("/api/v1/checkpoints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pipelineName", is("spark_http_events_incremental")))
                .andExpect(jsonPath("$.recordsProcessed", is(5000)));
    }
}
