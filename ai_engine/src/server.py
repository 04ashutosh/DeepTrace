"""
Python AI Engine gRPC Server.
Streams ReAct agent steps (Thought, Action, Observation, Conclusion) back to Java.
"""
from concurrent import futures
import time
import grpc

from ai_engine.src.grpc_gen import ai_service_pb2, ai_service_pb2_grpc


class AiInvestigationServicer(ai_service_pb2_grpc.AiInvestigationServiceServicer):

    def CheckHealth(self, request, context):
        return ai_service_pb2.HealthResponse(status="SERVING", version="1.0.0")

    def Investigate(self, request, context):
        """
        Streams multi-step ReAct investigation chunks over gRPC.
        """
        print(f"[*] Received investigation request for incident: {request.incident_id}")

        steps = [
            (
                ai_service_pb2.StepType.THOUGHT,
                f"Analyzing root cause for incident on service '{request.service_name}' (Trace ID: {request.trace_id}). Querying span waterfall...",
                "",
                "",
                "",
            ),
            (
                ai_service_pb2.StepType.ACTION,
                "Calling tool: query_trace_spans",
                "query_trace_spans",
                f'{{"trace_id": "{request.trace_id}"}}',
                "",
            ),
            (
                ai_service_pb2.StepType.OBSERVATION,
                "Observed 4 spans: frontend-proxy (500) -> api-gateway (500) -> order-service (500) -> payment-gateway (503 Gateway Timeout, duration: 450ms).",
                "query_trace_spans",
                "",
                '{"failed_service": "payment-gateway", "status": 503, "duration_ms": 450}',
            ),
            (
                ai_service_pb2.StepType.THOUGHT,
                "The error cascaded upstream from payment-gateway. Searching runbooks for 'Payment Gateway Timeout'...",
                "",
                "",
                "",
            ),
            (
                ai_service_pb2.StepType.CONCLUSION,
                "Root Cause Isolated: payment-gateway threw HTTP 503 due to upstream processor timeout. Mitigation: Enable circuit breaker and failover to secondary processor.",
                "",
                "",
                "",
            ),
        ]

        for step_type, content, tool_name, tool_in, tool_out in steps:
            time.sleep(0.1)  # Simulate reasoning latency
            yield ai_service_pb2.InvestigationStepResponse(
                incident_id=request.incident_id,
                step_type=step_type,
                content=content,
                tool_name=tool_name,
                tool_input_json=tool_in,
                tool_output_json=tool_out,
                timestamp_epoch_ms=int(time.time() * 1000),
            )

    def SearchRunbooks(self, request, context):
        chunks = [
            ai_service_pb2.RunbookChunk(
                title="Payment Gateway Outage Runbook",
                service_name="payment-gateway",
                content="Step 1: Check upstream third-party status page. Step 2: Enable secondary circuit breaker.",
                similarity_score=0.92,
            )
        ]
        return ai_service_pb2.RunbookSearchResponse(chunks=chunks)


def create_server(port: int = 50051) -> grpc.Server:
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    ai_service_pb2_grpc.add_AiInvestigationServiceServicer_to_server(
        AiInvestigationServicer(), server
    )
    server.add_insecure_port(f"[::]:{port}")
    return server


def serve(port: int = 50051):
    server = create_server(port)
    server.start()
    print(f"[+] DeepTrace AI Engine gRPC Server listening on port {port}")
    server.wait_for_termination()


if __name__ == "__main__":
    serve()