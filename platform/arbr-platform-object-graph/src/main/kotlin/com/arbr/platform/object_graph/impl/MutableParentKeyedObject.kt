package com.arbr.platform.object_graph.impl

import com.arbr.object_model.core.types.naming.NamedForeignKey
import reactor.core.publisher.Mono

interface MutableParentKeyedObject<ForeignKey: NamedForeignKey> {
    fun setParent(
        foreignKey: ForeignKey,
        newUuid: String?,
    ): Mono<Void>

    fun setParentConditionally(
        foreignKey: ForeignKey,
        expectedUuid: String?,
        newUuid: String?,
    ): Mono<Void>
}