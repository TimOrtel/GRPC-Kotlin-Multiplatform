#!/usr/bin/env bash
set -e  # Exit on error

source .venv/bin/activate

python -m grpc_tools.protoc -I ../src/commonMain/proto/general --python_out=server --grpc_python_out=server ../src/commonMain/proto/general/*

python -m grpc_tools.protoc -I ../src/commonMain/proto/editions/ -I ../src/commonMain/proto/general --python_out=server --grpc_python_out=server ../src/commonMain/proto/editions/*
