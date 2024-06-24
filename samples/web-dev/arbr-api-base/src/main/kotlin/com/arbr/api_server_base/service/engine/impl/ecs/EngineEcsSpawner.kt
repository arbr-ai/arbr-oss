package com.arbr.api_server_base.service.engine.impl.ecs

import com.arbr.api_server_base.service.engine.base.EngineSpawner
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

class EngineEcsSpawner: EngineSpawner {

    override fun spawnEngineWorker(): Mono<Void> {
        return Mono.error(
            NotImplementedError("Local spawner NYI")
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(EngineEcsSpawner::class.java)
    }

}
