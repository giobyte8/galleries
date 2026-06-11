# Codebase Concerns

## Core Sections (Required)

### 1) Top Risks (Prioritized)

| Severity | Concern | Evidence | Impact | Suggested action |
|----------|---------|----------|--------|------------------|
| high | Not-found image/video events are not fanned out from hub | `src/main/java/me/giobyte8/galleries/scanner/scanners/listeners/ScanEventsHub.java` (commented `listeners.forEach` in `onImageNotFound` / `onVideoNotFound`) | stats/telemetry/listeners can miss deletion-style signals | either fan out these events or remove dead callbacks from listener contract |
| high | Scan request processing catches all exceptions and does not rethrow | `src/main/java/me/giobyte8/galleries/scanner/amqp/ScanRequestsListener.java` | queue-level retry/dead-letter behavior may not trigger on failures | narrow exception handling and adopt explicit retry/dead-letter policy |
| medium | Directory path validation TODO in create endpoint | `src/main/java/me/giobyte8/galleries/api/DirectoriesController.java` | malformed/unsafe paths can enter storage and scanning flow | implement path validation in service/controller boundary |
| medium | Reconciliation currently deletes missing media directly; TODOs indicate undecided retention strategy | `src/main/java/me/giobyte8/galleries/scanner/scanners/listeners/GalleriesScanEventsListener.java` | possible irreversible deletes and immediate thumbnail deletion side effects | define soft-delete vs hard-delete policy and implement consistently |
| medium | Dev-default remember-me key exists in config | `src/main/resources/application.yml` (`dev-unsafe-change-me`) | weak session token key if default leaks into non-dev environments | require strong env-provided key in deployment configs |

### 2) Technical Debt

| Debt item | Why it exists | Where | Risk if ignored | Suggested fix |
|-----------|---------------|-------|-----------------|---------------|
| Unimplemented TODOs around validation/deletion | behavior intentionally deferred | `api/DirectoriesController.java`, `services/DirectoryService.java`, `scanner/scanners/listeners/GalleriesScanEventsListener.java` | unclear edge-case behavior and operational surprises | convert TODOs into explicit issues and implement with tests |
| Deprecated hash-path API still present | compatibility/transition from content hash to fingerprint | `src/main/java/me/giobyte8/galleries/scanner/services/HashingService.java` | accidental reuse of deprecated API | remove deprecated methods once all usages are gone |
| Commented-out unit tests | test drift after refactors | `src/test/java/me/giobyte8/galleries/scanner/amqp/ScanRequestsListenerTest.java` | false confidence in scan intake behavior | restore/update or remove obsolete test class |

### 3) Security Concerns

| Risk | OWASP category (if applicable) | Evidence | Current mitigation | Gap |
|------|--------------------------------|----------|--------------------|-----|
| Input validation gap on directory creation | A03: Injection (input validation weakness) | `src/main/java/me/giobyte8/galleries/api/DirectoriesController.java` TODO | path persists through repository APIs | formal validation/sanitization rules not implemented |
| Potential insecure remember-me key default in non-dev usage | A07: Identification and Authentication Failures | `src/main/resources/application.yml` | supports env override `GL_REMEMBER_ME_KEY` | default is weak and human-readable |
| Broad exception logging in scan intake can expose internal error details | N/A | `src/main/java/me/giobyte8/galleries/scanner/amqp/ScanRequestsListener.java` | error is logged with stacktrace | no explicit sanitization policy documented |

### 4) Performance and Scaling Concerns

| Concern | Evidence | Current symptom | Scaling risk | Suggested improvement |
|---------|----------|-----------------|-------------|-----------------------|
| Recursive graph traversals with large depth limits | `MediaRepositoryImpl` uses `[:CONTAINS*..5000]`; `CustomizedDirectoryRepositoryImpl` uses `[:CONTAINS*..10000]` | currently bounded but potentially large traversals | high query cost on deep/wide hierarchies | profile Cypher plans and tighten depth/indexing strategy |
| Per-file metadata extraction invokes external process for MP4 | `Mp4ExifToolMetaReader.forFile` uses `ProcessBuilder("exiftool", ...)` | one subprocess per file | CPU/process overhead with large scans | batch metadata extraction or introduce process pooling strategy |
| Scan traversal currently serial within a single scanner run | `LocalMediaScanner.scanNext` single queue + sequential per-path handling | predictable but single-threaded throughput | slower large-library indexing | [ASK USER] confirm whether parallel scan is desired before changing behavior |

### 5) Fragile/High-Churn Areas

| Area | Why fragile | Churn signal | Safe change strategy |
|------|-------------|-------------|----------------------|
| `src/main/java/me/giobyte8/galleries/scanner/scanners/LocalMediaScanner.java` | central discovery/classification logic touching metadata + events | high-churn section lists 7 changes in last 90 days | add/adjust unit + integration tests before scanner changes |
| `src/main/resources/application.yml` | integration contract keys and runtime wiring | high-churn section lists 7 changes in last 90 days | keep config key compatibility and validate with integration tests |
| `build.gradle` | dependency/test-suite/runtime wiring hub | high-churn section lists 6 changes in last 90 days | make minimal dependency changes and run both test suites |

### 6) `[ASK USER]` Questions

1. [ASK USER] Should missing images/videos during reconciliation be soft-marked (for example `NOT_FOUND`) instead of immediate hard delete?
2. [ASK USER] Should `onImageNotFound` and `onVideoNotFound` be fully fanned out to all scan listeners now, or is the current suppression intentional?
3. [ASK USER] Is parallelized scanning an intended roadmap item, or should scan processing remain single-threaded for determinism/resource control?

### 7) Evidence

- `docs/codebase/.codebase-scan.txt` (TODOs + high-churn sections)
- `src/main/java/me/giobyte8/galleries/scanner/scanners/listeners/ScanEventsHub.java`
- `src/main/java/me/giobyte8/galleries/scanner/amqp/ScanRequestsListener.java`
- `src/main/java/me/giobyte8/galleries/api/DirectoriesController.java`
- `src/main/java/me/giobyte8/galleries/scanner/scanners/listeners/GalleriesScanEventsListener.java`
- `src/main/java/me/giobyte8/galleries/persistence/repositories/MediaRepositoryImpl.java`
- `src/main/java/me/giobyte8/galleries/scanner/metadata/reader/Mp4ExifToolMetaReader.java`
