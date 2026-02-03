# Solo Module Architecture

The solo module provides a standalone single-node Adama server. It bundles the full runtime with in-memory storage and a web server, suitable for development, testing, and simple deployments.

## Key Concepts

- **Single Node**: Complete Adama in one process
- **In-Memory Storage**: Documents stored in memory (no persistence)
- **Bundled Web Server**: HTTP and WebSocket serving
- **Simplified API**: Streamlined routing for solo operation

## Package Structure

### `api/` - Response Types

- **SimpleResponder.java**: Basic success/failure responses
- **SeqResponder.java**: Sequence number responses
- **DataResponder.java**: Document data responses
- **YesResponder.java**: Boolean success responses
- **LoggedProxyResponder.java**: Logging wrapper for responders
- **GeneratedSoloRouterBase.java**: Generated API router base

### Root Package

- **Solo.java**: Main server class, wires everything together
- **SoloBundler.java**: Scan and compile Adama files from directory
- **SoloDataFactory.java**: Create data service for solo mode
- **SoloServiceBase.java**: Service abstraction for solo
- **SoloResponder.java**: Solo-specific response handling

## Architecture

```
Solo Server
    |
    +-- WebServer (HTTP + WebSocket)
    |       |
    |       +-- SoloServiceBase
    |               |
    |               +-- API Router
    |
    +-- CoreService (Adama Runtime)
    |       |
    |       +-- DeploymentFactoryBase
    |       |
    |       +-- InMemoryDataService
    |
    +-- SoloBundler (compile .adama files)
```

## Startup Flow

```java
new Solo(scanDir, webConfigJson)
    |
    v
SoloBundler.scan() - compile all .adama files
    |
    v
InMemoryDataService - create in-memory storage
    |
    v
CoreService - create runtime
    |
    v
SoloServiceBase - create service layer
    |
    v
ServiceRunnable - start web server
```

## Usage

```java
// Programmatic
Solo solo = new Solo("/path/to/adama/files", "{}");
solo.start();

// Or via CLI
java -jar solo.jar --scan /path/to/files
```

## API Routes

The solo server exposes a simplified API:
- `/~upload` - Asset uploads
- `/~adama/once` - One-shot API calls
- WebSocket at `/~adama` - Real-time connections

## Key Files

| Function | File |
|----------|------|
| Main Server | `Solo.java` |
| File Scanner | `SoloBundler.java` |
| Service Base | `SoloServiceBase.java` |
| API Router | `api/GeneratedSoloRouterBase.java` |
| Responders | `api/SimpleResponder.java` |
