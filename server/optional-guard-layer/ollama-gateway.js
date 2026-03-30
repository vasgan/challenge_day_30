import http from 'node:http';
import { URL } from 'node:url';

const config = {
  port: Number(process.env.GATEWAY_PORT || 11435),
  ollamaBaseUrl: process.env.OLLAMA_BASE_URL || 'http://127.0.0.1:11434',
  modelName: process.env.MODEL_NAME || 'gemma3',
  requestTimeoutMs: Number(process.env.REQUEST_TIMEOUT_MS || 60000),
  rateLimitWindowMs: Number(process.env.RATE_LIMIT_WINDOW_MS || 60000),
  rateLimitMaxRequests: Number(process.env.RATE_LIMIT_MAX_REQUESTS || 5),
  maxInputChars: Number(process.env.MAX_INPUT_CHARS || 2000),
  maxContextMessages: Number(process.env.MAX_CONTEXT_MESSAGES || 20),
  maxContextChars: Number(process.env.MAX_CONTEXT_CHARS || 8000)
};

const rateLimitState = new Map();

function json(res, statusCode, payload) {
  const body = JSON.stringify(payload);
  res.writeHead(statusCode, {
    'Content-Type': 'application/json; charset=utf-8',
    'Content-Length': Buffer.byteLength(body)
  });
  res.end(body);
}

function collectBody(req) {
  return new Promise((resolve, reject) => {
    const chunks = [];
    req.on('data', (chunk) => chunks.push(chunk));
    req.on('end', () => resolve(Buffer.concat(chunks).toString('utf-8')));
    req.on('error', reject);
  });
}

function extractClientKey(req) {
  const forwarded = req.headers['x-forwarded-for'];
  if (typeof forwarded === 'string' && forwarded.length > 0) {
    return forwarded.split(',')[0].trim();
  }
  return req.socket.remoteAddress || 'unknown';
}

function checkRateLimit(clientKey, nowMs) {
  const current = rateLimitState.get(clientKey);
  if (!current || nowMs - current.windowStartMs >= config.rateLimitWindowMs) {
    rateLimitState.set(clientKey, { windowStartMs: nowMs, count: 1 });
    return null;
  }

  if (current.count >= config.rateLimitMaxRequests) {
    const retryAfterMs = config.rateLimitWindowMs - (nowMs - current.windowStartMs);
    return {
      retryAfterSec: Math.max(1, Math.ceil(retryAfterMs / 1000))
    };
  }

  current.count += 1;
  return null;
}

function validateChatPayload(payload) {
  if (!payload || typeof payload !== 'object') {
    return 'Body must be a JSON object';
  }

  if (!Array.isArray(payload.messages) || payload.messages.length === 0) {
    return 'messages must be a non-empty array';
  }

  if (payload.messages.length > config.maxContextMessages) {
    return `Too many context messages. Max: ${config.maxContextMessages}`;
  }

  let totalChars = 0;
  for (const msg of payload.messages) {
    if (!msg || typeof msg !== 'object' || typeof msg.content !== 'string') {
      return 'Each message must contain a string content field';
    }
    totalChars += msg.content.length;
  }

  const lastMessage = payload.messages[payload.messages.length - 1];
  if (lastMessage.content.length > config.maxInputChars) {
    return `Input is too long. Max input chars: ${config.maxInputChars}`;
  }

  if (totalChars > config.maxContextChars) {
    return `Context is too long. Max context chars: ${config.maxContextChars}`;
  }

  if (payload.model && payload.model !== config.modelName) {
    return `Only model '${config.modelName}' is allowed by gateway`;
  }

  return null;
}

async function callOllama(payload) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), config.requestTimeoutMs);

  try {
    const target = new URL('/api/chat', config.ollamaBaseUrl).toString();
    return await fetch(target, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        model: config.modelName,
        stream: false,
        ...payload
      }),
      signal: controller.signal
    });
  } finally {
    clearTimeout(timeout);
  }
}

function mapOllamaErrorMessage(statusCode, rawError) {
  if (statusCode === 404 && rawError.includes('not found')) {
    return 'Model gemma3 is not available. Run `ollama pull gemma3`.';
  }
  if (statusCode >= 500) {
    return 'Local model runtime error';
  }
  return rawError || 'Unexpected Ollama error';
}

async function handleChat(req, res) {
  const clientKey = extractClientKey(req);
  const rate = checkRateLimit(clientKey, Date.now());
  if (rate) {
    res.setHeader('Retry-After', String(rate.retryAfterSec));
    return json(res, 429, {
      error: 'Too many requests, try again later',
      retryAfterSec: rate.retryAfterSec
    });
  }

  let body;
  try {
    body = await collectBody(req);
  } catch {
    return json(res, 400, { error: 'Cannot read request body' });
  }

  let payload;
  try {
    payload = JSON.parse(body || '{}');
  } catch {
    return json(res, 400, { error: 'Malformed JSON' });
  }

  const validationError = validateChatPayload(payload);
  if (validationError) {
    return json(res, 413, { error: validationError });
  }

  let ollamaResp;
  try {
    ollamaResp = await callOllama(payload);
  } catch (error) {
    if (error?.name === 'AbortError') {
      return json(res, 504, { error: 'Timeout waiting for local model response' });
    }
    return json(res, 503, { error: 'Local model is unavailable. Is Ollama running?' });
  }

  const text = await ollamaResp.text();
  let data = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = null;
  }

  if (!ollamaResp.ok) {
    const rawError = (data && data.error) || text || 'Ollama request failed';
    return json(res, ollamaResp.status, {
      error: mapOllamaErrorMessage(ollamaResp.status, String(rawError))
    });
  }

  if (!data || typeof data !== 'object') {
    return json(res, 502, { error: 'Malformed response from local model' });
  }

  return json(res, 200, data);
}

const server = http.createServer(async (req, res) => {
  if (req.method === 'GET' && req.url === '/health') {
    return json(res, 200, {
      status: 'ok',
      gateway: true,
      model: config.modelName,
      ollamaBaseUrl: config.ollamaBaseUrl
    });
  }

  if (req.method === 'POST' && req.url === '/api/chat') {
    return handleChat(req, res);
  }

  return json(res, 404, {
    error: 'Not found',
    endpoints: ['GET /health', 'POST /api/chat']
  });
});

server.listen(config.port, () => {
  console.log(`Ollama gateway listening on http://127.0.0.1:${config.port}`);
  console.log(`Forwarding to ${config.ollamaBaseUrl}/api/chat with model ${config.modelName}`);
});
