# Local Ollama (`gemma3`) + Android Emulator Chat Client

Монорепо из двух частей:

- `server/` — использование уже установленной Ollama + optional gateway с ограничениями
- `android-app/` — Android chat клиент (Kotlin, Compose, ViewModel, Repository, Retrofit)

## Архитектура

```text
Already installed Ollama on host
 -> model gemma3
 -> HTTP API on localhost:11434 (/api/chat)
 -> Android Emulator accesses host via 10.0.2.2:11434
 -> Android client sends chat request
 -> response rendered in chat UI
```

## 1) Использование уже установленной Ollama

Установка Ollama не переописывается. Используем существующую инсталляцию.

Запуск runtime (на хост-машине):

```bash
OLLAMA_HOST=0.0.0.0:11434 ollama serve
```

Загрузка/подготовка модели:

```bash
ollama pull gemma3
```

Явный запуск/прогрев модели:

```bash
ollama run gemma3 "Привет!"
```

Проверка HTTP API:

```bash
curl -s http://127.0.0.1:11434/api/tags
```

Проверка chat endpoint:

```bash
curl -s http://127.0.0.1:11434/api/chat \
  -H 'Content-Type: application/json' \
  -d '{
    "model": "gemma3",
    "messages": [{ "role": "user", "content": "Привет!" }],
    "stream": false
  }'
```

## 2) Как Android Emulator подключается

В эмуляторе `localhost` указывает на сам эмулятор, а не на хост.

Поэтому клиент использует:

- `http://10.0.2.2:11434/` (default)

Это настраивается в:

- `android-app/app/build.gradle.kts` (`DEFAULT_SERVER_URL`)
- поле `Server URL` в UI

## 3) Серверная часть

### Вариант A (основной): прямой доступ Android -> Ollama

- endpoint: `POST /api/chat`
- host: `127.0.0.1`
- port: `11434`

### Вариант B (optional): тонкий gateway

Файл: `server/optional-guard-layer/ollama-gateway.js`

- gateway host/port (default): `127.0.0.1:11435`
- проксирует в `http://127.0.0.1:11434/api/chat`
- добавляет:
  - rate limit
  - max input/context guards
  - понятные коды ошибок (`429`, `413`, `503`, `504`)

Запуск gateway:

```bash
cd server
npm start
```

Если хотите пустить Android через gateway, поменяйте URL в UI на:

- `http://10.0.2.2:11435/`

## 4) Rate limit и max context

Реализованы в двух местах:

1. Клиент (`android-app`) — `ChatRequestGuard`
- rate limit: `MAX_REQUESTS_PER_MINUTE`, `RATE_LIMIT_WINDOW_MS`
- max input: `MAX_INPUT_CHARS`
- max context: `MAX_CONTEXT_MESSAGES`, `MAX_CONTEXT_CHARS`

2. Optional server gateway (`server/optional-guard-layer/ollama-gateway.js`)
- `RATE_LIMIT_MAX_REQUESTS`, `RATE_LIMIT_WINDOW_MS`
- `MAX_INPUT_CHARS`, `MAX_CONTEXT_MESSAGES`, `MAX_CONTEXT_CHARS`

## 5) Проверка 3 последовательных запросов

Через CLI:

```bash
cd server
bash scripts/smoke-test.sh
```

Сценарий:
1. "Привет! Ответь одним предложением."
2. "Объясни простыми словами, что такое RAG."
3. "Сравни RAG и fine-tuning: когда лучше использовать каждый подход, с плюсами и минусами."

Через Android:
1. Запустить `android-app` в эмуляторе.
2. Убедиться, что `Server URL = http://10.0.2.2:11434/`.
3. Отправить те же 3 запроса подряд.
4. Проверить, что ответы отображаются и приложение не падает.

## 6) Обработка ошибок в Android UI

Реализованы понятные сообщения:

- "Локальная модель недоступна"
- "Модель gemma3 не запущена"
- "Слишком длинный запрос"
- "Слишком много запросов, попробуйте позже"
- timeout / malformed response / invalid URL

## 7) Структура проекта

```text
server/
  README.md
  package.json
  config/gateway.env.example
  scripts/check-ollama.sh
  scripts/start-gateway.sh
  scripts/smoke-test.sh
  optional-guard-layer/ollama-gateway.js

android-app/
  README.md
  settings.gradle.kts
  build.gradle.kts
  app/
    build.gradle.kts
    src/main/java/com/example/localollamachat/
      config/
      data/
      domain/
      ui/
```
