package com.arbr.engine.util

import com.arbr.platform.object_graph.impl.ObjectModelResource
import com.arbr.platform.object_graph.impl.Partial
import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap

fun <T : ObjectModelResource<T, P, *>, P : Partial<T, P, *>> immutableLinkedMapOfPartials(partials: List<P>): ImmutableLinkedMap<String, P> {
    return ImmutableLinkedMap(
        partials.map { it.uuid to it }
    )
}
