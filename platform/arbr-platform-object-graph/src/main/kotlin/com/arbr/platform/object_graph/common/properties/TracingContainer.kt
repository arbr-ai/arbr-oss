package com.arbr.og.object_model.common.properties

import com.arbr.content_formats.json_schematized.JsonSchema
import com.arbr.og.object_model.common.values.SourcedValue
import com.arbr.og.object_model.common.values.SourcedValueGeneratorInfo
import com.arbr.og.object_model.common.values.SourcedValueKind
import com.arbr.og.object_model.common.values.SourcedValueLazyImpl

class DependencyTracingContainerValueException: Exception()

interface TracingContainer<E: UB, UB>: FieldValueViewContainer<E, UB, TracingContainer<out UB, UB>> {
    val collectionDependencyTracingValueProvider: CollectionDependencyTracingValueProvider<E>

    override fun requireSizeAtLeast(minSize: Int): TracingContainer<E, UB> {
        collectionRequirementsProvider.requireSizeAtLeast(containerIdentifier, minSize)
        return this
    }

    override fun requireSizeAtMost(maxSize: Int): TracingContainer<E, UB> {
        collectionRequirementsProvider.requireSizeAtMost(containerIdentifier, maxSize)
        return this
    }

    override fun requireNonempty(): TracingContainer<E, UB> {
        collectionRequirementsProvider.requireNonempty(containerIdentifier)
        return requireSizeAtLeast(1)
    }

    override fun requireEmpty(): TracingContainer<E, UB> {
        collectionRequirementsProvider.requireEmpty(containerIdentifier)
        return requireSizeAtMost(0)
    }

    override fun isEmpty(): SourcedValue<Boolean> {
        return booleanLazySourcedValueless()
    }

    override fun isNotEmpty(): SourcedValue<Boolean> {
        return booleanLazySourcedValueless()
    }

    /**
     * Get the element at index i
     * TODO: Think about whether this is desirable for dependency tracing
     * Infinite loops are certainly possible, unless we always return a value of non-null type
     */
    override operator fun get(i: Int): E {
        return requireSizeAtLeast(i + 1)
            .collectionDependencyTracingValueProvider
            .provideValue(
                containerIdentifier.indexedAt(i),
            )
    }

    override fun last(): E {
        return requireNonempty()
            .collectionDependencyTracingValueProvider
            .provideValue(
                containerIdentifier.indexedAt(-1),
            )
    }

    fun <E2: UB> withProvider(
        provider: CollectionDependencyTracingValueProvider<E2>,
    ): TracingContainer<E2, UB>

    override fun <E2: UB> map(
        transform: (E) -> E2
    ): TracingContainer<E2, UB> {
        // A map doesn't require nonempty on its own
        // In fact, we can just wrap the provider to make the constraints lazy
        val nextProvider = CollectionDependencyTracingValueProvider<E2> {
            val sourceValue: E = collectionDependencyTracingValueProvider.provideValue(
                containerIdentifier.indexedAt(0),
            )
            transform(sourceValue)
        }

        // Temporary shortcut: trace the transform greedily. Long-term should trace the transform for a function
        // signature but only as requirements if a value is required
        transform(collectionDependencyTracingValueProvider.provideValue(containerIdentifier.indexedAt(0)))

        return withProvider(nextProvider)
    }

    override fun filter(transform: (E) -> SourcedValue<Boolean>): TracingContainer<E, UB> {
        // A filter doesn't require nonempty on its own
        // In fact, we can just wrap the provider to make the constraints lazy
        val nextProvider = CollectionDependencyTracingValueProvider<E> {
            val sourceValue: E = collectionDependencyTracingValueProvider.provideValue(
                containerIdentifier.indexedAt(0),
            )

            // Similar to the case for map, we don't actually use the values resulting from the transform, since
            // the source object has no meaning to begin with
            // For tracing, every filter is a positive result
            // Ideally we'd inject tracing info from the transform back into the source value
            transform(sourceValue)

            sourceValue
        }

        // Temporary shortcut: trace the transform greedily. Long-term should trace the transform for a function
        // signature but only as requirements if a value is required
        transform(collectionDependencyTracingValueProvider.provideValue(containerIdentifier.indexedAt(0)))

        return withProvider(nextProvider)
    }

    companion object {
        private fun booleanLazySourcedValueless(): SourcedValue<Boolean> {
            return SourcedValueLazyImpl<Boolean>(
                SourcedValueKind.COMPUTED,
                "Boolean",
                JsonSchema("boolean", null, null, null, null, null),
                SourcedValueGeneratorInfo(emptyList()),
            ) {
                throw DependencyTracingContainerValueException()
            }
        }
    }
}