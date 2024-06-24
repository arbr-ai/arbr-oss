@file:Suppress("MemberVisibilityCanBePrivate")

package com.arbr.platform.object_graph.common

import com.arbr.content_formats.json_schematized.JsonSchema
import com.arbr.content_formats.mapper.Mappers
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.og.object_model.common.properties.DependencyTracingValueProvider
import com.arbr.og.object_model.common.values.SourcedValue
import com.arbr.og.object_model.common.values.SourcedValueGeneratorInfo
import com.arbr.og.object_model.common.values.SourcedValueImpl
import com.arbr.og.object_model.common.values.SourcedValueKind
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.ObjectMapper


interface ObjectModel {

    /**
     * Representation of the type of a value in the Object Model.
     */
    interface ObjectType<ValueType, ST : Shape, SF : Shape, SourcedValueType : ObjectValue<ValueType, ST, SF, SourcedValueType>> :
        ObjectTensorType<ValueType, ST> {
        val typeName: String
            get() {
                return this::class.simpleName!!
            }

        val mapper: ObjectMapper
            get() = Mappers.mapper

        /**
         * For embedded properties, a key delineating which other embedded values are comparable.
         * In practice, the fully qualified name of the associated DB field for single-field types.
         * For compound types, some identifier uniquely associated with the type.
         */
        val embeddingKey: String?

        /**
         * JSON schema of backing DB object.
         */
        val jsonSchema: JsonSchema

        @Suppress("UNCHECKED_CAST")
        fun parseInnerValueFromObject(
            value: Any?,
        ) = if (value != null && value is Int) {
            // Hack for longs being serialized to integers in JSON cache storage
            value.toLong() as ValueType
        } else {
            value as ValueType
        }

        fun parseFromObject(
            kind: SourcedValueKind,
            value: Any?,
            sourcedValueGeneratorInfo: SourcedValueGeneratorInfo,
        ) = initialize(
            kind,
            parseInnerValueFromObject(value),
            sourcedValueGeneratorInfo,
        )

        fun initialize(
            kind: SourcedValueKind,
            value: ValueType,
            sourcedValueGeneratorInfo: SourcedValueGeneratorInfo,
        ): SourcedValueType

        fun trace(
            kind: SourcedValueKind,
            sourcedValueGeneratorInfo: SourcedValueGeneratorInfo,
            tracingValueProvider: DependencyTracingValueProvider<ValueType>,
        ): SourcedValueType

        fun constant(value: ValueType): SourcedValueType = initialize(
            SourcedValueKind.CONSTANT, value, SourcedValueGeneratorInfo(
                emptyList()
            )
        )

        fun materialized(value: ValueType): SourcedValueType = initialize(
            SourcedValueKind.MATERIALIZED, value, SourcedValueGeneratorInfo(
                emptyList()
            )
        )

        fun input(value: ValueType): SourcedValueType = initialize(
            SourcedValueKind.INPUT, value, SourcedValueGeneratorInfo(
                emptyList()
            )
        )

        fun generated(value: ValueType, sourcedValueGeneratorInfo: SourcedValueGeneratorInfo): SourcedValueType =
            initialize(
                SourcedValueKind.GENERATED,
                value,
                sourcedValueGeneratorInfo
            )

        fun computed(value: ValueType, sourcedValueGeneratorInfo: SourcedValueGeneratorInfo): SourcedValueType =
            initialize(
                SourcedValueKind.COMPUTED,
                value,
                sourcedValueGeneratorInfo
            )
    }

    interface ObjectValue<ValueType, ST : Shape, SF : Shape,
            OV : ObjectValue<ValueType, ST, SF, OV>> : SourcedValue<ValueType> {
        val objectType: ObjectType<ValueType, ST, SF, OV>

        override val typeName: String get() = objectType.typeName

        override val schema: JsonSchema get() = objectType.jsonSchema

        /**
         * Map to a value of a compatible type.
         */
        fun <W, U : ObjectValue<W, *, *, U>> map(
            type: ObjectType<W, *, *, U>,
            f: (ValueType) -> W,
        ): U {
            // Default: Use the concrete initializer
            // Overridden by Trace value classes
            return type.initialize(kind, f(value), generatorInfo)
        }

        /**
         * Map the underlying value within the same object type.
         */
        fun map(f: (ValueType) -> ValueType): OV {
            return map(
                objectType,
                f
            )
        }

        fun valueEquals(
            other: SourcedValue<ValueType>,
        ): SourcedValue<Boolean> {
            return zip(other)
                .mapValue { (thisValue, otherValue) ->
                    thisValue == otherValue
                }
        }

        @Deprecated(
            "Remove", ReplaceWith(
                "ObjectValueEquatable(this)",
                "com.arbr.og.object_model.common.ObjectValueEquatable",
                "com.arbr.og.object_model.common.ObjectModel.ObjectValue"
            )
        )
        fun simpleEquatableValue(): com.arbr.platform.object_graph.common.ObjectValueEquatable<ValueType> =
            com.arbr.platform.object_graph.common.ObjectValueEquatable(this)

        private fun booleanSourcedValueImpl(value: Boolean): SourcedValueImpl<Boolean> {
            return SourcedValueImpl(
                SourcedValueKind.COMPUTED,
                value,
                "Boolean",
                JsonSchema("boolean", null, null, null, null, null),
                SourcedValueGeneratorInfo(emptyList()),
            )
        }
    }

    /**
     * Representation of a concrete value in the Object Model.
     */
    abstract class ObjectValueImpl<ValueType, ST : Shape, SF : Shape, OV : ObjectValue<ValueType, ST, SF, OV>>(
        kind: SourcedValueKind,
        override val value: ValueType,
        @JsonIgnore
        override val objectType: ObjectType<ValueType, ST, SF, OV>,
        generatorInfo: SourcedValueGeneratorInfo,
    ) : SourcedValueImpl<ValueType>(
        kind,
        value,
        objectType.typeName,
        objectType.jsonSchema,
        generatorInfo,
    ), ObjectValue<ValueType, ST, SF, OV> {
        override val typeName: String get() = objectType.typeName

        override val schema: JsonSchema get() = objectType.jsonSchema

        @JsonValue
        fun toJson() = value
    }
}
