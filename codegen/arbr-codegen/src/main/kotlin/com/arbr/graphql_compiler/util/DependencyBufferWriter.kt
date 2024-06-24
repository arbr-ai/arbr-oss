package com.arbr.graphql_compiler.util

import java.io.Writer
import java.util.*

class DependencyBufferWriter<K : Any, T : Any>(
    private val innerWriter: Writer,
    private val factory: DependencyAwareAppendableTargetFactory<K, T>,
) : Writer(innerWriter) {
    private val seenMap = mutableMapOf<K, DependencyAwareAppendableTarget<K, T>>()
    private val satisfiedKeys = mutableSetOf<K>()
    private val dependents = mutableMapOf<K, MutableSet<K>>()
    private val dependencies = mutableMapOf<K, MutableSet<K>>()

    override fun write(cbuf: CharArray, off: Int, len: Int) {
        innerWriter.write(cbuf, off, len)
    }

    override fun flush() {
        innerWriter.flush()
    }

    override fun close() {
        innerWriter.close()
    }

    private fun notifyFrom(newElement: DependencyAwareAppendableTarget<K, T>) {
        val visitOrder = mutableListOf<K>()
        val frontier = PriorityQueue<DependencyAwareAppendableTarget<K, T>>()
        frontier.add(newElement)

        var currentElement: DependencyAwareAppendableTarget<K, T>? = frontier.poll()
        var currentKey = currentElement?.key
        while (currentKey != null) {
            visitOrder.add(currentKey)
            println("Visiting $currentKey")

            val children = dependents[currentKey] ?: mutableSetOf()
            dependents[currentKey]?.clear()
            children.forEach { childKey ->
                println("Hit child $childKey")
                val newDependencies = dependencies[childKey]?.also { it.remove(currentKey) } ?: mutableSetOf()
                if (newDependencies.isEmpty()) {
                    dependencies.remove(childKey)
                    frontier.add(seenMap[childKey]!!)
                }
            }

            if (frontier.isEmpty()) {
                break
            }
            currentElement = frontier.poll()
            currentKey = currentElement?.key
            while (currentKey in visitOrder) {
                if (frontier.isEmpty()) {
                    currentKey = null
                    break
                }
                currentElement = frontier.poll()
                currentKey = currentElement?.key
            }

            currentElement?.let { wrapped ->
                // Actually process
                val key = currentKey!!
                satisfiedKeys.add(key)
                wrapped.appendWith(this)
            }
        }
    }

    fun append(target: T): DependencyBufferWriter<K, T> {
        val wrapped: DependencyAwareAppendableTarget<K, T> = factory.makeDependencyAwareAppendableTarget(target)
        val elementKey = wrapped.key
        val elementDependencies = wrapped.getDependencyKeys().toSet()
            .minus(satisfiedKeys)
            .minus(elementKey)
        if (elementDependencies.isEmpty()) {
            satisfiedKeys.add(elementKey)
            wrapped.appendWith(this)
            notifyFrom(wrapped)
        } else {
            seenMap[elementKey] = wrapped
            elementDependencies.forEach { dependency ->
                dependents.compute(dependency) { _, dependentSet ->
                    if (dependentSet == null) {
                        mutableSetOf(elementKey)
                    } else {
                        dependentSet.add(elementKey)
                        dependentSet
                    }
                }
            }
            dependencies[elementKey] = elementDependencies.toMutableSet()

            println("Enqueued $elementKey")
        }

        println("Dependencies $dependencies")
        println("Dependents $dependents")

        return this
    }
}