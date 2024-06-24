package com.arbr.og.util

import com.arbr.og.object_model.common.model.ProposedValueWriteStream
import reactor.core.publisher.Mono

fun <S: Any> ProposedValueWriteStream<S>.set(s: S) {
    proposeAsync {
        Mono.just(s)
    }.block()
    getCombinedBatchProposal()!!.block()!!.accept()
}
