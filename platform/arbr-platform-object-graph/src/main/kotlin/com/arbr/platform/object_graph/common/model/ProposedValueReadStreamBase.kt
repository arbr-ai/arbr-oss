package com.arbr.platform.object_graph.common.model

import reactor.core.publisher.Flux
import java.util.*

interface ProposedValueReadStreamBase<S : Any> {

    fun hasAnyUnresolvedProposals(): Boolean

    fun getLatestAcceptedValueFlux(): Flux<Optional<S>>

    fun getLatestValueFlux(): Flux<Optional<S>>
}
