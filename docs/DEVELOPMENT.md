# Scanner development

- [Local development](#local-development)
- [Testing](#testing)
  - [Integration tests](#integration-tests)
- [Building for production](#building-for-production)
  - [Executing jar file](#executing-jar-file)
- [Building and releasing Docker images](#building-and-releasing-docker-images)
  - [Prerequisites](#prerequisites-for-building)
  - [Locally building and testing image](#locally-building-and-testing-image)
  - [Releasing](#release-a-new-image-version)

## Local development

Scanner is written in Java and uses Gradle. Clone the project and import it
into your IDE.

### Requirements

- JDK 25 (as configured in `build.gradle` toolchain)
- Running instances of:
  - RabbitMQ
  - Neo4j
- A media library to test scanner behavior
  - You can start with: `src/test/resources/galleries`

### Setup application properties

Some properties commonly overridden in IDE run configs:

```yaml
spring.rabbitmq.username: <value>
spring.rabbitmq.password: <value>
neo4j.username: <value>
neo4j.password: <value>

# Point to your local content root
galleries.scanner.content_dirs.root_path: /absolute/path/to/media/library
```

### Setup development users and seed data

1. (Optional) Generate a BCrypt hash for a custom development password:

```shell
./scripts/encrypt_password.sh <plain_password>
```

> If Python bcrypt is missing, install it first:

```shell
pip3 install bcrypt
```

> Put the generated hash into `scripts/dev_data_reset.cypher`.

2. Create `scripts/.env` from `scripts/template.env` and adjust values:

```shell
cp scripts/template.env scripts/.env
```

Example:
```dotenv
NEO4J_HOST=host.docker.internal
NEO4J_PORT=7687
NEO4J_USERNAME=neo4j
NEO4J_PASSWORD=<your_dev_password>
NEO4J_DATABASE=neo4j

DEV_SCAN_DIRS='
Wallpapers
galleries/custom/path
'
```

3. Run the reset script (drops existing data and reseeds):

```shell
./scripts/dev_data_reset.sh
```

Optional dry-run to inspect generated Cypher first:

```shell
./scripts/dev_data_reset.sh --dry-run
```

The default seed user in `scripts/dev_data_reset.cypher` is `dev`.

## Testing

Unit and integration tests are configured as separate Gradle suites in
`build.gradle`.

### Integration tests

Integration tests use Testcontainers (Neo4j + RabbitMQ), so you do not need a
manually managed test database.

Requirements:

- Docker daemon running locally

Run tests from terminal:

```shell
./gradlew test
./gradlew integrationTest
```

From IntelliJ, run `testing.suites.test` and
`testing.suites.integrationTest` from `build.gradle`.

## Building for production

Update the `version` field in `build.gradle` and build the boot jar:

```shell
./gradlew bootJar
```

The jar is generated under `build/libs`.

### Executing jar file

Create a `.yml` file with environment-specific values (using
`src/main/resources/application.yml` as reference), then run:

```shell
java -jar build/libs/galleries-<version>.jar --spring.config.location=file:///<path_to_your_yml>
```

## Building and releasing Docker images

Multi-arch Docker images are released per service version.

### Prerequisites for building

#### 1. Dedicated builder with support for multiple architectures

Docker `buildx` lets you use dedicated builders for multi-arch images.

```shell
docker buildx create --name hservices --use

# Other useful commands:
docker buildx ls
docker buildx use hservices
docker context ls
```

#### 2. Login to Docker registry to push images

```shell
docker login
```

### Locally building and testing image
In some scenarios you may want to build and test the Docker image before
pushing it to a registry.

1. Create `docker/scanner.env` from the provided template and set values
   for your environment.
   ```shell
   cp docker/scanner.template.env docker/scanner.env
   ```
2. Build image without pushing it to registry
   ```shell
   # Make sure you're using the right builder:
   # > docker buildx ls
   # > docker buildx use <builder-name>

   cd docker
   ./build_push_image.bash 0.0.1-testing
   ```
3. Use `docker/run_dev_container.bash`.
   You might need to edit the script to update values of `IMAGE_TAG` and
   `HOST_CONTENT_DIR` variables before executing it.
4. Once the container is running, use `docker/request_scan.bash` to
   trigger a directory scan.

### Release a new image version

1. Update `version` in `build.gradle`.
2. Ensure you are using the expected builder:

```shell
docker buildx ls
docker buildx use <builder-name>
```

3. Build and push:

```shell
cd docker
./build_push_image.bash <new_version> --push
```
