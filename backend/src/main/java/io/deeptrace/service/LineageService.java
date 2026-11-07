package io.deeptrace.service;

import io.deeptrace.entity.LineageEdge;
import io.deeptrace.repository.LineageEdgeRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LineageService {

    private final LineageEdgeRepository lineageEdgeRepository;

    public LineageService(LineageEdgeRepository lineageEdgeRepository) {
        this.lineageEdgeRepository = lineageEdgeRepository;
    }

    public List<LineageEdge> getAllEdges() {
        return lineageEdgeRepository.findAll();
    }

    public LineageEdge recordEdge(String source, String target, String transformType, String jobName) {
        LineageEdge edge = new LineageEdge(source, target, transformType, jobName);
        return lineageEdgeRepository.save(edge);
    }

    public Map<String, Object> getGraph() {
        List<LineageEdge> edges = lineageEdgeRepository.findAll();
        Set<String> nodes = new HashSet<>();
        for (LineageEdge e : edges) {
            nodes.add(e.getSourceDataset());
            nodes.add(e.getTargetDataset());
        }

        Map<String, Object> graph = new HashMap<>();
        graph.put("nodes", nodes);
        graph.put("edges", edges);
        return graph;
    }
}