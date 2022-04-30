# Extends the galleries image by adding a cron job to periodicaly sync
# galleries against its remote sources
#
# Note: galleries image should be build before this one
#

FROM giobyte8/galleries:1.0.0

RUN apk add --no-cache supercronic
CMD ["supercronic", "/media/config/downloader.crontab"]
