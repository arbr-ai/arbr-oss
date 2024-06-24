package com.arbr.types.homotopy

import com.arbr.types.homotopy.config.HomotopyFilterSpec
import com.arbr.types.homotopy.functional.BaseHomotopyGroundMap
import com.arbr.types.homotopy.functional.HTypeConstantSpecFilterMapper
import com.arbr.types.homotopy.spec.ContractionHomotopy
import com.arbr.types.homotopy.util.HTypeIndentingStringReducer
import org.junit.jupiter.api.Test

class HomotopyContractionsTest {

    /**
     * Placeholder
     */
    data class DiffPatchTestCase(
        val name: String
    )

    /**
     * Placeholder
     */
    data class ArbrRootModel(
        val name: String
    )

    @Test
    fun `contracts to string fields`() {
        val baseType = Homotopy.getBaseType<DiffPatchTestCase>()

        /**
         * 1. A nullable ground mapping: BaseObj.Singleton = F -> G?
         */
        val nullableGroundMap = BaseHomotopyGroundMap<String?, String?> { singleton, context ->
            println("Evaluating base transform at path: ${context.pathString} - $singleton")
            when (singleton) {
                is PlainType.Leaf -> context.pathString
                is PlainType.ListOf -> null
                is PlainType.MapOf -> null
                is PlainType.NullableOf -> null
                is PlainType.Ref -> null
            }
        }

        /**
         * 2. A nullable homotopy spec (lifted to be a total homotopy on a boxed target type)
         *
         * This defines how interior nodes might admit the trait
         * It is NOT necessary to combine + propagate conformance
         *
         * Empty here since we only want String fields
         */
        val nullableHomotopySpec = HomotopyFilterSpec.configure<String> {}

        /**
         * 3. Conformance to partial non-null homotopy in the target field, specifically for Contractible compositions
         */
        val contractionHomotopy = object : ContractionHomotopy<String> {

            override fun liftNode(
                context: MutablePathContext,
                valueTypeImplementor: String,
                innerImplementors: List<String>
            ): String {
                println("(C) Lifting node at path: ${context.pathString}")
                return "node $valueTypeImplementor"
            }
        }

        val mapper = HTypeConstantSpecFilterMapper(nullableHomotopySpec, nullableGroundMap)
        val filtered = mapper.filterMap(
            contractionHomotopy,
            baseType,
            MutablePathContext.new(),
        )

        val reducer = HTypeIndentingStringReducer<String>(
            { str, _ ->
                appendLine("<${str}>")
            },
            { str, _ ->
                appendLine("</${str}>")
            },
        )

        filtered.forEach { node ->
            println("=".repeat(16))
            val reducedString = reducer.reduce(node.nodeHType)
            println(reducedString)
            println("=".repeat(16))
        }
    }

    @Test
    fun `contracts to classes`() {
        val baseType = Homotopy.getBaseType<ArbrRootModel>()

        /**
         * 1. A nullable ground mapping: BaseObj.Singleton = F -> G?
         */
        val nullableGroundMap = BaseHomotopyGroundMap<String?, String?> { singleton, context ->

            when (singleton) {
                is PlainType.Leaf -> null
                is PlainType.ListOf -> null
                is PlainType.MapOf -> null
                is PlainType.NullableOf -> null
                is PlainType.Ref -> {
                    // Look for cycles
//                    if (singleton.kType == ArbrRootModel::class.starProjectedType) {
//                        println("Including base transform at path: ${context.pathString} - $singleton")
//                        context.pathString
//                    } else {
//                        null
//                    }

                    context.pathString
                }
            }
        }

        /**
         * 2. A nullable homotopy spec (lifted to be a total homotopy on a boxed target type)
         *
         * This defines how interior nodes might admit the trait
         * It is NOT necessary to combine + propagate conformance
         *
         * Empty here since we only want String fields
         */
        val nullableHomotopySpec = HomotopyFilterSpec.configure<String> {
            liftNode { context, valueTypeImplementor, innerImplementors ->
                if (valueTypeImplementor == null) {
                    println("Excluding interior node at path ${context.pathString}")
                    null
                } else {
                    println("Including interior node at path ${context.pathString} - $valueTypeImplementor")
                    valueTypeImplementor
                }

            }
        }

        /**
         * 3. Conformance to partial non-null homotopy in the target field, specifically for Contractible compositions
         */
        val contractionHomotopy = object : ContractionHomotopy<String> {

            override fun liftNode(
                context: MutablePathContext,
                valueTypeImplementor: String,
                innerImplementors: List<String>
            ): String {
                println("(C) Lifting node at path: ${context.pathString}")
                return "node"
            }
        }

        val mapper = HTypeConstantSpecFilterMapper(nullableHomotopySpec, nullableGroundMap)
        val filtered = mapper.filterMap(
            contractionHomotopy,
            baseType,
            MutablePathContext.new(),
        )

        val reducer = HTypeIndentingStringReducer<String>(
            { str, _ ->
                appendLine("<${str}>")
            },
            { str, _ ->
                appendLine("</${str}>")
            },
        )

        filtered.forEach { node ->
            println("Base:")
            println(node.nodeHType.baseValue)
            println("Tree reduced:")
            println(reducer.reduce(node.nodeHType))
            println("-".repeat(10))
        }
    }
}