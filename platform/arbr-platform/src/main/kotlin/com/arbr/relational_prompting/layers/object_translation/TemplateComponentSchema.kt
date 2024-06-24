package com.arbr.relational_prompting.layers.object_translation

import com.arbr.content_formats.json_schematized.JsonSchema
import com.arbr.platform.object_graph.common.ObjectModel
import com.arbr.og.object_model.common.model.*
import com.arbr.content_formats.mapper.Mappers
import com.arbr.relational_prompting.generics.model.ChatMessage
import com.arbr.relational_prompting.layers.prompt_composition.PropertyListParser
import com.arbr.relational_prompting.layers.prompt_composition.PropertyListSerializer
import com.arbr.relational_prompting.layers.prompt_composition.YamlPropertyListParser
import com.arbr.relational_prompting.layers.prompt_composition.YamlPropertyListSerializer
import com.arbr.relational_prompting.services.ai_application.config.*
import com.arbr.content_formats.json_schematized.JsonSchemaValidation
import com.arbr.og.object_model.common.values.SourcedValue
import com.arbr.og.object_model.common.values.SourcedValueGeneratorInfo
import com.arbr.og.object_model.common.values.SourcedValueImpl
import com.arbr.og.object_model.common.values.collections.*
import org.slf4j.LoggerFactory

inline fun <reified R : SourcedStruct, B : TemplateComponentSchema.BuilderBase<R, B>> schema(f: TemplateComponentSchema.Builder0.() -> B): TemplateComponentSchema<R> =
    f(TemplateComponentSchema.Builder0()).build(
        objectClassProxy()
    )

inline fun <reified T> objectClassProxy() = T::class.java

private typealias OV = ObjectModel.ObjectValue<*, *, *, *>

/**
 * Input Schema: Record of Sourced Values (values + schema) -> List of serialized prompt elements
 */
