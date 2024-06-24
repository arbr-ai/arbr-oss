package com.arbr.codegen.base.generator.targets

import com.arbr.codegen.base.generator.DisplayRootModel

fun interface GeneratorTargetModelMapper {
    fun transform(displayRootModel: DisplayRootModel): DisplayRootModel

    data object Id: GeneratorTargetModelMapper {
        override fun transform(displayRootModel: DisplayRootModel): DisplayRootModel {
            return displayRootModel
        }
    }
}