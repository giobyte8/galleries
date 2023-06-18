FROM amazoncorretto:17-alpine3.17 AS builder

# Use non-root user for security
RUN addgroup galleries; adduser --ingroup galleries --disabled-password scanner
USER scanner

# Copy source code into image
WORKDIR /opt/galleries/scanner
COPY gradle/         /opt/galleries/scanner/gradle/
COPY src/            /opt/galleries/scanner/src/
COPY build.gradle    /opt/galleries/scanner/build.gradle
COPY gradlew         /opt/galleries/scanner/gradlew
COPY settings.gradle /opt/galleries/scanner/settings.gradle

# Compile and package into .jar file
RUN ./gradlew bootJar


# Runtime only stage
FROM amazoncorretto:17-alpine3.17 AS runtime

# Use non-root user for security
RUN addgroup galleries; adduser --ingroup galleries --disabled-password scanner
USER scanner

WORKDIR /opt/galleries/scanner
COPY --from=builder "/opt/galleries/scanner/build/libs/*.jar" /opt/galleries/scanner/scanner.jar
ENTRYPOINT ["java", "-jar", "scanner.jar"]
