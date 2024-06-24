package com.arbr.data_common.base.etl.load

data class DataLoaderConfigurationProperties(
    val writeOutputParallelism: Int = 1,

    /**
     * If true, overwrite any existing file during output at the same path.
     */
    val overwriteExistingFiles: Boolean = true,
)