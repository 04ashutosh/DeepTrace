package io.deeptrace.controller;

import io.deeptrace.dto.SpanNodeDto;
import io.deeptrace.dto.TraceTreeResponse;
import io.deeptrace.service.TraceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TraceController.class)
public class TraceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TraceService traceService;

    @Test
    void shouldReturnTraceTree() throws Exception {
        SpanNodeDto child = new SpanNodeDto(
                "sp-pay", "tr-100", "sp-order", "payment-gateway",
                "ChargeCard", Instant.now(), Instant.now(), 450.0,
                503, "Payment Timeout", true, true, Collections.emptyList()
        );

        SpanNodeDto root = new SpanNodeDto(
                "sp-root", "tr-100", null, "frontend-proxy",
                "GET /checkout", Instant.now(), Instant.now(), 480.0,
                500, "Error", false, true, List.of(child)
        );

        TraceTreeResponse response = new TraceTreeResponse(
                "tr-100", 480.0, 2, "sp-pay", "payment-gateway", List.of(root)
        );

        when(traceService.getTraceTree("tr-100")).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/v1/traces/tr-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.traceId", is("tr-100")))
                .andExpect(jsonPath("$.rootCauseSpanId", is("sp-pay")))
                .andExpect(jsonPath("$.rootCauseService", is("payment-gateway")))
                .andExpect(jsonPath("$.spanCount", is(2)));
    }

    @Test
    void shouldReturn404ForMissingTrace() throws Exception {
        when(traceService.getTraceTree("tr-nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/traces/tr-nonexistent"))
                .andExpect(status().isNotFound());
    }
}
