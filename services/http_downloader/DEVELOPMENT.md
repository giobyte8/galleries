# http_downloader development

## TODO
- [ ] Make queue names configurable
- [ ] Make AMQP exchange configurable
- [ ] Add delay between each downloaded file to avoid site overload
- [ ] Automate docker image build and release
- [ ] Complete README.md documentation

## Local development

### Prerequisites

1. Python 3.8
2. RabbitMQ, MySQL and Redis instances
   Refer to root project folder: `docker.dev/docker-compose.yml` to start it

### Env setup
1. Clone project and setup python env
   ```shell
   git clone git@github.com:giobyte8/galleries.git
   cd services/http_downloader

   # Make sure you're using python 3.8+
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
   vim gallery-dl.conf.json

   # Some sites need authentication using cookies to allow downloads
   cp gallery-dl.cookies.template.json gallery-dl.cookies.json
   vim gallery-dl.cookies.json
   # Add your own cookie values for each site
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

### Start app for development testing
From `http_downloader` root rum following command:

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
