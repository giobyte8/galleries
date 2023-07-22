# Galleries
Manage and expose image, video and text galleries by fetching items from
multiple local and remote sources.

- [Use cases](#some-use-cases)
- [Supported media sources](#supported-media-sources)
- [Deployment](#deployment)
    - [Required infrastructure](#required-infrastructure)
    - [Global config](#global-config)
    - Database setup
    - Redis setup
    - RabbitMQ setup
  - [Http Downloader](#http-downloader)
  - [Synchronizer](#synchronizer)

## Some use cases

- Combine a collection from unsplash.com and a pinterest.com board into a single
  gallery and expose their contents through a single API
- Expose a list of custom quotes (text fragments) through a single API
- Expose your pictures from local folders through galleries API

## Supported media sources

- Unsplash images and collections
- Pinterest pins and boards
- Specific folders inside git repositories
- Local folders
- Local database collections of JSON documents

## Disclaimer

Please be aware that content on remotes sites such as pinterest, unsplash or
500px may be subject to copyright, use content from those sources
appropriately

## Deployment
Galleries is composed of multiple, independent microservices. Each one is
deployed independently.

All of the microservices can be started through docker compose. Start by
fetching the compose file and the environment template

```shell
mkdir galleries && cd galleries

wget https://github.com/giobyte8/galleries/raw/main/docker/.gitignore
wget https://github.com/giobyte8/galleries/raw/main/docker/docker-compose.yml
wget -O .env https://github.com/giobyte8/galleries/raw/main/docker/compose.template.env
```

### Required Infrastructure
You'll need access to running instances of:
- MySQL
- Redis
- RabbitMQ

If you don't have preexisting instances you can create them based on contents
from `docker.dev/` directory.

### Global config

1. Make sure to enter values for below env variables into `.env` file
   ```shell
   # Path in host file system where galleries content will be stored
   CONTENT_DIR=

   # Path in host file system where to store runtime files (logs, temp files, etc)
   RUNTIME_DIR=
   ```

2. **Optional:** Update `docker-compose.yml` to put services into appropiate network
   ```yml
   // ...other configs
   services:
     <http_downloader|synchronizer|scanner>:
       networks:
         - hservices
   // ...

   networks:
     hservices:
       external: true
   ```

### Http Downloader

1. Prepare `gallery-dl` config template and cookies file
   ```shell
   mkdir -p config/http_downloader && cd config/http_downloader

   wget https://github.com/giobyte8/galleries/raw/main/services/http_downloader/config/gallery-dl.conf.template.json
   wget https://github.com/giobyte8/galleries/raw/main/services/http_downloader/config/gallery-dl.cookies.template.json
   ```

   Some sites needs authentication in order to download content, if that's the
   case edit your own `gallery-dl.cookies.json` file and add your own cookie values.
   Remove those that you don't intend to use.
   ```shell
   cp gallery-dl.cookies.template.json gallery-dl.cookies.json
   vim gallery-dl.cookies.json
   ```

   You can edit `gallery-dl.conf.template.json` file directly to add custom options.
   Check [gallery-dl repo](https://github.com/mikf/gallery-dl#configuration)
   for complete documentation of allowed options.
   ```shell
   vim gallery-dl.conf.template.json
   ```

2. Make sure to enter values for below env variables into `.env` file
   ```shell

   # Path in host file system where config files are stored for http_downloader
   HTTP_DOWNLOADER_CONFIG=
   ```

3. Setup http downloader specific env
   ```shell
   wget -O http_downloader.docker.env https://github.com/giobyte8/galleries/raw/main/services/http_downloader/docker/http_downloader.docker.template.env
   vim http_downloader.docker.env

   # Enter right values for rabbitmq connection and other settings
   ```

4. Start http_downloader service
   ```shell
   docker compose up -d http_downloader

   # Monitor running container:
   docker logs -f gl-downloader

   # Or watch application logs directly in provided runtime path
   tail -f logs/http_downloader.log
   ```

### Synchronizer

1. Prepare config files
   ```shell
   mkdir -p config/synchronizer && cd config/synchronizer
   wget https://github.com/giobyte8/galleries/raw/main/services/synchronizer/config/sync_scheduler.template.crontab
   ```

   Edit crontab file to match your desired synchronization time
   ```shell
   cp sync_scheduler.template.crontab sync_scheduler.crontab
   vim sync_scheduler.crontab
   ```

2. Make sure to enter values for below env variables into `.env` file
   ```shell

   # Path in host file system where config flles are stored for synchronizer
   SYNCHRONIZER_CONFIG=
   ```

3. Setup `synchronizer` specific env variables
   ```shell
   wget -O synchronizer.docker.env https://github.com/giobyte8/galleries/raw/main/services/synchronizer/docker/synchronizer.docker.template.env
   vim synchronizer.docker.env

   # Enter right values for rabbitmq, database and other settings
   ```

4. Start `synchronizer` services
   ```shell
   docker compose up -d synchronizer sync-sch
   docker logs -f gl-sync             # Monitor synchronizer logs
   tail -f logs/synchronizer.log      # ...Or watch app logs directly from runtime path

   docker logs -f gl-sync-sch         # Monitor sync scheduler logs
   tail -f logs/sync-sch.log          # ...or watch app logs directly from runtime path
   ```


# Legacy Docs

### 4. Setup your databse

1. From project root: `cd db && cp migrations.template.yml migrations.yml`
2. Edit `migrations.yml` and add your datasource config (MySQL)
3. Make sure entered database was previously created
4. Apply the migrations: `./matw migrate`

> Note: You may want to insert some sources into `http_source` table
