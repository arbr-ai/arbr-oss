package com.arbr.platform.object_graph.impl

import com.arbr.platform.object_graph.common.ObjectModel
import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap

interface PartialProperties<P : Partial<*, *, *>> {
    val propertyMap: ImmutableLinkedMap<String, ObjectModel.ObjectValue<*, *, *, *>?>
}