pluginManagement {
    resolutionStrategy.eachPlugin {
        // Something is off with the naming such that it can't resolve the plugin without manually resolving the
        // module here
        if (requested.id.id.startsWith("com.arbr")) {
            useModule("com.arbr:gradle:1.0")
        }
    }
    repositories {
        gradlePluginPortal()
        mavenLocal {
            System.getenv("MAVEN_LOCAL_HOST")?.takeIf { it.isNotBlank() }?.let { localMavenHost ->
                isAllowInsecureProtocol = true
                url = java.net.URI("http://$localMavenHost/releases")
                credentials {
                    username = "admin"
                    password = "secret"
                }
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "arbr"

include("codegen")
include("codegen:arbr-codegen")
include("codegen:arbr-codegen-target-object-model")
include("codegen:arbr-codegen-gradle-plugin")

include("platform")
include("platform:arbr-platform-object-graph")
include("platform:arbr-platform-alignable")
include("platform:arbr-platform-base")
include("platform:arbr-platform")
include("platform:arbr-platform-spring-autoconfigure")

include("samples")
include("samples:web-dev")
include("samples:web-dev:arbr-api-base")
include("samples:web-dev:arbr-platform-web-dev")
include("samples:web-dev:arbr-web-dev")
