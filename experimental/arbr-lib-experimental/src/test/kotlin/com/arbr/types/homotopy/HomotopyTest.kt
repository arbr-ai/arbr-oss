package com.arbr.types.homotopy

import com.arbr.types.homotopy.config.HomotopyFilterSpec
import com.arbr.types.homotopy.functional.BaseHomotopyGroundMap
import com.arbr.types.homotopy.functional.HTypeConstantSpecFilterMapper
import com.arbr.types.homotopy.spec.ContractionHomotopy
import com.arbr.types.homotopy.util.HTypeIndentingStringReducer
import org.junit.jupiter.api.Test

data class AAA(
    val name: String,
    val bbb: BBB
)

data class BBB(
    val age: Int,
    val ccs: List<CCC>,
)

data class CCC(
    val double: Double,
    val parent: ATaskModel
)

data class ATaskModel(
    val branchName: String,
    val pullRequestBody: String?,
    val pullRequestHtmlUrl: String,
    val pullRequestTitle: String?,
    val taskQuery: String,
    val taskVerbosePlan: String?,
    val parent: AProjectModel,
)

data class AProjectModel(
    val description: String,
    val fullName: String,
    val platform: String?,
    val primaryLanguage: String?,
    val techStackDescription: DDD?,
    val title: String?,
    val tasks: List<ATaskModel>,
)

data class DDD(
    val id: String,
    val a: Int,
)

class HomotopyTest {

    @Test
    fun test() {
        val baseType = Homotopy.getBaseType<AAA>()

        /**
         * 1. A nullable ground mapping: BaseObj.Singleton = F -> G?
         */
        val nullableGroundMap = BaseHomotopyGroundMap<String?, String?> { singleton, context ->
//            when (singleton) {
//                is BaseHObj.HSingleton.HResourceRef -> {
//                    if (singleton.resourceKey.clazz == DDD::class.java) {
//                        println("Found class ref to ${singleton.resourceKey.clazz}")
//                        "F:" + singleton.resourceKey.clazz.simpleName
//                    } else {
//                        null
//                    }
//                }
//
//                else -> null
//            }

            null
        }

        /**
         * 2. A nullable homotopy spec (lifted to be a total homotopy on a boxed target type)
         *
         * This defines how interior nodes might admit the trait
         * It is NOT necessary to combine + propagate conformance
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
                return "node:${valueTypeImplementor.takeLast(4)}:${innerImplementors.joinToString(",") { it.takeLast(4) }}"
            }
        }

        val mapper = HTypeConstantSpecFilterMapper(nullableHomotopySpec, nullableGroundMap)
        val filtered = mapper.filterMap(
            contractionHomotopy,
            baseType,
            MutablePathContext.new(),
        )!!

        val reducer = HTypeIndentingStringReducer<String>(
            { str, _ ->
                appendLine("<${str}>")
            },
            { str, _ ->
                appendLine("</${str}>")
            },
        )

//        filtered.nodes.forEach { node ->
//            val reducedString = reducer.reduce(node)
//            println(reducedString)
//            println("=".repeat(40))
//        }


    }
}