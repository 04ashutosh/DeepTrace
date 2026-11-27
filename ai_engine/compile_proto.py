"""
Compiles shared proto/ai_service.proto into Python gRPC stubs.
"""
from pathlib import Path
import subprocess
import sys


def compile_proto():
    root_dir = Path(__file__).resolve().parent.parent
    proto_dir = root_dir / "proto"
    output_dir = Path(__file__).resolve().parent / "src" / "grpc_gen"
    output_dir.mkdir(parents=True, exist_ok=True)

    cmd = [
        sys.executable,
        "-m",
        "grpc_tools.protoc",
        f"-I{proto_dir}",
        f"--python_out={output_dir}",
        f"--grpc_python_out={output_dir}",
        str(proto_dir / "ai_service.proto"),
    ]

    print(f"[*] Compiling Protobuf: {' '.join(cmd)}")
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0:
        print(f"[-] Compilation failed: {result.stderr}")
        sys.exit(1)

    (output_dir / "__init__.py").touch()

    # Fix grpc_tools relative package import quirk in generated grpc file
    grpc_file = output_dir / "ai_service_pb2_grpc.py"
    if grpc_file.exists():
        content = grpc_file.read_text(encoding="utf-8")
        if "import ai_service_pb2 as ai__service__pb2" in content:
            content = content.replace(
                "import ai_service_pb2 as ai__service__pb2",
                "from . import ai_service_pb2 as ai__service__pb2",
            )
            grpc_file.write_text(content, encoding="utf-8")

    print("[+] Protobuf compiled successfully into ai_engine/src/grpc_gen!")


if __name__ == "__main__":
    compile_proto()