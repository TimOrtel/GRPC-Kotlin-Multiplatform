#!/usr/bin/env bash
set -e  # Exit on error

profile="${1:-dev}"
target_group="${2:-all}"

targets_apple_test=("aarch64-apple-darwin" "aarch64-apple-ios" "aarch64-apple-ios-sim" "x86_64-apple-ios" "x86_64-apple-darwin")
targets_others_test=("x86_64-unknown-linux-gnu" "aarch64-unknown-linux-gnu")
targets_other=("x86_64-pc-windows-gnu")

case "$target_group" in
  apple_test)
    selected_targets=("${targets_apple_test[@]}")
    ;;
  other_test)
    selected_targets=("${targets_others_test[@]}")
    ;;
  all)
    selected_targets=(
      "${targets_apple_test[@]}"
      "${targets_others_test[@]}"
      "${targets_other[@]}"
    )
    ;;
  *)
    echo "Invalid target group: '$target_group'. Use 'all', 'apple_test', or 'other_test'."
    exit 1
    ;;
esac

# Ensure all selected targets are installed
for target in "${selected_targets[@]}"; do
    echo "Adding Rust target: $target"
    rustup target add "$target"
done

# Build an array of --target flags
target_flags_apple=()
for target in "${selected_targets[@]}"; do
    if [[ " ${targets_apple_test[@]} " =~ " ${target} " ]]; then
        target_flags_apple+=(--target "$target")
    fi
done

# Run cargo build with all macOS supported targets
cargo build "${target_flags_apple[@]}" --profile "${profile}"

# Run cargo build with all non-natively-macOS supported targets
# This is required because tls-aws-lc needs to be built for each platform
for target in "${selected_targets[@]}"; do
    if [[ ! " ${targets_apple_test[@]} " =~ " ${target} " ]]; then
        # running cross build for all targets at once throws an error, so a loop is used instead.
        cross build --target "$target" --profile "${profile}"
    fi
done