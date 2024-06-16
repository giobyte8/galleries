# Scanner development

- [Local development](#local-development)
- [Testing](#testing)
  - [Integration tests](#integration-tests)
- [Building for production](#building-for-production)
  - [Executing jar file](#executing-jar-file)
- [Building and Releasing docker images](#building-and-releasing-docker-images)
  - [Prerequisites](#prerequisites-for-building)
  - [Locally building and testing image](#locally-building-and-testing-image)
  - [Releasing](#release-a-new-image-version)

## Local development
Scanner is written in java and uses gradle as build system. Clone the project and
import it into your IDE.

You'll need java >= 17

## Testing
Unit tests and integration tests are configured to run as individual suites into
`build.gradle` file.

### Integration tests

Some integration tests need a real database to truthfully simulate production scenarios. The connection params are defined in `src/integrationTest/resources/application.yaml`.

By default configuration points to provided database for integration tests, which is defined as `test_neo4j` service in `galleries/docker.dev/docker-compose.yml` file.

> NOTE: The integration tests suite will remove all data from database after each test run, hence, make sure to use a dedicated database for testing purposes.

**Start provided neo4j database for tests**

```shell
docker compose up -d testn4j
```

> - Integration tests database doesn't require authentication
> - Port mapping was changed from default to prevent conflicts with other possible instances

You can now navigate to http://localhost:7074 and connect to your test database

- Note that in connection params the port of target DB should be `7087` as defined in `docker-compose.yaml` file
- Authentication method should be `No authentication` or `None`

**Run integration tests**

Open `build.gradle` file in IntelliJ and execute `testing.suites.integrationTest` entry.
Or you can execute each test class/method directly.

Alternatively form terminal run

```shell
> TODO: Enter command to run integration tests
```

## Building for production

Update `version` field in `build.gradle` file and run gradle build:

```shell
./gradlew bootJar
```
> jar file will be placed in `build/libs` directory

### Executing jar file
Create a `.yml` file with appropriate values for your env. You can use
`src/main/resources/application.yml` as a reference. Then run the
application with:

```shell
java -jar scanner-1.1.0.jar --spring.config.location=file:///<path_to_your_yml>
```

## Building and Releasing docker images
Multi arch prebuilt docker images are released with every version of scanner

### Prerequisites for building
Below are the steps required before building multi arch docker images for the
first time in your machine.

#### 1. Dedicated builder with support for multiple architectures
Docker provides the `buildx` command, which allows to setup and use different
image builders for different images.

Create a specific builder with multi-arch support:
```shell
docker buildx create --name hservices --use


# Other useful commands:

# List all available builders
docker buildx ls

# Switch to a specific builder
docker buildx use hservices

# List all docker contexts
docker context ls
```

#### 2. Login to docker registry to push images
In order to allow docker pushes multi-arch images to registry make sure
to be logged in:

```shell
docker login
# Enter username and password
```

### Locally building and testing image
In some scenarios you may want to build and test docker image before push
it to docker registry.

1. Create your custom `scanner.env` file by using provided template as a base
   and entering appropriate values for your env
   ```shell
   cp scanner.template.env scanner.env
   vim scanner.env
   ```
2. Build image without pushing it to registry
   ```shell
   # Make sure you're using the right builder:
   # > docker buildx ls
   # > docker buildx use <builder-name>
   
   cd docker
   ./build_push_image.bash 0.0.1-testing
   ```
3. Use provided script: `docker/run_dev_container.bash`.
   You might need to edit the script to update values of `IMAGE_TAG` and 
   `HOST_CONTENT_DIR` variables before executing it.
4. Once the container is up and running you can use the script at
   `docker/request_scan.bash` to trigger a directory scan

### Release a new image version

1. Update `version` value in `build.gradle` file
2. Make sure you're using the right docker builder
   ```shell
   docker buildx ls
   docker buildx use <builder-name>
   ```
3. Run script to build and push image to registry
   ```shell
   # From scanner root directory
   cd docker
   ./build_push_image.bash <new_version> --push
   ```
