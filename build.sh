#!/bin/zsh

set -ex

./gradlew codegen:arbr-codegen:publish
./gradlew codegen:arbr-codegen-gradle-plugin:publish
./gradlew codegen:arbr-codegen-target-object-model:publish

./gradlew platform:arbr-platform-base:publish
./gradlew platform:arbr-platform:publish
./gradlew platform:arbr-platform-alignable:publish
./gradlew platform:arbr-platform-object-graph:publish
./gradlew platform:arbr-platform-spring-autoconfigure:publish

./gradlew samples:web-dev:arbr-platform-web-dev:generateSourceFromGraphQLSchema
./gradlew samples:web-dev:arbr-platform-web-dev:publish
./gradlew samples:web-dev:arbr-api-base:publish

./gradlew samples:web-dev:arbr-web-dev:assemble
./gradlew samples:web-dev:arbr-web-dev:bootRun