class TemplateComponentSchema<R : SourcedStruct>(
    private val propertySchemaList: List<PropertySchema<*, *>>,
    private val elementClasses: List<Class<out SourcedValue<*>>>,
    private val propertyListSerializer: PropertyListSerializer,
    private val propertyListParser: PropertyListParser<R>,
    val objectClass: Class<R>,
) {
    class DuplicatePropertyKeyException(val key: String) : Exception("Duplicate property schemas with same key: $key")

    private val mapper = Mappers.mapper

    init {
        // Validate no duplicate properties
        val keys = mutableSetOf<String>()
        for (ps in propertySchemaList) {
            val key = ps.property().first
            if (key in keys) {
                throw DuplicatePropertyKeyException(key)
            }

            keys.add(key)
        }
    }

    fun withPropertyListSerializer(propertyListSerializer: PropertyListSerializer): TemplateComponentSchema<R> {
        return TemplateComponentSchema(
            propertySchemaList,
            elementClasses,
            propertyListSerializer,
            propertyListParser,
            objectClass,
        )
    }

    fun withPropertyListParser(propertyListParser: PropertyListParser<R>): TemplateComponentSchema<R> {
        return TemplateComponentSchema(
            propertySchemaList,
            elementClasses,
            propertyListSerializer,
            propertyListParser,
            objectClass,
        )
    }

    fun description(): TemplateDescriptionElement {
        return propertyListSerializer.describeProperties(
            propertySchemaList
        )
    }

    fun serializedValue(input: R): TemplateValueElement {
        return propertyListSerializer.serializeValuedProperties(
            propertySchemaList,
            input.values().map { it.value },
        )
    }

    fun serializedEmbeddingContents(input: R): List<ResourceEmbeddingContent> {
        // Schema ID relates to the whole record
        val schemaId = input.getSchemaId()
        val values = input.values()

        val wholeInputTemplateElement = propertyListSerializer.serializeValuedProperties(propertySchemaList, values.map { it.value })
        val wholeInputEmbeddingContents = listOf(
            ResourceEmbeddingContent(
                schemaId,
                ResourceEmbeddingKind(schemaId),
                wholeInputTemplateElement.serializedValue,
            )
        )

        val propertyEmbeddingContents = propertySchemaList.mapIndexedNotNull { index, propertySchema ->
            propertySchema.embeddingKind?.let { resourceEmbeddingKind ->
                // Serialize each property individually.
                // TODO: Consider allowing compound properties to define multiple embedded properties
                val templateElement = propertyListSerializer.serializeValuedProperties(
                    listOf(propertySchema),
                    listOf(values[index].value),
                )

                ResourceEmbeddingContent(
                    schemaId,
                    resourceEmbeddingKind,
                    templateElement.serializedValue,
                )
            }
        }

        return wholeInputEmbeddingContents + propertyEmbeddingContents
    }

    /**
     * Serialization / deserialization note:
     * There are many options here, but the easiest way to store records of ObjectValues is to represent them as an
     * intermediate form, List<SourcedValue<*>>, where each element is a generic sourced value.
     */
    fun deserializeFromSourcedValues(
        sourcedValues: List<SourcedValue<*>>,
    ): R {
        val prototypeRecordList = sourcedValues
            .zip(elementClasses)
            .zip(propertySchemaList)
            .map { (t0, propertySchema) ->
                val (sourcedValue, _) = t0
                propertySchema.objectType.parseFromObject(
                    sourcedValue.kind,
                    sourcedValue.value,
                    sourcedValue.generatorInfo
                )
            }

        @Suppress("UNCHECKED_CAST")
        return SourcedStruct.of(prototypeRecordList) as R
    }

    fun serializedToSourcedValues(input: R): List<SourcedValue<*>> {
        return input.map { typedSourcedValue ->
            // Create a generic sourced value
            SourcedValueImpl(
                typedSourcedValue.kind,
                typedSourcedValue.value,
                typedSourcedValue.typeName,
                typedSourcedValue.schema,
                typedSourcedValue.generatorInfo
            )
        }
    }

    fun parse(chatMessage: ChatMessage, sourcedValueGeneratorInfo: SourcedValueGeneratorInfo): R? {
        return try {
            propertyListParser.parseValue(
                propertySchemaList,
                chatMessage.content,
                sourcedValueGeneratorInfo,
                objectClass,
            )
        } catch (se: OutputSchemaException) {
            val parsedMap = se.parsedMap

            val violations = try {
                JsonSchemaValidation.detectSchemaViolations(propertySchemaList, parsedMap)
            } catch (e: Exception) {
                throw OutputFormatException(chatMessage.content, e)
            }

            if (violations.isEmpty()) {
                throw OutputFormatException(chatMessage.content, se.cause)
            } else {
                // Throw a special error type so the consumer can decide whether/how to retry.
                throw OutputSchemaExceptionWithKnownViolations(chatMessage.content, parsedMap, violations, se.cause)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TemplateComponentSchema::class.java)
    }

    abstract class BuilderBase<R : SourcedStruct, BB : BuilderBase<R, BB>> {
        abstract var propertySchemaList: List<PropertySchema<*, *>>
        abstract val classList: List<Class<out SourcedValue<*>>>

        fun configure(
            f: (JsonSchema) -> JsonSchema
        ): BB {
            val head = propertySchemaList.dropLast(1)
            val lastElement = propertySchemaList.takeLast(1).map {
                PropertySchema(it.objectType, f(it.rootedJsonSchema), embeddingKind = it.embeddingKind)
            }

            @Suppress("UNCHECKED_CAST")
            return also {
                propertySchemaList = head + lastElement
            } as BB
        }

        fun withKey(key: String): BB = configure { js ->
            js.copy(
                properties = LinkedHashMap<String, JsonSchema>().also {
                    it[key] = js.properties!!.values.first()
                }
            )
        }

        fun withDescription(description: String): BB = configure { js ->
            js.copy(
                properties = LinkedHashMap<String, JsonSchema>().also {
                    val (key, propJsonSchema) = js.properties!!.entries.first()
                    it[key] = propJsonSchema.copy(
                        description = description
                    )
                }
            )
        }

        fun build(recordClass: Class<R>): TemplateComponentSchema<R> {
            return TemplateComponentSchema(
                propertySchemaList,
                classList,
                YamlPropertyListSerializer,
                YamlPropertyListParser(),
                recordClass,
            )
        }
    }

    class Builder0 {

        inline fun <V, reified S : ObjectModel.ObjectValue<V, *, *, S>> p(propertyType: ObjectModel.ObjectType<V, *, *, S>): Builder1<S> {
            val embeddingKind = propertyType.embeddingKey?.let { ResourceEmbeddingKind(it) }
            val propertySchema =
                PropertySchema(propertyType, propertyType.jsonSchema, embeddingKind = embeddingKind)
            return Builder1(listOf(propertySchema), listOf(S::class.java))
        }
    }

    class Builder1<S1 : OV>(
        override var propertySchemaList: List<PropertySchema<*, *>>,
        override val classList: List<Class<out SourcedValue<*>>>,
    ) : BuilderBase<SourcedStruct1<S1>, Builder1<S1>>() {

        inline fun <V, reified S : ObjectModel.ObjectValue<V, *, *, S>> p(propertyType: ObjectModel.ObjectType<V, *, *, S>): Builder2<S1, S> {
            val embeddingKind = propertyType.embeddingKey?.let { ResourceEmbeddingKind(it) }
            val propertySchema = PropertySchema(propertyType, propertyType.jsonSchema, embeddingKind)
            return Builder2(propertySchemaList + propertySchema, classList + S::class.java)
        }
    }

    class Builder2<S1 : OV, S2 : OV>(
        override var propertySchemaList: List<PropertySchema<*, *>>,
        override val classList: List<Class<out SourcedValue<*>>>,
    ) : BuilderBase<SourcedStruct2<S1, S2>, Builder2<S1, S2>>() {

        inline fun <V, reified S : ObjectModel.ObjectValue<V, *, *, S>> p(propertyType: ObjectModel.ObjectType<V, *, *, S>): Builder3<S1, S2, S> {
            val embeddingKind = propertyType.embeddingKey?.let { ResourceEmbeddingKind(it) }
            val propertySchema = PropertySchema(propertyType, propertyType.jsonSchema, embeddingKind)
            return Builder3(propertySchemaList + propertySchema, classList + S::class.java)
        }
    }

    class Builder3<S1 : OV, S2 : OV, S3 : OV>(
        override var propertySchemaList: List<PropertySchema<*, *>>,
        override val classList: List<Class<out SourcedValue<*>>>,
    ) : BuilderBase<SourcedStruct3<S1, S2, S3>, Builder3<S1, S2, S3>>() {

        inline fun <V, reified S : ObjectModel.ObjectValue<V, *, *, S>> p(propertyType: ObjectModel.ObjectType<V, *, *, S>): Builder4<S1, S2, S3, S> {
            val embeddingKind = propertyType.embeddingKey?.let { ResourceEmbeddingKind(it) }
            val propertySchema = PropertySchema(propertyType, propertyType.jsonSchema, embeddingKind)
            return Builder4(propertySchemaList + propertySchema, classList + S::class.java)
        }
    }

    class Builder4<S1 : OV, S2 : OV, S3 : OV, S4 : OV>(
        override var propertySchemaList: List<PropertySchema<*, *>>,
        override val classList: List<Class<out SourcedValue<*>>>,
    ) : BuilderBase<SourcedStruct4<S1, S2, S3, S4>, Builder4<S1, S2, S3, S4>>() {

        inline fun <V, reified S : ObjectModel.ObjectValue<V, *, *, S>> p(propertyType: ObjectModel.ObjectType<V, *, *, S>): Builder5<S1, S2, S3, S4, S> {
            val embeddingKind = propertyType.embeddingKey?.let { ResourceEmbeddingKind(it) }
            val propertySchema = PropertySchema(propertyType, propertyType.jsonSchema, embeddingKind)
            return Builder5(propertySchemaList + propertySchema, classList + S::class.java)
        }
    }

    class Builder5<S1 : OV, S2 : OV, S3 : OV, S4 : OV, S5 : OV>(
        override var propertySchemaList: List<PropertySchema<*, *>>,
        override val classList: List<Class<out SourcedValue<*>>>,
    ) : BuilderBase<SourcedStruct5<S1, S2, S3, S4, S5>, Builder5<S1, S2, S3, S4, S5>>() {

        inline fun <V, reified S : ObjectModel.ObjectValue<V, *, *, S>> p(propertyType: ObjectModel.ObjectType<V, *, *, S>): Builder6<S1, S2, S3, S4, S5, S> {
            val embeddingKind = propertyType.embeddingKey?.let { ResourceEmbeddingKind(it) }
            val propertySchema = PropertySchema(propertyType, propertyType.jsonSchema, embeddingKind)
            return Builder6(propertySchemaList + propertySchema, classList + S::class.java)
        }
    }

    class Builder6<S1 : OV, S2 : OV, S3 : OV, S4 : OV, S5 : OV, S6 : OV>(
        override var propertySchemaList: List<PropertySchema<*, *>>,
        override val classList: List<Class<out SourcedValue<*>>>,
    ) : BuilderBase<SourcedStruct6<S1, S2, S3, S4, S5, S6>, Builder6<S1, S2, S3, S4, S5, S6>>() {

        inline fun <V, reified S : ObjectModel.ObjectValue<V, *, *, S>> p(propertyType: ObjectModel.ObjectType<V, *, *, S>): Builder7<S1, S2, S3, S4, S5, S6, S> {
            val embeddingKind = propertyType.embeddingKey?.let { ResourceEmbeddingKind(it) }
            val propertySchema = PropertySchema(propertyType, propertyType.jsonSchema, embeddingKind)
            return Builder7(propertySchemaList + propertySchema, classList + S::class.java)
        }
    }

    class Builder7<S1 : OV, S2 : OV, S3 : OV, S4 : OV, S5 : OV, S6 : OV, S7 : OV>(
        override var propertySchemaList: List<PropertySchema<*, *>>,
        override val classList: List<Class<out SourcedValue<*>>>,
    ) : BuilderBase<SourcedStruct7<S1, S2, S3, S4, S5, S6, S7>, Builder7<S1, S2, S3, S4, S5, S6, S7>>() {

        inline fun <V, reified S : ObjectModel.ObjectValue<V, *, *, S>> p(propertyType: ObjectModel.ObjectType<V, *, *, S>): Builder8<S1, S2, S3, S4, S5, S6, S7, S> {
            val embeddingKind = propertyType.embeddingKey?.let { ResourceEmbeddingKind(it) }
            val propertySchema = PropertySchema(propertyType, propertyType.jsonSchema, embeddingKind)
            return Builder8(propertySchemaList + propertySchema, classList + S::class.java)
        }
    }

    class Builder8<S1 : OV, S2 : OV, S3 : OV, S4 : OV, S5 : OV, S6 : OV, S7 : OV, S8 : OV>(
        override var propertySchemaList: List<PropertySchema<*, *>>,
        override val classList: List<Class<out SourcedValue<*>>>,
    ) : BuilderBase<SourcedStruct8<S1, S2, S3, S4, S5, S6, S7, S8>, Builder8<S1, S2, S3, S4, S5, S6, S7, S8>>()
}
