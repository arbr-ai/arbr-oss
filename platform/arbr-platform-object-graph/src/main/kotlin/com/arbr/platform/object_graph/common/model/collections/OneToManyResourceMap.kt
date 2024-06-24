package com.arbr.og.object_model.common.model.collections

import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.og.object_model.common.model.ProposedForeignKeyCollectionStream
import com.arbr.platform.object_graph.impl.ObjectModelResource
import com.arbr.platform.object_graph.impl.Partial
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue
import reactor.core.publisher.Flux
import java.util.*
import kotlin.jvm.optionals.getOrNull

// TODO: Deprecate the UUID or replace the whole class with the PFKCS
class OneToManyResourceMap<T : ObjectModelResource<T, P, ForeignKey>, P : Partial<T, P, ForeignKey>, ForeignKey: NamedForeignKey>(
    val uuid: String,
    val items: ProposedForeignKeyCollectionStream<T, P, ForeignKey>,
) {
    constructor(
        items: ProposedForeignKeyCollectionStream<T, P, ForeignKey>
    ): this(
        UUID.randomUUID().toString(),
        items,
    )

    /**
     * Warning! Does not subscribe to change on elements within the list of items
     */
    @JsonIgnore
    fun getLatestValueFlux(): Flux<OneToManyResourcePartialMap<T>> {
        return items.getLatestValueFlux()
            .map { optItems ->
                OneToManyResourcePartialMapImpl(
                    uuid,
                    optItems.getOrNull()?.mapValuesNotNull { it.value.resource() }
                )
            }
    }

    @JsonValue
    fun toJson() = items

}
