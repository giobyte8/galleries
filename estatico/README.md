# Estático

This is the component and config used to serve static resources for the galleries.

- Media files (images, videos, audios)
- Thumbnails

## Architecture

It's composed of a Caddy server deployed as a container.

```text

 -------        -------------
| Caddy | ---> | Media files |
 -------        -------------
 
```

## Endpoints

- `/` shows a browsable index with the mounted asset directories
- `/media/...` serves media files from `DATA_MEDIA` and supports directory browsing
- `/thumbs/...` serves thumbnails from `DATA_THUMBS`, supports directory browsing, and adds long-lived cache headers
- `/healthz` returns `200 OK` for a lightweight health check
