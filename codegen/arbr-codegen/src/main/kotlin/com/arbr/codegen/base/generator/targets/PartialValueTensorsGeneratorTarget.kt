package com.arbr.codegen.base.generator.targets

// TODO: Decide role of value tensors vs type tensors, if any

//import com.arbr.codegen.base.dependencies.MapperConfig
//import com.arbr.codegen.base.generator.DisplayRootModel
//import com.arbr.codegen.base.generator.TemplatingEngine
//
//internal data object PartialValueTensorsGeneratorTarget : SealedGeneratorTarget() {
//    override val mapper = MapperConfig().mapper
//
//    override fun generate(displayRootModel: DisplayRootModel): List<GeneratorTargetOutput> {
//        val engine = TemplatingEngine(templateFileUrl("PartialValueTensor.kttmpl"))
//        val outputs = mutableListOf<GeneratorTargetOutput>()
//
//        displayRootModel.schema.forEach { schema ->
//            schema.table.forEach { table ->
//                val innerDisplayModel = displayRootModel.copy(schema = listOf(schema.copy(table = listOf(table))))
//                val bindings = mapper.convertValue(innerDisplayModel, mapClass)
//                val output = engine.render(bindings)
//                val targetSubpath = "partial_value_tensor/PartialValueTensor${table.titleName}.kt"
//
//                outputs.add(GeneratorTargetOutput(GeneratorTargetOutputCollection.CORE, targetSubpath, output))
//            }
//        }
//
//        return outputs
//    }
//}