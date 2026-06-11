# Architecture

## Core Sections (Required)

### 1) Architectural Style

- Primary style: layered + event-driven scanning pipeline.
- Why this classification: controllers call services, services use repositories/scanners, and scan side-effects are dispatched through `ScanEventsHub` to listeners.
- Primary constraints:
  - Scan intake is AMQP-based (`@RabbitListener`).
  - Scan discovery and side-effects are separated (`LocalMediaScanner` vs listener classes).
  - Persistence is centered on Neo4j repositories/custom Cypher extensions.

### 2) System Flow

```text
RabbitMQ scan message -> ScanRequestsListener -> ScanService validation ->
LocalMediaScanner filesystem walk -> ScanEventsHub listener fan-out ->
repositories/thumbnails/metrics updates -> API/admin reads from storage
```

Flow steps:
1. `ScanRequestsListener.onScanRequest` receives `ScanRequest` from queue bindings (`galleries.scanner.amqp.*`).
2. `ScanService.scan` loads target directory, rejects missing or `SCAN_IN_PROGRESS`, then opens `ScanStatsContext`.
3. `LocalMediaScanner.scan` traverses local filesystem, classifies files as new/updated/unchanged via `FingerprintService` + existing DB records.
4. `ScanEventsHub` broadcasts events to listeners (`GalleriesScanEventsListener`, `TelemetryScanEventsListener`, `StatsScanEventsListener`).
5. `GalleriesScanEventsListener` reconciles DB state and issues thumbnail generate/refresh/delete requests.
6. Clients read indexed state through REST/admin controllers backed by repositories and services.

### 3) Layer/Module Responsibilities

| Layer or module | Owns | Must not own | Evidence |
|-----------------|------|--------------|----------|
| API/Admin controllers | HTTP routes, request params, model attributes, view names | scan traversal and DB query details | `src/main/java/me/giobyte8/galleries/api/DirectoriesController.java`, `src/main/java/me/giobyte8/galleries/admin/controllers/FragmentsController.java` |
| Service layer | validation/orchestration and coordination | direct request mapping annotations | `src/main/java/me/giobyte8/galleries/scanner/services/ScanService.java`, `src/main/java/me/giobyte8/galleries/schedule/services/ScanSchedulesService.java` |
| Scanner discovery | filesystem walk + classification | persistence side-effects | `src/main/java/me/giobyte8/galleries/scanner/scanners/LocalMediaScanner.java` |
| Scan listeners/hub | side-effects for scan lifecycle events | filesystem traversal | `src/main/java/me/giobyte8/galleries/scanner/scanners/listeners/ScanEventsHub.java`, `src/main/java/me/giobyte8/galleries/scanner/scanners/listeners/GalleriesScanEventsListener.java` |
| Persistence repositories | Neo4j CRUD/custom Cypher and pagination | HTTP concerns | `src/main/java/me/giobyte8/galleries/persistence/repositories/DirectoryRepository.java`, `src/main/java/me/giobyte8/galleries/persistence/repositories/CustomizedDirectoryRepositoryImpl.java` |

### 4) Reused Patterns

| Pattern | Where found | Why it exists |
|---------|-------------|---------------|
| Repository pattern | `persistence/repositories/*` | isolate graph query/mutation details |
| Event/listener hub | `ScanEventsHub` + `ScanEventsListener` implementations | decouple scan discovery from side effects |
| Adapter/service boundary | `ThumbnailsService` + `RemoteThumbnailsService`; `MediaMetaExtractor` + `LFSMediaMetaExtractor` | allow swapping integration implementations |
| Request-scoped context | `ScanStatsContext.runWith(...)` + listeners | aggregate per-scan counters across event callbacks |

### 5) Known Architectural Risks

- `onImageNotFound`/`onVideoNotFound` events are not currently fanned out in `ScanEventsHub`, so listeners relying on those hooks will miss them.
- `ScanRequestsListener` catches `Exception` and logs without rethrow, which may hide message-handling failures from queue-level retry/dead-letter behavior.

### 6) Evidence

- `src/main/java/me/giobyte8/galleries/scanner/amqp/ScanRequestsListener.java`
- `src/main/java/me/giobyte8/galleries/scanner/services/ScanService.java`
- `src/main/java/me/giobyte8/galleries/scanner/scanners/LocalMediaScanner.java`
- `src/main/java/me/giobyte8/galleries/scanner/scanners/listeners/ScanEventsHub.java`
- `src/main/java/me/giobyte8/galleries/scanner/scanners/listeners/GalleriesScanEventsListener.java`
- `src/main/java/me/giobyte8/galleries/persistence/repositories/DirectoryRepository.java`
