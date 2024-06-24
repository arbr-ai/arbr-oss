package com.arbr.types.homotopy

import com.arbr.object_model.core.model.ArbrFileModel
import com.arbr.types.homotopy.util.HTypeIndentingStringReducer
import org.junit.jupiter.api.Test

class DefaultHomotopyProviderTest {

    @Test
    fun `builds contraction for leaf filter`() {
        val baseType = Homotopy.getBaseType<ArbrFileModel>()

        val nameLeaves = baseType
            .contraction()
            .mapPrimitivesNotNull { plainType, pathContext ->
                if (pathContext.pathString.contains("name", ignoreCase = true)) {
                    plainType
                } else {
                    null
                }
            }()

        nameLeaves.forEach {
            println(it.nodeHType.baseValue)
        }
    }

    @Test
    fun `builds contraction with node lifting`() {
        val baseType = Homotopy.getBaseType<ArbrFileModel>()

        val nameSheaf = baseType
            .contraction()
            .mapPrimitivesNotNull { _, pathContext ->
                if (pathContext.pathString.contains("content", ignoreCase = true)) {
                    pathContext
                } else {
                    null
                }
            }
            .liftNodes { pathContext, valuePathContext, _ ->
                if (valuePathContext != null) {
                    pathContext
                } else {
                    null
                }
            }()

        nameSheaf.forEach {
            println(it.nodeHType.baseValue)
        }
    }

    @Test
    fun `constructs shape of tree`() {
        val baseType = Homotopy.getBaseType<ArbrFileModel>()

        val shape = baseType
            .contraction()
            .mapPrimitivesNotNull { _, _ ->
                1
            }
            .liftNodes { _, nodeValue, childValues ->
                // Count value + child nodes, but ignore their particular values
                (childValues + nodeValue).count { it != null }.takeIf { it > 0 }
            }()

        shape.forEach {
            val reduced = HTypeIndentingStringReducer<Int>(
                { count, _ ->
                    appendLine(count.toString())
                },
                { _, _ ->
                    appendLine("")
                },
            ).reduce(it.nodeHType)
            println(reduced)
        }
    }

    @Test
    fun `counts cumulative nodes`() {
        val baseType = Homotopy.getBaseType<ArbrFileModel>()

        val shape = baseType
            .contraction()
            .mapPrimitivesNotNull { _, _ ->
                1
            }
            .liftNodes { _, nodeValue, childValues ->
                // Count value + child nodes, but ignore their particular values
                (childValues + nodeValue).filterNotNull().sum().takeIf { it > 0 }
            }()

        shape.forEach {
            val reduced = HTypeIndentingStringReducer<Int>(
                { count, _ ->
                    appendLine(count.toString())
                },
                { _, _ ->
                    appendLine("")
                },
            ).reduce(it.nodeHType)
            println(reduced)
        }
    }

}