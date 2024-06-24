package com.arbr.codegen.base.generator.targets

// Disabled - only needs to exist in root
//internal data object CollectionTypesGeneratorTarget : SealedGeneratorTarget() {
//    override val mapper = MapperConfig().mapper
//
//    override fun generate(
//        displayRootModel: DisplayRootModel
//    ): List<GeneratorTargetOutput> {
//        val engine = TemplatingEngine(templateFileUrl("CollectionObjectType.kttmpl"))
//
//        val numCollectionTypes = 8
//        val bindings = (1..numCollectionTypes).map { i ->
//            mapOf(
//                "value" to i.toString(),
//                "innerIndex" to (1..i).map { j ->
//                    mapOf("value" to j.toString())
//                },
//                "innerIndexFromTwo" to (2..i).map { j ->
//                    mapOf("value" to j.toString())
//                },
//                "innerIndexExceptLast" to (1 until i).map { j ->
//                    mapOf("value" to j.toString())
//                },
//                "innerIndexLast" to listOf(mapOf("value" to i.toString())),
//            )
//        }.let { mapOf(
//            "packageDomain" to displayRootModel.packageDomain,
//            "outerIndex" to it
//        ) }
//
//        val output = engine.render(bindings)
//        val targetSubpath = "ArbrCollectionObjectTypes.kt"
//
//        return listOf(
//            GeneratorTargetOutput(
//                GeneratorTargetOutputCollection.CORE,
//                targetSubpath,
//                output,
//            )
//        )
//    }
//}