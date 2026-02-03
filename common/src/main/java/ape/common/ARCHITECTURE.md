# Common Module Architecture

The common module provides foundational utilities, abstractions, and shared infrastructure used throughout the Adama platform. It contains no domain-specific logic, instead focusing on reusable components for async operations, caching, serialization, networking, and cryptography.

## Overview

This module serves as the foundation layer that all other modules depend upon. It provides consistent patterns for error handling, asynchronous callbacks, JSON processing, and various utility functions.

## Package Structure

```
ape.common
├── cache/       # LRU caching implementations (sync and async)
├── capacity/    # Load monitoring and capacity management
├── codec/       # Binary serialization code generation
├── csv/         # CSV reading and writing utilities
├── dns/         # DNS resolution utilities
├── gossip/      # Gossip protocol for cluster membership
│   └── codec/   # Gossip protocol message encoding
├── graph/       # Graph utilities (cycle detection)
├── html/        # HTML tokenization and processing
├── jvm/         # JVM monitoring utilities
├── keys/        # Cryptographic key management
├── metrics/     # Metrics and monitoring infrastructure
├── net/         # Network utilities and executors
├── pool/        # Object pooling
├── queue/       # Queue processing utilities
├── rate/        # Rate limiting
├── ref/         # Reference counting
├── template/    # Simple template parsing and rendering
│   ├── fragment/
│   └── tree/
└── web/         # Web/URI utilities
```

## Core Components

### Async Primitives

| Class | Purpose |
|-------|---------|
| `Callback<T>` | Primary async result callback interface with success/failure methods |
| `ErrorCodeException` | Exception with integer error codes for structured error handling |
| `ExceptionLogger` | Logging interface for exceptions with conversion tracking |
| `ExceptionCallback` | Callback wrapper that handles checked exceptions |
| `AwaitHelper` | Utilities for blocking on async operations |

### JSON Utilities

| Class | Purpose |
|-------|---------|
| `Json` | Static helpers for Jackson JSON library (parse, create, read fields) |
| `ConfigObject` | Configuration wrapper around ObjectNode |

### Hashing & Cryptography

| Class | Purpose |
|-------|---------|
| `Hashing` | MessageDigest factory methods (MD5, SHA-256, SHA-384) |
| `HMACSHA256` | HMAC-SHA256 implementation |
| `Hex` | Hexadecimal encoding/decoding |
| `AlphaHex` | Alphanumeric hex encoding |

### Key Management (keys/)

| Class | Purpose |
|-------|---------|
| `MasterKey` | AES encryption/decryption with master key |
| `PrivateKeyBundle` | Bundle of private keys for signing/encryption |
| `PublicPrivateKeyPartnership` | Elliptic curve key exchange (ECDH) |
| `SigningKeyPair` | Ed25519 key pairs for signing |
| `VAPIDFactory` | VAPID key generation for web push |

### Caching (cache/)

| Class | Purpose |
|-------|---------|
| `SyncCacheLRU` | Synchronous LRU cache with size limits |
| `AsyncSharedLRUCache` | Async LRU cache with concurrent access |
| `Measurable` | Interface for objects that report memory usage |

### Gossip Protocol (gossip/)

| Class | Purpose |
|-------|---------|
| `Engine` | Core gossip protocol engine for cluster membership |
| `Instance` | Represents a single cluster member |
| `InstanceSet` | Set of instances in a gossip round |
| `InstanceSetChain` | Historical chain of instance sets |
| `GarbageMap` | Tracks deleted instances for propagation |

### Networking (net/)

| Class | Purpose |
|-------|---------|
| `ChannelClient` | Netty-based channel client |
| `ByteStream` | Stream abstraction for byte data |
| `Lifecycle` | Connection lifecycle callbacks |
| `SchedulerSimpleExecutor` | Scheduler-backed simple executor |

### Metrics (metrics/)

| Class | Purpose |
|-------|---------|
| `MetricsFactory` | Factory for creating metric instances |
| `CallbackMonitor` | Monitor wrapper for callbacks |
| `Inflight` | Tracks in-flight operations |

### Capacity (capacity/)

| Class | Purpose |
|-------|---------|
| `LoadMonitor` | Monitors system load and triggers events |
| `BinaryEventOrGate` | Combines multiple boolean signals |
| `RepeatingSignal` | Periodic signal emitter |

### Codec (codec/)

| Class | Purpose |
|-------|---------|
| `CodecCodeGen` | Generates binary serialization code |
| `TypeId` / `TypeCommon` | Annotations for codec types |
| `Helper` | Runtime codec helpers |

## Key Design Patterns

### Callback Pattern
The `Callback<T>` interface is the primary mechanism for async operations:
```java
public interface Callback<T> {
    void success(T value);
    void failure(ErrorCodeException ex);
}
```

### Error Code Pattern
All errors use `ErrorCodeException` with integer codes for consistent error handling across API boundaries.

### Immutable Configuration
Configuration objects wrap `ObjectNode` and provide type-safe accessors.

### Object Pooling
The pool package provides pooling for expensive-to-create objects like byte buffers.

## Threading Model

- Most utilities are thread-safe
- Async operations use `SimpleExecutor` or Netty event loops
- Gossip engine runs on its own scheduled executor
- Caches support concurrent access with appropriate locking

## Dependencies

- **Jackson**: JSON processing
- **Netty**: Network I/O and DNS resolution
- **SLF4J**: Logging facade
- **Bouncy Castle**: Cryptographic operations (optional for some key types)
