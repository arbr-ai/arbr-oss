package com.arbr.og.object_model.common.model.collections

import com.arbr.content_formats.json_schematized.JsonSchema
import com.arbr.platform.ml.linear.typed.base.GroundField
import com.arbr.platform.ml.linear.typed.base.Scalar
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.shape.Shape
import com.arbr.platform.ml.linear.typed.tensor.base.Tensor
import com.arbr.platform.object_graph.common.ObjectListTensorFactory
import com.arbr.platform.object_graph.common.ObjectModel
import com.arbr.og.object_model.common.values.SourcedValueGeneratorInfo
import com.arbr.og.object_model.common.values.SourcedValueKind
import com.arbr.og.object_model.common.values.collections.SourcedStruct1
import com.arbr.platform.object_graph.util.JsonSchemaUtils
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

class NestedObjectListType1<V1, QT1 : Shape, QF1 : Shape, S1 : ObjectModel.ObjectValue<V1, QT1, QF1, S1>>(
    key: String,
    description: String,
    val innerType1: ObjectModel.ObjectType<V1, QT1, QF1, S1>,


    ) : NestedObjectListType<List<NestedObjectListType1.InnerValue<V1>>,
        Shape.Product<Dim.VariableC, QT1>,
        Shape.Product<Dim.VariableC, QF1>,
        NestedObjectListType1.Value<V1, QT1, QF1, S1>> {
    override val typeName: String
        get() = "[{${innerType1.typeName},}]"

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

                )
        },
        required = listOf(key),
    )

    override val tensor: Tensor<
            Shape.Product<Dim.VariableC, QT1>,
            GroundField.Real, Scalar
            > = ObjectListTensorFactory.of(innerType1.tensor)

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
    data class InnerValue<V1>(
        private val objectType: NestedObjectListType1<V1, *, *, *>,
        val t1: V1,

        ) {

        @JsonValue
        fun jsonSerialized() = linkedMapOf(

            objectType.innerType1.jsonSchema.properties!!.entries.first().key to t1,
        )
    }

    data class Value<V1, QT1 : Shape, QF1 : Shape, S1 : ObjectModel.ObjectValue<V1, QT1, QF1, S1>>(
        override val objectType: NestedObjectListType1<V1, QT1, QF1, S1>,
        override val kind: SourcedValueKind,
        override val value: List<InnerValue<V1>>,
        override val generatorInfo: SourcedValueGeneratorInfo,
    ) : ObjectModel.ObjectValueImpl<List<InnerValue<V1>>,
            Shape.Product<Dim.VariableC, QT1>,
            Shape.Product<Dim.VariableC, QF1>,
            Value<V1, QT1, QF1, S1>>(
        kind,
        value,
        objectType,
        generatorInfo,
    ) {

        val containers: List<SourcedStruct1<S1>> by lazy {
            value.map {
                SourcedStruct1(
                    objectType.innerType1.initialize(
                        kind,
                        it.t1,
                        generatorInfo,
                    ),

                    )
            }
        }
    }

    override fun parseInnerValueFromObject(value: Any?): List<InnerValue<V1>> {
        val valueList = value as List<*>


        val key1 = innerType1.jsonSchema.properties!!.entries.first().key

        return valueList.map {
            val elementMap = it as Map<*, *>


            val v1 = elementMap[key1]
            if (JsonSchemaUtils.requires(innerType1.jsonSchema, key1) && v1 == null) {
                throw NullPointerException()
            }

            InnerValue(
                this,

                innerType1.parseInnerValueFromObject(v1),
            )
        }
    }

    fun initializeMerged(
        containers: List<SourcedStruct1<S1>>,
    ): Value<V1, QT1, QF1, S1> = initialize(
        containers.flatMap {
            listOf(
                it.t1.kind,


                )
        }.maxOrNull() ?: SourcedValueKind.CONSTANT,
        containers.map {
            InnerValue(
                this,
                it.t1.value,

                )
        },
        SourcedValueGeneratorInfo(
            containers.flatMap {
                listOf(
                    it.t1.generatorInfo.generators,


                    ).flatten()
            }.distinctBy { it.completionCacheKey ?: it.operationId }
        )
    )

    override fun initialize(
        kind: SourcedValueKind,
        value: List<InnerValue<V1>>,
        sourcedValueGeneratorInfo: SourcedValueGeneratorInfo
    ): Value<V1, QT1, QF1, S1> = Value(this, kind, value, sourcedValueGeneratorInfo)
}