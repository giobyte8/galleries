# http_downloader development

- [Local development](#local-development)
   - [Prerequisites](#prerequisites)
   - [Env setup](#env-setup)
   - [Tests](#tests)
   - [Run http_downloader](#run-httpdownloader)
   - [Trigger download request](#trigger-download-request)
- [Building and Releasing docker images](#building-and-releasing-docker-images)
   - [Prerequisites](#prerequisites-for-building)
   - [Locally building and testing image](#locally-building-and-testing-image)
   - [Release a new image version](#release-a-new-image-version)


## TODO
- [x] Make queue names configurable
- [x] Make AMQP exchange configurable
- [ ] Add delay between each downloaded file to avoid site overload
- [x] Automate docker image build and release
- [x] Complete README.md documentation

## Local development

### Prerequisites

1. Python 3.8
2. RabbitMQ, MySQL and Redis instances.
   Development containers are provided in project's root
   `docker.dev/docker-compose.yml`

### Env setup
1. Clone project and setup python env
   ```shell
   git clone git@github.com:giobyte8/galleries.git
   cd services/http_downloader

   # Make sure you're using python 3.8+ and create virtual env
   python -m venv .venv/
   source .venv/bin/activate

   # Optionally upgrade pip to latest version
   python -m pip intall --upgrade pip

   # Install dependencies
   pip install -r requirements.txt
   pip install -r requirements-dev.txt
   ```
2. Prepare config files
   ```shell
   cd config
   cp gallery-dl.conf.template.json gallery-dl.conf.json

   # Optional: Some sites need authentication using cookies to allow downloads,
   # if you plan to fetch private content from such sites, add your own cookie
   # values to corresponding extractor config
   vim gallery-dl.conf.json
   ```

   Finally prepare runtime folder for logs and temporary files during content
   download. From http_downloader root:
   ```shell
   mkdir -p data.dev/runtime

   # Add absolute path to 'runtime' dir to '.env' file in next step
   ```

3. Setup env file
   ```shell
   cp env.template .env

   vim .env
   # Enter appropriate values for your dev env
   ```

### Tests
Execute pytest From `http_downloader` root:
```shell
python -m pytest
```

### Run http_downloader
From `http_downloader` root run following command:

```shell
python http_downloader/downloader.py
```

### Trigger download request
First you'll need at least one http source registered into `http_source` table.

> You can use provided dev database for that, make sure to run
> `mysql_reset.bash` from `docker.dev` dir at galleries project root.

When application is running, it listens for sync requests messages through
AMQP. Expected messages must look like:

```json
{
    "source_id": 1,
    "url": "https://unsplash.com/collections/72573628/portraits",
    "content_path": "portraits/"
}
```

- `source_id`: Database id for `http_source` record
- `url`: Http url to download content
- `content_path`: Path to folder where content for this http source is
  downloaded. Provided path must be relative to value of `CONTENT_ROOT`
  variable in `.env` file

**For development** purposes you can execute
`scripts/request_http_source_sync.bash` script to post a sync request to
development rabbit instance

```shell
./scripts/request_http_source_sync.bash
```

## Building and Releasing docker images
Multi arch prebuilt docker images are released with every version of http_downloader

### Prerequisites for building
Below are the steps required before building multi arch docker images for the
first time in your machine.

1. Dedicated builder with support for multiple architectures
   Docker provides the buildx command, which allows to setup and use different
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

2. Login to docker registry to push images
   In order to allow docker pushes multi-arch images to registry make sure
   to be logged in:

   ```shell
   docker login
   # Enter username and password
   ```

### Locally building and testing image
You may want to build and test docker image before push it to docker registry.

1. Create your custom `http_downloader.docker.env` file by using provided
   template as a base and entering appropriate values for your env

   ```shell
   cp http_downloader.docker.template.env http_downloader.docker.env
   vim http_downloader.docker.env
   ```
2. Build image without pushing it to registry
   ```shell
   cd docker
   ./build_push_image.bash local-testing
   ```
3. Use provided script: docker/run_dev_container.bash. You might need to edit
   the script to update values of IMAGE_TAG, HOST_CONTENT_DIR and
   HOST_RUNTIME_DIR variables before execute.
4. Once the container is up and running you can use the script at
   `scripts/request_http_source_sync.bash` to trigger source sync

### Release a new image version

1. Make sure you're using the right docker builder
   ```shell
   docker buildx ls
   docker buildx use <builder-name>
   ```
2. Run script to build and push image to registry
   ```shell
   # From http_downloader root directory
   cd docker
   ./build_push_image.bash <new_version> --push
   ```
