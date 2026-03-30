#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${OLLAMA_BASE_URL:-http://127.0.0.1:11434}"
MODEL="${MODEL_NAME:-gemma3}"

requests=(
  "Привет! Ответь одним предложением."
  "Объясни простыми словами, что такое RAG."
  "Сравни RAG и fine-tuning: когда лучше использовать каждый подход, с плюсами и минусами."
)

for i in "${!requests[@]}"; do
  n=$((i + 1))
  prompt="${requests[$i]}"
  printf "\n[%d/3] %s\n" "$n" "$prompt"

  body=$(cat <<JSON
{"model":"$MODEL","messages":[{"role":"user","content":"$prompt"}],"stream":false}
JSON
)

  response=$(curl -sS -w '\nHTTP_STATUS:%{http_code}\n' "$BASE_URL/api/chat" \
    -H 'Content-Type: application/json' \
    -d "$body")

  status=$(printf '%s' "$response" | awk -F: '/HTTP_STATUS/ {print $2}' | tr -d '\r\n')
  payload=$(printf '%s' "$response" | sed '/HTTP_STATUS:/d')

  printf "HTTP %s\n" "$status"
  printf "%s\n" "$payload" | sed 's/{/\n{/g'

  if [[ "$status" -ge 400 ]]; then
    printf "Request %d failed\n" "$n" >&2
    exit 1
  fi
done

printf "\nAll 3 requests completed successfully.\n"
