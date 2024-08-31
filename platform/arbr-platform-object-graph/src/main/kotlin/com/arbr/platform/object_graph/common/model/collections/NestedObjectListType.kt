package com.arbr.platform.object_graph.common.model.collections

import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.object_graph.common.ObjectModel
import com.arbr.platform.object_graph.common.properties.DependencyTracingValueProvider
import com.arbr.platform.object_graph.common.values.SourcedValueGeneratorInfo
import com.arbr.platform.object_graph.common.values.SourcedValueKind

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
