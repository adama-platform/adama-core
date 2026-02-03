# Core Module Architecture

The core module implements the Adama runtime - a reactive document database engine that executes compiled Adama code as "living documents". Each document is a tiny virtual machine with reactive state management, privacy-aware delta synchronization, and async message processing.

## Key Concepts

- **Living Document**: A stateful VM instance that processes messages, tracks reactive state, and synchronizes views to connected clients
- **Reactive Types (Rx*)**: Data types that track changes, propagate dirtiness up the parent chain, and support delta commits/reverts
- **Native Types (Nt*)**: Immutable value types representing Adama language primitives (Principal, Asset, Date, Complex, etc.)
- **Delta Types (D*)**: Privacy-aware view projections that track per-client state and compute minimal diffs

## Package Structure

### `runtime.sys` - System Infrastructure
The heart of the runtime containing `LivingDocument`, the base class for all compiled Adama documents:
- **LivingDocument.java**: Central VM class managing state, message queues, views, caching, cron jobs, web handlers
- **CoreRequestContext.java**: Request metadata (who, origin, IP, key) for policy evaluation
- **DocumentMonitor.java**: Performance tracking and metrics collection
- **capacity/**: Capacity management and load balancing
- **cron/**: Scheduled task execution
- **domains/**: Domain routing and resolution
- **web/**: HTTP request/response handling within documents

### `runtime.reactives` - Reactive Data System
Implements the reactive programming model with change tracking:
- **RxBase.java**: Abstract base for all reactive types - tracks dirty state, manages subscriptions, emits deltas
- **RxTable.java**: Reactive table with indexing, pub/sub, and efficient scanning
- **RxRecordBase.java**: Base class for table rows with lifecycle management
- **RxLazy.java**: Lazy computed values with invalidation
- **RxInt32, RxString, RxBoolean, etc.**: Primitive reactive wrappers

### `runtime.natives` - Native Value Types
Immutable value types used by the Adama language:
- **NtPrincipal.java**: User identity (agent@authority)
- **NtAsset.java**: File asset metadata (id, name, size, hashes)
- **NtDate, NtDateTime, NtTime, NtTimeSpan**: Temporal types
- **NtComplex**: Complex numbers
- **NtMaybe<T>**: Optional values
- **NtList<T>, NtMap<K,V>**: Collections
- **NtVec2/3/4, NtMatrix2/3/4**: Linear algebra types

### `runtime.json` - JSON Streaming
High-performance JSON processing:
- **JsonStreamReader.java**: Tokenizing pull parser with string deduplication
- **JsonStreamWriter.java**: Fast StringBuilder-based JSON construction
- **PrivateView.java**: Per-client view with privacy filtering
- **PrivateLazyDeltaWriter.java**: Lazy delta emission with change detection

### `runtime.delta` - Delta Synchronization
Privacy-aware state projection and diffing:
- **DRecordList.java**: Tracks list ordering changes and emits minimal diffs
- **DInt32, DString, etc.**: Primitive delta trackers
- **DeltaNode.java**: Interface for all delta-capable types

### `runtime.async` - Asynchronous Operations
Message handling and async coordination:
- **AsyncTask.java**: Wraps messages for deferred execution with lifecycle tracking
- **OutstandingFutureTracker.java**: Manages pending async operations
- **TimeoutTracker.java**: Handles operation timeouts

### `runtime.contracts` - Core Interfaces
Fundamental contracts for the reactive system:
- **RxParent.java**: Parent notification interface for dirty propagation
- **RxChild.java**: Child invalidation interface
- **RxKillable.java**: Lifecycle termination
- **WhereClause.java**: Query predicate interface for table scanning
- **IndexQuerySet.java**: Index-based query optimization

### `runtime.remote` - Remote Services
External service integration:
- **ServiceRegistry.java**: Maps service names to implementations
- **Service.java**: Abstract external service interface
- **RxCache.java**: Caches remote call results with invalidation
- **Deliverer.java**: Delivers remote results back to documents

### `runtime.stdlib` - Standard Library
Built-in functions exposed to Adama code:
- **LibMath.java**: Mathematical functions with Maybe support
- **LibString.java**: String manipulation
- **LibDate.java, LibTime.java**: Temporal operations
- **LibStatistics.java**: Statistical aggregations
- **LibLists.java**: List operations
- **LibSearch.java**: Full-text search utilities
- **LibMatrix.java, LibVector.java**: Linear algebra operations

### `runtime.text` - Collaborative Text
Operational transformation for real-time text editing:
- **ot/Operand.java**: OT operand interface
- **ot/Join.java**: Composite operand for efficient merging
- **ot/Range.java**: Character range references
- **search/**: Full-text search indexing

### `runtime.data` - Data Structures
Core data transfer objects:
- **Key.java**: Document identifier (space + key)
- **RemoteDocumentUpdate.java**: Update payload for persistence
- **UpdateType.java**: Categorizes update operations

### `runtime.index` - Indexing
Reactive index maintenance:
- **ReactiveIndex.java**: Maintains sorted sets for indexed columns
- **IndexQuerySet.java**: Builds index-optimized query plans

### `runtime.graph` - Graph Tracking
Differential edge tracking for relationships:
- **DifferentialEdgeTracker.java**: Tracks parent-child relationships efficiently

## Design Patterns

### Reactive Propagation
```
RxBase.__raiseDirty()
  -> parent.__raiseDirty() (up the tree)
  -> __invalidateSubscribers() (notify dependents)
```

### Delta Commit Flow
```
LivingDocument.__commit()
  -> forwardDelta (changes to persist)
  -> reverseDelta (undo log)
  -> __lowerDirtyCommit() (clear dirty flags)
```

### Privacy-Aware Views
```
PrivateView.update()
  -> filter by viewer's privacy scope
  -> DRecordList.Walk (track ordering changes)
  -> emit minimal JSON diff
```

### Message Processing
```
AsyncTask.execute()
  -> action.execute() (generated handler code)
  -> __commit() or __revert()
  -> broadcast delta to connected views
```

## Key Files by Function

| Function | Primary File |
|----------|-------------|
| Document VM | `runtime/sys/LivingDocument.java` |
| Reactive base | `runtime/reactives/RxBase.java` |
| Table storage | `runtime/reactives/RxTable.java` |
| JSON I/O | `runtime/json/JsonStreamReader.java`, `JsonStreamWriter.java` |
| User identity | `runtime/natives/NtPrincipal.java` |
| Delta tracking | `runtime/delta/DRecordList.java` |
| External services | `runtime/remote/ServiceRegistry.java` |
| Standard library | `runtime/stdlib/Lib*.java` |
| Web handling | `runtime/sys/web/WebResponse.java` |
