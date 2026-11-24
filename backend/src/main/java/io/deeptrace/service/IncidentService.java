package io.deeptrace.service;

import io.deeptrace.entity.IncidentEntity;
import io.deeptrace.repository.IncidentRepository;
import io.deeptrace.grpc.AiInvestigationServiceGrpc;
import io.deeptrace.grpc.InvestigationRequest;
import io.deeptrace.grpc.InvestigationStepResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final String aiServiceHost;
    private final int aiServicePort;

    public IncidentService(
            IncidentRepository incidentRepository,
            @Value("${ai.service.host:localhost}") String aiServiceHost,
            @Value("${ai.service.port:50051}") int aiServicePort) {
        this.incidentRepository = incidentRepository;
        this.aiServiceHost = aiServiceHost;
        this.aiServicePort = aiServicePort;
    }

    public List<IncidentEntity> getAllIncidents() {
        return incidentRepository.findAll();
    }

    public Optional<IncidentEntity> getIncident(UUID id) {
        return incidentRepository.findById(id);
    }

    public IncidentEntity createIncident(IncidentEntity incident) {
        return incidentRepository.save(incident);
    }

    /**
     * Calls Python AI Engine over gRPC and streams response chunks to client via SSE.
     */
    public SseEmitter streamInvestigation(UUID incidentId) {
        SseEmitter emitter = new SseEmitter(180_000L); // 3-minute timeout

        IncidentEntity incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + incidentId));

        incident.setStatus("INVESTIGATING");
        incidentRepository.save(incident);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(aiServiceHost, aiServicePort)
                .usePlaintext()
                .build();

        AiInvestigationServiceGrpc.AiInvestigationServiceStub stub =
                AiInvestigationServiceGrpc.newStub(channel);

        InvestigationRequest request = InvestigationRequest.newBuilder()
                .setIncidentId(incident.getIncidentId().toString())
                .setTraceId(incident.getTraceId())
                .setServiceName(incident.getServiceName())
                .setErrorMessage("Cascading failure detected")
                .build();

        stub.investigate(request, new StreamObserver<InvestigationStepResponse>() {
            @Override
            public void onNext(InvestigationStepResponse step) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("step")
                            .data(step.getContent()));

                    // If final conclusion, update incident root cause
                    if ("CONCLUSION".equals(step.getStepType().name())) {
                        incident.setRootCause(step.getContent());
                        incident.setStatus("RESOLVED");
                        incidentRepository.save(incident);
                    }
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                channel.shutdown();
                emitter.completeWithError(t);
            }

            @Override
            public void onCompleted() {
                channel.shutdown();
                emitter.complete();
            }
        });

        return emitter;
    }
}