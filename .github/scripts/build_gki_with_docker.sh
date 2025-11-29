#!/usr/bin/env bash
set -euo pipefail

: "${GITHUB_WORKSPACE:?GITHUB_WORKSPACE is required}"
: "${GKI_DOCKER_IMAGE:?GKI_DOCKER_IMAGE is required}"

KSU_REPO="$GITHUB_WORKSPACE/KernelSU"
KSU_OUTPUT_ROOT="$GITHUB_WORKSPACE/KernelSUbuild"
KSU_CACHE_ROOT="$GITHUB_WORKSPACE/.cache"

mkdir -p "$KSU_OUTPUT_ROOT" "$KSU_CACHE_ROOT"

docker pull "$GKI_DOCKER_IMAGE"

docker run --rm \
  -e KSU_DEBUG \
  -v "$KSU_REPO":/workspace/KernelSU \
  -v "$KSU_OUTPUT_ROOT":/workspace/KernelSUbuild \
  -v "$KSU_CACHE_ROOT":/workspace/.cache \
  "$GKI_DOCKER_IMAGE" \
  bash -lc '
    gki-builder add-git-safe /workspace -r

    args=(
      python3 /workspace/KernelSU/.github/scripts/kernel_patch.py
      --kernel-source-path "$GKI_SOURCE_ROOT"
      --ksu-source-path /workspace/KernelSU
      --workspace /workspace
    )

    if [[ "${KSU_DEBUG:-false}" == "true" ]]; then
      args+=(--debug)
    fi

    "${args[@]}"

    gki-builder warmup-build --output-root /workspace/KernelSUbuild
  '

shopt -s dotglob nullglob globstar

for kernel_dir in "$KSU_OUTPUT_ROOT"/**/kernel_*; do
  [[ -d "$kernel_dir" ]] || continue

  for kernel_file in "$kernel_dir"/*; do
    sudo mv "$kernel_file" "$KSU_OUTPUT_ROOT"/
  done

  current_dir="$kernel_dir"
  while [[ "$current_dir" != "$KSU_OUTPUT_ROOT" ]]; do
    sudo rmdir "$current_dir" 2>/dev/null || break
    current_dir=$(dirname "$current_dir")
  done
done
