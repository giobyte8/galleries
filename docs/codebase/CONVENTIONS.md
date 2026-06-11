# Coding Conventions

## Core Sections (Required)

### 1) Naming Rules

| Item | Rule | Example | Evidence |
|------|------|---------|----------|
| Files | Java source files use `PascalCase.java` matching top-level type | `ScanService.java`, `AdminSecurityConfig.java` | `src/main/java/me/giobyte8/galleries/scanner/services/ScanService.java`, `src/main/java/me/giobyte8/galleries/security/AdminSecurityConfig.java` |
| Functions/methods | lowerCamelCase methods | `triggerScan`, `onScanRequest`, `findRoots` | `src/main/java/me/giobyte8/galleries/admin/services/ScanRequestsService.java`, `src/main/java/me/giobyte8/galleries/scanner/amqp/ScanRequestsListener.java`, `src/main/java/me/giobyte8/galleries/persistence/repositories/DirectoryRepository.java` |
| Types/interfaces | `PascalCase`, interfaces often suffix with `Service`/`Repository`/`Listener` | `MediaScanner`, `ThumbnailsService`, `ScanEventsListener` | `src/main/java/me/giobyte8/galleries/scanner/scanners/MediaScanner.java`, `src/main/java/me/giobyte8/galleries/scanner/thumbnails/ThumbnailsService.java`, `src/main/java/me/giobyte8/galleries/scanner/scanners/listeners/ScanEventsListener.java` |
| Constants/env vars | env vars are uppercase snake case; enum-like metric names centralized | `RABBITMQ_HOST`, `GL_REMEMBER_ME_KEY` | `src/main/resources/application.yml`, `scripts/template.env` |

### 2) Formatting and Linting

- Formatter: [TODO] no formatter configuration file found in repo root.
- Linter: [TODO] no linter configuration file found in repo root.
- Most relevant enforced rules: [TODO] rule set not explicitly configured in repository.
- Run commands: `./gradlew test`, `./gradlew integrationTest`, `./gradlew bootJar`.

### 3) Import and Module Conventions

- Import grouping/order: standard Java grouped imports with package imports before `java.*` in many files, but no explicit tool-enforced order was found.
- Alias vs relative import policy: Java package imports only (no alias mechanism).
- Public exports/barrel policy: not applicable in Java package layout; types are referenced via package/class imports.

### 4) Error and Logging Conventions

- Error strategy by layer: service/controller layers typically return or throw domain exceptions (`DirectoryNotFoundException`) and map them via `@ExceptionHandler`/`@RestControllerAdvice`; scanner intake logs and swallows exceptions in AMQP listener.
- Logging style and required context fields: uses Lombok `@Slf4j`; log messages include request IDs, directory path, and exception details.
- Sensitive-data redaction rules: [TODO] no explicit redaction policy file found; however config docs indicate not logging sensitive values.

### 5) Testing Conventions

- Test file naming/location rule: `*Test.java` / `*Tests.java` under `src/test/java` and `src/integrationTest/java`.
- Mocking strategy norm: Mockito (`@Mock`, `@InjectMocks`, `MockitoExtension`) for unit tests; Testcontainers + Spring Boot test context for integration tests.
- Coverage expectation: [TODO] no explicit coverage threshold/config discovered.

### 6) Evidence

- `src/main/java/me/giobyte8/galleries/scanner/services/ScanService.java`
- `src/main/java/me/giobyte8/galleries/api/RestExceptionHandler.java`
- `src/main/java/me/giobyte8/galleries/scanner/amqp/ScanRequestsListener.java`
- `src/test/java/me/giobyte8/galleries/scanner/scanners/LocalMediaScannerTests.java`
- `src/integrationTest/java/me/giobyte8/galleries/scanner/BaseIntegrationTest.java`
- `docs/codebase/.codebase-scan.txt`
