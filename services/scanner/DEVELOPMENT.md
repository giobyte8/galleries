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

## Testing
Unit tests and integration tests are configured to run as individual suites into
`build.gradle` file.

### Integration tests
Some tests need a real database instance to upsert some records as part
of their flow. You can use a predefined service named `testdb` from
`galleries/docker.dev/docker-compose.yml` file:

```shell
docker-compose up -d testdb
```

Once test db is up and running, double check test config values at
`src/integrationTest/resources/application.yml` before running tests.

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
