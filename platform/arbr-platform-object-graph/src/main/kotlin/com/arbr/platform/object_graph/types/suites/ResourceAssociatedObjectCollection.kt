package com.arbr.object_model.core.types.suites

class ResourceAssociatedObjectCollection<E : EnumLike, T: Any>(
    private val enumValues: Array<E>,
    private val dependentObjects: Array<T>,
): Map<E, T> {
    private class MapEntryImpl<K, V>(
        override val key: K,
        override val value: V,
    ): Map.Entry<K, V>

    override val entries: Set<Map.Entry<E, T>> = enumValues.zip(dependentObjects).map {
        MapEntryImpl(it.first, it.second)
    }.toSet()

    override val keys: Set<E> = enumValues.toSet()
    override val size: Int = enumValues.size
    override val values: Collection<T> = dependentObjects.toList()

    override fun isEmpty(): Boolean {
        return enumValues.isEmpty()
    }

    override fun get(key: E): T {
        return dependentObjects[key.ordinal]
    }

    /**
     * Interface of get won't allow for non-nullability
     */
    fun getAssociatedObject(key: E): T {
        return get(key)
    }

    override fun containsValue(value: T): Boolean {
        // The assumption here is that T is strongly tied to a resource type such that for a caller to query with a
        // value of type T means that it uniquely corresponds to an enum value and must already exist in the array
        return true
    }

    override fun containsKey(key: E): Boolean {
        return true
    }

    private class AssociatedArrayCollection<T: Any>(
        private val dependentObjects: Array<T>,
    ): Collection<T> {
        override val size: Int = dependentObjects.size

        private val arrayIsEmpty = dependentObjects.isEmpty()
        override fun isEmpty(): Boolean {
            return arrayIsEmpty
        }

        override fun iterator(): Iterator<T> {
            return iterator {
                for (element in dependentObjects) {
                    yield(element)
                }
            }
        }

        override fun containsAll(elements: Collection<T>): Boolean {
            // See assumption note for map
            return true
        }

        override fun contains(element: T): Boolean {
            // See assumption note for map
            return true
        }
    }

    companion object {
        inline fun <reified E : EnumLike, reified T: Any> new(
            enumValues: Array<E>,
            transform: (E) -> T,
        ): ResourceAssociatedObjectCollection<E, T> {
            return ResourceAssociatedObjectCollection(
                enumValues,
                Array(enumValues.size) {
                    transform(enumValues[it])
                }
            )
        }
    }
}