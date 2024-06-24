package com.arbr.engine.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import java.time.Duration

class FluxIngestorTest {

    @Test
    fun `ingests values`() {
        val ingestor = FluxIngestor<String>()

        val order = mutableListOf<Int>()

        val monoA = ingestor.ingest {
            Mono.delay(Duration.ofMillis(50L))
                .thenReturn("abcd")
        }
            .doOnNext { order.add(0) }

        val monoB = ingestor.ingest {
            Mono.just("efgh")
        }
            .doOnNext { order.add(1) }

        val b = monoA
            .flatMap { monoB }
            .block()!!

        Assertions.assertEquals("efgh", b)
        Assertions.assertEquals(0, order[0])
        Assertions.assertEquals(1, order[1])
    }

}