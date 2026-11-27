"""
Markdown Runbook Chunker.
Splits technical documentation by Markdown headers while preserving service context and hierarchy.
"""
from dataclasses import dataclass
from pathlib import Path
import re
from typing import List


@dataclass
class DocumentChunk:
    chunk_id: str
    doc_title: str
    service_name: str
    section_title: str
    content: str


class MarkdownRunbookChunker:

    def chunk_file(self, file_path: Path) -> List[DocumentChunk]:
        text = file_path.read_text(encoding="utf-8")
        lines = text.splitlines()

        doc_title = file_path.stem.replace("_", " ").title()
        service_name = "general"
        chunks: List[DocumentChunk] = []

        # Extract title from H1 if present
        for line in lines:
            if line.startswith("# "):
                doc_title = line.replace("# ", "").strip()
                break

        # Extract service name metadata if present (e.g. ## Service: payment-gateway)
        service_match = re.search(r"##\s*Service:\s*([\w\-]+)", text, re.IGNORECASE)
        if service_match:
            service_name = service_match.group(1).strip()

        # Split into sections based on ## or ###
        sections = re.split(r"\n(?=#{2,3}\s+)", text)
        for idx, sec in enumerate(sections):
            sec_clean = sec.strip()
            if not sec_clean:
                continue

            # Extract section header
            first_line = sec_clean.splitlines()[0]
            sec_title = re.sub(r"^#{1,3}\s*", "", first_line).strip()

            chunk = DocumentChunk(
                chunk_id=f"{file_path.stem}#sec-{idx}",
                doc_title=doc_title,
                service_name=service_name,
                section_title=sec_title,
                content=sec_clean,
            )
            chunks.append(chunk)

        return chunks

    def chunk_directory(self, dir_path: Path) -> List[DocumentChunk]:
        all_chunks: List[DocumentChunk] = []
        for file in dir_path.glob("*.md"):
            all_chunks.extend(self.chunk_file(file))
        return all_chunks