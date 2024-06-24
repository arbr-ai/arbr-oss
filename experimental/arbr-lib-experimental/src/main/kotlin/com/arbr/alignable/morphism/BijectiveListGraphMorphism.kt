package com.arbr.alignable.morphism

import com.arbr.platform.alignable.alignable.Alignable
import com.arbr.platform.alignable.alignable.AlignableProxy
import com.arbr.platform.alignable.alignable.graph.AlignableHomogenousRootedListDAG
import com.arbr.platform.alignable.alignable.graph.HomogenousRootedListDAG
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono

abstract class BijectiveListGraphMorphism<SourceType: Any, V, ParsedNodeType, E : Alignable<E, O>, O> {

    abstract fun encodeElement(sourceNodeValue: V): E

    abstract fun applyInnerMap(sourceGraph: AlignableHomogenousRootedListDAG<E, O>): Mono<AlignableHomogenousRootedListDAG<E, O>>

    /**
     * In the inverse mapping stage, convert a source node whose value was preserved or aligned in place into a parsed
     * node type.
     */
    abstract fun convertPreservedSourceNode(
        sourceNodeValue: V,
        alignedValue: E,
        parent: ParsedNodeType?
    ): ParsedNodeType

    /**
     * In the inverse mapping stage, construct a new node of the parsed node type.
     */
    abstract fun constructNewParsedNode(resultNodeValue: E, parent: ParsedNodeType?): ParsedNodeType

    abstract fun buildGraphFromSourceObject(source: SourceType): HomogenousRootedListDAG<V>

    abstract fun buildSourceObjectFromGraph(resultGraph: HomogenousRootedListDAG<ParsedNodeType>): SourceType

    fun apply(source: SourceType): Mono<SourceType> {
        val pairedDAG: AlignableHomogenousRootedListDAG<AlignableProxy<V, E, O>, O> = buildGraphFromSourceObject(source).flatMapAlignable {
            AlignableProxy(it, encodeElement(it))
        }

        val flatSourceGraph = pairedDAG.flatMapAlignable { it.alignableElement }
        val targetGraphMono = applyInnerMap(flatSourceGraph)

        return targetGraphMono.flatMap { targetGraph ->
            val pairedTargetDAG: AlignableHomogenousRootedListDAG<AlignableProxy<V, E, O>, O> = targetGraph.flatMapAlignable {
                AlignableProxy(null, it)
            }

            val pairedAlignment = pairedDAG
                .align(pairedTargetDAG)

            val resultPairedGraph = pairedDAG.applyAlignment(pairedAlignment.operations)
            val resultDecodedGraph = resultPairedGraph.mapWithParent<ParsedNodeType> { alignableProxy, u ->
                val sourceValue = alignableProxy.sourceValue
                if (sourceValue == null) {
                    constructNewParsedNode(alignableProxy.alignableElement, u)
                } else {
                    convertPreservedSourceNode(sourceValue, alignableProxy.alignableElement, u)
                }
            }

            Mono.just(buildSourceObjectFromGraph(resultDecodedGraph))
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BijectiveListGraphMorphism::class.java)
    }
}
