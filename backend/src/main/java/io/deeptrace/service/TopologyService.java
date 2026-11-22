package io.deeptrace.service;

import io.deeptrace.entity.ServiceDependencyEntity;
import io.deeptrace.repository.ServiceDependencyRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TopologyService {

    private final ServiceDependencyRepository dependencyRepository;

    public TopologyService(ServiceDependencyRepository dependencyRepository) {
        this.dependencyRepository = dependencyRepository;
    }

    public List<ServiceDependencyEntity> getAllDependencies() {
        return dependencyRepository.findAll();
    }

    public Map<String, Object> getTopologyGraph() {
        List<ServiceDependencyEntity> dependencies = dependencyRepository.findAll();
        Set<String> services = new HashSet<>();
        List<Map<String, Object>> edges = new ArrayList<>();

        for (ServiceDependencyEntity dep : dependencies) {
            services.add(dep.getCallerService());
            services.add(dep.getCalleeService());

            double errorRate = dep.getCallCount() > 0
                    ? (double) dep.getErrorCount() / dep.getCallCount()
                    : 0.0;

            Map<String, Object> edge = new HashMap<>();
            edge.put("source", dep.getCallerService());
            edge.put("target", dep.getCalleeService());
            edge.put("callCount", dep.getCallCount());
            edge.put("errorCount", dep.getErrorCount());
            edge.put("errorRate", Math.round(errorRate * 10000.0) / 100.0); // % rounded to 2 decimals
            edge.put("avgLatencyMs", dep.getAvgLatencyMs());
            edge.put("p95LatencyMs", dep.getP95LatencyMs());
            edges.add(edge);
        }

        Map<String, Object> graph = new HashMap<>();
        graph.put("services", services);
        graph.put("dependencies", edges);
        return graph;
    }

    public List<ServiceDependencyEntity> getBottlenecks(double errorRateThreshold, double latencyThresholdMs) {
        return dependencyRepository.findAll().stream()
                .filter(dep -> {
                    double rate = dep.getCallCount() > 0 ? (double) dep.getErrorCount() / dep.getCallCount() : 0.0;
                    return rate >= errorRateThreshold || dep.getP95LatencyMs() >= latencyThresholdMs;
                })
                .toList();
    }
}