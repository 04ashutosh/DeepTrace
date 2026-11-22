package io.deeptrace.controller;

import io.deeptrace.entity.ServiceDependencyEntity;
import io.deeptrace.service.TopologyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TopologyController.class)
public class TopologyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TopologyService topologyService;

    @Test
    void shouldReturnTopologyGraph() throws Exception {
        Map<String, Object> edge = Map.of(
                "source", "order-service",
                "target", "payment-gateway",
                "callCount", 1000L,
                "errorCount", 150L,
                "errorRate", 15.0,
                "p95LatencyMs", 450.0
        );

        Map<String, Object> graph = Map.of(
                "services", Set.of("order-service", "payment-gateway"),
                "dependencies", List.of(edge)
        );

        when(topologyService.getTopologyGraph()).thenReturn(graph);

        mockMvc.perform(get("/api/v1/topology/graph"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.services", hasItem("order-service")))
                .andExpect(jsonPath("$.dependencies", hasSize(1)))
                .andExpect(jsonPath("$.dependencies[0].source", is("order-service")))
                .andExpect(jsonPath("$.dependencies[0].target", is("payment-gateway")));
    }

    @Test
    void shouldReturnBottlenecks() throws Exception {
        ServiceDependencyEntity bottleneck = new ServiceDependencyEntity(
                "order-service", "payment-gateway", 1000L, 150L, 250.0, 480.0
        );

        when(topologyService.getBottlenecks(0.05, 200.0)).thenReturn(List.of(bottleneck));

        mockMvc.perform(get("/api/v1/topology/bottlenecks?minErrorRate=0.05&minP95LatencyMs=200.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].callerService", is("order-service")))
                .andExpect(jsonPath("$[0].calleeService", is("payment-gateway")));
    }
}
