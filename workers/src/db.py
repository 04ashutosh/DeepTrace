import psycopg
from contextlib import contextmanager
from typing import Generator
from .config import DatabaseConfig

class DatabaseManager:
    def __init__(self, config: DatabaseConfig = None):
        self.config = config or DatabaseConfig()

    @contextmanager
    def get_connection(self) -> Generator[psycopg.Connection, None, None]:
        conn = psycopg.connect(self.config.conninfo)
        try:
            yield conn
            conn.commit()
        except Exception:
            conn.rollback()
            raise
        finally:
            conn.close()