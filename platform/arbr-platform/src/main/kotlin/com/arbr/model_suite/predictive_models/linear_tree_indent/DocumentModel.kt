package com.arbr.model_suite.predictive_models.linear_tree_indent

import com.arbr.data_structures_common.partial_order.KeyedPartialOrderSet
import com.arbr.data_structures_common.partial_order.MaskedMap
import com.arbr.data_structures_common.partial_order.Orders
import com.arbr.platform.ml.linear.typed.base.ColumnVector
import com.arbr.platform.ml.linear.typed.impl.TypedColumnVector
import com.arbr.platform.ml.linear.typed.impl.TypedMatrices
import com.arbr.platform.ml.linear.typed.impl.asScalar
import com.arbr.platform.ml.linear.typed.shape.Dim
import java.util.*
import kotlin.math.*

class DocumentModel(
    segmenterService: SegmenterService,
    vocabulary: DocumentVocabulary,
    filePath: String,
    inputDocument: String
) {
    private val normalizedDocument = normalize(inputDocument)
    val normalizedDocumentLines = normalizedDocument.lines()
    private val normalizedDocumentLineCharNumbers = normalizedDocument
        .mapIndexedNotNull { index: Int, c: Char ->
            // Assm. normalized, ignore \r
            if (c == '\n') {
                index to c
            } else {
                null
            }
        }

    val whitespaceUnit = inferWhitespaceUnit(normalizedDocumentLines)
    private val normalizedWhitespaceUnitCounts =
        normalizedLeadingWhitespaceUnitCounts(normalizedDocumentLines, whitespaceUnit)
    private val segmentation = segmenterService.segmentFileContentUniversal(
        filePath,
        normalizedDocument,
    )
    private val segmentTree = segmentation.segmentReprTree
    val nodeInfo = buildWhitespaceAndAncestorInfo(
        vocabulary,
        segmentTree,
        normalizedDocumentLineCharNumbers,
        normalizedWhitespaceUnitCounts,
    )

    private val lineIndexMap = normalizedDocument
        .asSequence()
        .map { it.toString() }
        .fold(listOf(0 to "")) { nlctl, agg ->
            if (agg == "\n") nlctl + (nlctl.last().first + 1 to agg)
            else nlctl + (nlctl.last().first to agg)
        }
        .drop(1)
        .map { it.first }
        .withIndex()
        .associate { it.index to it.value }

    fun lineIndex(charIndex: Int): Int {
        return lineIndexMap[charIndex]!!
    }

    companion object {

        private val leadingSpaceRe = Regex("^(\\s*).*?$")
        private val leadingTabRe = Regex("^(\t+)(.*?)$")

        private const val NUM_TAB_SPACES = 4
        private const val MIN_TEST_INDENT_SIZE = 2
        private const val MAX_TEST_INDENT_SIZE = 10
        private const val OFFSET_ERROR_PENALTY = 1.0
        private const val DIVISIBILITY_PENALTY = 0.1

        private const val numNearestNeighborsIncluded = 4

        private val divisibilityTaxMultipliers: Map<Int, Double> =
            (MIN_TEST_INDENT_SIZE..MAX_TEST_INDENT_SIZE).associateWith { t ->
                val dividesCount = (t..MAX_TEST_INDENT_SIZE).count { t2 ->
                    t2 % t == 0
                }

                exp(-DIVISIBILITY_PENALTY * (dividesCount - 1))
            }

        private fun normalize(text: String): String {
            return text.lines()
                .joinToString("\n") { line ->
                    val tabsMatch = leadingTabRe.matchEntire(line)
                    if (tabsMatch == null) {
                        line
                    } else {
                        val tabString = tabsMatch.groups[1]!!.value
                        val lineRemainder = tabsMatch.groups[2]!!.value
                        " ".repeat(NUM_TAB_SPACES * tabString.length) + lineRemainder
                    }
                }
        }

        /**
         * Quantize leading whitespace to multiples of the given unit, rounding as needed, and return a list of unit counts.
         */
        private fun normalizedLeadingWhitespaceUnitCounts(lines: List<String>, whitespaceUnit: Int): List<Int> {
            return lines
                .map { line ->
                    val match = leadingSpaceRe.matchEntire(line)
                        ?: throw IllegalStateException()

                    val whitespaceSize = match.groupValues[1].length

                    val multiple = whitespaceSize / whitespaceUnit
                    val remainder = whitespaceSize % whitespaceUnit

                    if (remainder < whitespaceUnit / 2) {
                        multiple
                    } else {
                        multiple + 1
                    }
                }
        }

        private fun inferWhitespaceUnit(lines: List<String>): Int {
            val leadingWhitespaceLengths = lines
                .map { line ->
                    val match = leadingSpaceRe.matchEntire(line)
                        ?: throw IllegalStateException()

                    match.groupValues[1].length
                }

            val leadingWhitespaceLengthCounts = leadingWhitespaceLengths.groupingBy { it }.eachCount()

            val sumSqErrors = (MIN_TEST_INDENT_SIZE..MAX_TEST_INDENT_SIZE).map { t ->
                t to leadingWhitespaceLengthCounts.entries.sumOf { (whitespaceLength, count) ->
                    val remainder = whitespaceLength % t

                    min(
                        remainder,
                        t - remainder,
                    ).toDouble().pow(2) * count
                }
            }

            val scores = sumSqErrors.map { (t, error) ->
                val errorScore = 1.0 / (1 + OFFSET_ERROR_PENALTY * error)
                t to errorScore * (divisibilityTaxMultipliers[t] ?: throw Exception("No divisibility tax entry for $t"))
            }

            return scores.maxByOrNull { (t, score) ->
                score
            }?.first ?: throw Exception("No options for indent inference")
        }

        private fun lineIndex(
            charIndex: Int,
            normalizedDocumentLineCharNumbers: List<Pair<Int, Char>>,
        ): Int {
            val comparator = compareBy<Pair<Int, Char>> {
                it.first
            }
            return normalizedDocumentLineCharNumbers.binarySearch(
                charIndex to 'z',
                comparator
            ).let { idx ->
                if (idx < 0) {
                    -idx - 1
                } else {
                    idx + 1
                }
            }
        }

        private fun nodeIsValid(
            node: SegmentIndexReprTreeNode
        ): Boolean {
            return node.ruleName != "miscellaneous"
        }

        private val d = 16
        private val bigN = 100
        private val r = bigN.toDouble().pow(2.0 / d)
        private val defaultNorm = sqrt(d * 1.0 / 2)

        private fun positionalEncodingSingle(
            t: Int,
        ): ColumnVector<Dim.VariableD> {
            // theta = t / r^k
            // r = bigN^(2/d)
            val vec = DoubleArray(d) { 0.0 }

            for (ki in 0 until d / 2) {
                val k0 = 2 * ki
                val k1 = k0 + 1 // maxes at d - 1
                val theta = t / r.pow(ki)

                vec[k0] = sin(theta) / defaultNorm
                vec[k1] = cos(theta) / defaultNorm
            }

            return TypedColumnVector(vec, Dim.VariableD)
        }

        private fun nodeInfoAddKNN(
            linearTreeIndentNodeInfoList: List<LinearTreeIndentNodeInfo>,
            k: Int,
        ): List<LinearTreeIndentNodeInfo> {
            val indexDifferences = linearTreeIndentNodeInfoList.map { n0 ->
                linearTreeIndentNodeInfoList.map { n1 ->
                    (n0.preIndex - n0.postIndex) - (n1.preIndex - n1.postIndex)
                }
            }

            val diffPosEncs = indexDifferences.flatten()
                .distinct()
                .associateWith { dt -> positionalEncodingSingle(dt) }

            val rawPosEncoding = positionalEncodingSingle(0)

            val newLinearTreeIndentNodeInfoList = mutableListOf<LinearTreeIndentNodeInfo>()
            for (n0 in linearTreeIndentNodeInfoList) {
                // Pos. Dist to PreIndex
                val rowNeighbors = mutableListOf<Pair<Double, Int>>()
                for (n1 in linearTreeIndentNodeInfoList) {
                    val indexDiff = (n0.preIndex - n0.postIndex) - (n1.preIndex - n1.postIndex)
                    val diffPosEnc = diffPosEncs[indexDiff]!!

                    val diffDiagMatrix = TypedMatrices.diag(diffPosEnc, rawPosEncoding.numRowsShape)
                    val scaledPosColumn = diffDiagMatrix.mult(rawPosEncoding)
                    val normDistM = rawPosEncoding.transpose().mult(scaledPosColumn).asScalar()

                    val normDist = normDistM.value.absoluteValue
                    rowNeighbors.add(normDist to n1.preIndex)
                }

                val nearestNeighborPreIndexes = rowNeighbors.sortedBy { it.first }.take(k).map { it.second }
                newLinearTreeIndentNodeInfoList.add(
                    n0.copy(
                        nearestNeighborPreIndexes = nearestNeighborPreIndexes
                    )
                )
            }

            return linearTreeIndentNodeInfoList
        }

        private fun buildWhitespaceAndAncestorInfo(
            combinedVocabulary: DocumentVocabulary,
            segmentTree: SegmentIndexReprTree,
            normalizedDocumentLineCharNumbers: List<Pair<Int, Char>>,
            normalizedWhitespaceUnitCounts: List<Int>,
        ): List<LinearTreeIndentNodeInfo> {

            // Note the keyed index here is just insertion order in this list - don't interpret it as DFS order
            val nodeList = mutableListOf<Orders.IndexKeyedValue<SegmentIndexReprTreeNode>>()
            val nodeMap = mutableMapOf<Int, KeyedPartialOrderSet.KeyNode<Int>>()

            fun dfsInto(node: SegmentIndexReprTree, parentIndex: Int?): Int {
                val index = nodeList.size
                nodeList.add(Orders.IndexKeyedValue(index, node.toNode()))

                val childSet = mutableSetOf<Int>()
                for (child in node.childElements) {
                    val childIndex = dfsInto(child, index)
                    childSet.add(childIndex)
                }

                nodeMap[index] = KeyedPartialOrderSet.KeyNode(
                    index,
                    setOfNotNull(parentIndex),
                    childSet,
                )

                return index
            }
            dfsInto(segmentTree, null)

            val poset = KeyedPartialOrderSet(
                nodeList.associateBy { it.key },
                MaskedMap(nodeMap, mutableSetOf()) { elt, masks ->
                    elt.copy(
                        inferiors = elt.inferiors - masks,
                        superiors = elt.superiors - masks,
                    )
                }
            )

            val nodesByLine = nodeList.groupBy {
                lineIndex(it.element.startIndex, normalizedDocumentLineCharNumbers)
            }
            val nodesByEndLine = nodeList.groupBy {
                lineIndex(it.element.endIndex, normalizedDocumentLineCharNumbers)
            }

            // Lists parallel to `nodeList`
            val nodePreorderIndexList = MutableList(nodeList.size) { -1 }
            val nodePostorderIndexList = MutableList(nodeList.size) { -1 }

            val ancestorList: MutableList<List<Int>?> = MutableList(nodeList.size) { null }
            val whitespaceCountList: MutableList<Int> = MutableList(nodeList.size) { -1 }
            val whitespaceCountChangeList: MutableList<Int> = MutableList(nodeList.size) { -1 }

            val nodeIndexStack = Stack<Int>().also { it.push(combinedVocabulary.reservedIndices.first()) }
            val whitespaceUnitCountStack = Stack<Int>().also { it.push(0) }
            var preIndex = 0
            var postIndex = 0
            poset.dfsPreAndPostfix(
                { node ->
                    if (node != null) {
                        ancestorList[node.key] = nodeIndexStack.toList()
                        val lineIndex = lineIndex(node.element.startIndex, normalizedDocumentLineCharNumbers)
                        val indentUnitCount = normalizedWhitespaceUnitCounts[
                            lineIndex
                        ]
                        whitespaceCountList[node.key] = indentUnitCount
                        whitespaceCountChangeList[node.key] = indentUnitCount - whitespaceUnitCountStack.peek()
                        whitespaceUnitCountStack.push(indentUnitCount)

                        val ruleName = node.element.ruleName
                        nodeIndexStack.push(combinedVocabulary.encode(ruleName))
                        nodePreorderIndexList[node.key] = preIndex
                        preIndex++
                    }
                },
                { node ->
                    if (node != null) {
                        nodeIndexStack.pop()
                        whitespaceUnitCountStack.pop()
                        nodePostorderIndexList[node.key] = postIndex
                        postIndex++
                    }
                },
            )

            val linearTreeIndentNodeInfoList = nodeList.map { indexedNode ->
                val preorderIndex = nodePreorderIndexList[indexedNode.key]
                val postorderIndex = nodePostorderIndexList[indexedNode.key]
                val ancestors = ancestorList[indexedNode.key]
                val whitespaceUnitCount = whitespaceCountList[indexedNode.key]
                val whitespaceUnitCountChange = whitespaceCountChangeList[indexedNode.key]
                val lineIndex = lineIndex(indexedNode.element.startIndex, normalizedDocumentLineCharNumbers)
                val lineMates = nodesByLine[lineIndex]

//                val endLineIndex = lineIndex(indexedNode.element.endIndex, normalizedDocumentLineCharNumbers)
//                val endLineMates = nodesByLine[lineIndex]

                val prevLineNodes = nodesByLine[lineIndex - 1] ?: emptyList()
                val prevLineWhitespace = prevLineNodes.firstOrNull()?.let { prevNode ->
                    whitespaceCountList[prevNode.key]
                } ?: 0

                val postLineNodes = nodesByLine[lineIndex + 1] ?: emptyList()
                val postLineWhitespace = prevLineNodes.firstOrNull()?.let { postNode ->
                    whitespaceCountList[postNode.key]
                } ?: 0

//                Invariants.check { require ->
//                    require(ancestors != null)
//                    require(lineMates != null)
//                    require(whitespaceUnitCount >= 0)
//                    require(preorderIndex >= 0 && postorderIndex >= 0)
//                }

                val lineMateCodes = lineMates!!.map {
                    combinedVocabulary.encode(it.element.ruleName)
                }
                val prevLineMateCodes = prevLineNodes.map {
                    combinedVocabulary.encode(it.element.ruleName)
                }
                val postLineMateCodes = postLineNodes.map {
                    combinedVocabulary.encode(it.element.ruleName)
                }

                LinearTreeIndentNodeInfo(
                    preorderIndex,
                    postorderIndex,
                    indexedNode.element,
                    ancestors ?: throw IllegalStateException(),
                    lineMateCodes,
                    whitespaceUnitCount,
                    whitespaceUnitCountChange,
                    emptyList(),
                    prevLineMateCodes,
                    prevLineWhitespace,
                    postLineMateCodes,
                    postLineWhitespace,
                )
            }.filter { nodeIsValid(it.node) }

            return nodeInfoAddKNN(
                linearTreeIndentNodeInfoList,
                k = numNearestNeighborsIncluded,
            )
        }
    }
}
