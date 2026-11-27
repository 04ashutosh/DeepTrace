from concurrent import futures
import grpc
import pytest

from ai_engine.src.server import AiInvestigationServicer
from ai_engine.src.grpc_gen import ai_service_pb2, ai_service_pb2_grpc


@pytest.fixture(scope="module")
def grpc_server():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=2))
    ai_service_pb2_grpc.add_AiInvestigationServiceServicer_to_server(
        AiInvestigationServicer(), server
    )
    port = server.add_insecure_port("[::]:0")  # Bind to ephemeral port
    server.start()
    yield port
    server.stop(0)


def test_grpc_health_check(grpc_server):
    with grpc.insecure_channel(f"localhost:{grpc_server}") as channel:
        stub = ai_service_pb2_grpc.AiInvestigationServiceStub(channel)
        response = stub.CheckHealth(ai_service_pb2.HealthRequest())
        assert response.status == "SERVING"
        assert response.version == "1.0.0"


def test_grpc_streaming_investigation(grpc_server):
    with grpc.insecure_channel(f"localhost:{grpc_server}") as channel:
        stub = ai_service_pb2_grpc.AiInvestigationServiceStub(channel)
        request = ai_service_pb2.InvestigationRequest(
            incident_id="inc-123",
            trace_id="tr-456",
            service_name="payment-gateway",
            error_message="Gateway Timeout",
        )
        stream = stub.Investigate(request)
        steps = list(stream)

        assert len(steps) == 5
        assert steps[0].step_type == ai_service_pb2.StepType.THOUGHT
        assert steps[-1].step_type == ai_service_pb2.StepType.CONCLUSION
        assert "payment-gateway" in steps[-1].content


def test_grpc_search_runbooks(grpc_server):
    with grpc.insecure_channel(f"localhost:{grpc_server}") as channel:
        stub = ai_service_pb2_grpc.AiInvestigationServiceStub(channel)
        request = ai_service_pb2.RunbookSearchRequest(
            query="payment failure timeout",
            top_k=3,
        )
        response = stub.SearchRunbooks(request)
        assert len(response.chunks) == 1
        assert response.chunks[0].service_name == "payment-gateway"
        assert response.chunks[0].similarity_score > 0.8