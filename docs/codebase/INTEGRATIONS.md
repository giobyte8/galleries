# External Integrations

## Core Sections (Required)

### 1) Integration Inventory

| System | Type (API/DB/Queue/etc) | Purpose | Auth model | Criticality | Evidence |
|--------|---------------------------|---------|------------|-------------|----------|
| RabbitMQ (`GL_*` queues/exchange) | Queue/broker | scan request intake and thumbnail request publishing | username/password from Spring config env vars | high | `src/main/resources/application.yml`, `src/main/java/me/giobyte8/galleries/scanner/amqp/ScanRequestsListener.java`, `src/main/java/me/giobyte8/galleries/scanner/config/AMQPConfig.java` |
| Neo4j | Graph DB | persist directories, media, users, scan stats, schedules | username/password from env-backed config | high | `src/main/resources/application.yml`, `src/main/java/me/giobyte8/galleries/scanner/config/ScannerConfig.java`, `src/main/java/me/giobyte8/galleries/persistence/repositories/DirectoryRepository.java` |
| SQLite (scheduler datasource) | SQL DB | JobRunr schedule/job persistence | filesystem DB path config (`GL_SCHEDULER_DB`) | medium | `src/main/resources/application.yml`, `src/main/java/me/giobyte8/galleries/schedule/SchedulerConfig.java` |
| ExifTool binary | Local process/CLI integration | extract MP4 metadata via subprocess | no auth (local process call) | medium | `src/main/java/me/giobyte8/galleries/scanner/metadata/reader/Mp4ExifToolMetaReader.java`, `docker/galleries.dockerfile` |
| OTLP collector | Telemetry endpoint | metrics export | [TODO] auth headers/tokens not visible in repo | low/optional | `src/main/resources/application.yml` |

### 2) Data Stores

| Store | Role | Access layer | Key risk | Evidence |
|-------|------|--------------|----------|----------|
| Neo4j | primary app storage for gallery graph and domain entities | Spring Data Neo4j repositories + custom Neo4jClient queries | deep recursive graph queries (e.g., `*..5000`, `*..10000`) may become expensive at scale | `src/main/java/me/giobyte8/galleries/persistence/repositories/MediaRepositoryImpl.java`, `src/main/java/me/giobyte8/galleries/persistence/repositories/CustomizedDirectoryRepositoryImpl.java` |
| SQLite | JobRunr persistence for scheduler jobs | datasource bean `schedulerDatasource` | local file DB path can be environment-dependent | `src/main/java/me/giobyte8/galleries/schedule/SchedulerConfig.java`, `src/main/resources/application.yml` |

### 3) Secrets and Credentials Handling

- Credential sources: environment-variable interpolation in `application.yml` (`RABBITMQ_*`, `NEO4J_*`, `GL_REMEMBER_ME_KEY`, OTLP settings), plus local templates (`scripts/template.env`).
- Hardcoding checks: defaults exist for some values (for example `GL_REMEMBER_ME_KEY:dev-unsafe-change-me`, Neo4j default local creds) and should be overridden for production.
- Rotation or lifecycle notes: [TODO] no documented credential rotation lifecycle found.

### 4) Reliability and Failure Behavior

- Retry/backoff behavior: partial.
  - JobRunr scheduled publish path rethrows `AmqpException` to enable JobRunr retries.
  - Scan AMQP intake catches all exceptions and logs; queue-level retry behavior is effectively bypassed for those failures.
- Timeout policy: ExifTool invocation waits up to 5 seconds before failing metadata extraction.
- Circuit-breaker or fallback behavior: [TODO] no explicit circuit breaker/fallback framework usage found.

### 5) Observability for Integrations

- Logging around external calls: yes (`ScanRequestsListener`, `ScanScheduler`, metadata parsing warnings/errors).
- Metrics/tracing coverage: yes; Micrometer metrics via `MetricsService`, OpenTelemetry export toggles in config.
- Missing visibility gaps: [TODO] no explicit per-integration latency/error dashboards or SLO definitions found in repo.

### 6) Evidence

- `src/main/resources/application.yml`
- `src/main/java/me/giobyte8/galleries/scanner/amqp/ScanRequestsListener.java`
- `src/main/java/me/giobyte8/galleries/scanner/config/AMQPConfig.java`
- `src/main/java/me/giobyte8/galleries/scanner/config/ScannerConfig.java`
- `src/main/java/me/giobyte8/galleries/schedule/SchedulerConfig.java`
- `src/main/java/me/giobyte8/galleries/schedule/ScanScheduler.java`
- `src/main/java/me/giobyte8/galleries/scanner/metadata/reader/Mp4ExifToolMetaReader.java`
