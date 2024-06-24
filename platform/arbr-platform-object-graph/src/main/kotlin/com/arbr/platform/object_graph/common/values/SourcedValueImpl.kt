package com.arbr.og.object_model.common.values

import com.arbr.content_formats.json_schematized.JsonSchema
import com.arbr.og.object_model.common.properties.DependencyTracingValueProvider
import java.util.*

open class SourcedValueImpl<T>(
    override val kind: SourcedValueKind,
    override val value: T,
    override val typeName: String,
    override val schema: JsonSchema,
    override val generatorInfo: SourcedValueGeneratorInfo,
): SourcedValue<T> {

    override val id: String = UUID.randomUUID().toString()
}

open class TracedSourcedValueImpl<T>(
    override val kind: SourcedValueKind,
    private val tracingValueProvider: DependencyTracingValueProvider<T>,
    override val typeName: String,
    override val schema: JsonSchema,
    override val generatorInfo: SourcedValueGeneratorInfo,
): SourcedValue<T> {
    override val value: T by tracingValueProvider.providingValue()

    override val id: String = UUID.randomUUID().toString()
}

open class SourcedValueLazyImpl<T>(
    override val kind: SourcedValueKind,
    override val typeName: String,
    override val schema: JsonSchema,
    override val generatorInfo: SourcedValueGeneratorInfo,
    private val getValue: () -> T,
): SourcedValue<T> {

    override val id: String = UUID.randomUUID().toString()

    override val value: T by lazy {
        getValue()
    }
}
