package com.arbr.og.object_model.common.model.collections

import com.arbr.content_formats.json_schematized.JsonSchema
import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.base.Scalar
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.SumTensor
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor
import com.arbr.platform.object_graph.common.ObjectModel
import com.arbr.og.object_model.common.values.SourcedValueGeneratorInfo
import com.arbr.og.object_model.common.values.SourcedValueKind
import com.arbr.og.object_model.common.values.collections.SourcedStruct2
import com.arbr.platform.object_graph.common.ObjectListTensorFactory
import com.arbr.platform.object_graph.util.JsonSchemaUtils
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

class NestedObjectListType2<V1, V2, QT1 : Shape, QF1 : Shape, QT2 : Shape, QF2 : Shape, S1 : ObjectModel.ObjectValue<V1, QT1, QF1, S1>, S2 : ObjectModel.ObjectValue<V2, QT2, QF2, S2>>(
    key: String,
    description: String,
    val innerType1: ObjectModel.ObjectType<V1, QT1, QF1, S1>,
    val innerType2: ObjectModel.ObjectType<V2, QT2, QF2, S2>,


    ) : NestedObjectListType<List<NestedObjectListType2.InnerValue<V1, V2>>,
        Shape.Product<Dim.VariableC, Shape.Sum<QT1, QT2>>,
        Shape.Product<Dim.VariableC, Shape.Sum<QF1, QF2>>,
        NestedObjectListType2.Value<V1, V2, QT1, QF1, QT2, QF2, S1, S2>> {
    override val typeName: String
        get() = "[{${innerType1.typeName},${innerType2.typeName},}]"

    override val embeddingKey: String? = null
    override val jsonSchema: JsonSchema = JsonSchema(
        "object",
        description = null,
        enum = null,
        items = null,
        properties = linkedMapOf<String, JsonSchema>().also {
            it[key] = JsonSchemaUtils.mergedArrayJsonSchema(
                description,
                innerType1,
                innerType2,

                )
        },
        required = listOf(key),
    )

    override val tensor: Tensor<
            Shape.Product<Dim.VariableC, Shape.Sum<QT1, QT2>>,
            GroundField.Real, Scalar
            > = ObjectListTensorFactory.of(SumTensor.of(innerType1.tensor, innerType2.tensor))

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
    data class InnerValue<V1, V2>(
        private val objectType: NestedObjectListType2<V1, V2, *, *, *, *, *, *>,
        val t1: V1,
        val t2: V2,

        ) {

        @JsonValue
        fun jsonSerialized() = linkedMapOf(

            objectType.innerType1.jsonSchema.properties!!.entries.first().key to t1,
            objectType.innerType2.jsonSchema.properties!!.entries.first().key to t2,
        )
    }

    data class Value<V1, V2, QT1 : Shape, QF1 : Shape, QT2 : Shape, QF2 : Shape, S1 : ObjectModel.ObjectValue<V1, QT1, QF1, S1>, S2 : ObjectModel.ObjectValue<V2, QT2, QF2, S2>>(
        override val objectType: NestedObjectListType2<V1, V2, QT1, QF1, QT2, QF2, S1, S2>,
        override val kind: SourcedValueKind,
        override val value: List<InnerValue<V1, V2>>,
        override val generatorInfo: SourcedValueGeneratorInfo,
    ) : ObjectModel.ObjectValueImpl<List<InnerValue<V1, V2>>,
            Shape.Product<Dim.VariableC, Shape.Sum<QT1, QT2>>,
            Shape.Product<Dim.VariableC, Shape.Sum<QF1, QF2>>,
            Value<V1, V2, QT1, QF1, QT2, QF2, S1, S2>>(
        kind,
        value,
        objectType,
        generatorInfo,
    ) {

        val containers: List<SourcedStruct2<S1, S2>> by lazy {
            value.map {
                SourcedStruct2(
                    objectType.innerType1.initialize(
                        kind,
                        it.t1,
                        generatorInfo,
                    ),
                    objectType.innerType2.initialize(
                        kind,
                        it.t2,
                        generatorInfo,
                    ),

                    )
            }
        }
    }

    override fun parseInnerValueFromObject(value: Any?): List<InnerValue<V1, V2>> {
        val valueList = value as List<*>


        val key1 = innerType1.jsonSchema.properties!!.entries.first().key
        val key2 = innerType2.jsonSchema.properties!!.entries.first().key

        return valueList.map {
            val elementMap = it as Map<*, *>


            val v1 = elementMap[key1]
            if (JsonSchemaUtils.requires(innerType1.jsonSchema, key1) && v1 == null) {
                throw NullPointerException()
            }
            val v2 = elementMap[key2]
            if (JsonSchemaUtils.requires(innerType2.jsonSchema, key2) && v2 == null) {
                throw NullPointerException()
            }

            InnerValue(
                this,

                innerType1.parseInnerValueFromObject(v1),
                innerType2.parseInnerValueFromObject(v2),
            )
        }
    }

    fun initializeMerged(
        containers: List<SourcedStruct2<S1, S2>>,
    ): Value<V1, V2, QT1, QF1, QT2, QF2, S1, S2> = initialize(
        containers.flatMap {
            listOf(
                it.t1.kind,
                it.t2.kind,


                )
        }.maxOrNull() ?: SourcedValueKind.CONSTANT,
        containers.map {
            InnerValue(
                this,
                it.t1.value,
                it.t2.value,

                )
        },
        SourcedValueGeneratorInfo(
            containers.flatMap {
                listOf(
                    it.t1.generatorInfo.generators,
                    it.t2.generatorInfo.generators,


                    ).flatten()
            }.distinctBy { it.completionCacheKey ?: it.operationId }
        )
    )

    override fun initialize(
        kind: SourcedValueKind,
        value: List<InnerValue<V1, V2>>,
        sourcedValueGeneratorInfo: SourcedValueGeneratorInfo
    ): Value<V1, V2, QT1, QF1, QT2, QF2, S1, S2> = Value(this, kind, value, sourcedValueGeneratorInfo)
}