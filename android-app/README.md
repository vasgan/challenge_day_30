# Android client (Kotlin + Compose)

Приложение отправляет сообщения в локальную модель Ollama через HTTP API `/api/chat`.

## Ключевые параметры

- Default base URL: `http://10.0.2.2:11434/`
- Default model: `gemma3`
- Endpoint: `POST /api/chat`

`10.0.2.2` используется потому, что это специальный alias в Android Emulator на localhost хост-машины.

## Где менять URL

1. Runtime: поле `Server URL` на основном экране.
2. Build-time default: `app/build.gradle.kts` -> `DEFAULT_SERVER_URL`.

## Запуск

1. Убедиться, что на хосте работает Ollama и доступна модель `gemma3`.
2. Запустить Android Emulator.
3. Открыть проект `android-app` в Android Studio.
4. Run `app`.

## Ограничения в клиенте

Реализованы в `ChatRequestGuard`:

- rate limit: `MAX_REQUESTS_PER_MINUTE` в окне `RATE_LIMIT_WINDOW_MS`
- max input size: `MAX_INPUT_CHARS`
- max context messages: `MAX_CONTEXT_MESSAGES`
- max context chars: `MAX_CONTEXT_CHARS`

Все значения конфигурируются в `app/build.gradle.kts`.

## Ошибки, которые обрабатываются в UI

- "Локальная модель недоступна"
- "Модель gemma3 не запущена"
- "Слишком длинный запрос"
- "Слишком много запросов, попробуйте позже"
- timeout / malformed response / invalid URL

## 3 запроса для проверки стабильности

Последовательно отправьте:

1. `Привет! Ответь одним предложением.`
2. `Объясни простыми словами, что такое RAG.`
3. `Сравни RAG и fine-tuning: когда лучше использовать каждый подход, с плюсами и минусами.`

Ожидаемо:

- все ответы появляются в списке сообщений
- приложение не падает
- при проблемах показывается понятная ошибка
