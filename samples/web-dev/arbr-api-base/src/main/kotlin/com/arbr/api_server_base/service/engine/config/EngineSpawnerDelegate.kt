package com.arbr.api_server_base.service.engine.config

enum class EngineSpawnerDelegate(val serializedName: String) {
    LOCAL("local"),
    HYPERVISOR("hypervisor"),
    ECS("ecs");

    companion object {
        fun from(configString: String): EngineSpawnerDelegate {
            val lowerConfigString = configString.lowercase()
            return EngineSpawnerDelegate.values().firstOrNull {
                it.serializedName == lowerConfigString
            } ?: LOCAL
        }
    }
}
