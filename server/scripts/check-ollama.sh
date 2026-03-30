#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${OLLAMA_BASE_URL:-http://127.0.0.1:11434}"
MODEL="${MODEL_NAME:-gemma3}"

printf "Checking Ollama tags on %s...\n" "$BASE_URL"
curl -sS "$BASE_URL/api/tags" | sed 's/{/\n{/g'

printf "\nChecking chat endpoint with model %s...\n" "$MODEL"
curl -sS "$BASE_URL/api/chat" \
  -H 'Content-Type: application/json' \
  -d "{\"model\":\"$MODEL\",\"messages\":[{\"role\":\"user\",\"content\":\"Привет! Ответь одним предложением.\"}],\"stream\":false}" \
  | sed 's/{/\n{/g'

printf "\nDone.\n"
