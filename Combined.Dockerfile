## Baseline gradle
FROM gradle:8.7-jdk21-graal

## Codegen

WORKDIR /code
COPY codegen codegen

## Codegen Base

WORKDIR /code/codegen/arbr-codegen
RUN ./gradlew assemble publishToMavenLocal --no-daemon

## Codegen Extension: Object Model

WORKDIR /code/codegen/arbr-codegen-target-object-model
RUN ./gradlew assemble publishToMavenLocal --no-daemon

## Codegen Gradle Plugin

WORKDIR /code/codegen/arbr-codegen-gradle-plugin
RUN ./gradlew assemble publishPluginMavenPublicationToMavenLocal --no-daemon

## Platform

WORKDIR /code
COPY platform platform

## Platform Base

WORKDIR /code/platform/arbr-platform-base
RUN ./gradlew assemble publishToMavenLocal --no-daemon

## Platform Core

WORKDIR /code/platform/arbr-platform
RUN ./gradlew assemble publishToMavenLocal --no-daemon

## Samples

WORKDIR /code
COPY samples samples

## Sample: Web Dev (API Base)

WORKDIR /code/samples/web-dev/arbr-api-base
RUN ./gradlew assemble publishToMavenLocal --no-daemon

## Sample: Web Dev (Platform Extension)

WORKDIR /code/samples/web-dev/arbr-platform-web-dev
RUN ./gradlew generateMain --no-daemon
RUN ./gradlew assemble publishToMavenLocal --no-daemon

## Sample: Web Dev (Server)

WORKDIR /code/samples/web-dev/arbr-web-dev
RUN ./gradlew assemble --no-daemon

ENTRYPOINT ["/bin/bash", "-c"]
CMD ["ls"]
