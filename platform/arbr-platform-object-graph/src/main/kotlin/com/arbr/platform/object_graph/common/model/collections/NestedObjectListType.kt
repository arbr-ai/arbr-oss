package com.arbr.og.object_model.common.model.collections

import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.object_graph.common.ObjectModel
import com.arbr.og.object_model.common.properties.DependencyTracingValueProvider
import com.arbr.og.object_model.common.values.SourcedValueGeneratorInfo
import com.arbr.og.object_model.common.values.SourcedValueKind

interface NestedObjectListType<
        LV,
        QT : Shape,
        QF : Shape,
        LVT : ObjectModel.ObjectValue<LV, QT, QF, LVT>,
        > : ObjectModel.ObjectType<
        LV,
        QT,
        QF,
        LVT
        > {

    override fun trace(
        kind: SourcedValueKind,
        sourcedValueGeneratorInfo: SourcedValueGeneratorInfo,
        tracingValueProvider: DependencyTracingValueProvider<LV>
    ): LVT = initialize(
        kind,
        tracingValueProvider.provideValue(),
        sourcedValueGeneratorInfo,
    )
}
