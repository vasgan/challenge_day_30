#!/usr/bin/env bash
set -euo pipefail

PROMPT="${1:-Привет!}"
ollama run gemma3 "$PROMPT"
