# Web Module Architecture

The web module provides HTTP/WebSocket server and client infrastructure for the Adama platform, built on Netty NIO for high-performance async I/O.

## Overview

This module handles all external web traffic including:
- HTTP API requests and static asset delivery
- Real-time WebSocket connections for document subscriptions
- File uploads and downloads with caching and transformations
- Outbound HTTP/WebSocket client connections

## Package Structure

```
ape.web
├── assets/           # Asset storage, caching, and transformations
│   ├── cache/        # Two-tier LRU cache (memory + disk)
│   └── transforms/   # Image processing pipeline
├── client/           # HTTP/WebSocket client for outbound requests
│   ├── pool/         # HTTP/1.1 connection pooling
│   └── socket/       # WebSocket client with retry logic
├── contracts/        # Core service interfaces
├── features/         # Optional features (URL preview generation)
├── firewall/         # Request filtering and security
├── io/               # Request/response I/O abstractions
└── service/          # HTTP server implementation
    ├── cache/        # Response caching
    └── routes/       # WebSocket routing (Adama /~s, MCP /~mcp)
```

## Core Abstractions

### ServiceBase (contracts/)
Root factory interface providing four capabilities:
- `establish(context)` - Create per-client WebSocket handler for Adama protocol
- `http()` - Access HTTP request handler
- `assets()` - Access file storage system
- `mcp()` - Access MCP (Model Context Protocol) handler (optional)

### HttpHandler (contracts/)
HTTP request processing interface with domain/URI routing. Returns `HttpResult` which can be:
- Body content (bytes + content type)
- Asset reference (space/key/id for streaming)
- Redirect (301/302 with location)

### ServiceConnection (contracts/)
Per-client WebSocket connection managing JSON-RPC request execution, keepalive health checks, and connection termination.

## Request Flow

### HTTP Request Processing
```
Client Request
     │
     ▼
┌─────────────────────┐
│   WebHandler        │  Netty HTTP handler
│   channelRead0()    │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ WebRequestShield    │  Security filter (block /wp-admin, etc)
│     .block()        │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  handleInternal()   │  Platform routes (/~health, /~upload, etc)
└──────────┬──────────┘
           │ (if not internal)
           ▼
┌─────────────────────┐
│ HttpHandler.handle()│  Domain/space routing
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ handleHttpResult()  │  Send response (body, asset, redirect)
└─────────────────────┘
```

### WebSocket Connection Flow
```
WS Upgrade Request
     │
     ▼
┌─────────────────────┐
│ WebSocketHandler    │  Handshake complete event
│ userEventTriggered()│
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ ServiceBase         │
│   .establish()      │  Create ServiceConnection
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ WebSocketHandler    │  Frame handler loop
│ channelRead0()      │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ ServiceConnection   │
│   .execute()        │  Process JSON-RPC request
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ JsonResponder       │  stream() / finish() / error()
└─────────────────────┘
```

### MCP (Model Context Protocol)
The server supports MCP for AI/LLM integration on the `/~mcp` WebSocket endpoint.

**Protocol**: JSON-RPC 2.0 over WebSocket (protocol version 2025-11-25)

**Key Interfaces**:
- `MCPSession` - Per-connection handler for MCP operations (established via `ServiceBase.establishMCP()`)
- `MCPSession.NOOP` - Static stub implementation returning empty results/errors

**Supported Methods**:
| Method | Description |
|--------|-------------|
| initialize | Capability negotiation |
| tools/list | List available tools |
| tools/call | Execute a tool |
| resources/list | List available resources |
| resources/read | Read resource content |
| prompts/list | List available prompts |
| prompts/get | Get prompt messages |
| ping | Health check |

**Data Types**:
- `ToolDefinition`: name, description, inputSchema (JSON Schema)
- `ToolResult`: content array, isError flag
- `ResourceDefinition`: uri, name, description, mimeType
- `ResourceContent`: uri, mimeType, text
- `PromptDefinition`: name, description, arguments
- `PromptMessages`: description, messages array

## Asset System

### Storage Interface (AssetSystem)
Four operations:
- `request(AssetRequest)` - Stream asset by space/key/id
- `request(Key, NtAsset)` - Stream document-attached asset
- `attach()` - Associate uploaded asset with document
- `upload()` - Store asset bytes

### Caching (WebHandlerAssetCache)
Two-tier LRU cache:
- **Memory tier**: HTML/CSS/JS files up to 196KB, 64MB total
- **Disk tier**: Files up to 16MB, 1GB total

Both tiers support concurrent requestors - first requestor triggers fetch, others attach to in-progress stream.

### Transformations (TransformQueue)
On-the-fly image processing:
- Resize: width/height parameters
- Algorithms: fit-center, squish, crop
- Interpolation: bilinear, bicubic, nearest-neighbor
- Grayscale conversion

Transformed results cached with LRU eviction (30-min TTL, 1GB limit).

## Client Infrastructure

### WebClientBase
Three connection modes:
- `execute()` - One-shot HTTP with dedicated connection
- `executeShared()` - Pooled HTTP/1.1 for high throughput
- `open()` - WebSocket with lifecycle callbacks

### MultiWebClientRetryPool
Resilient WebSocket pool with:
- Multiple connections to single endpoint
- Exponential backoff reconnection
- Request queuing during reconnection
- Request-response and streaming patterns

## Configuration (WebConfig)

Key settings:
| Property | Default | Description |
|----------|---------|-------------|
| http-port | 8080 | Main server port |
| http-redirect-port | 8085 | Redirect server port |
| http-max-content-length-size | 12MB | Max request body |
| websocket-max-frame-size | 4MB | Max WebSocket frame |
| websocket-heart-beat-ms | 1000 | Heartbeat interval |
| http-read-idle-sec | 60 | Read timeout |
| shared-connection-max-lifetime-ms | 10000 | Pool connection TTL |
| shared-connection-max-pool-size | 50 | Max pooled connections |

## Security

### WebRequestShield
First-pass filter blocking common attack vectors:
- Hidden files (except .well-known)
- WordPress admin (/wp-admin/)
- CGI paths (/cgi-bin/)
- Actuator endpoints (/actuator/)
- Scanner probes (/api/, /console/, /portal/)

### TLS/SNI Support
- Optional TLS via cert.pem/key.pem files
- SNI-based certificate selection via CertificateFinder
- Enables multi-tenant HTTPS with per-domain certificates

### Cookie-Based Identity
- Identity cookies use `id_` prefix
- HttpOnly + Secure flags required
- SameSite=Lax for cross-origin requests

## Metrics (WebMetrics)

Prometheus-compatible metrics:
- `websockets_active` - Current WebSocket connections
- `webhandler_get/post/delete` - Request counts by method
- `webhandler_found/notfound` - Response outcomes
- `web_asset_upload` - Upload latency distribution
- Client-reported metrics via /~bm/ endpoint

## Dependencies

- **ape-core**: Core platform functionality
- **Netty**: Async networking (via core)
- **imgscalr-lib 4.2**: Image transformations
- **Jackson**: JSON processing (via core)
