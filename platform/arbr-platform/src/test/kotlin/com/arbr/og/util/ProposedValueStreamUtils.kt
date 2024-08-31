package com.arbr.platform.object_graph.util

import com.arbr.platform.object_graph.common.model.ProposedValueWriteStream
import reactor.core.publisher.Mono

fun <S: Any> ProposedValueWriteStream<S>.set(s: S) {
    proposeAsync {
        Mono.just(s)
    }.block()
    getCombinedBatchProposal()!!.block()!!.accept()
}
