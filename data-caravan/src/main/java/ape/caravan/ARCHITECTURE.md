# Data-Caravan Module Architecture

The data-caravan module provides local disk-based storage for Adama documents with cloud backup integration. It implements the DataService interface using memory-mapped files and a write-ahead log (WAL) for durability.

## Key Concepts

- **DurableListStore**: Core storage abstraction using memory-mapped files
- **Cloud Backup**: Async replication to cloud storage (S3, etc.)
- **Write-Ahead Log**: Append-only log for crash recovery
- **Local Cache**: In-memory cache of active documents

## Package Structure

### `contracts/` - Storage Interfaces

- **ByteArrayStream.java**: Streaming interface for reading byte arrays
- **Cloud.java**: Cloud storage interface (upload, download, delete)
- **WALEntry.java**: Write-ahead log entry interface

### `data/` - Storage Implementation

- **DurableListStore.java**: Main storage engine using memory-mapped files
- **DurableListStoreSizing.java**: Size calculations and limits
- **MemoryMappedFileStorage.java**: Low-level memory-mapped file operations
- **SequenceStorage.java**: Sequence number management
- **Storage.java**: Abstract storage interface
- **DiskMetrics.java**: Disk usage metrics

### `entries/` - WAL Entry Types

- **Append.java**: Append data to a document
- **Delete.java**: Mark document as deleted
- **Trim.java**: Trim old history from document
- **MapKey.java**: Map logical key to physical storage
- **DelKey.java**: Remove key mapping
- **OrganizationSnapshot.java**: Snapshot of key organization

### `events/` - Event Processing

- **AssetByteAccountant.java**: Track asset byte usage
- **AssetWalker.java**: Walk asset references in documents
- **EventCodec.java**: Encode/decode events
- **RestoreLoader.java**: Load documents from cloud backup

### Root Package

- **CaravanDataService.java**: Main DataService implementation
- **CaravanBoot.java**: Bootstrap and initialization
- **CaravanMetrics.java**: Prometheus metrics

## Data Flow

```
Write Request
    |
    v
CaravanDataService.patch()
    |
    v
LocalCache (in-memory)
    |
    v
DurableListStore.append() (WAL)
    |
    v
Cloud.upload() (async backup)
```

```
Read Request
    |
    v
LocalCache.get()
    |
    [cache miss]
    v
DurableListStore.read()
    |
    [not found locally]
    v
Cloud.download() -> RestoreLoader
```

## Key Files

| Function | File |
|----------|------|
| Data Service | `CaravanDataService.java` |
| Storage Engine | `data/DurableListStore.java` |
| Memory Mapping | `data/MemoryMappedFileStorage.java` |
| Cloud Interface | `contracts/Cloud.java` |
| WAL Append | `entries/Append.java` |
| Metrics | `CaravanMetrics.java` |
