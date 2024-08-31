package com.arbr.platform.object_graph.common.values

import com.arbr.platform.object_graph.common.values.collections.SourcedStruct

interface InputElement<S: SourcedStruct> {
    val sourcedStruct: S
}

data class InputStruct<S: SourcedStruct>(
    override val sourcedStruct: S,
): InputElement<S>
