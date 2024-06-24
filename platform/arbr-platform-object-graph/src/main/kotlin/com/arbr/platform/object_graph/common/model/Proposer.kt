package com.arbr.og.object_model.common.model

import reactor.core.publisher.Mono

fun interface Proposer<in S> {

    fun proposeAsync(s: S?): Mono<in S>

}