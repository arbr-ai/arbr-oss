package com.arbr.engine.app.routine

import reactor.core.publisher.Mono

interface WorkerRoutine {
    fun run(): Mono<Void>
}

