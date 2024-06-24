package com.arbr.types.homotopy

import com.arbr.object_model.core.model.ArbrRootModel
import com.arbr.types.homotopy.config.DefaultHomotopyIntrospectionConfigService
import com.arbr.types.homotopy.util.HTypeIndentingStringReducer
import org.junit.jupiter.api.Test
import kotlin.reflect.full.starProjectedType

class HomotopyIntrospectionTest {

    @Test
    fun `introspects root`() {
        val intro = HomotopyIntrospection(DefaultHomotopyIntrospectionConfigService().getConfig())
        val baseType = intro.introspectBaseType(
            ArbrRootModel::class.starProjectedType,
            MutablePathContext.new(),
        )

        val reducer = HTypeIndentingStringReducer<PlainType>(
            { str, _ ->
                appendLine("<$str>")
            },
            { str, _ ->
                appendLine("</$str>")
            },
        )

        println(reducer.reduce(baseType))

    }
}