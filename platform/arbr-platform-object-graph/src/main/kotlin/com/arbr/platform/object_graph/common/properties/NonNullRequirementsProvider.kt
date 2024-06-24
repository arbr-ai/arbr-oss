package com.arbr.og.object_model.common.properties

import com.arbr.platform.object_graph.common.ObjectModel
import com.arbr.og.object_model.common.values.SourcedValue

interface NonNullRequirementsProvider {
    fun <ValueType : Any, T : ObjectModel.ObjectValue<ValueType?, *, *, T>> getNonNullValue(
        objectValue: ObjectModel.ObjectValue<ValueType?, *, *, T>,
    ): SourcedValue<ValueType>

    fun <V : Any, T : ObjectModel.ObjectValue<V?, *, *, T>> T.nonnull(): SourcedValue<V> {
        return getNonNullValue(this)
    }
}
