# ANTIGRAVITY MASTER PROMPT — DEEPTRACE (AI ENGINEERING EDITION)

## DeepTrace: Autonomous AI Incident Investigation, Distributed Observability & Root-Cause Analysis Platform

---

### 1. PROJECT MISSION

Build an original, production-oriented engineering platform named:
**DeepTrace — Autonomous AI Incident Investigation & Distributed Systems Observability Platform**

DeepTrace combines principles from modern distributed systems observability (distributed tracing, span waterfall graphs, metric anomalies) with agentic AI engineering (autonomous multi-step reasoning, tool use, hybrid RAG over runbooks, and automated incident triage).

When a microservice outage or latency anomaly occurs, human on-call engineers spend critical hours correlating traces, logs, and wikis. **DeepTrace automates this workflow**:
1. Ingests distributed traces, service dependencies, and error events in real-time.
2. Dispatches an **Autonomous AI Investigation Agent (ReAct)** to isolate the root cause by actively querying traces, checking error cascading chains, and reading system runbooks.
3. Performs **Hybrid RAG** (dense vector embeddings + keyword search) over past incident post-mortems and architecture docs to suggest immediate mitigations.
4. Streams the agent's real-time thought process (**Thought $\to$ Action $\to$ Observation $\to$ Synthesis**) to an interactive web dashboard.
5. Runs an automated **LLM Evaluation (Evals) Suite** to test and verify agent reasoning accuracy, tool-calling correctness, and retrieval precision.

---

### 2. NON-NEGOTIABLE FREE-FIRST CONSTRAINTS

1. **Zero Mandatory Paid APIs:** The entire development and test workflow must run locally for free without requiring OpenAI, Anthropic, or any paid cloud subscriptions.
2. **Local AI Stack:**
   - LLMs: Supported via local **Ollama** (e.g. `llama3.2`, `mistral`, `phi-3`) or an included deterministic **Mock LLM engine** for reproducible CI/CD unit and integration testing. Optional cloud API adapters (OpenAI, Gemini) supported if keys are provided.
   - Vector Storage: **pgvector** running inside PostgreSQL or embedded **ChromaDB**.
   - Embeddings: Open-source local sentence-transformers (e.g. `all-MiniLM-L6-v2` / `FastEmbed`).
3. **Resource-Aware Defaults:** Optimized to run comfortably on a developer laptop with 4 CPU cores and 8–16 GB RAM.

---

### 3. TARGET THREE-TIER ARCHITECTURE

```
┌────────────────────────────────────────────────────────┐
│             TypeScript Frontend (React / Vite)          │
│  - Trace Waterfall Flamegraph & Topology DAG           │
│  - Live Agent Reasoning Stream (SSE Terminal)          │
│  - Incident Triage Board & Runbook Knowledge Base      │
└───────────────────────────┬────────────────────────────┘
                            │ REST / SSE Stream
                            ▼
┌────────────────────────────────────────────────────────┐
│         Java 21 / Spring Boot 3 Control Plane          │
│  - High-Throughput Telemetry Ingestion (Spans/Traces)  │
│  - Incident State Machine (OPEN, INVESTIGATING, RESOLVED│
│  - SSE Dispatcher & Event Broadcast                    │
│  - PostgreSQL Persistence + Flyway Versioned Schema     │
└───────────────────────────┬────────────────────────────┘
                            │ Internal RPC / REST
                            ▼
┌────────────────────────────────────────────────────────┐
│         Python 3.11+ AI Engine & Evaluation Suite       │
│  - Autonomous ReAct Agent Loop with Tool Calling       │
│  - Hybrid RAG Engine (Vector Embeddings + Runbooks)    │
│  - Telemetry Analysis Tools (Span queries, Bottlenecks)│
│  - LLM Evaluation Suite (Recall@k, Tool Accuracy)      │
└────────────────────────────────────────────────────────┘
```

#### Tier 1: Python AI Engine (`ai_engine/`)
- **FastAPI / Pydantic v2**: Typed REST interface for agent investigations and RAG search.
- **Agentic ReAct Loop**: Multi-step autonomous agent with tool-calling capabilities (`query_trace_spans`, `analyze_critical_path`, `search_runbooks`, `get_service_dependencies`).
- **Hybrid RAG Engine**: Semantic search across incident runbooks, architecture markdown docs, and historical post-mortems using vector embeddings.
- **LLM Evaluation Pipeline (`evals/`)**: Automated test framework measuring:
  - Tool Selection Accuracy (%)
  - Retrieval Recall@k & MRR
  - Root-cause identification accuracy against a benchmark dataset of synthetic incidents.
  - Latency and token consumption tracking.

#### Tier 2: Java Control Plane (`backend/`)
- **Java 21 & Spring Boot 3.3.x**: Enterprise control plane and API gateway.
- **Spring Data JPA & PostgreSQL**: Manages spans, traces, incident state machines, and audit logs.
- **Server-Sent Events (SSE)**: Streams agent reasoning tokens and tool observations in real-time to the frontend.
- **Flyway Migrations**: Production-grade database version control.
- **JUnit 5 & MockMvc**: Integration and controller slice test suites.

#### Tier 3: TypeScript Frontend (`frontend/`)
- **React 18+ / Vite / TailwindCSS**: Modern, responsive developer UI.
- **Trace Explorer**: Visual waterfall visualization showing parent-child span latencies and critical paths.
- **AI Agent Terminal**: Live-streaming terminal displaying the agent's chain of thought, tool calls, and final post-mortem.
- **Incident Workspace**: Triage board with severity levels, service topology graphs, and suggested mitigations.

---

### 4. SDE 1–2 ENGINEERING CRITERIA

The platform must adhere to rigorous software engineering practices:
1. **Separation of Concerns:** Python owns AI/ML/Evals; Java owns ingestion, state management, and enterprise APIs; TypeScript owns visualization.
2. **Deterministic Automated Testing:** Pytest for the Python AI engine and evals; JUnit 5 for the Java control plane; automated end-to-end integration contracts.
3. **Resilience & Fallbacks:** Graceful degradation when external LLM endpoints fail; timeout handling; retry loops; input sanitation.
4. **Clean Code & Typings:** Strict type safety (Python Pydantic models, Java records/DTOs, TypeScript interfaces).

---

### 5. MILESTONE IMPLEMENTATION ROADMAP

- **Milestone 1: Telemetry & Observability Foundation (Java + PostgreSQL)**
  - Distributed tracing data model (`spans`, `traces`, `service_dependencies`).
  - High-throughput ingestion API and critical path reconstruction.
  - Flyway migration and integration tests.
- **Milestone 2: Hybrid RAG Runbook Engine (Python + Vector DB)**
  - Markdown runbook chunker and embedder.
  - Vector similarity search (`all-MiniLM-L6-v2`) + keyword search.
  - Integration with PostgreSQL/pgvector or embedded vector store.
- **Milestone 3: Autonomous Incident Investigation Agent (Python ReAct)**
  - ReAct reasoning loop with function/tool calling.
  - Live streaming of agent steps (Thought, Action, Observation, Conclusion).
  - Integration between Spring Boot SSE gateway and Python agent.
- **Milestone 4: LLM Evaluation Framework & Guardrails (Python Evals)**
  - Benchmark dataset of known microservice failure scenarios.
  - Automated evaluation harness for tool selection accuracy and root-cause precision.
- **Milestone 5: Interactive AI Observability UI (TypeScript + React)**
  - Waterfall trace visualizer, streaming AI terminal, and incident triage dashboard.
