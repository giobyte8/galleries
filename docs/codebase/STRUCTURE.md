# Codebase Structure

## Core Sections (Required)

### 1) Top-Level Map

| Path | Purpose | Evidence |
|------|---------|----------|
| `src/main/java/` | Application source code | `docs/codebase/.codebase-scan.txt` |
| `src/main/resources/` | Runtime config + Thymeleaf templates | `src/main/resources/application.yml`, `src/main/resources/templates/admin/layout.html` |
| `src/test/java/` | Unit tests | `build.gradle`, `src/test/java/me/giobyte8/galleries/scanner/scanners/LocalMediaScannerTests.java` |
| `src/integrationTest/java/` | Integration tests (separate Gradle suite) | `build.gradle`, `src/integrationTest/java/me/giobyte8/galleries/scanner/BaseIntegrationTest.java` |
| `src/integrationTest/resources/` | Integration-test-only config | `src/integrationTest/resources/application.yml` |
| `docs/` | Architecture/development documentation | `README.md`, `docs/DEVELOPMENT.md`, `docs/architecture.md` |
| `database/` | Cypher schema/queries for graph storage setup | `database/up/1_schema.cypher`, `database/queries.cypher` |
| `docker/` | Dockerfile + image helper scripts | `docker/galleries.dockerfile`, `docker/image_build.bash` |
| `scripts/` | Local utility scripts and env template | `scripts/template.env`, `scripts/dev_data_reset.sh` |
| `.github/specs/` | Requirement/spec markdown files | `.github/specs/1_ADMIN_UI.md` |
| `estatico/` | Static media-serving component configuration | `estatico/README.md`, `estatico/docker-compose.yml` |

### 2) Entry Points

- Main runtime entry: `src/main/java/me/giobyte8/galleries/ScannerApplication.java`
- Secondary entry points (worker/cli/jobs): RabbitMQ listener `src/main/java/me/giobyte8/galleries/scanner/amqp/ScanRequestsListener.java`; recurring scheduler `src/main/java/me/giobyte8/galleries/schedule/ScanScheduler.java`
- How entry is selected (script/config): Spring Boot starts from `ScannerApplication`; AMQP listener bindings and queue names come from `galleries.scanner.amqp.*` in `application.yml`.

### 3) Module Boundaries

| Boundary | What belongs here | What must not be here |
|----------|-------------------|------------------------|
| `api` / `admin.controllers` | HTTP transport and view routing | persistence-query implementation details |
| `services` / `schedule.services` / `scanner.services` | business and orchestration logic | controller request binding / template rendering |
| `scanner.scanners` | filesystem discovery/classification | direct DB mutation side-effects |
| `scanner.scanners.listeners` | scan side-effects fan-out (storage, telemetry, stats, thumbnails) | directory traversal/discovery |
| `persistence.repositories` (+ `Customized*Impl`) | query/mutation access to Neo4j | HTTP concerns |
| `persistence.models` | graph entities/node state | transport DTO concerns |

### 4) Naming and Organization Rules

- File naming pattern: Java types use `PascalCase` filenames (example: `ScanRequestsListener.java`, `DirectoryRepository.java`).
- Directory organization pattern: layer-oriented packages under `me.giobyte8.galleries` (`api`, `services`, `scanner`, `persistence`, `security`, `schedule`, `admin`).
- Import aliasing or path conventions: standard Java package imports (no TypeScript-style aliases).

### 5) Evidence

- `docs/codebase/.codebase-scan.txt`
- `src/main/java/me/giobyte8/galleries/ScannerApplication.java`
- `src/main/java/me/giobyte8/galleries/scanner/amqp/ScanRequestsListener.java`
- `src/main/resources/application.yml`
- `.github/copilot-instructions.md`
