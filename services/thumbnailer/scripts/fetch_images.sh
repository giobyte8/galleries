#!/bin/bash
# Fetch some test images to use during development
# Usage: ./fetch_images.sh

# ref: https://stackoverflow.com/a/4774063/3211029
SCRIPT_DIR="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
CALLER_DIR="$(pwd)"
cd "$SCRIPT_DIR"

DOWNLOAD_DST=$(realpath "../runtime/originals")
echo "Downloading images to: $DOWNLOAD_DST"

# Clean up destination directory
rm "$DOWNLOAD_DST/*"

galleries=(
    "https://500px.com/p/kid_of_ozz/galleries/places"
    "https://500px.com/p/kid_of_ozz/galleries/endless-summer-x-kidofozz"
    "https://500px.com/p/DmitriySoloduhin/galleries/my-lego-photos"
)

# Download each gallery
for gallery in "${galleries[@]}"; do
    echo
    echo "Downloading gallery: $gallery"
    gallery-dl -D "$DOWNLOAD_DST" "$gallery"
done

cd "$CALLER_DIR"