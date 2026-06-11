# Technology Stack

## Core Sections (Required)

### 1) Runtime Summary

| Area | Value | Evidence |
|------|-------|----------|
| Primary language | Java | `build.gradle`, `src/main/java/me/giobyte8/galleries/ScannerApplication.java` |
| Runtime + version | Java 25 (toolchain), Spring Boot 4.0.5 | `build.gradle`, `mise.toml` |
| Package manager | Gradle Wrapper (`./gradlew`) | `gradlew`, `build.gradle`, `docs/DEVELOPMENT.md` |
| Module/build system | Single-module Gradle project (`rootProject.name = 'galleries'`) | `settings.gradle` |

### 2) Production Frameworks and Dependencies

| Dependency | Version | Role in system | Evidence |
|------------|---------|----------------|----------|
| `org.springframework.boot:spring-boot-starter-web` | managed by Spring Boot 4.0.5 | HTTP API + MVC endpoints | `build.gradle`, `src/main/java/me/giobyte8/galleries/api/MediaController.java` |
| `org.springframework.boot:spring-boot-starter-thymeleaf` | managed by Spring Boot 4.0.5 | Server-rendered admin UI templates | `build.gradle`, `src/main/resources/templates/admin/layout.html` |
| `org.springframework.boot:spring-boot-starter-amqp` | managed by Spring Boot 4.0.5 | RabbitMQ scan/thumbnail messaging | `build.gradle`, `src/main/java/me/giobyte8/galleries/scanner/amqp/ScanRequestsListener.java` |
| `org.springframework.boot:spring-boot-starter-data-neo4j` | managed by Spring Boot 4.0.5 | Neo4j persistence for directory/media graph | `build.gradle`, `src/main/java/me/giobyte8/galleries/persistence/repositories/DirectoryRepository.java` |
| `org.springframework.boot:spring-boot-starter-security` | managed by Spring Boot 4.0.5 | `/admin/**` authn/authz | `build.gradle`, `src/main/java/me/giobyte8/galleries/security/AdminSecurityConfig.java` |
| `org.jobrunr:jobrunr-spring-boot-4-starter` | `8.5.2` | Recurring scan scheduling and retries | `build.gradle`, `src/main/java/me/giobyte8/galleries/schedule/ScanScheduler.java` |
| `org.xerial:sqlite-jdbc` | `3.53.0.0` | JobRunr SQL storage (scheduler datasource) | `build.gradle`, `src/main/java/me/giobyte8/galleries/schedule/SchedulerConfig.java` |
| `com.drewnoakes:metadata-extractor` | `2.19.0` | EXIF/metadata extraction from media files | `build.gradle`, `src/main/java/me/giobyte8/galleries/scanner/metadata/LFSMediaMetaExtractor.java` |
| `org.springframework.boot:spring-boot-starter-opentelemetry` | managed by Spring Boot 4.0.5 | OTLP metrics export | `build.gradle`, `src/main/resources/application.yml` |

### 3) Development Toolchain

| Tool | Purpose | Evidence |
|------|---------|----------|
| Gradle test suites (`test`, `integrationTest`) | test/build | `build.gradle` |
| Spring Boot DevTools | local development hot reload | `build.gradle` |
| Testcontainers (Neo4j, RabbitMQ) | integration test dependencies | `build.gradle`, `src/integrationTest/java/me/giobyte8/galleries/scanner/BaseIntegrationTest.java` |
| Docker (buildx + image scripts) | container build/release | `docs/DEVELOPMENT.md`, `docker/image_build.bash` |
| `exiftool` (runtime package in image) | video metadata extraction dependency | `docker/galleries.dockerfile`, `src/main/java/me/giobyte8/galleries/scanner/metadata/reader/Mp4ExifToolMetaReader.java` |

### 4) Key Commands

```bash
./gradlew bootJar
./gradlew test
./gradlew integrationTest
[TODO] lint command not defined in repo config
```

### 5) Environment and Config

- Config sources: `src/main/resources/application.yml`, `src/main/resources/application-local.yml`, `src/integrationTest/resources/application.yml`, `scripts/template.env`, `estatico/template.env`
- Required env vars: `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASS`, `NEO4J_HOST`, `NEO4J_PORT`, `NEO4J_USER`, `NEO4J_PASS`, `GL_CONTENTS_ROOT_PATH`, `GL_SCHEDULER_DB`, `GL_REMEMBER_ME_KEY`, `OTEL_ENABLED`, `OTEL_COLLECTOR_HTTP_ENDPOINT`
- Deployment/runtime constraints: JDK 25 toolchain; RabbitMQ + Neo4j required for app runtime; Docker image expects `exiftool` installed.

### 6) Evidence

- `build.gradle`
- `settings.gradle`
- `mise.toml`
- `src/main/resources/application.yml`
- `docs/DEVELOPMENT.md`
- `docker/galleries.dockerfile`
