package com.arbr.platform.object_graph.alignable

import com.arbr.platform.alignable.alignable.SwapAlignable
import com.arbr.platform.alignable.alignable.collections.KeyValueAlignmentOperation
import com.arbr.platform.alignable.alignable.collections.MapAlignmentOperation
import com.arbr.platform.object_graph.common.ObjectValueEquatable

data class PartialOperation(
    val uuid: String?,
    val typeName: String?,
    val parentUuids: KeyValueAlignmentOperation<ForeignAlignmentKey, PartialRefAlignable, String?>?,
    val childContainerUuids: KeyValueAlignmentOperation<ForeignAlignmentKey, SwapAlignable<ForeignAlignmentKey>, ForeignAlignmentKey>?,
    val properties: MapAlignmentOperation<SwapAlignable<ObjectValueEquatable<*>>, ObjectValueEquatable<*>>?,
)