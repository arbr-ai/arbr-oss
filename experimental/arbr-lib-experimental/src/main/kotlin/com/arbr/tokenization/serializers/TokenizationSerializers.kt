package com.arbr.tokenization.serializers

import com.arbr.content_formats.format.Either
import com.arbr.content_formats.format.tokenizer.TokenizationSerializer
import com.arbr.data_structures_common.immutable.ImmutableLinkedSet
import com.arbr.data_structures_common.partial_order.*
import java.util.*

object TokenizationSerializers {

    fun <D, T : Any, T1> flattener(
        outerSerializer: TokenizationSerializer<D, T>,
        innerSerializer: TokenizationSerializer<T, T1>,
        reverseFamilySelector: (List<Pair<Either<T, PartialOrderSet<T1>>, T1>>, T1) -> Either<T, PartialOrderSet<T1>>?,
    ): TokenizationSerializer<D, Pair<Optional<T>, T1>> =
        TokenizationSerializer { tokens, _ -> // Note formatter ignored
            val destinyMap = mutableMapOf<T1, Either<T, PartialOrderSet<T1>>>()
            val allTokenEdges = Orders
                .relationPairs(tokens)
                .map { (p0, p1) -> p0.second to p1.second }
                .toMutableSet()

            // Initialize empty posets for every group with a known pre-value, to be filled via dfs
            val preValuedPosetMap = tokens
                .elements
                .mapNotNull { (opt, _) ->
                    if (opt.isPresent) {
                        opt.get()
                    } else {
                        null
                    }
                }
                .distinct()
                .associateWith { emptyPoset<T1>() }
                .toMutableMap()

            val subgraphStackMap = mutableMapOf<Either<T, PartialOrderSet<T1>>, Stack<T1>>()
            val stack = Stack<T1>()

            tokens.dfsPreAndPostfix(
                { entered ->
                    if (entered != null) {
                        val destinyOpt = entered.first
                        val innerElement = entered.second

                        val destiny = if (destinyOpt.isPresent) {
                            Either.Left(destinyOpt.get())
                        } else {
                            val familyOptions = subgraphStackMap
                                .entries
                                .mapNotNull { (family, familyStack) ->
                                    if (familyStack.isEmpty()) {
                                        null
                                    } else {
                                        family to familyStack.peek()
                                    }
                                }

                            reverseFamilySelector(familyOptions, innerElement)
                                ?: Either.Right(singletonPoset(innerElement))
                        }

//                        val stack = subgraphStackMap.computeIfAbsent(destiny) {
//                            Stack<T1>()
//                        }
                        val subgraphPoset = destiny.mapEither(
                            { preValuedPosetMap[it]!! }
                        ) { it }

                        val nextPoset = if (stack.isEmpty()) {
                            subgraphPoset
                                .push(innerElement)
                        } else {
                            val parent = stack.peek()
                            allTokenEdges.add(parent to innerElement)

                            subgraphPoset
                                .push(innerElement)
                                .pushRelationship(parent, innerElement) // may be inter-cluster for new cluster
                        }.asPartialOrderSet()

                        val newDestiny = destiny.mapEither<Either<T, PartialOrderSet<T1>>>(
                            {
                                preValuedPosetMap[it] = nextPoset
                                Either.Left(it)
                            }
                        ) {
                            Either.Right(nextPoset)
                        }

                        stack.push(innerElement)

                        subgraphStackMap.remove(destiny)
                        subgraphStackMap[newDestiny] = stack
                        destinyMap[innerElement] = newDestiny
                    }
                }
            ) { exited ->
                if (exited != null) {
                    if (stack.isNotEmpty()) {
                        stack.pop()
                    }
                }
            }

            val tokenPosetMap = tokens.elements
                .map { (_, token) ->
                    token to destinyMap[token]!!.mapEither(
                        { preValuedPosetMap[it]!! }
                    ) { it }
                }
                .groupBy { it.second }
                .mapValues { (_, l) -> l.map { it.first } }

                .toMutableMap()

            var finalDocument: D? = null

            while (finalDocument == null) {
                val contractionMap = tokenPosetMap.map { (subgraphPoset, tokens) ->
                    val intermediateDocument = innerSerializer.serialize(subgraphPoset)
                    Struct3(intermediateDocument, subgraphPoset, tokens)
                }
                val tokenDocumentMap = contractionMap
                    .flatMap { it.t3.map { t -> t to it.t1 } }
                    .toMap()

                val interClusterEdges = allTokenEdges
                    .mapNotNull { (p0, p1) ->
                        val t0 = tokenDocumentMap[p0]!!
                        val t1 = tokenDocumentMap[p1]!!

                        if (t0 == t1) {
                            null
                        } else {
                            t0 to t1
                        }
                    }

                val nestedPoset = posetOf(
                    ImmutableLinkedSet(contractionMap.map { it.t1 }),
                    interClusterEdges,
                )

                val cycles = nestedPoset.findCycles()

                if (cycles.isEmpty()) {
                    finalDocument = outerSerializer.serialize(
                        nestedPoset
                    )
                } else {
                    val firstCycle = cycles.first()
                    val oldPosets = firstCycle
                        .map { t ->
                            contractionMap.first { it.t1 == t }.t2
                        }

                    val firstOldPoset = oldPosets.firstOrNull()
                    if (firstOldPoset != null) {
                        val reducedPoset = Orders.unionAll(firstOldPoset, oldPosets.drop(1))
                            .asPartialOrderSet()

                        val memberTokens = reducedPoset.elements.toList()
                        oldPosets.forEach { p -> tokenPosetMap.remove(p) }
                        tokenPosetMap[reducedPoset] = memberTokens
                    }
                }
            }

            finalDocument
        }

    private data class Struct3<T1, T2, T3>(
        val t1: T1,
        val t2: T2,
        val t3: T3,
    )
}
