package com.arbr.og.object_model.common.model

import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.object_model.core.types.naming.NamedPropertyKey
import com.arbr.platform.object_graph.impl.ObjectModelResource
import com.arbr.platform.object_graph.impl.ObjectRef
import com.arbr.platform.object_graph.impl.Partial
import com.arbr.platform.object_graph.store.KVStore
import com.fasterxml.jackson.annotation.JsonValue
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.*
import kotlin.jvm.optionals.getOrNull


class ProposedForeignKeyCollectionStream<S : ObjectModelResource<S, P, ForeignKey>, P : Partial<S, P, ForeignKey>, ForeignKey: NamedForeignKey>(
    override val identifier: PropertyIdentifier,
    val foreignKey: ForeignKey,
    // TODO: Provide generically
    private val kvStore: KVStore<String, ObjectModelResource<*, *, ForeignKey>, ForeignKey>,
) : ProposedValueReadStream<ImmutableLinkedMap<String, ObjectRef<out S, P, ForeignKey>>>,
    ProposedValueWriteStream<ImmutableLinkedMap<String, ObjectRef<out S, P, ForeignKey>>> {

    private var latestCollectionValue: List<ObjectModelResource<*, *, ForeignKey>>? = null
        get() = synchronized(this) {
            field
        }
        set(value) = synchronized(this) {
            field = value
        }
    private var latestAcceptedCollectionValue: List<ObjectModelResource<*, *, ForeignKey>>? = null
        get() = synchronized(this) {
            field
        }
        set(value) = synchronized(this) {
            field = value
        }

    private val latestValueCollectionFlux = kvStore.foreignKeyListIndex(foreignKey, identifier.resourceUuid)
        .flatMap { foreignKeyChildListResult ->
            val isAccepted = foreignKeyChildListResult.isAccepted
            val newList = foreignKeyChildListResult.children

            val lastValue = if (isAccepted) {
                val lastValue = latestAcceptedCollectionValue
                latestAcceptedCollectionValue = newList
                lastValue
            } else {
                val lastValue = latestCollectionValue
                latestCollectionValue = newList
                lastValue
            }

            if (isAccepted) {
                itemContainerExistsProposedValueStream
                    .getCombinedBatchProposal()
                    ?.map {
                        it.accept()
                        newList
                    }
                    ?: Mono.just(newList)
            } else if (lastValue != null) {
                Mono.just(newList)
            } else {
                // Always emit a nonempty Optional here since we have observed at least one proposal of a non-empty value
                // This handles the case of child updates that aren't proposed through this collection
                // Similar to proposeAsync, the only relevant case is when the new value is an empty map, such as when
                // the last child detaches itself.
                // Ideally we could hook into that last proposal, since otherwise we end up with an extra, parallel
                // proposal on the flag here
                itemContainerExistsProposedValueStream.proposeAsync {
                    if (it == true) {
                        Mono.empty()
                    } else {
                        Mono.just(true)
                    }
                }.thenReturn(newList)
            }
        }
        .share()

    @Suppress("UNCHECKED_CAST")
    private val latestValueFlux: Flux<Optional<ImmutableLinkedMap<String, ObjectRef<out S, P, ForeignKey>>>> = Flux.concat(
        Flux.just(Optional.empty()),
        latestValueCollectionFlux.mapNotNull {
            latestCollectionValue?.let { childList ->
                Optional.of(
                    ImmutableLinkedMap(childList.map { it.uuid to ObjectRef.OfResource(it as S) })
                )
            }
        }
    )

    @Suppress("UNCHECKED_CAST")
    private val latestAcceptedValueFlux: Flux<Optional<ImmutableLinkedMap<String, ObjectRef<out S, P, ForeignKey>>>> = Flux.concat(
        Flux.just(Optional.empty()),
        latestValueCollectionFlux.mapNotNull {
            latestAcceptedCollectionValue?.let { childList ->
                Optional.of(
                    ImmutableLinkedMap(childList.map { it.uuid to ObjectRef.OfResource(it as S) })
                )
            }
        }
    )

    private val itemContainerExistsPropertyKey = object : NamedPropertyKey {
        override val name: String
            get() = identifier.propertyKey.name + ".itemContainerExists"
        override val ordinal: Int
            get() = 0
    }
    private val itemContainerExistsProposedValueStream = ProposedValueStream<Boolean>(
        PropertyIdentifierBase(
            identifier.resourceKey,
            itemContainerExistsPropertyKey,
            identifier.relationship,
            identifier.resourceUuid,
        )
    )

    private fun queryAllKeys(
        predicate: (ProposedValueStream<ObjectRef<*, *, ForeignKey>>) -> Boolean = { true },
    ): List<Pair<ObjectModelResource<*, *, ForeignKey>, ProposedValueStream<ObjectRef<*, *, ForeignKey>>>> {
        // Opt for CPU over memory usage for now and compute on the fly
        return kvStore
            .queryForeignKey(foreignKey, identifier.resourceUuid)
            .filter { obj ->
                obj.getForeignKeys()[foreignKey]?.let {
                    predicate(it)
                } == true
            }
            .mapNotNull { obj ->
                obj.getForeignKeys()[foreignKey]?.let {
                    obj to it
                }
            }
    }

    init {
        latestValueCollectionFlux
            .subscribeOn(Schedulers.boundedElastic(), false)
            .subscribe()
        itemContainerExistsProposedValueStream
            .getLatestValueFlux()
            .doOnNext {
                if (it.getOrNull() == true) {
                    synchronized(this) {
                        val latestValue = latestCollectionValue
                        if (latestValue == null) {
                            latestCollectionValue = emptyList()
                        }
                    }
                }
            }
            .subscribeOn(Schedulers.boundedElastic(), false)
            .subscribe()
    }

    /**
     * Warning: needs to correspond exactly to `getCombinedBatchProposal() != null`, but the latter is expensive, so
     * this is useful to have.
     */
    @Synchronized
    override fun hasAnyUnresolvedProposals(): Boolean {
        if (itemContainerExistsProposedValueStream.hasAnyUnresolvedProposals()) {
            return true
        }

        return queryAllKeys {
            it.hasAnyUnresolvedProposals()
        }.isNotEmpty()
    }

    @Suppress("UNCHECKED_CAST")
    @Synchronized
    override fun getLatestAcceptedValue(): ImmutableLinkedMap<String, ObjectRef<out S, P, ForeignKey>>? = queryAllKeys {
        it.getLatestAcceptedValue()?.uuid == identifier.resourceUuid
    }
        .map { (obj, _) ->
            obj as S
        }
        .let { sList ->
            if (sList.isEmpty() && (itemContainerExistsProposedValueStream.getLatestAcceptedValue() != true)) {
                null
            } else {
                ImmutableLinkedMap(
                    sList.map { it.uuid to ObjectRef.OfResource(it) }
                )
            }
        }

    @Synchronized
    override fun getLatestAcceptedValueFlux(): Flux<Optional<ImmutableLinkedMap<String, ObjectRef<out S, P, ForeignKey>>>> = Flux.concat(
        Flux.just(Optional.ofNullable(getLatestAcceptedValue())),
        latestAcceptedValueFlux,
    )

    @Suppress("UNCHECKED_CAST")
    @Synchronized
    override fun getLatestValue(): ImmutableLinkedMap<String, ObjectRef<out S, P, ForeignKey>>? = latestCollectionValue?.associate {
        it.uuid to ObjectRef.OfResource<S, P, ForeignKey>(it as S)
    }?.let {
        ImmutableLinkedMap(it.toList())
    }

    @Synchronized
    override fun getLatestValueFlux(): Flux<Optional<ImmutableLinkedMap<String, ObjectRef<out S, P, ForeignKey>>>> = Flux.concat(
        Flux.just(Optional.ofNullable(getLatestValue())),
        latestValueFlux,
    )

    @Synchronized
    override fun proposeAsync(update: (ImmutableLinkedMap<String, ObjectRef<out S, P, ForeignKey>>?) -> Mono<ImmutableLinkedMap<String, ObjectRef<out S, P, ForeignKey>>>): Mono<ImmutableLinkedMap<String, ObjectRef<out S, P, ForeignKey>>> {
        val currentProposedMap = getLatestValue()
        val currentProposedUuids = currentProposedMap?.mapNotNull { it.value.uuid }?.toSet() ?: emptySet()

        return update(currentProposedMap).flatMap { nextMap ->
            // A direct proposal switches to default empty even if proposed map is empty
            // This update is only relevant as a direct proposal when going from no value (null) to an empty map -
            // otherwise, the existence of the children dictate the null/emptiness
            val containerExistsProposalMono = if (currentProposedMap == null && nextMap.isEmpty()) {
                itemContainerExistsProposedValueStream.proposeAsync {
                    if (it == true) {
                        Mono.empty()
                    } else {
                        Mono.just(true)
                    }
                }.then()
            } else {
                Mono.empty()
            }

            containerExistsProposalMono.then(Mono.defer {
                val nextUuids = nextMap?.mapNotNull { it.value.uuid }?.toSet() ?: emptySet()

                val lostUuids = currentProposedUuids - nextUuids
                val gainedUuids = nextUuids - currentProposedUuids

                val lostUpdates = lostUuids.mapNotNull { uuid ->
                    currentProposedMap?.get(uuid)?.resource()
                }.map { s ->
                    s.setParentConditionally(foreignKey, identifier.resourceUuid, null)
                }

                val gainedUpdates = gainedUuids.mapNotNull { uuid ->
                    nextMap[uuid]?.resource()
                }.map { s ->
                    s.setParentConditionally(foreignKey, null, identifier.resourceUuid)
                }

                Flux.fromIterable(lostUpdates + gainedUpdates).flatMap { it }
                    .collectList()
                    .then(
                        Mono.defer {
                            Mono.justOrEmpty(getLatestValue())
                        }
                    )
            })
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Synchronized
    override fun getCombinedBatchProposal(): Mono<Proposal<ImmutableLinkedMap<String, ObjectRef<out S, P, ForeignKey>>>>? {
        return queryAllKeys()
            .mapNotNull { (_, refPvs) ->
                refPvs.getCombinedBatchProposal()
            }
            .let { refProposals ->
                val allProposals =
                    refProposals + listOfNotNull(itemContainerExistsProposedValueStream.getCombinedBatchProposal())

                if (allProposals.isEmpty()) {
                    null
                } else {
                    Flux.fromIterable(allProposals)
                        .flatMap {
                            it as Mono<Proposal<Any>>
                        }
                        .collectList()
                        .defaultIfEmpty(emptyList())
                        .map { innerProposals ->
                            val acceptedValue = getLatestAcceptedValue()
                            val latestValue = getLatestValue()

                            ProposalImpl(
                                acceptedValue,
                                latestValue,
                                {
                                    innerProposals.forEach { prop ->
                                        prop.accept()
                                    }
                                },
                                {
                                    innerProposals.forEach { it.reject() }
                                }
                            )
                        }
                }
            }
    }

    @JsonValue
    fun toJson() = getLatestValue()
}
