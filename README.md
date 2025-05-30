# Vertx Proxy

## Prerequisites

- Java 17 or later
- Gradle 8.x


## Package

```
gradle clean package
```

## Run

### Set the API Key

create a file named `.env` in the root directory of the project and add the following lines to it:

```text
# use static keys for test only
# SECURE_PROXY_WITH_STATIC=True
# SECURE_PROXY_STATIC_MAP=key1=user1,key2=user2
SECURE_PROXY_WITH_REDIS=True
REDIS_URL=redis://localhost:6379/0
SECURE_PROXY_TARGET=http://localhost
SECURE_PROXY_PORT=8080
```

```bash
gradle clean run
```

## Test for Ollama

### Start Ollama

for linux/macOS:
```bash
export OLLAMA_HOST=0.0.0.0:11435
```
for Windows PowerShell:
```powershell
$env:OLLAMA_HOST="0.0.0.0:11435"
```
Then start Ollama:
```bash
# Make sure Ollama is installed and in your PATH, and qwen2.5:3b model is downloaded
ollama serve

# ollama pull qwen2.5:3b
```

## Test for Vertex Proxy
Open a new terminal:

```bash
curl -H "Authorization: Bearer key1" http://localhost:11434/api/generate -d '{
  "model": "qwen2.5:3b",
  "prompt": "Why is the sky blue?",
  "raw": true,
  "stream": false
}'
```

```bash
# or using httpie
http POST http://localhost:11434/api/generate \
  "Authorization:Bearer key1" \
  model="qwen2.5:3b" \
  prompt="Why is the sky blue?" \
  raw:=true \
  stream:=false

```


## References

- [https://vertx.io/docs/vertx-http-proxy/java/](https://vertx.io/docs/vertx-http-proxy/java/)
- [https://github.com/vert-x3/vertx-examples/tree/5.x/http-proxy-examples](https://github.com/vert-x3/vertx-examples/tree/5.x/http-proxy-examples)
