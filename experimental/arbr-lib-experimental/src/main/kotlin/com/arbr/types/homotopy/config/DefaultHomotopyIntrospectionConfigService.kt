package com.arbr.types.homotopy.config

import com.arbr.types.homotopy.BaseHomotopySpec
import com.arbr.types.homotopy.PlainType
import com.arbr.types.homotopy.spec.HomotopySpec

class DefaultHomotopyIntrospectionConfigService: HomotopyIntrospectionConfigService {
    private val allowedResourcePackages: Collection<String> = setOf(
        "com.arbr",
        "com.arbr.ml",
        "java.lang",
        "java.util",
        "kotlin",
    )
    private val baseHomotopySpec: HomotopySpec<PlainType> = BaseHomotopySpec
    private val defaultHomotopyIntrospectionConfig = HomotopyIntrospectionConfig(
        allowedResourcePackages,
        baseHomotopySpec,
    )

    override fun getConfig(): HomotopyIntrospectionConfig {
        return defaultHomotopyIntrospectionConfig
    }
}
