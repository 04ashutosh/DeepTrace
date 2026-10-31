package io.deeptrace.controller;

import io.deeptrace.dto.DatasetResponse;
import io.deeptrace.service.DatasetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DatasetController.class)
class DatasetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DatasetService datasetService;

    @Test
    void shouldReturnAllDatasets() throws Exception {
        UUID id = UUID.randomUUID();
        DatasetResponse sample = new DatasetResponse(
                id,
                "silver_http_events",
                "urn:deeptrace:dataset:silver:silver_http_events",
                "SILVER",
                "POSTGRES_TABLE",
                "public.silver_http_events",
                50000L,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(datasetService.getAllDatasets()).thenReturn(List.of(sample));

        mockMvc.perform(get("/api/v1/datasets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("silver_http_events"))
                .andExpect(jsonPath("$[0].layer").value("SILVER"))
                .andExpect(jsonPath("$[0].recordCount").value(50000));
    }
}