package io.deeptrace.controller;

import io.deeptrace.entity.IncidentEntity;
import io.deeptrace.service.IncidentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IncidentController.class)
public class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IncidentService incidentService;

    @Test
    void shouldReturnAllIncidents() throws Exception {
        IncidentEntity incident = new IncidentEntity(
                "Payment Failure", "payment-gateway", "tr-100", "CRITICAL"
        );
        when(incidentService.getAllIncidents()).thenReturn(List.of(incident));

        mockMvc.perform(get("/api/v1/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Payment Failure")))
                .andExpect(jsonPath("$[0].serviceName", is("payment-gateway")));
    }

    @Test
    void shouldReturnIncidentById() throws Exception {
        UUID id = UUID.randomUUID();
        IncidentEntity incident = new IncidentEntity(
                "Payment Failure", "payment-gateway", "tr-100", "CRITICAL"
        );
        incident.setIncidentId(id);

        when(incidentService.getIncident(id)).thenReturn(Optional.of(incident));

        mockMvc.perform(get("/api/v1/incidents/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Payment Failure")))
                .andExpect(jsonPath("$.serviceName", is("payment-gateway")));
    }

    @Test
    void shouldCreateIncident() throws Exception {
        IncidentEntity incident = new IncidentEntity(
                "Auth Timeout", "auth-service", "tr-200", "HIGH"
        );
        when(incidentService.createIncident(any(IncidentEntity.class))).thenReturn(incident);

        String payload = """
            {
                "title": "Auth Timeout",
                "serviceName": "auth-service",
                "traceId": "tr-200",
                "severity": "HIGH"
            }
        """;

        mockMvc.perform(post("/api/v1/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Auth Timeout")))
                .andExpect(jsonPath("$.serviceName", is("auth-service")));
    }
}