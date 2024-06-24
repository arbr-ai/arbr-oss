package com.arbr.api_server_base.service.engine.base

import reactor.core.publisher.Mono

/**
 * Spawns Engine instances for workflow execution.
 */
interface EngineSpawner {

    fun spawnEngineWorker(): Mono<Void>

}