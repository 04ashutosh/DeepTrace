from pathlib import Path
from ai_engine.src.rag.chunker import MarkdownRunbookChunker


def test_chunk_markdown_file(tmp_path: Path):
    sample_doc = tmp_path / "payment_gateway_timeout.md"
    sample_doc.write_text(
        """# Runbook: Payment Gateway Timeout
## Service: payment-gateway

### Symptoms
- Cascading 500s in order-service.

### Immediate Mitigation Steps
- Enable circuit breaker.
""",
        encoding="utf-8",
    )

    chunker = MarkdownRunbookChunker()
    chunks = chunker.chunk_file(sample_doc)

    assert len(chunks) >= 2
    assert chunks[0].service_name == "payment-gateway"
    assert any("circuit breaker" in c.content for c in chunks)