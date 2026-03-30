#!/usr/bin/env bash
set -euo pipefail

if [[ -f "config/gateway.env.example" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "config/gateway.env.example"
  set +a
fi

node optional-guard-layer/ollama-gateway.js
