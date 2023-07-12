# Due to 'COPY' commands below, build context must be root of
# 'synchronizer' service

FROM python:3.8.17-alpine3.18

# Copy project files into image
WORKDIR /opt/galleries/synchronizer
COPY synchronizer/    /opt/galleries/synchronizer/synchronizer
COPY requirements.txt /opt/galleries/synchronizer/requirements.txt

# Install dependencies and run app
RUN apk add --no-cache build-base tzdata supercronic libffi-dev && \
    pip install -r requirements.txt
ENTRYPOINT ["python", "synchronizer/synchronizer.py"]
