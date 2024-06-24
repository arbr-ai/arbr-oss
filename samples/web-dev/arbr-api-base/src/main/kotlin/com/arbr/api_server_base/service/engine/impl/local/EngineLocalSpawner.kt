package com.arbr.api_server_base.service.engine.impl.local

import com.arbr.api_server_base.service.engine.base.EngineSpawner
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

class EngineLocalSpawner: EngineSpawner {

    override fun spawnEngineWorker(): Mono<Void> {
        return Mono.empty<Void?>()
            .doOnSubscribe {
                logger.info("Attempting to spawn engine worker locally")
            }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(EngineLocalSpawner::class.java)
    }

}
