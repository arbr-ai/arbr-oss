package com.arbr.og.object_model.common.model

import reactor.core.publisher.Mono

interface ProposedValueWriteStream<S : Any>: ProposedValueReadStream<S> {

    fun proposeAsync(update: (S?) -> Mono<S>): Mono<S>

    @Suppress("UNCHECKED_CAST")
    fun proposeWith(proposer: Proposer<S>): Mono<S> = proposeAsync {
        proposer.proposeAsync(it).map { result ->
            result as S
        }
    }

    fun getCombinedBatchProposal(): Mono<Proposal<S>>?

    fun propose(newValue: S) {
        val proposalMono = proposeAsync { Mono.just(newValue) }

        // Subscribe without delegating to a Scheduler to update immediately
        // (?) What is the expected behavior if the last Mono takes time
        proposalMono
            .subscribe()
    }

    fun proposeUpdate(update: (S?) -> S) {
        val proposalMono = proposeAsync { Mono.just(update(it)) }

        // Subscribe without delegating to a Scheduler to update immediately
        // (?) What is the expected behavior if the last Mono takes time
        proposalMono
            .subscribe()
    }
}