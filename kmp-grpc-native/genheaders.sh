#!/usr/bin/env bash
mkdir -p include
cbindgen --config cbindgen.toml --crate kmp-grpc-native --lang c --output include/rpcnative.h