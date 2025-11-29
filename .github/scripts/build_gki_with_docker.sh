#!/usr/bin/env bash
set -euo pipefail

: "${GITHUB_WORKSPACE:?GITHUB_WORKSPACE is required}"
: "${GKI_DOCKER_IMAGE:?GKI_DOCKER_IMAGE is required}"

KSU_REPO="$GITHUB_WORKSPACE/KernelSU"
KSU_OUTPUT_ROOT="$GITHUB_WORKSPACE/KernelSUbuild"
KSU_OUTER_CACHE_ROOT="$GITHUB_WORKSPACE/.outer-cache"

mkdir -p "$KSU_OUTPUT_ROOT"
mkdir -p "$KSU_OUTER_CACHE_ROOT"

if [[ -f "$KSU_OUTER_CACHE_ROOT/next-outer-cache.img" ]]; then
  mv "$KSU_OUTER_CACHE_ROOT/next-outer-cache.img" "$KSU_OUTER_CACHE_ROOT/outer-cache.img"
fi

if [[ -f "$KSU_OUTER_CACHE_ROOT/next-outer-cache.json" ]]; then
  mv "$KSU_OUTER_CACHE_ROOT/next-outer-cache.json" "$KSU_OUTER_CACHE_ROOT/outer-cache.json"
fi

docker pull "$GKI_DOCKER_IMAGE"

docker run --rm \
  --privileged \
  -e KSU_DEBUG \
  -e KSU_IMAGE_LABEL \
  -v "$KSU_REPO":/workspace/KernelSU \
  -v "$KSU_OUTER_CACHE_ROOT":/workspace/docker_datas/outerimage \
  -v "$KSU_OUTPUT_ROOT":/workspace/KernelSUbuild \
  "$GKI_DOCKER_IMAGE" \
  bash -lc '
    set -euo pipefail

    trap "gki-builder runtime-cache-export || true" EXIT

    gki-builder runtime-cache-init

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

    if [[ -n "${KSU_IMAGE_LABEL:-}" ]]; then
      args+=(--image-label "$KSU_IMAGE_LABEL")
    fi

    "${args[@]}"

    export CCACHE_LOGFILE=/tmp/ccache.log

    gki-builder warmup-build --output-root /workspace/KernelSUbuild

    cp /tmp/ccache.log /workspace/KernelSUbuild || true
  '

shopt -s dotglob nullglob globstar

for vmlinux_path in "$KSU_OUTPUT_ROOT"/**/vmlinux; do
  [[ -f "$vmlinux_path" ]] || continue

  kernel_dir=$(dirname "$vmlinux_path")

  if [[ "$kernel_dir" != "$KSU_OUTPUT_ROOT" ]]; then
    sudo mv -v "$kernel_dir/"* "$KSU_OUTPUT_ROOT"
  fi
  break
done
