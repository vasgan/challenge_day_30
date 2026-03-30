# Local LLM Service (Ollama + gemma3)

Этот модуль использует **уже установленную** Ollama как локальный LLM runtime.

## 1) Запуск Ollama runtime

```bash
# Рекомендуется, чтобы API был доступен хост-машине и Android Emulator
OLLAMA_HOST=0.0.0.0:11434 ollama serve
# или
bash scripts/start-ollama.sh
```

Если Ollama уже запущена как system service, этот шаг можно пропустить.

## 2) Подготовка модели `gemma3`

```bash
ollama pull gemma3
```

Опционально прогреть модель (явный запуск):

```bash
ollama run gemma3 "Привет!"
# или
bash scripts/run-gemma3.sh "Привет!"
```

Проверка, что модель доступна:

```bash
curl -s http://127.0.0.1:11434/api/tags
```

## 3) Проверка Chat HTTP API напрямую

```bash
curl -s http://127.0.0.1:11434/api/chat \
  -H 'Content-Type: application/json' \
  -d '{
    "model": "gemma3",
    "messages": [
      { "role": "user", "content": "Привет!" }
    ],
    "stream": false
  }'
```

## 4) Smoke test на 3 последовательных запроса

```bash
bash scripts/smoke-test.sh
```

Скрипт отправляет:
1. `Привет! Ответь одним предложением.`
2. `Объясни простыми словами, что такое RAG.`
3. `Сравни RAG и fine-tuning...`

## 5) Optional thin gateway (rate limit + max context)

Ollama API не даёт из коробки прикладные ограничения на уровне вашего приложения,
поэтому добавлен опциональный gateway.

Запуск:

```bash
# при необходимости поправьте переменные в config/gateway.env.example
npm start
```

По умолчанию gateway слушает:
- `http://127.0.0.1:11435`
- endpoint: `POST /api/chat`
- health: `GET /health`

Ограничения (конфигурируемые):
- `RATE_LIMIT_MAX_REQUESTS` за `RATE_LIMIT_WINDOW_MS`
- `MAX_INPUT_CHARS`
- `MAX_CONTEXT_MESSAGES`
- `MAX_CONTEXT_CHARS`

## 6) Как Android Emulator подключается

- Ollama на хосте: `http://127.0.0.1:11434`
- Из Android Emulator этот же хост доступен как `http://10.0.2.2:11434`

Поэтому в Android-клиенте базовый URL должен быть:

- `http://10.0.2.2:11434/` (по умолчанию)

Если хотите использовать gateway вместо прямого Ollama:

- `http://10.0.2.2:11435/`
