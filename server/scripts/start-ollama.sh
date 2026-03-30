#!/usr/bin/env bash
set -euo pipefail

# Starts already installed Ollama runtime on host machine.
OLLAMA_HOST="${OLLAMA_HOST:-0.0.0.0:11434}" ollama serve
