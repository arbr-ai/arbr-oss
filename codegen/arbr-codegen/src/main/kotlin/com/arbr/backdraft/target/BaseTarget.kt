package com.arbr.backdraft.target

import java.nio.file.Path

sealed interface BaseTarget {

    fun setBuildOutputDir(
        buildOutputDir: Path
    )
}
