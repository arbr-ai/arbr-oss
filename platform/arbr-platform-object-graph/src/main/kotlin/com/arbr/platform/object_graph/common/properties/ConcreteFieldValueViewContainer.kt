package com.arbr.og.object_model.common.properties

import com.arbr.content_formats.json_schematized.JsonSchema
import com.arbr.og.object_model.common.model.CompoundPropertyIdentifier
import com.arbr.og.object_model.common.values.SourcedValue
import com.arbr.og.object_model.common.values.SourcedValueGeneratorInfo
import com.arbr.og.object_model.common.values.SourcedValueImpl
import com.arbr.og.object_model.common.values.SourcedValueKind

@Suppress("MemberVisibilityCanBePrivate")
class ConcreteFieldValueViewContainer<E : UB, UB>(
    val backingCollection: Collection<E>,
    override val containerIdentifier: CompoundPropertyIdentifier
) : FieldValueViewContainer<E, UB, ConcreteFieldValueViewContainer<out UB, UB>> {
    override val collectionRequirementsProvider: CollectionRequirementsProvider =
        ConcreteCollectionRequirementsProvider(
            backingCollection.size,
        )

    override fun isEmpty(): SourcedValue<Boolean> {
        return booleanSourcedValueImpl(backingCollection.isEmpty())
    }

    override fun isNotEmpty(): SourcedValue<Boolean> {
        return booleanSourcedValueImpl(backingCollection.isNotEmpty())
    }

    override fun get(i: Int): E {
        return requireSizeAtLeast(i + 1)
            .let {
                backingCollection.elementAt(i)
            }
    }

    override fun last(): E {
        return requireNonempty()
            .let {
                backingCollection.last()
            }
    }

    override fun <E2 : UB> map(transform: (E) -> E2): ConcreteFieldValueViewContainer<E2, UB> {
        // Actually map
        val nextCollection = backingCollection.map(transform)
        return ConcreteFieldValueViewContainer(nextCollection, containerIdentifier)
    }

    override fun filter(transform: (E) -> SourcedValue<Boolean>): ConcreteFieldValueViewContainer<E, UB> {
        // Actually filter
        val nextCollection = backingCollection.filter {
            transform(it).value
        }
        return ConcreteFieldValueViewContainer(nextCollection, containerIdentifier)
    }

    override fun <E2 : UB> plusContainer(other: FieldValueViewContainer<E2, UB, ConcreteFieldValueViewContainer<out UB, UB>>): ConcreteFieldValueViewContainer<out UB, UB> {
        // The type system won't let us use this as an argument type but it's guaranteed by the self-dependent type
        other as ConcreteFieldValueViewContainer<E2, UB>

        val unionContainerId = CompoundPropertyIdentifier.UnionOf(containerIdentifier, other.containerIdentifier)
        return ConcreteFieldValueViewContainer(
            backingCollection + other.backingCollection,
            unionContainerId,
        )
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