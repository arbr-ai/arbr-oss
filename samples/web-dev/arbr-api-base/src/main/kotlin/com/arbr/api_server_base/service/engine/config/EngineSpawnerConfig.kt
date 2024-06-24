package com.arbr.api_server_base.service.engine.config

import com.arbr.api_server_base.service.engine.base.EngineSpawner
import com.arbr.api_server_base.service.engine.impl.ecs.EngineEcsSpawner
import com.arbr.api_server_base.service.engine.impl.hypervisor.EngineHypervisorClient
import com.arbr.api_server_base.service.engine.impl.local.EngineLocalSpawner
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EngineSpawnerConfig(
    @Value("\${arbr.engine-spawner.delegate:local}")
    private val delegateName: String,
    @Value("\${arbr.engine-hypervisor.host}")
    private val engineHypervisorHost: String?,
) {
    private val delegate = EngineSpawnerDelegate.from(delegateName)

    private fun engineLocalSpawner(): EngineSpawner {
        return EngineLocalSpawner()
    }

    private fun engineHypervisorSpawner(): EngineSpawner {
        if (engineHypervisorHost == null) {
            throw IllegalStateException("Engine hypervisor selected for spawner without configured host")
        }

        return EngineHypervisorClient(
            engineHypervisorHost
        )
    }

    private fun engineEcsSpawner(): EngineSpawner {
        return EngineEcsSpawner()
    }

    @Bean
    fun engineSpawner(): EngineSpawner {
        return when (delegate) {
            EngineSpawnerDelegate.LOCAL -> engineLocalSpawner()
            EngineSpawnerDelegate.HYPERVISOR -> engineHypervisorSpawner()
            EngineSpawnerDelegate.ECS -> engineEcsSpawner()
        }
    }

}
