package com.arbr.types.homotopy

import com.arbr.types.homotopy.config.HomotopyIntrospectionConfig
import com.arbr.types.homotopy.htype.HType
import com.arbr.types.homotopy.keys.PropertyKeys
import com.arbr.util_common.LexIntSequence
import org.slf4j.LoggerFactory
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.withNullability
import kotlin.reflect.jvm.jvmErasure

/**
 * Introspection functionality for the base representation of the type graph
 */
@Suppress("SameParameterValue")
class PlainTypeIntrospection(
    private val config: HomotopyIntrospectionConfig,
) {

    private val introspectionNodes = mutableMapOf<KType, PlainTypeNode>()
    private val introspectionCache = mutableMapOf<KType, PlainType>()
    private val introspectionStack = mutableListOf<KType>()
    private val introspectionQueue = mutableListOf<KType>()

    private inline fun <reified T> starTypeOf() = T::class.starProjectedType

    private fun introspectTypeParameter(typeProjection: KTypeProjection): PlainType {
        val elementType = typeProjection.type

        return if (elementType == null) {
            PlainType.Leaf(starTypeOf<Any?>())
        } else {
            introspect(elementType, false)
        }
    }

    private fun introspectListType(objectType: KType): PlainType {
        check(objectType.arguments.size == 1)

        val typeParameterHType = introspectTypeParameter(
            objectType.arguments.first(),
        )

        return PlainType.ListOf(typeParameterHType)
    }

    private fun introspectMapType(objectType: KType): PlainType {
        check(objectType.arguments.size == 2)

        val keyProperty = introspectTypeParameter(objectType.arguments.first())
        val valueProperty = introspectTypeParameter(objectType.arguments.first())

        return PlainType.MapOf(keyProperty, valueProperty)
    }

    private fun introspectInner(objectType: KType, shouldDereference: Boolean): PlainType {
        val existing = introspectionCache[objectType]
        if (existing != null) {
            return existing
        }

        if (objectType.isMarkedNullable) {
            return PlainType.NullableOf(introspect(objectType.withNullability(false), false))
                .also { introspectionCache[objectType] = it }
        }

        if (objectType.jvmErasure.qualifiedName == null) {
            return PlainType.Leaf(starTypeOf<Any?>())
                .also { introspectionCache[objectType] = it }
        }

        // Primitives
        val primitive = when (objectType.jvmErasure.starProjectedType) {
            String::class.starProjectedType,
            Double::class.starProjectedType,
            Long::class.starProjectedType,
            Float::class.starProjectedType,
            Int::class.starProjectedType,
            Boolean::class.starProjectedType,
            Any::class.starProjectedType -> PlainType.Leaf(objectType.jvmErasure.starProjectedType)

            else -> null
        }
        if (primitive != null) {
            return primitive
                .also { introspectionCache[objectType] = it }
        }

        // Exact match on classifiers to avoid cases like MyClass<T>: List<Optional<T>>
        val starProjectedClassifier = objectType.classifier?.starProjectedType
        val isSupportedCollection = when (starProjectedClassifier) {
            Map::class.starProjectedType,
            List::class.starProjectedType,
            Set::class.starProjectedType,
            Collection::class.starProjectedType -> true

            else -> false
        }

        // Star-project other carried type parameters
        val hasArguments = objectType.arguments.any { it != KTypeProjection.STAR }
        val cacheableType = if (!isSupportedCollection && hasArguments) {
            objectType.jvmErasure.starProjectedType
        } else {
            objectType
        }

        // Exact match on classifiers to avoid cases like MyClass<T>: List<Optional<T>>
        // Check before the stack short circuit so we don't try to reference anonymous types
        val collectionResult = when (starProjectedClassifier) {
            Map::class.starProjectedType -> introspectMapType(cacheableType)
            List::class.starProjectedType -> introspectListType(cacheableType)
            Set::class.starProjectedType -> introspectListType(cacheableType)
            Collection::class.starProjectedType -> introspectListType(cacheableType)
            else -> null
        }
        if (collectionResult != null) {
            return collectionResult
                .also { introspectionCache[objectType] = it }
        }

        /**
         * Potential dereference
         */

        val objectClass = objectType.jvmErasure
        val packageQualifiedName = objectClass.qualifiedName!!
        if (!config.allowedResourcePackages.any { packageQualifiedName.startsWith("$it.") }) {
            return PlainType.Leaf(objectType)
                .also { introspectionCache[objectType] = it }
        }

        val ref = PlainType.Ref(objectType)

        return if (shouldDereference) {
            val properties = try {
                objectClass.memberProperties
            } catch (e: kotlin.reflect.jvm.internal.KotlinReflectionInternalError) {
                logger.warn("Could not introspect ${objectClass.qualifiedName}\n${e}")
                return PlainType.Leaf(objectType)
            }
            val eligibleProperties = properties.filter {
                !it.returnType.isSubtypeOf(HType::class.starProjectedType)
            }

            val propertyKeys = PropertyKeys.ofProperties(eligibleProperties)
            val elementHTypes = eligibleProperties.zip(propertyKeys).map { (property, key) ->
                key to introspect(property.returnType, false)
            }
            val node = PlainTypeNode(ref, elementHTypes)
            introspectionNodes[objectType] = node
            introspectionCache[objectType] = ref
            ref
        } else {
            if (objectType !in introspectionQueue && objectType !in introspectionCache) {
                introspectionQueue.add(objectType)
            }
            ref
        }
    }

    private fun introspect(objectType: KType, shouldDereference: Boolean): PlainType {
        return if (objectType in introspectionStack) {
            PlainType.Ref(objectType)
        } else {
            introspectionStack.add(objectType)
            introspectInner(objectType, shouldDereference)
                .also { introspectionStack.removeLast() }
        }
    }

    fun introspectBaseType(objectType: KType): List<OrderedPlainTypeNode> {
        introspect(objectType, true)
        while (introspectionQueue.isNotEmpty()) {
            val nextType = introspectionQueue.removeFirst()
            check(nextType !in introspectionCache) {
                nextType.toString()
            }
            introspect(nextType, true)
            check(nextType in introspectionCache)
        }

        // TODO: Need to reflect broken cycles in the result
        val dependencySortedNodes = sortByDependencies2(introspectionNodes)

        dependencySortedNodes.forEach { (intSeq, nodeOf) ->
            logger.debug("{}. {}", intSeq.ints, nodeOf.ref.kType)
        }

        return dependencySortedNodes
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PlainTypeIntrospection::class.java)

        private fun shallowRefDependencies(plainType: PlainType): List<Pair<Int, PlainType.Ref>> {
            return when (plainType) {
                is PlainType.Leaf -> emptyList()
                is PlainType.ListOf -> shallowRefDependencies(plainType.inner)
                    .map { it.first + 1 to it.second }
                is PlainType.MapOf -> (shallowRefDependencies(plainType.innerKey) + shallowRefDependencies(plainType.innerValue))
                    .map { it.first + 1 to it.second }
                is PlainType.NullableOf -> shallowRefDependencies(plainType.inner)
                    .map { it.first + 1 to it.second }
                is PlainType.Ref -> listOf(0 to plainType)
            }
        }

        private fun sortByDependencies2(nodes: Map<KType, PlainTypeNode>): List<OrderedPlainTypeNode> {
            val satisfied = mutableSetOf<KType>()
            val remaining = nodes.keys.toMutableSet()
            val dependencies = nodes.mapValues { (_, node) ->
                node.children
                    .flatMap { (propertyKey, plainType) ->
                        shallowRefDependencies(plainType)
                            .distinctBy { (dependencyTier, ref) ->
                                ref.kType
                            }
                            .map { propertyKey to it }
                    }.toSet()
            }
            val ordinals = mutableMapOf<KType, LexIntSequence>()

            // Sort roots alphabetically
            val rootIndices = dependencies
                .filter { it.value.none { p -> p.second.first == 0 } }
                .keys
                .sortedBy { it.toString() }
                .withIndex()
                .associate { it.value to it.index }

            while (remaining.isNotEmpty()) {
                fun getNewlySatisfied(maxDependencyDepth: Int): Map<KType, LexIntSequence> {
                    return remaining
                        .mapNotNull {
                            val deps = dependencies[it]!!
                                .filter { t -> t.second.first <= maxDependencyDepth }

                            if (deps.isEmpty()) {
                                val ordinal = rootIndices[it]!!
                                it to LexIntSequence(listOf(0, ordinal))
                            } else {
                                val depTypes = deps.map { t -> t.second.second.kType }
                                val unsat = depTypes - satisfied
                                it.takeIf { unsat.isEmpty() }?.let { kType ->
                                    val (pKey, maxDep) = deps.maxBy { (_, dep) -> ordinals[dep.second.kType]!! }
                                    val maxOrdinal = ordinals[maxDep.second.kType]!!
                                    // Track the node depth as the first ordinal token
                                    val newOrdinal =
                                        LexIntSequence(listOf(maxOrdinal.ints.first() + 1) + maxOrdinal.ints.drop(1) + pKey.ordinal)
                                    kType to newOrdinal
                                }
                            }
                        }
                        .toMap()
                }

                var maxDependencyDepth = 2
                var newlySatisfied = getNewlySatisfied(maxDependencyDepth)
                while (newlySatisfied.isEmpty() && maxDependencyDepth >= 0) {
                    maxDependencyDepth--

                    logger.debug("Attempting to break cycles above type depth $maxDependencyDepth")
                    newlySatisfied = getNewlySatisfied(maxDependencyDepth)
                }
                if (newlySatisfied.isEmpty()) {
                    throw Exception("Hard reference cycle in type graph. Unsatisfiable: [${remaining.joinToString { it.toString() }}]")
                }

                remaining.removeAll(newlySatisfied.keys)
                satisfied.addAll(newlySatisfied.keys)
                ordinals.putAll(newlySatisfied)
            }

            return ordinals.entries.sortedBy { it.value }.map { (kType, lexIntSequence) ->
                OrderedPlainTypeNode(lexIntSequence, nodes[kType]!!)
            }
        }

        private fun sortByDependencies(nodes: Map<KType, PlainTypeNode>): List<OrderedPlainTypeNode> {
            val satisfied = mutableSetOf<KType>()
            val remaining = nodes.keys.toMutableSet()
            val dependencies = nodes.mapValues { (_, node) ->
                node.children.mapNotNull { (propertyKey, plainType) ->
                    if (plainType is PlainType.Ref) {
                        propertyKey to plainType.kType
                    } else {
                        null
                    }
                }.toSet()
            }
            val ordinals = mutableMapOf<KType, LexIntSequence>()

            // Sort roots alphabetically
            val rootIndices = dependencies
                .filter { it.value.isEmpty() }
                .keys
                .sortedBy { it.toString() }
                .withIndex()
                .associate { it.value to it.index }

            while (remaining.isNotEmpty()) {
                val newlySatisfied = remaining
                    .mapNotNull {
                        val deps = dependencies[it]!!

                        if (deps.isEmpty()) {
                            val ordinal = rootIndices[it]!!
                            it to LexIntSequence(listOf(0, ordinal))
                        } else {
                            val depTypes = deps.map { t -> t.second }
                            val unsat = depTypes - satisfied
                            it.takeIf { unsat.isEmpty() }?.let { kType ->
                                val (pKey, maxDep) = deps.maxBy { (_, dep) -> ordinals[dep]!! }
                                val maxOrdinal = ordinals[maxDep]!!
                                // Track the node depth as the first ordinal token
                                val newOrdinal =
                                    LexIntSequence(listOf(maxOrdinal.ints.first() + 1) + maxOrdinal.ints.drop(1) + pKey.ordinal)
                                kType to newOrdinal
                            }
                        }
                    }
                    .toMap()

                if (newlySatisfied.isEmpty()) {
                    throw Exception("Hard reference cycle in type graph. Unsatisfiable: [${remaining.joinToString { it.toString() }}]")
                }

                remaining.removeAll(newlySatisfied.keys)
                satisfied.addAll(newlySatisfied.keys)
                ordinals.putAll(newlySatisfied)
            }

            return ordinals.entries.sortedBy { it.value }.map { (kType, lexIntSequence) ->
                OrderedPlainTypeNode(lexIntSequence, nodes[kType]!!)
            }
        }
    }
}

