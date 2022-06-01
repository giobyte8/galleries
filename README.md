# Galleries

Manage and expose image, video and text galleries combining elements from
several local and internet sources.

## Some use cases

- Combine a collection from unsplash.com and a pinterest board into a single
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
500px may be subject to copyrights, use content from those sources
appropriately

## Deployment through docker

**Requirements:** You should have access to running instances of: MySQL,
Redis and RabbitMQ. If you don't have preexisting instances you could
create them based on contents from `docker.dev` directory

1. Clone repo and navigate to root
2. Setup http downloader config files
  1. `cd config/http_downloader && cp gallery-dl.cookies.template.json gallery-dl.cookies.json`
  2. Add your own cookie values to `gallery-dl.cookies.json` files. Remove
     those that you don't intend to use.
  3. (Optional) You can edit `gallery-dl.conf.template.json` to adjust it to your needs
3. Setup synchronizer automatic run schedule
  1. From project root: `cd config/synchronizer && cp src_sync.template.crontab src_sync.crontab`
  2. Edit `src_sync.crontab`file to match your desired schedule. This config
     will be used by the synchronizer to scan sources for new content changes.
4. Setup your databse
  1. From project root: `cd db && cp migrations.template.yml migrations.yml`
  2. Edit `migrations.yml` and add your datasource config (MySQL)
  3. Make sure entered database was previously created
  4. Apply the migrations: `./matw migrate`
5. Setup your runtime environment
  1. From project root: `cd docker && cp env.template .env`
  2. Edit `.env` and add the appropriate values for your own environment
6. Setup containers env
  1. `cd docker && cp env.template .env`
  2. Edit `.env` and adjust to your own environment
7. Start containers: `cd docker && docker-compose up -d`

> Note: You may want to insert some sources into `http_source` table
