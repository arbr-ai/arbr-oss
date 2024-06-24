package com.arbr.types.homotopy

import com.arbr.object_model.core.model.ArbrRootModel
import com.arbr.types.homotopy.config.DefaultHomotopyIntrospectionConfigService
import org.junit.jupiter.api.Test
import kotlin.reflect.full.starProjectedType

class PlainTypeIntrospectionTest {
    @Test
    fun `introspects plain types`() {
        val intro = PlainTypeIntrospection(DefaultHomotopyIntrospectionConfigService().getConfig())
        intro.introspectBaseType(
            ArbrRootModel::class.starProjectedType,
        )
    }

}