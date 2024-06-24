package com.arbr.engine.util

import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

class FluxPoolTest {


    @Test
    fun `pools elements`() {
        val eltSource = Flux
            .range(0, 8)
            .delayElements(Duration.ofMillis(100L))

        FluxPool.create(
            mutableListOf(),
            eltSource,
        )
            .concatMap {
                println(it)
                Mono.delay(Duration.ofMillis(1000L))
            }
            .collectList()
            .block()
    }

}
