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
import com.arbr.og.object_model.common.values.collections.SourcedStruct4
import com.arbr.platform.object_graph.util.JsonSchemaUtils
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

class NestedObjectListType4<V1, V2, V3, V4, QT1 : Shape, QF1 : Shape, QT2 : Shape, QF2 : Shape, QT3 : Shape, QF3 : Shape, QT4 : Shape, QF4 : Shape, S1 : ObjectModel.ObjectValue<V1, QT1, QF1, S1>, S2 : ObjectModel.ObjectValue<V2, QT2, QF2, S2>, S3 : ObjectModel.ObjectValue<V3, QT3, QF3, S3>, S4 : ObjectModel.ObjectValue<V4, QT4, QF4, S4>>(
    key: String,
    description: String,
    val innerType1: ObjectModel.ObjectType<V1, QT1, QF1, S1>,
    val innerType2: ObjectModel.ObjectType<V2, QT2, QF2, S2>,
    val innerType3: ObjectModel.ObjectType<V3, QT3, QF3, S3>,
    val innerType4: ObjectModel.ObjectType<V4, QT4, QF4, S4>,


    ) : NestedObjectListType<List<NestedObjectListType4.InnerValue<V1, V2, V3, V4>>,
        Shape.Product<Dim.VariableC, Shape.Sum<QT1, Shape.Sum<QT2, Shape.Sum<QT3, QT4>>>>,
        Shape.Product<Dim.VariableC, Shape.Sum<QF1, Shape.Sum<QF2, Shape.Sum<QF3, QF4>>>>,
        NestedObjectListType4.Value<V1, V2, V3, V4, QT1, QF1, QT2, QF2, QT3, QF3, QT4, QF4, S1, S2, S3, S4>> {
    override val typeName: String
        get() = "[{${innerType1.typeName},${innerType2.typeName},${innerType3.typeName},${innerType4.typeName},}]"

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

                )
        },
        required = listOf(key),
    )

    override val tensor: Tensor<
            Shape.Product<Dim.VariableC, Shape.Sum<QT1, Shape.Sum<QT2, Shape.Sum<QT3, QT4>>>>,
            GroundField.Real, Scalar
            > = ObjectListTensorFactory.of(
        SumTensor.of(
            innerType1.tensor,
            SumTensor.of(innerType2.tensor, SumTensor.of(innerType3.tensor, innerType4.tensor))
        )
    )

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
    data class InnerValue<V1, V2, V3, V4>(
        private val objectType: NestedObjectListType4<V1, V2, V3, V4, *, *, *, *, *, *, *, *, *, *, *, *>,
        val t1: V1,
        val t2: V2,
        val t3: V3,
        val t4: V4,

        ) {

        @JsonValue
        fun jsonSerialized() = linkedMapOf(

            objectType.innerType1.jsonSchema.properties!!.entries.first().key to t1,
            objectType.innerType2.jsonSchema.properties!!.entries.first().key to t2,
            objectType.innerType3.jsonSchema.properties!!.entries.first().key to t3,
            objectType.innerType4.jsonSchema.properties!!.entries.first().key to t4,
        )
    }

    data class Value<V1, V2, V3, V4, QT1 : Shape, QF1 : Shape, QT2 : Shape, QF2 : Shape, QT3 : Shape, QF3 : Shape, QT4 : Shape, QF4 : Shape, S1 : ObjectModel.ObjectValue<V1, QT1, QF1, S1>, S2 : ObjectModel.ObjectValue<V2, QT2, QF2, S2>, S3 : ObjectModel.ObjectValue<V3, QT3, QF3, S3>, S4 : ObjectModel.ObjectValue<V4, QT4, QF4, S4>>(
        override val objectType: NestedObjectListType4<V1, V2, V3, V4, QT1, QF1, QT2, QF2, QT3, QF3, QT4, QF4, S1, S2, S3, S4>,
        override val kind: SourcedValueKind,
        override val value: List<InnerValue<V1, V2, V3, V4>>,
        override val generatorInfo: SourcedValueGeneratorInfo,
    ) : ObjectModel.ObjectValueImpl<List<InnerValue<V1, V2, V3, V4>>,
            Shape.Product<Dim.VariableC, Shape.Sum<QT1, Shape.Sum<QT2, Shape.Sum<QT3, QT4>>>>,
            Shape.Product<Dim.VariableC, Shape.Sum<QF1, Shape.Sum<QF2, Shape.Sum<QF3, QF4>>>>,
            Value<V1, V2, V3, V4, QT1, QF1, QT2, QF2, QT3, QF3, QT4, QF4, S1, S2, S3, S4>>(
        kind,
        value,
        objectType,
        generatorInfo,
    ) {

        val containers: List<SourcedStruct4<S1, S2, S3, S4>> by lazy {
            value.map {
                SourcedStruct4(
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

                    )
            }
        }
    }

    override fun parseInnerValueFromObject(value: Any?): List<InnerValue<V1, V2, V3, V4>> {
        val valueList = value as List<*>


        val key1 = innerType1.jsonSchema.properties!!.entries.first().key
        val key2 = innerType2.jsonSchema.properties!!.entries.first().key
        val key3 = innerType3.jsonSchema.properties!!.entries.first().key
        val key4 = innerType4.jsonSchema.properties!!.entries.first().key

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

            InnerValue(
                this,

                innerType1.parseInnerValueFromObject(v1),
                innerType2.parseInnerValueFromObject(v2),
                innerType3.parseInnerValueFromObject(v3),
                innerType4.parseInnerValueFromObject(v4),
            )
        }
    }

    fun initializeMerged(
        containers: List<SourcedStruct4<S1, S2, S3, S4>>,
    ): Value<V1, V2, V3, V4, QT1, QF1, QT2, QF2, QT3, QF3, QT4, QF4, S1, S2, S3, S4> = initialize(
        containers.flatMap {
            listOf(
                it.t1.kind,
                it.t2.kind,
                it.t3.kind,
                it.t4.kind,


                )
        }.maxOrNull() ?: SourcedValueKind.CONSTANT,
        containers.map {
            InnerValue(
                this,
                it.t1.value,
                it.t2.value,
                it.t3.value,
                it.t4.value,

                )
        },
        SourcedValueGeneratorInfo(
            containers.flatMap {
                listOf(
                    it.t1.generatorInfo.generators,
                    it.t2.generatorInfo.generators,
                    it.t3.generatorInfo.generators,
                    it.t4.generatorInfo.generators,


                    ).flatten()
            }.distinctBy { it.completionCacheKey ?: it.operationId }
        )
    )

    override fun initialize(
        kind: SourcedValueKind,
        value: List<InnerValue<V1, V2, V3, V4>>,
        sourcedValueGeneratorInfo: SourcedValueGeneratorInfo
    ): Value<V1, V2, V3, V4, QT1, QF1, QT2, QF2, QT3, QF3, QT4, QF4, S1, S2, S3, S4> =
        Value(this, kind, value, sourcedValueGeneratorInfo)


}