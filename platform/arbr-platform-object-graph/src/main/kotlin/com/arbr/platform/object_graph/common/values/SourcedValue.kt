package com.arbr.og.object_model.common.values

import com.arbr.content_formats.json_schematized.JsonSchema
import com.arbr.og.object_model.common.values.collections.SourcedStruct1

interface SourcedValue<T> : InputElement<SourcedStruct1<SourcedValue<T>>> {
    val id: String
    val kind: SourcedValueKind
    val value: T
    val typeName: String
    val schema: JsonSchema
    val generatorInfo: SourcedValueGeneratorInfo

    override val sourcedStruct get() = SourcedStruct1(this)

    fun <W> mapValue(
        f: (T) -> W,
    ): SourcedValue<W> {
        return SourcedValueLazyImpl(
            kind,
            typeName,
            schema,
            generatorInfo,
        ) {
            f(value)
        }
    }

    fun <U> zip(
        other: SourcedValue<U>,
    ): SourcedValue<Pair<T, U>> {
        return SourcedValueLazyImpl(
            SourcedValueKind.COMPUTED,
            "Tuple[${typeName}, ${other.typeName}]",
            JsonSchema(
                "object", null, null, null,
                linkedMapOf(
                    "t0" to schema,
                    "t1" to other.schema,
                ),
                listOf("t0", "t1"),
            ),
            generatorInfo.copy(generators = generatorInfo.generators + other.generatorInfo.generators),
        ) {
            this.value to other.value
        }
    }
}
