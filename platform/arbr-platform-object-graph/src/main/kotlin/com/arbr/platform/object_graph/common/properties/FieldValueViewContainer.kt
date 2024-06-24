package com.arbr.og.object_model.common.properties

import com.arbr.content_formats.json_schematized.JsonSchema
import com.arbr.og.object_model.common.model.CompoundPropertyIdentifier
import com.arbr.og.object_model.common.values.SourcedValue
import com.arbr.og.object_model.common.values.SourcedValueGeneratorInfo
import com.arbr.og.object_model.common.values.SourcedValueImpl
import com.arbr.og.object_model.common.values.SourcedValueKind

interface FieldValueViewContainer<E : UB, UB, C : FieldValueViewContainer<out UB, UB, C>> {
    val collectionRequirementsProvider: CollectionRequirementsProvider
    val containerIdentifier: CompoundPropertyIdentifier

    fun requireSizeAtLeast(minSize: Int): FieldValueViewContainer<E, UB, C> {
        collectionRequirementsProvider.requireSizeAtLeast(containerIdentifier, minSize)
        return this
    }

    fun requireSizeAtMost(maxSize: Int): FieldValueViewContainer<E, UB, C> {
        collectionRequirementsProvider.requireSizeAtMost(containerIdentifier, maxSize)
        return this
    }

    fun requireNonempty(): FieldValueViewContainer<E, UB, C> {
        collectionRequirementsProvider.requireNonempty(containerIdentifier)
        return requireSizeAtLeast(1)
    }

    fun requireEmpty(): FieldValueViewContainer<E, UB, C> {
        collectionRequirementsProvider.requireEmpty(containerIdentifier)
        return requireSizeAtMost(0)
    }

    fun isEmpty(): SourcedValue<Boolean>

    fun isNotEmpty(): SourcedValue<Boolean>

    operator fun get(i: Int): E

    fun first(): E {
        return requireNonempty()[0]
    }

    fun last(): E

    fun <E2 : UB> map(
        transform: (E) -> E2
    ): FieldValueViewContainer<E2, UB, C>

    fun filter(transform: (E) -> SourcedValue<Boolean>): FieldValueViewContainer<E, UB, C>

    fun find(element: E): E {
        return filter {
            booleanSourcedValueImpl(it == element)
        }.first()
    }

    fun <E2 : UB> plusContainer(other: FieldValueViewContainer<E2, UB, C>): C

    operator fun <E2 : UB> plus(other: FieldValueViewContainer<E2, UB, *>): FieldValueViewContainer<E2, UB, *> {
        @Suppress("UNCHECKED_CAST")
        return plusContainer(other as FieldValueViewContainer<E2, UB, C>) as FieldValueViewContainer<E2, UB, *>
    }

    companion object {
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
}
