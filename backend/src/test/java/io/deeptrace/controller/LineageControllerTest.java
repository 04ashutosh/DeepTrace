package io.deeptrace.controller;

import io.deeptrace.entity.LineageEdge;
import io.deeptrace.service.LineageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LineageController.class)
public class LineageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LineageService lineageService;

    @Test
    void shouldReturnLineageEdges() throws Exception {
        LineageEdge edge = new LineageEdge(
                "raw.test_events",
                "analytics.test_mart",
                "DBT_MODEL",
                "test_job"
        );
        when(lineageService.getAllEdges()).thenReturn(List.of(edge));

        mockMvc.perform(get("/api/v1/lineage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sourceDataset", is("raw.test_events")))
                .andExpect(jsonPath("$[0].targetDataset", is("analytics.test_mart")));
    }

    @Test
    void shouldReturnLineageGraph() throws Exception {
        LineageEdge edge = new LineageEdge(
                "raw.test_events",
                "analytics.test_mart",
                "DBT_MODEL",
                "test_job"
        );
        Map<String, Object> graph = Map.of(
                "nodes", Set.of("raw.test_events", "analytics.test_mart"),
                "edges", List.of(edge)
        );
        when(lineageService.getGraph()).thenReturn(graph);

        mockMvc.perform(get("/api/v1/lineage/graph"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodes", hasItem("raw.test_events")))
                .andExpect(jsonPath("$.nodes", hasItem("analytics.test_mart")))
                .andExpect(jsonPath("$.edges", hasSize(1)));
    }

    @Test
    void shouldRecordNewLineageEdge() throws Exception {
        LineageEdge created = new LineageEdge(
                "landing/api_logs.json",
                "raw.api_logs",
                "PYTHON_STREAMING",
                "json_ingestor"
        );
        when(lineageService.recordEdge(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(created);

        String jsonPayload = """
            {
                "sourceDataset": "landing/api_logs.json",
                "targetDataset": "raw.api_logs",
                "transformationType": "PYTHON_STREAMING",
                "jobName": "json_ingestor"
            }
        """;

        mockMvc.perform(post("/api/v1/lineage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceDataset", is("landing/api_logs.json")))
                .andExpect(jsonPath("$.targetDataset", is("raw.api_logs")));
    }
}
