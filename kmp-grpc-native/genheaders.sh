#!/usr/bin/env bash

cargo install cbindgen

mkdir -p include
$HOME/.cargo/bin/cbindgen --config cbindgen.toml --crate kmp-grpc-native --lang c --output include/rpcnative.h