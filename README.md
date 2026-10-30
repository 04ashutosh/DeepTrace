# DeepTrace

**Unified Data Intelligence, Data Engineering & Failure Intelligence Platform**

DeepTrace is a free-first, local-first engineering platform combining concepts from enterprise data platforms, lakehouses, distributed systems observability, and AI-assisted incident investigation.

---

## Architecture Overview (Phase 1)

In Phase 1, DeepTrace establishes a deterministic data plane and control plane:
- **Control Plane & Metadata:** Tracks registered datasets, pipeline definitions, and audit executions in PostgreSQL (`datasets`, `pipelines`, `pipeline_runs`).
- **Lakehouse Storage Layers:**
  - **Bronze:** Immutable raw payload capture (`bronze_http_events`).
  - **Silver:** Strongly-typed, normalized, query-indexed analytical events (`silver_http_events`).
  - **Quarantine:** Isolated bad or corrupt records with exact line numbers, raw contents, and violation codes (`rejected_records`).
- **Streaming Worker (Python):** Chunked constant-memory streaming I/O (`< 80 MB` RSS) with Pydantic contract validation and PostgreSQL binary COPY bulk loading.

---

## Project Structure

```
deeptrace/
├── .env.example
├── .gitignore
├── docker-compose.minimal.yml
├── pyproject.toml
├── README.md
├── data/
│   ├── archive/
│   ├── landing/
│   └── quarantine/
├── scripts/
│   └── generate_sample_data.py
├── sql/
│   ├── 01_metadata_schema.sql
│   └── 02_lakehouse_tables.sql
└── workers/
    ├── requirements.txt
    ├── src/
    │   ├── cli.py
    │   ├── config.py
    │   ├── db.py
    │   ├── ingestor/
    │   │   ├── csv_ingestor.py
    │   │   └── json_ingestor.py
    │   └── validator/
    │       ├── quarantine.py
    │       └── schema.py
    └── tests/
        ├── test_csv_ingestor.py
        └── test_validator.py
```

---

## Quickstart (Local & Free)

### 1. Start PostgreSQL (Port 5434 on host to avoid port 5432 collisions)
```bash
docker compose -f docker-compose.minimal.yml up -d
```

### 2. Setup Python Virtual Environment
```bash
python -m venv workers/.venv
source workers/.venv/bin/activate  # On Windows: .\workers\.venv\Scripts\Activate.ps1
pip install -r workers/requirements.txt
```

### 3. Run Tests
```bash
pytest -v
```

### 4. Generate Telemetry & Run Ingestion
```bash
# Generate 50,000 synthetic HTTP logs with ~1% intentional data quality anomalies
python scripts/generate_sample_data.py --rows 50000 --bad-rate 0.01 --out data/landing/http_events.csv

# Execute Streaming Ingestion Worker
python -m workers.src.cli ingest --file data/landing/http_events.csv --format csv --pipeline-name http_log_pipeline
```
