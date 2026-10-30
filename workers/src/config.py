import os
from dataclasses import dataclass

@dataclass(frozen=True)
class DatabaseConfig:
    host: str = os.getenv("POSTGRES_HOST", "localhost")
    port: int = int(os.getenv("POSTGRES_PORT", "5434"))
    dbname: str = os.getenv("POSTGRES_DB", "deeptrace")
    user: str = os.getenv("POSTGRES_USER", "deeptrace_user")
    password: str = os.getenv("POSTGRES_PASSWORD", "deeptrace_password")

    @property
    def conninfo(self) -> str:
        return f"host={self.host} port={self.port} dbname={self.dbname} user={self.user} password={self.password}"
