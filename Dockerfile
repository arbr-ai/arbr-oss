## Baseline gradle
FROM gradle:8.7-jdk21-graal as gradle_base

WORKDIR /code_gradle_base

# Load any gradle
COPY codegen/arbr-codegen/gradle gradle
COPY codegen/arbr-codegen/gradle* ./

# TODO: Load common dependencies
RUN ./gradlew --no-daemon

## Codegen
FROM gradle_base as arbr_codegen

WORKDIR /code

# Copy gradle build files

COPY codegen/arbr-codegen/gradle gradle
COPY codegen/arbr-codegen/gradle* ./
COPY codegen/arbr-codegen/settings.gradle.kts settings.gradle.kts
COPY codegen/arbr-codegen/build.gradle.kts build.gradle.kts

# Copy source files

COPY codegen/arbr-codegen/src src

RUN ./gradlew assemble publish --no-daemon


## Codegen Extension: Object Model
FROM gradle_base as arbr_codegen_object_model

COPY --from=arbr_codegen /root/.m2 /root/.m2

WORKDIR /code

# Copy gradle build files

COPY arbr-codegen-target-object-model/gradle gradle
COPY arbr-codegen-target-object-model/gradle* ./
COPY arbr-codegen-target-object-model/settings.gradle.kts settings.gradle.kts
COPY arbr-codegen-target-object-model/build.gradle.kts build.gradle.kts

# Copy source files

COPY arbr-codegen-target-object-model/src src

RUN ./gradlew assemble publish --no-daemon

FROM gradle_base as arbr_platform_base

WORKDIR /code

# Copy gradle build files

COPY arbr-platform-base/gradle gradle
COPY arbr-platform-base/gradle* ./
COPY arbr-platform-base/settings.gradle.kts settings.gradle.kts
COPY arbr-platform-base/build.gradle.kts build.gradle.kts

# Copy source files

COPY arbr-platform-base/src src

RUN ./gradlew assemble publish --no-daemon

## Platform
FROM gradle_base as arbr_platform

COPY --from=arbr_platform_base /root/.m2 /root/.m2

WORKDIR /code

# Copy gradle build files

COPY arbr-platform/gradle gradle
COPY arbr-platform/gradle* ./
COPY arbr-platform/settings.gradle.kts settings.gradle.kts
COPY arbr-platform/build.gradle.kts build.gradle.kts

# Copy source files

COPY arbr-platform/src src

RUN ./gradlew assemble publish --no-daemon

## Platform
FROM gradle_base as arbr_platform_web_dev

WORKDIR /code

COPY --from=arbr_platform /root/.m2 /root/.m2
COPY --from=arbr_codegen /root/.m2 /root/.m2
COPY --from=arbr_codegen_object_model /root/.m2 /root/.m2

COPY --from=arbr_codegen /root/.m2/repository/com/arbr/arbr-codegen/1.0/arbr-codegen-1.0.jar codegen.jar
COPY --from=arbr_codegen_object_model /root/.m2/repository/com/arbr/arbr-codegen-target-object-model/1.0/arbr-codegen-target-object-model-1.0.jar codegen-target-object-model.jar

COPY samples/web-dev/arbr-platform-web-dev/src/main/arbr/object_model.graphqls src/main/arbr/object_model.graphqls

RUN java -cp codegen.jar:codegen-target-object-model.jar com.arbr.backdraft.TaskMainKt \
    --target com.arbr.codegen.target.compiler.om.GraphQlSchemaToObjectModelCompiler \
    --output build/generated-src/arbr/main/generate_object_model_source/ \
    src/main/arbr/object_model.graphqls

RUN cp -r build/generated-src/arbr/main/generate_object_model_source/com src/main/kotlin
RUN rm -rf build/generated-src/arbr/main/generate_object_model_source
RUN rm codegen*.jar

# Copy gradle build files
COPY samples/web-dev/arbr-platform-web-dev/gradle gradle
COPY samples/web-dev/arbr-platform-web-dev/gradle* ./
COPY samples/web-dev/arbr-platform-web-dev/settings.gradle.kts settings.gradle.kts
COPY samples/web-dev/arbr-platform-web-dev/build.gradle.kts build.gradle.kts

# Copy source files
COPY samples/web-dev/arbr-platform-web-dev/src src

RUN gradle assemble publish --no-daemon

FROM gradle_base as arbr_web_dev

WORKDIR /code

RUN mkdir -p /var/lib/arbr/artifacts_tmp
RUN mkdir -p /var/lib/arbr/artifacts
COPY --from=arbr_platform_web_dev /root/.m2 /root/.m2
COPY --from=arbr_platform_web_dev /root/.m2 /var/lib/arbr/artifacts_tmp

# Copy gradle build files
COPY samples/web-dev/arbr-web-dev/gradle gradle
COPY samples/web-dev/arbr-web-dev/gradle* ./
COPY samples/web-dev/arbr-web-dev/settings.gradle.kts settings.gradle.kts
COPY samples/web-dev/arbr-web-dev/build.gradle.kts build.gradle.kts

# Copy source files
COPY samples/web-dev/arbr-web-dev/src src
RUN mkdir -p build/logs

ENTRYPOINT ["/bin/bash", "-c"]
CMD ["ls"]
#RUN gradle build --no-daemon
#
#CMD ["gradle", "bootRun", "--no-daemon"]
