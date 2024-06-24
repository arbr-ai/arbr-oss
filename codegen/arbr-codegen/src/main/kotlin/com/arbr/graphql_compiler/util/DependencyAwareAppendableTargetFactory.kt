package com.arbr.graphql_compiler.util

abstract class DependencyAwareAppendableTargetFactory<Key : Any, T : Any> {

    abstract fun makeDependencyAwareAppendableTarget(
        value: T,
    ): DependencyAwareAppendableTarget<Key, T>

    companion object {
        fun <K : Any, V : Any> from(
            comparator: Comparator<V>,
            keyExtractor: (V) -> K,
            dependencyKeyExtractor: (V) -> List<K>,
            renderTo: Appendable.(V) -> Unit,
        ): DependencyAwareAppendableTargetFactory<K, V> {
            return object : DependencyAwareAppendableTargetFactory<K, V>() {
                override fun makeDependencyAwareAppendableTarget(value: V): DependencyAwareAppendableTarget<K, V> {
                    val thisKey = keyExtractor(value)
                    val thisValue = value

                    val dependencies = dependencyKeyExtractor(thisValue)

                    return object : DependencyAwareAppendableTarget<K, V> {
                        override fun compareTo(other: DependencyAware<K, V>): Int {
                            return comparator.compare(thisValue, other.value)
                        }

                        override val key: K
                            get() = thisKey

                        override val value: V
                            get() = thisValue

                        override fun getDependencyKeys(): List<K> {
                            return dependencies
                        }

                        override fun appendWith(appendable: Appendable): Appendable {
                            renderTo(appendable, value)
                            return appendable
                        }
                    }
                }
            }
        }
    }
}