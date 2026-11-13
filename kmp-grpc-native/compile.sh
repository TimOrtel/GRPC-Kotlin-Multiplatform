#!/usr/bin/env bash
set -e  # Exit on error

export PATH="$HOME/.cargo/bin:$PATH"

profile="${1:-dev}"
target_group="${2:-all}"

targets_apple_test=("aarch64-apple-darwin" "aarch64-apple-ios" "aarch64-apple-ios-sim" "x86_64-apple-ios" "x86_64-apple-darwin")
targets_others_test=("x86_64-unknown-linux-gnu" "aarch64-unknown-linux-gnu")
targets_other=("x86_64-pc-windows-gnu")

if [[ "$target_group" != "all" && "$target_group" != "other_test" && "$target_group" != "apple_test" && "$target_group" != "non_apple" ]]; then
    echo "Invalid target group: '$target_group'. Use 'all', 'apple_test', 'other_test', or 'non_apple'."
    exit 1
fi

if [[ "$(uname)" == "Linux" && ( "$target_group" == "all" || "$target_group" == "apple_test" ) ]]; then
    echo "Cannot build Apple targets on Linux. Use 'other_test' or 'non_apple'."
    exit 1
fi

include_apple_test_targets=false
include_others_test_targets=false
include_other_targets=false

[[ "$target_group" == "all" || "$target_group" == "apple_test" ]] && include_apple_test_targets=true
[[ "$target_group" == "all" || "$target_group" == "other_test" || "$target_group" == "non_apple" ]] && include_others_test_targets=true
[[ "$target_group" == "all" || "$target_group" == "non_apple" ]] && include_other_targets=true

# For non-apple targets, install cross if not already available
if [[ "$include_others_test_targets" == true || "$include_other_targets" == true ]]; then
    if ! command -v cross >/dev/null 2>&1; then
        cargo install --locked cross --git https://github.com/cross-rs/cross
    fi
fi

if [[ "$(uname)" == "Darwin" && "$include_apple_test_targets" == true ]]; then
    targets_cargo_build=()
    for target in "${targets_apple_test[@]}"; do
        echo "Adding Rust target: $target"
        rustup target add "$target"
        targets_cargo_build+=(--target "$target")
    done

    # Run cargo build with all Apple targets
    cargo build "${targets_cargo_build[@]}" --profile "${profile}"
fi

# Run cross build with all other targets
# This is required because ring needs a C toolchain for each platform
if [[ "$include_others_test_targets" == true ]]; then
    for target in "${targets_others_test[@]}"; do
        # running cross build for all targets at once throws an error, so a loop is used instead.
        cross build --target "$target" --profile "${profile}"
    done
fi

if [[ "$include_other_targets" == true ]]; then
    for target in "${targets_other[@]}"; do
        # running cross build for all targets at once throws an error, so a loop is used instead.
        cross build --target "$target" --profile "${profile}"
    done
fi
