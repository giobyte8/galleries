# Testing Patterns

## Core Sections (Required)

### 1) Test Stack and Commands

- Primary test framework: JUnit Jupiter (configured via Gradle `useJUnitJupiter()`).
- Assertion/mocking tools: JUnit assertions, Mockito, Spring Security test helpers, Spring MockMvc, Testcontainers.
- Commands:

```bash
./gradlew test
./gradlew test
./gradlew integrationTest
[TODO] coverage command/config not found
```

### 2) Test Layout

- Test file placement pattern: unit tests in `src/test/java`; integration tests in dedicated suite `src/integrationTest/java`.
- Naming convention: class names end with `Test` or `Tests`.
- Setup files and where they run: integration profile config in `src/integrationTest/resources/application.yml`; shared integration container bootstrap in `BaseIntegrationTest`.

### 3) Test Scope Matrix

| Scope | Covered? | Typical target | Notes |
|-------|----------|----------------|-------|
| Unit | yes | scanner/services/metadata/security classes | Mockito-based isolation in `src/test/java` |
| Integration | yes | repositories, admin security, scanner workflows | Spring Boot + Testcontainers + Neo4j/RabbitMQ in `src/integrationTest/java` |
| E2E | [TODO] | [TODO] | no explicit browser/API end-to-end suite found |

### 4) Mocking and Isolation Strategy

- Main mocking approach: Mockito annotations (`@Mock`, `@InjectMocks`, `@ExtendWith(MockitoExtension.class)`), plus lenient stubs in scanner unit tests.
- Isolation guarantees: integration tests reset Neo4j state after each test (`MATCH (n) DETACH DELETE n` in `@AfterEach`).
- Common failure mode in tests: some legacy unit tests are commented out (for example in `ScanRequestsListenerTest`), indicating outdated/unfinished test coverage in that area.

### 5) Coverage and Quality Signals

- Coverage tool + threshold: [TODO] not configured in current repository.
- Current reported coverage: [TODO] not present in repository outputs/config.
- Known gaps/flaky areas:
  - `ScanRequestsListenerTest` currently has commented test cases.
  - [TODO] no automated flaky-test tracking mechanism found.

### 6) Evidence

- `build.gradle`
- `src/test/java/me/giobyte8/galleries/scanner/scanners/LocalMediaScannerTests.java`
- `src/test/java/me/giobyte8/galleries/scanner/amqp/ScanRequestsListenerTest.java`
- `src/integrationTest/java/me/giobyte8/galleries/scanner/BaseIntegrationTest.java`
- `src/integrationTest/java/me/giobyte8/galleries/admin/security/AdminSecurityIntegrationTests.java`
- `src/integrationTest/resources/application.yml`
