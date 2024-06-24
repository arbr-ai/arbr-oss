package com.arbr.data_common.base.etl.extract

data class DataExtractorConfig(
    val manifestPageMaxSize: Int = 100,
    val maxNumGroupDescriptors: Int = Int.MAX_VALUE,
    val loadResourcesParallelism: Int = 1
)