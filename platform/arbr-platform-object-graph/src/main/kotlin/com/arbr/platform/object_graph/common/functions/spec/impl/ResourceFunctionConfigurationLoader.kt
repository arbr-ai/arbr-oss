package com.arbr.og.object_model.common.functions.spec.impl

import com.arbr.og.object_model.common.functions.config.ResourceFunctionConfigurationService
import org.slf4j.LoggerFactory
import java.util.*

class ResourceFunctionConfigurationLoader {
    private var resourceFunctionConfig: ResourceFunctionConfig? = null

    private fun <S : Any> loadServiceConfig(
        serviceClass: Class<S>,
        provideDefaultService: () -> S,
    ): S {
        val introspectionConfigServiceLoader = ServiceLoader.load(serviceClass)
        val configServices = introspectionConfigServiceLoader.toList()
        val service = configServices.firstOrNull() ?: provideDefaultService()

        logger.info("ResourceFunctionConfigurationLoader found configured services: [${configServices.joinToString(", ") { it::class.java.canonicalName }}]")
        logger.info("ResourceFunctionConfigurationLoader using: ${service::class.java}")

        return service
    }

    @Synchronized
    private fun loadResourceFunctionConfig(): ResourceFunctionConfig {
        val currentResourceFunctionConfig = resourceFunctionConfig
        if (currentResourceFunctionConfig != null) {
            return currentResourceFunctionConfig
        }

        val service = loadServiceConfig(ResourceFunctionConfigurationService::class.java) {
            throw Exception("No resource functions configured")
        }
        return service
            .renderConfig()
            .also { resourceFunctionConfig = it }
    }

    fun load(): ResourceFunctionConfig {
        val resourceFunctionConfig = loadResourceFunctionConfig()
        return resourceFunctionConfig
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ResourceFunctionConfigurationLoader::class.java)
    }
}
