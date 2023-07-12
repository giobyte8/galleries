# Synchronizer development

- [Local development](#local-development)
   - [Prerequisites](#prerequisites)
   - [Env setup](#env-setup)
   - [Run synchronizer](#run-synchronizer)
   - [Trigger sources sync process](#trigger-sources-sync-process)
- [Building and Releasing docker images](#building-and-releasing-docker-images)
   - [Prerequisites](#prerequisites)
   - [Locally building and testing image](#locally-building-and-testing-image)
   - [Release new image version](#release-a-new-image-version)

## Local development

### Prerequisites
1. Python 3.8
2. Running instances of:
    - MySQL
    - RabbitMQ
    - Redis
   > Dev containers are provided in project root `docker.dev/docker-compose.yml`

### Env setup
1. Clone project and setup python env
   ```shell
   git clone git@github.com:giobyte8/galleries.git
   cd services/synchronizer

   # Make sure you're using python 3.8+ and create virtual env
   python -m venv .venv/
   source .venv/bin/activate

   # Optionally upgrade pip to latest version
   python -m pip intall --upgrade pip

   # Install dependencies
   pip install -r requirements.txt
   pip install -r requirements-dev.txt
   ```

2. Setup env file
   ```shell
   cp template.env .env

   vim .env
   # Enter appropriate values for your dev env
   ```

### Run synchronizer
From `synchronizer` root run following command:

```shell
python synchronizer/synchronizer.py
```

### Trigger sources sync process
From `synchronizer` root run following command:

```shell
python synchronizer/cmd_sync_sources.py
```

## Building and releasing docker images

### Prerequisites for building

TODO: Add link to docker builder setup docs

### Locally building and testing image
You may want to build and test docker image before push it to docker registry.

1. Create your custom `synchronizer.docker.env` file by using provided
   template as a base and entering appropriate values for your env

   ```shell
   cd docker/
   cp synchronizer.docker.template.env synchronizer.docker.env
   vim synchronizer.docker.env
   ```
2. Create your custom `sync_scheduler.crontab1 file by using provided
   template as a base and entering right execution time for your dev
   env
   ```shell
   cd config/
   cp sync_scheduler.template.crontab sync_scheduler.crontab
   vim sync_scheduler.crontab
   ```
3. Build image without pushing it to registry
   ```shell
   cd docker
   ./build_push_image.bash local-testing
   ```
4. **Run synchronizer:** Execute provided script
   `docker/run_dev_ctr_synchronizer.bash`. You may need to edit script to
   update values for `IMAGE_TAG`, `HOST_CONTENT_DIR` and `HOST_RUNTIME_DIR`.
5. **Run sync scheduler:** Execute provided script
   `docker/run_dev_ctr_sync_scheduler.bash`. You may need to edit script to
   update values for `IMAGE_TAG`, `HOST_CONTENT_DIR`, `HOST_RUNTIME_DIR` and
   `HOST_CONFIG_DIR`

> The `sync-sch` container will request sources synchronization based on
> provided cron config. An instance of `http_downloader` is required in order
> to handle such requests and generate events to be consumed by `synchronizer`
> container

### Release a new image version

1. Make sure you're using the right docker builder
   ```shell
   docker buildx ls
   docker buildx use <builder-name>
   ```
2. Run script to build and push image to registry
   ```shell
   cd docker
   ./build_push_image.bash <new_version> --push
   ```
