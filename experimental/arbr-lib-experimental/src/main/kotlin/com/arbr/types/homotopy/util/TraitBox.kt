package com.arbr.types.homotopy.util

import com.arbr.types.homotopy.keys.PropertyKey

sealed interface TraitBox<Trait> {
    data class Node<Trait>(val trait: Trait) : TraitBox<Trait> {
        override fun <Trait2> zipWith(
            other: TraitBox<Trait>,
            f: (Trait, Trait) -> Trait2
        ): TraitBox<Trait2> {
            if (other is Node) {
                return Node(f(trait, other.trait))
            } else {
                throw IllegalStateException()
            }
        }
    }

    data class Property<Trait>(val propertyKey: PropertyKey, val node: Node<Trait>) : TraitBox<Trait> {
        override fun <Trait2> zipWith(other: TraitBox<Trait>, f: (Trait, Trait) -> Trait2): Property<Trait2> {
            if (other is Property && this.propertyKey == other.propertyKey) {
                return Property(propertyKey, Node(f(node.trait, other.node.trait)))
            } else {
                throw IllegalStateException()
            }
        }
    }

    data class Struct<Trait>(val properties: List<Property<Trait>>) : TraitBox<Trait> {
        override fun <Trait2> zipWith(
            other: TraitBox<Trait>,
            f: (Trait, Trait) -> Trait2
        ): TraitBox<Trait2> {
            if (other is Struct && this.properties.size == other.properties.size) {
                return Struct(
                    properties.zip(other.properties).map { (c0, c1) ->
                        c0.zipWith(c1, f)
                    }
                )
            } else {
                throw IllegalStateException()
            }
        }
    }

    fun <Trait2> mapTraits(f: (Trait) -> Trait2): TraitBox<Trait2> {
        return when (this) {
            is Node -> Node(f(trait))
            is Property -> Property(propertyKey, Node(f(node.trait)))
            is Struct -> Struct(properties.map { Property(it.propertyKey, Node(f(it.node.trait))) })
        }
    }

    fun <Trait2> zipWith(other: TraitBox<Trait>, f: (Trait, Trait) -> Trait2): TraitBox<Trait2>


    companion object {
        fun <Trait> push(boxes: List<TraitBox<Trait>>): TraitBox<List<Trait>> {
            val singletons = boxes.map { box -> box.mapTraits { listOf(it) } }
            return singletons.reduce { acc, contractionBox ->
                acc.zipWith(contractionBox) { a, b ->
                    a + b
                }
            }
        }
    }
}

