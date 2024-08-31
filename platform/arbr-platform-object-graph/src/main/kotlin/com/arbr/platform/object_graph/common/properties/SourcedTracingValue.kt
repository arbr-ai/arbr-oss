package com.arbr.platform.object_graph.common.properties

import com.arbr.content_formats.json_schematized.JsonSchema
import com.arbr.platform.object_graph.common.values.SourcedValue
import com.arbr.platform.object_graph.common.values.SourcedValueGeneratorInfo
import com.arbr.platform.object_graph.common.values.SourcedValueKind

class SourcedTracingValue<T>(
    override val id: String,
    override val kind: SourcedValueKind,
    val tracingValueProvider: DependencyTracingValueProvider<T>,
    override val typeName: String,
    override val schema: JsonSchema,
    override val generatorInfo: SourcedValueGeneratorInfo,
): SourcedValue<T> {
    override val value by tracingValueProvider.providingValue()
}