#!/usr/bin/env bash
set -e

export PATH="$HOME/.cargo/bin:$PATH"

targets=(
  "aarch64-apple-darwin"
  "aarch64-apple-ios"
  "aarch64-apple-ios-sim"
  "x86_64-apple-ios"
  "x86_64-apple-darwin"
  "x86_64-unknown-linux-gnu"
  "aarch64-unknown-linux-gnu"
  "x86_64-pc-windows-gnu"
)

if ! command -v cargo-about >/dev/null 2>&1; then
    cargo install --locked --features cli cargo-about
fi

for target in "${targets[@]}"; do
  out_dir="target/${target}"
  mkdir -p "$out_dir"
  echo "Generating license file for target: $target"
  output=$(cargo about generate about.hbs --target "${target}" 2>&1 > "${out_dir}/THIRD_PARTY_LICENSES.html")

  if echo "$output" | grep -q "WARN" ; then
    echo "Error: cargo-about emitted warnings:"
    echo "$output"
    exit 1
  fi
done
