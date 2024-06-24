package com.arbr.og.object_model.common.properties

import com.arbr.content_formats.json_schematized.JsonSchema
import com.arbr.og.object_model.common.values.SourcedValue
import com.arbr.og.object_model.common.values.SourcedValueGeneratorInfo
import com.arbr.og.object_model.common.values.SourcedValueKind

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