package com.arbr.og.object_model.common.values

import com.arbr.og.object_model.common.values.collections.SourcedStruct

interface InputElement<S: SourcedStruct> {
    val sourcedStruct: S
}

data class InputStruct<S: SourcedStruct>(
    override val sourcedStruct: S,
): InputElement<S>
