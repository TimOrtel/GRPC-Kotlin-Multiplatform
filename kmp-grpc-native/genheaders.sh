#!/usr/bin/env bash
set -e

export PATH="$HOME/.cargo/bin:$PATH"

if ! command -v cbindgen >/dev/null 2>&1; then
    cargo install --locked cbindgen
fi

mkdir -p include
cbindgen --config cbindgen.toml --crate kmp-grpc-native --lang c --output include/rpcnative.h