package com.arbr.og.object_model.common.model

import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.platform.object_graph.impl.ObjectModelResource
import com.arbr.platform.object_graph.impl.ObjectRef
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.scheduler.Schedulers
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ProposedForeignKeyFluxDelegator<ForeignKey: NamedForeignKey> {
    
    data class Update<ForeignKey: NamedForeignKey>(
        val childResource: ObjectModelResource<*, *, ForeignKey>,
        val optionalParentRef: Optional<ObjectRef<*, *, ForeignKey>>,
        val isAccepted: Boolean,
    )

    private val innerFluxMap = ConcurrentHashMap<String, Flux<Update<ForeignKey>>>()

    /**
     * String Parent UUID -> Flux of updates potentially relevant to that parent
     */
    private val outerFluxMap =
        ConcurrentHashMap<String, Flux<Update<ForeignKey>>>()
    private val outerFluxSinkMap =
        ConcurrentHashMap<String, FluxSink<Update<ForeignKey>>>()

    private fun getOrCreateFlux(parentUuid: String): Flux<Update<ForeignKey>> {
        return outerFluxMap.compute(parentUuid) { _, existingFlux ->
            existingFlux ?: Flux.create<Update<ForeignKey>> { sink ->
                outerFluxSinkMap[parentUuid] = sink
            }.cache(1)
        }!!
    }

    fun getFlux(parentUuid: String): Flux<Update<ForeignKey>> {
        return getOrCreateFlux(parentUuid)
    }

    private fun getRelevantFluxSinks(element: ProposedValueStream<ObjectRef<*, *, ForeignKey>>): List<FluxSink<Update<ForeignKey>>> {
        val latestUuid = element.getLatestValue()?.uuid
        val acceptedUuid = element.getLatestAcceptedValue()?.uuid

        return listOfNotNull(latestUuid, acceptedUuid)
            .distinct()
            .mapNotNull { u ->
                outerFluxSinkMap[u]
            }
    }

    private fun subscribeTo(
        child: ObjectModelResource<*, *, ForeignKey>,
        parentRefInitialValue: ObjectRef<*, *, ForeignKey>?,
        parentRefStream: ProposedValueStream<ObjectRef<*, *, ForeignKey>>,
    ): String {

        val parentRefFlux = Flux.merge(
            parentRefStream.getLatestValueFlux()
                .map {
                    Update(child, it, isAccepted = false)
                },
            parentRefStream.getLatestAcceptedValueFlux()
                .map {
                    Update(child, it, isAccepted = true)
                },
        )

        val id = UUID.randomUUID().toString()
        innerFluxMap[id] = parentRefFlux

        val parentUuidInitialValue = parentRefInitialValue?.uuid
        if (parentUuidInitialValue != null) {
            getOrCreateFlux(parentUuidInitialValue)
                .subscribeOn(Schedulers.boundedElastic(), false)
                .subscribe()
        }

        parentRefFlux
            .doOnNext {
                getRelevantFluxSinks(parentRefStream).forEach { sink ->
                    sink.next(it)
                }
            }
            .doOnError {
                getRelevantFluxSinks(parentRefStream).forEach { sink ->
                    sink.error(it)
                }
            }
            .subscribeOn(Schedulers.boundedElastic(), false)
            .subscribe()

        return id
    }

    fun add(
        child: ObjectModelResource<*, *, ForeignKey>,
        parentRefInitialValue: ObjectRef<*, *, ForeignKey>?,
        parentRefStream: ProposedValueStream<ObjectRef<*, *, ForeignKey>>,
    ): String {
        return subscribeTo(
            child,
            parentRefInitialValue,
            parentRefStream,
        )
    }

    fun remove(id: String): Flux<Update<ForeignKey>>? {
        return innerFluxMap.remove(id)
    }

}