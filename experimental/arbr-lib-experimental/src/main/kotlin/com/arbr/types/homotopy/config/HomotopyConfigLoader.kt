package com.arbr.types.homotopy.config

import org.slf4j.LoggerFactory
import java.util.*

class HomotopyConfigLoader {
    private fun <S: Any> loadServiceConfig(
        serviceClass: Class<S>,
        provideDefaultService: () -> S,
    ): S {
        val introspectionConfigServiceLoader = ServiceLoader.load(serviceClass)
        val configServices = introspectionConfigServiceLoader.toList()
        val service = configServices.firstOrNull() ?: provideDefaultService()

        logger.info("HomotopyConfigLoader found configured services: [${configServices.joinToString(", ") { it::class.java.canonicalName }}]")
        logger.info("HomotopyConfigLoader using: ${service::class.java}")

        return service
    }

    private fun loadIntrospectionConfig(): HomotopyIntrospectionConfig {
        val service = loadServiceConfig(HomotopyIntrospectionConfigService::class.java) {
            DefaultHomotopyIntrospectionConfigService()
        }
        return service.getConfig()
    }

    fun load(): HomotopyConfig {
        val introspectionConfig = loadIntrospectionConfig()
        return HomotopyConfig(
            introspectionConfig,
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(HomotopyConfigLoader::class.java)
    }
}
