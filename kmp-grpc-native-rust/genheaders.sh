#!/usr/bin/env bash
cbindgen --config cbindgen.toml --crate kmp-grpc-native-rust --lang c --output rpcnative.h