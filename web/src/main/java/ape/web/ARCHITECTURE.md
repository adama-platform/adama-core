# Web Module Architecture

The web module provides HTTP/WebSocket server and client infrastructure built on Netty. It handles all external web traffic for the Adama platform including API requests, asset delivery, file uploads, and real-time WebSocket connections.

## Key Concepts

- **ServiceBase**: Factory interface for establishing connections and handling HTTP/WebSocket requests
- **WebHandler**: Netty channel handler for HTTP request processing with routing, CORS, cookies
- **WebClientBase**: HTTP/WebSocket client with connection pooling for outbound requests
- **AssetSystem**: Storage abstraction for file uploads, downloads, and transformations

## Package Structure

### `service/` - HTTP Server Infrastructure
The main web server implementation using Netty:

- **ServiceRunnable.java**: Main server bootstrap - binds port, configures SSL, manages lifecycle
- **WebHandler.java**: HTTP request handler with routing for:
  - Health checks (`/~health`, `/~deep-health`)
  - Asset uploads (`/~upload`)
  - Cookie management (`/~set/`, `/~stash/`)
  - JavaScript client delivery (`/libadama.js`)
  - One-shot API calls (`/~adama/once`)
  - Domain-based routing to documents
- **WebSocketHandler.java**: WebSocket upgrade and message handling for persistent connections
- **Initializer.java**: Netty channel pipeline configuration
- **WebConfig.java**: Server configuration (ports, timeouts, paths, global domains)
- **WebMetrics.java**: Prometheus metrics for request tracking
- **cache/HttpResultCache.java**: Response caching with TTL

### `client/` - HTTP Client
Outbound HTTP/WebSocket client for external service calls:

- **WebClientBase.java**: Core client with two modes:
  - `execute()` - Single-use connections for one-off requests
  - `executeShared()` - Pooled connections for high-throughput
  - `open()` - WebSocket connections with lifecycle callbacks
- **SimpleHttpRequest/Response**: Request/response DTOs
- **pool/**: Connection pooling via `AsyncPool`
  - **WebClientSharedConnection.java**: Reusable HTTP/1.1 connection
  - **WebEndpoint.java**: Pool key (host + port + secure)
- **socket/**: WebSocket client support
  - **MultiWebClientRetryPool.java**: Reconnecting WebSocket pool
  - **WebClientConnection.java**: Single WebSocket connection

### `contracts/` - Service Interfaces
Core abstractions for pluggable behavior:

- **ServiceBase.java**: Root interface providing:
  - `establish()` - Create ServiceConnection for new clients
  - `http()` - Get HttpHandler for web requests
  - `assets()` - Get AssetSystem for file operations
- **ServiceConnection.java**: Per-client connection for WebSocket message handling
- **HttpHandler.java**: HTTP request processing with `HttpResult` response type
- **WebLifecycle.java**: WebSocket connection lifecycle callbacks
- **CertificateFinder.java**: Dynamic SSL certificate resolution (SNI)
- **WellKnownHandler.java**: ACME/.well-known endpoint handling

### `assets/` - Asset Management
File storage, caching, and transformation:

- **AssetSystem.java**: Storage interface (upload, download, attach to documents)
- **AssetStream.java**: Chunked asset streaming for large files
- **AssetRequest.java**: Asset download request parameters
- **AssetFact.java**: Computes file metadata (size, MD5, SHA-384)
- **cache/**:
  - **WebHandlerAssetCache.java**: Two-tier caching (memory + disk)
  - **CachedAsset.java**: Cached asset interface
  - **MemoryCacheAsset.java**: In-memory cached asset
  - **FileCacheAsset.java**: Disk-backed cached asset
- **transforms/**: Image processing pipeline
  - **Transform.java**: Transformation interface
  - **ImageTransform.java**: Image resize/crop operations
  - **TransformQueue.java**: Async transformation execution
  - **TransformFactory.java**: Creates transforms from parameters

### `io/` - Request/Response I/O
JSON-RPC style request handling:

- **ConnectionContext.java**: HTTP context (origin, IP, user-agent, cookies)
- **JsonRequest.java**: Typed accessor for JSON-RPC request fields
- **JsonResponder.java**: Response interface (stream, finish, error)
- **JsonLogger.java**: Request/response logging
- **BulkLatch.java**: Coordinate multiple async operations

### `firewall/` - Security
Request filtering and protection:

- **WebRequestShield.java**: Blocks malicious URI patterns (path traversal, etc.)

### `features/` - Optional Features
Additional web capabilities:

- **UrlSummaryGenerator.java**: OpenGraph/meta tag extraction for URL previews

## Request Flow

### HTTP Request
```
Netty -> WebHandler.channelRead0()
  -> WebRequestShield.block() (security filter)
  -> handleInternal() (Adama routes like /~upload, /libadama.js)
  -> httpHandler.handle() (domain/space routing)
  -> handleHttpResult() (send response with CORS, cookies)
```

### WebSocket Connection
```
Netty -> WebSocketHandler
  -> ServiceBase.establish() (create ServiceConnection)
  -> ServiceConnection.execute() (process JSON-RPC messages)
  -> JsonResponder.stream()/finish()/error()
```

### Asset Download
```
WebHandler.handleNtAsset()
  -> TransformQueue.process() (if transform requested)
  -> WebHandlerAssetCache.get() (check cache)
  -> AssetSystem.request() (fetch from storage)
  -> AssetStream.body() (stream to client)
```

## Key Files by Function

| Function | Primary File |
|----------|-------------|
| HTTP Server | `service/ServiceRunnable.java` |
| Request Routing | `service/WebHandler.java` |
| WebSocket Server | `service/WebSocketHandler.java` |
| HTTP Client | `client/WebClientBase.java` |
| Connection Pool | `client/pool/WebClientSharedConnection.java` |
| Service Interface | `contracts/ServiceBase.java` |
| HTTP Handler | `contracts/HttpHandler.java` |
| Asset Cache | `assets/cache/WebHandlerAssetCache.java` |
| Image Transform | `assets/transforms/ImageTransform.java` |
| Request Context | `io/ConnectionContext.java` |
| JSON-RPC Request | `io/JsonRequest.java` |

## Design Patterns

### Connection Pooling
```java
pool.get(endpoint, callback -> {
  connection.writeRequest(request, responder);
  // on success: connection.returnToPool()
  // on failure: connection.signalFailure()
});
```

### Chunked Streaming
```java
AssetStream response = new AssetStream() {
  void headers(length, contentType, md5) { ... }
  void body(chunk, offset, length, last) { ... }
  void failure(code) { ... }
};
```

### Service Abstraction
```java
ServiceBase provides:
  - establish(context) -> ServiceConnection (WebSocket)
  - http() -> HttpHandler (HTTP requests)
  - assets() -> AssetSystem (file operations)
```
