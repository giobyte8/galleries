FROM amazoncorretto:25-alpine AS builder

# Prepare non root user for security
RUN addgroup -S galleries \
	&& adduser -S galleries -G galleries

WORKDIR /opt/galleries

# Copy Gradle wrapper and build scripts first to maximize cache hits.
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./
RUN chmod +x gradlew

# Copy application source only after build scripts are in place.
COPY src/ src/

RUN chown -R galleries:galleries /opt/galleries
USER galleries

# Compile and package into .jar file
RUN ./gradlew --no-daemon bootJar


# Runtime only stage
FROM amazoncorretto:25-alpine AS runtime

RUN apk add --no-cache exiftool \
	&& addgroup -S galleries \
	&& adduser -S galleries -G galleries \
	&& mkdir -p /opt/galleries/data.dev \
	&& chown -R galleries:galleries /opt/galleries

WORKDIR /opt/galleries
COPY --from=builder /opt/galleries/build/libs/*.jar galleries.jar

USER galleries

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
  CMD wget -q -O - http://localhost:8100/actuator/health | \
	  grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "galleries.jar"]
