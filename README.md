# The Adama Platform

Adama: Because fuck modern dev tools. Build stateful apps without the spaghetti.

## Win the war against software complexity.

Adama Platform: simplify your tech stack with the ultimate state machine nano-container platform.
Escape the nightmare of tech stack spaghetti and leverage Adama Platform to simplify daily cron jobs, real time messaging, and multiplayer features.
Dive into the power of the platform, and ship features faster today!

This is an ego project like you wouldn't believe.

[https://www.adama-platform.com](https://www.adama-platform.com)

## Architecture (Embed Edition)

This is the open-core embeddable version. One process. No cluster nonsense. Just embed and ship.

```
┌─────────────────────────────────────────────────────────┐
│                      Solo Server                        │
│              Single-node Adama runtime                  │
└────────────────────────┬────────────────────────────────┘
                         │
         ┌───────────────┼───────────────┐
         │               │               │
         ▼               ▼               ▼
┌─────────────┐   ┌─────────────┐   ┌─────────────┐
│     web     │   │    core     │   │   rxhtml    │
│  HTTP/WS    │   │  Runtime    │   │  Templates  │
│  Assets     │   │  Documents  │   │  Reactive   │
└─────────────┘   └──────┬──────┘   └─────────────┘
                         │
              ┌──────────┴──────────┐
              │                     │
              ▼                     ▼
       ┌─────────────┐       ┌─────────────┐
       │data-caravan │       │   common    │
       │  WAL + Disk │       │  Utilities  │
       │Cloud Backup │       │  Async/Crypto│
       └─────────────┘       └─────────────┘
```

### Modules

| Module | Purpose |
|--------|---------|
| **solo** | The main event. Single-node server that bundles everything. Point it at your `.adama` files and go. |
| **core** | Living documents as tiny VMs. Reactive state, delta sync, privacy-aware views. The brain. |
| **web** | Netty HTTP/WebSocket server. Serves your app, handles uploads, terminates SSL. |
| **rxhtml** | Reactive HTML templates compiled to JavaScript. Data bindings that just work. |
| **data-caravan** | Local disk storage with write-ahead logging. Optional cloud backup to S3. |
| **common** | Foundation utilities. Async callbacks, caching, crypto, the boring stuff that makes everything work. |
| **errors** | Error codes. Because integers are universal. |

### Quick Start

```java
// Embed Adama in your app
Solo solo = new Solo("/path/to/adama/files", "{}");
// That's it. You now have a reactive document database which is in-memory only.
```

### What You Get

- **Living Documents** — Stateful VMs that process messages and sync state to clients
- **Delta Sync** — Clients get minimal JSON diffs, not full state dumps
- **Reactive Templates** — RxHTML compiles to JS that auto-updates when data changes
- **Write-Ahead Log** — Crash-safe persistence with optional cloud backup
- **One Process** — No Redis. No Kafka. No Kubernetes. Just run it.