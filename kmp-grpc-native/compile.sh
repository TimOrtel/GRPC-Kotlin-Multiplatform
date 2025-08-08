#!/usr/bin/env bash
set -e  # Exit on error

profile="${1:-dev}"
target_group="${2:-all}"

targets_apple_test=("aarch64-apple-darwin" "aarch64-apple-ios" "aarch64-apple-ios-sim" "x86_64-apple-ios" "x86_64-apple-darwin")
targets_others_test=("x86_64-unknown-linux-gnu" "aarch64-unknown-linux-gnu")
targets_other=("x86_64-pc-windows-gnu")

if [[ "$target_group" != "all" && "$target_group" != "other_test" && "$target_group" != "apple_test" ]]; then
    echo "Invalid target group: '$target_group'. Use 'all', 'apple_test', or 'other_test'."
    exit 1
fi

if [[ "$(uname)" == "Linux" && target_group == "all" ]]; then
    echo "Cannot build mac targets on linux. Use the other_test parameter."
    exit 1
fi

include_apple_test_targets=false
include_others_test_targets=false

[[ "$target_group" == "all" || "$target_group" == "targets_apple_test" ]] && include_apple_test_targets=true
[[ "$target_group" == "all" || "$target_group" == "other_test" ]] && include_others_test_targets=true

# Ensure all selected targets are installed for apple targets
if [[ "$target_group" != "apple_test" ]]; then
  for target in "${targets_apple_test[@]}"; do
      echo "Adding Rust target: $target"
      rustup target add "$target"
  done
fi

# For non apple targets, install cross if on mac
if [[ ("$target_group" == "all" || "$target_group" == "other_test") && "$(uname)" == "Darwin" ]]; then
    cargo install cross --git https://github.com/cross-rs/cross
fi

targets_cargo_build=()
if [[ include_apple_test_targets ]]; then
  for target in "${targets_apple_test[@]}"; do
      targets_cargo_build+=(--target "$target")
  done
fi

if [[ include_others_test_targets && "$(uname)" == "Linux" ]]; then
  for target in "${targets_others_test[@]}"; do
      targets_cargo_build+=(--target "$target")
  done
fi

# Run cargo build with all targets supposed to run with cargo build
cargo build "${targets_cargo_build[@]}" --profile "${profile}"

if [[ "$(uname)" == "Darwin" ]]; then
  # Run cargo build with all non-natively-macOS supported targets
  # This is required because tls-aws-lc needs to be built for each platform
  if [[ "$target_group" == "all" || "$target_group" == "other_test" ]]; then
      for target in "${targets_others_test[@]}"; do
          # running cross build for all targets at once throws an error, so a loop is used instead.
          cross build --target "$target" --profile "${profile}"
      done
  fi

  if [[ "$target_group" == "all" ]]; then
      for target in "${targets_other[@]}"; do
          # running cross build for all targets at once throws an error, so a loop is used instead.
          cross build --target "$target" --profile "${profile}"
      done
  fi
fi
