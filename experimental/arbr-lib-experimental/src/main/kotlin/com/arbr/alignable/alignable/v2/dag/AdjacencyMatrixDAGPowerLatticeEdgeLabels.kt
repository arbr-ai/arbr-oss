package com.arbr.alignable.alignable.v2.dag

class AdjacencyMatrixDAGPowerLatticeEdgeLabels(
    val weightMatrix: Array<Array<PowerLatticeVector?>>,
) {

    override fun toString(): String {
        val n = weightMatrix.size
        val rowsString = weightMatrix.joinToString("\n") { r ->
            " [ " + r.joinToString(", ") {
                it?.toString() ?: " ".repeat(2 * n + 1)
            } + " ]"
        }

        return "[\n$rowsString\n]"
    }

    companion object {

        fun ofDag(adjacencyMatrixDAG: AdjacencyMatrixDAGValued<*>): AdjacencyMatrixDAGPowerLatticeEdgeLabels {
            // Assumes rooted
            val n = adjacencyMatrixDAG.matrix.size
            val matrix = Array<Array<PowerLatticeVector?>>(n) {
                arrayOfNulls(n)
            }

            // Leaves: i : A[i, *] = 0
            val remainingIndices = (0 until n).toMutableSet()

            var isFirstRound = true
            while (remainingIndices.isNotEmpty()) {
                val newlyVisited = mutableSetOf<Int>()

                for (i in remainingIndices) {
                    val row = adjacencyMatrixDAG.matrix[i]
                    var isEligible = true
                    val plvArray = Array(n) { 0 }
                    for ((j, isChild) in row.withIndex()) {
                        if (!isChild) {
                            continue
                        }

                        // Do not jump levels in one outer pass; breaks leaf special case
                        if (j in newlyVisited) {
                            isEligible = false
                            break
                        }

                        val childEdgePlv = matrix[i][j]
                        if (childEdgePlv == null) {
                            isEligible = false
                            break
                        }

                        println("$i, $j  $childEdgePlv")

                        for ((k, pk) in childEdgePlv.powerArray.withIndex()) {
                            plvArray[k] += pk
                        }

                        println(PowerLatticeVector(plvArray))
                    }

                    if (!isEligible) {
                        continue
                    }

                    println("$i eligible $isFirstRound")

                    if (isFirstRound) {
                        // Leaf - set corresponding power to 1
                        plvArray[i] = 1
                    }

                    newlyVisited.add(i)

                    // Set inbound edge labels
                    val plv = PowerLatticeVector(plvArray)
                    for (j in (0 until n)) {
                        if (adjacencyMatrixDAG.matrix[j][i]) {
                            println("$j, $i = $plv")
                            matrix[j][i] = plv
                        }
                    }
                }

                isFirstRound = false
                remainingIndices.removeAll(newlyVisited)
            }

            return AdjacencyMatrixDAGPowerLatticeEdgeLabels(matrix)
        }
    }
}