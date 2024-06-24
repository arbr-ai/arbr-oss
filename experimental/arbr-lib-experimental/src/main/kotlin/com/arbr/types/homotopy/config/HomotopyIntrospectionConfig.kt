package com.arbr.types.homotopy.config

import com.arbr.types.homotopy.PlainType
import com.arbr.types.homotopy.spec.HomotopySpec

data class HomotopyIntrospectionConfig(
    val allowedResourcePackages: Collection<String>,
    val baseHomotopySpec: HomotopySpec<PlainType>,
)
