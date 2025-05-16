#!/usr/bin/env bash
set -e

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

cargo install cargo-about

for target in "${targets[@]}"; do
  out_dir="target/${target}"
  mkdir -p "$out_dir"
  echo "Generating license file for target: $target"
  cargo about generate about.hbs --target "${target}" > "${out_dir}/THIRD_PARTY_LICENSES.html"
done
