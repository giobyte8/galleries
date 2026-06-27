# Instructions for AI coding agents — 'Galleries'

## Project purpose

This service indexes gallery directories and images by scanning local
filesystem paths, persists indexed state in storage, and exposes a small
HTTP API for browsing.

Main runtime flows:

- HTTP read/manage endpoints for directories and images.
- RabbitMQ-driven scan requests.
- Event-based scan side effects (storage updates, metrics, thumbnail requests).

## Architecture guardrails

- Preserve the separation between:
  - `api` controllers (transport)
  - `services` / `scanner.services` (application logic)
  - `scanner.scanners` (filesystem discovery)
  - `scanner.scanners.listeners` (scan side effects)
  - `persistence.repositories` (query/mutation access)
- Keep scanning and side effects decoupled:
  - Discovery belongs in `LocalMediaScanner`.
  - Side effects belong in scan event listeners behind `ScanEventsHub`.
- Prefer extending via interfaces already present (`MediaScanner`,
  `ThumbnailsService`, `ImgMetaExtractor`, `ScanEventsListener`) instead of
  hard-coding new dependencies.

## Coding conventions in this repo

- Java + Spring Boot conventions:
  - Use constructor injection (prefer `@RequiredArgsConstructor` where
    appropriate).
  - Keep controllers thin; delegate to service classes.
  - Keep business logic out of controller methods.
- DTO style:
  - Use Java `record` for simple request/response DTOs.
  - For API pagination responses, use `me.giobyte8.galleries.dto.Page` wrapper
    (`Page.from(...)`).
- Domain model style:
  - Use Lombok for model boilerplate where already established.
  - Keep `Directory` and `Image` status transitions explicit (`DirStatus`,
    `ImageStatus`).
- Logging:
  - Use `@Slf4j` and concise, contextual messages.
  - Do not log sensitive configuration values.
- Formatting:
  - Follow existing code style (e.g., spacing, line breaks) for consistency.
  - It's important that lines do not exceed 80 characters for readability
    break long lines appropriately.

## Scanner behavior requirements

When changing scan logic, preserve these invariants:

- A scan request is accepted from RabbitMQ and validated in `ScanService`.
- Scan should not proceed for missing directories or directories already
  in `SCAN_IN_PROGRESS`.
- `LocalMediaScanner` classifies discovered files as new/updated/unchanged.
- Reconciliation uses `VERIFYING` statuses and completion cleanup in listeners.
- Thumbnail work is requested asynchronously via `ThumbnailsService`.

If adding new scan outcomes or events:

- Add methods/events in `ScanEventsListener` and propagate via `ScanEventsHub`.
- Update relevant listener implementations (`GalleriesScanEventsListener`,
  `TelemetryScanEventsListener`, etc...).
- Add tests for both behavior and side effects.

## Persistence and storage guidance

- Prefer repository interfaces and existing custom repository extensions over
  ad-hoc DB calls in services.
- Keep storage-specific details inside repository/config layers.
- Maintain backend-agnostic wording in docs and API-level comments (say `storage`
  unless storage engine specifics are required).
- If query behavior changes, update integration tests under `src/integrationTest`.

## Configuration and messaging

- Keep configuration in `application.yml` + `@ConfigurationProperties` classes
  (`ScannerProps`, `Neo4jProps`).
- For scanner settings, use `galleries.scanner.*` properties.
- Be careful when renaming AMQP property keys or queue names; these are
  integration contracts.
- Keep message payloads aligned with current DTOs (`ScanRequest`,
  `ThumbnailsRequest`).

## Testing expectations

When generating or changing code, include or update tests:

- Unit tests in `src/test/java` for isolated logic.
- Integration tests in `src/integrationTest/java` for repository/scan workflows.
- Prefer AssertJ or JUnit assertions already used in nearby tests.
- Use existing Testcontainers-based base classes for integration coverage.

Minimum verification before proposing major scanner/persistence changes:

```bash
./gradlew test
./gradlew integrationTest
```

## Known rough edges (do not accidentally regress)

- `ScanEventsHub.onImageNotFound(...)` currently does not fan out to listeners.
- `StatsScanEventsListener` is currently a stub.
- `HashingService.hashPath(...)` methods are deprecated; prefer `hashContent(...)`
  for change detection.
- Keep behavior consistent with current scan reconciliation unless a change is
  explicitly requested.

## Change quality bar

- Keep changes small and local to the requested feature.
- Favor backwards-compatible refactors.
- If a behavior change is intentional, mention it clearly in code comments/tests.
- Do not introduce broad framework migrations or package-level reorganizations
  unless explicitly requested.
