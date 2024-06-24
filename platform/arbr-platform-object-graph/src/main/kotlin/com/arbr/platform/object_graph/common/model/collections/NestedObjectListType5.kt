package com.arbr.og.object_model.common.model.collections

import com.arbr.content_formats.json_schematized.JsonSchema
import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.base.Scalar
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.SumTensor
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor
import com.arbr.platform.object_graph.common.ObjectListTensorFactory
import com.arbr.platform.object_graph.common.ObjectModel
import com.arbr.og.object_model.common.values.SourcedValueGeneratorInfo
import com.arbr.og.object_model.common.values.SourcedValueKind
import com.arbr.og.object_model.common.values.collections.SourcedStruct5
import com.arbr.platform.object_graph.util.JsonSchemaUtils
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

class NestedObjectListType5<V1, V2, V3, V4, V5, QT1 : Shape, QF1 : Shape, QT2 : Shape, QF2 : Shape, QT3 : Shape, QF3 : Shape, QT4 : Shape, QF4 : Shape, QT5 : Shape, QF5 : Shape, S1 : ObjectModel.ObjectValue<V1, QT1, QF1, S1>, S2 : ObjectModel.ObjectValue<V2, QT2, QF2, S2>, S3 : ObjectModel.ObjectValue<V3, QT3, QF3, S3>, S4 : ObjectModel.ObjectValue<V4, QT4, QF4, S4>, S5 : ObjectModel.ObjectValue<V5, QT5, QF5, S5>>(
    key: String,
    description: String,
    val innerType1: ObjectModel.ObjectType<V1, QT1, QF1, S1>,
    val innerType2: ObjectModel.ObjectType<V2, QT2, QF2, S2>,
    val innerType3: ObjectModel.ObjectType<V3, QT3, QF3, S3>,
    val innerType4: ObjectModel.ObjectType<V4, QT4, QF4, S4>,
    val innerType5: ObjectModel.ObjectType<V5, QT5, QF5, S5>,


    ) : NestedObjectListType<List<NestedObjectListType5.InnerValue<V1, V2, V3, V4, V5>>,
        Shape.Product<Dim.VariableC, Shape.Sum<QT1, Shape.Sum<QT2, Shape.Sum<QT3, Shape.Sum<QT4, QT5>>>>>,
        Shape.Product<Dim.VariableC, Shape.Sum<QF1, Shape.Sum<QF2, Shape.Sum<QF3, Shape.Sum<QF4, QF5>>>>>,
        NestedObjectListType5.Value<V1, V2, V3, V4, V5, QT1, QF1, QT2, QF2, QT3, QF3, QT4, QF4, QT5, QF5, S1, S2, S3, S4, S5>> {
    override val typeName: String
        get() = "[{${innerType1.typeName},${innerType2.typeName},${innerType3.typeName},${innerType4.typeName},${innerType5.typeName},}]"

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
                innerType3,
                innerType4,
                innerType5,

                )
        },
        required = listOf(key),
    )

    override val tensor: Tensor<
            Shape.Product<Dim.VariableC, Shape.Sum<QT1, Shape.Sum<QT2, Shape.Sum<QT3, Shape.Sum<QT4, QT5>>>>>,
            GroundField.Real, Scalar
            > = ObjectListTensorFactory.of(
        SumTensor.of(
            innerType1.tensor,
            SumTensor.of(
                innerType2.tensor,
                SumTensor.of(innerType3.tensor, SumTensor.of(innerType4.tensor, innerType5.tensor))
            )
        )
    )

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
    data class InnerValue<V1, V2, V3, V4, V5>(
        private val objectType: NestedObjectListType5<V1, V2, V3, V4, V5, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *>,
        val t1: V1,
        val t2: V2,
        val t3: V3,
        val t4: V4,
        val t5: V5,

        ) {

        @JsonValue
        fun jsonSerialized() = linkedMapOf(

            objectType.innerType1.jsonSchema.properties!!.entries.first().key to t1,
            objectType.innerType2.jsonSchema.properties!!.entries.first().key to t2,
            objectType.innerType3.jsonSchema.properties!!.entries.first().key to t3,
            objectType.innerType4.jsonSchema.properties!!.entries.first().key to t4,
            objectType.innerType5.jsonSchema.properties!!.entries.first().key to t5,
        )
    }

    data class Value<V1, V2, V3, V4, V5, QT1 : Shape, QF1 : Shape, QT2 : Shape, QF2 : Shape, QT3 : Shape, QF3 : Shape, QT4 : Shape, QF4 : Shape, QT5 : Shape, QF5 : Shape, S1 : ObjectModel.ObjectValue<V1, QT1, QF1, S1>, S2 : ObjectModel.ObjectValue<V2, QT2, QF2, S2>, S3 : ObjectModel.ObjectValue<V3, QT3, QF3, S3>, S4 : ObjectModel.ObjectValue<V4, QT4, QF4, S4>, S5 : ObjectModel.ObjectValue<V5, QT5, QF5, S5>>(
        override val objectType: NestedObjectListType5<V1, V2, V3, V4, V5, QT1, QF1, QT2, QF2, QT3, QF3, QT4, QF4, QT5, QF5, S1, S2, S3, S4, S5>,
        override val kind: SourcedValueKind,
        override val value: List<InnerValue<V1, V2, V3, V4, V5>>,
        override val generatorInfo: SourcedValueGeneratorInfo,
    ) : ObjectModel.ObjectValueImpl<List<InnerValue<V1, V2, V3, V4, V5>>,
            Shape.Product<Dim.VariableC, Shape.Sum<QT1, Shape.Sum<QT2, Shape.Sum<QT3, Shape.Sum<QT4, QT5>>>>>,
            Shape.Product<Dim.VariableC, Shape.Sum<QF1, Shape.Sum<QF2, Shape.Sum<QF3, Shape.Sum<QF4, QF5>>>>>,
            Value<V1, V2, V3, V4, V5, QT1, QF1, QT2, QF2, QT3, QF3, QT4, QF4, QT5, QF5, S1, S2, S3, S4, S5>>(
        kind,
        value,
        objectType,
        generatorInfo,
    ) {

        val containers: List<SourcedStruct5<S1, S2, S3, S4, S5>> by lazy {
            value.map {
                SourcedStruct5(
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
                    objectType.innerType3.initialize(
                        kind,
                        it.t3,
                        generatorInfo,
                    ),
                    objectType.innerType4.initialize(
                        kind,
                        it.t4,
                        generatorInfo,
                    ),
                    objectType.innerType5.initialize(
                        kind,
                        it.t5,
                        generatorInfo,
                    ),

                    )
            }
        }
    }

    override fun parseInnerValueFromObject(value: Any?): List<InnerValue<V1, V2, V3, V4, V5>> {
        val valueList = value as List<*>


        val key1 = innerType1.jsonSchema.properties!!.entries.first().key
        val key2 = innerType2.jsonSchema.properties!!.entries.first().key
        val key3 = innerType3.jsonSchema.properties!!.entries.first().key
        val key4 = innerType4.jsonSchema.properties!!.entries.first().key
        val key5 = innerType5.jsonSchema.properties!!.entries.first().key

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
            val v3 = elementMap[key3]
            if (JsonSchemaUtils.requires(innerType3.jsonSchema, key3) && v3 == null) {
                throw NullPointerException()
            }
            val v4 = elementMap[key4]
            if (JsonSchemaUtils.requires(innerType4.jsonSchema, key4) && v4 == null) {
                throw NullPointerException()
            }
            val v5 = elementMap[key5]
            if (JsonSchemaUtils.requires(innerType5.jsonSchema, key5) && v5 == null) {
                throw NullPointerException()
            }

            InnerValue(
                this,

                innerType1.parseInnerValueFromObject(v1),
                innerType2.parseInnerValueFromObject(v2),
                innerType3.parseInnerValueFromObject(v3),
                innerType4.parseInnerValueFromObject(v4),
                innerType5.parseInnerValueFromObject(v5),
            )
        }
    }

    fun initializeMerged(
        containers: List<SourcedStruct5<S1, S2, S3, S4, S5>>,
    ): Value<V1, V2, V3, V4, V5, QT1, QF1, QT2, QF2, QT3, QF3, QT4, QF4, QT5, QF5, S1, S2, S3, S4, S5> = initialize(
        containers.flatMap {
            listOf(
                it.t1.kind,
                it.t2.kind,
                it.t3.kind,
                it.t4.kind,
                it.t5.kind,


                )
        }.maxOrNull() ?: SourcedValueKind.CONSTANT,
        containers.map {
            InnerValue(
                this,
                it.t1.value,
                it.t2.value,
                it.t3.value,
                it.t4.value,
                it.t5.value,

                )
        },
        SourcedValueGeneratorInfo(
            containers.flatMap {
                listOf(
                    it.t1.generatorInfo.generators,
                    it.t2.generatorInfo.generators,
                    it.t3.generatorInfo.generators,
                    it.t4.generatorInfo.generators,
                    it.t5.generatorInfo.generators,


                    ).flatten()
            }.distinctBy { it.completionCacheKey ?: it.operationId }
        )
    )

    override fun initialize(
        kind: SourcedValueKind,
        value: List<InnerValue<V1, V2, V3, V4, V5>>,
        sourcedValueGeneratorInfo: SourcedValueGeneratorInfo
    ): Value<V1, V2, V3, V4, V5, QT1, QF1, QT2, QF2, QT3, QF3, QT4, QF4, QT5, QF5, S1, S2, S3, S4, S5> =
        Value(this, kind, value, sourcedValueGeneratorInfo)
}
