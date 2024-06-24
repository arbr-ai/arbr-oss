package com.arbr.backdraft.target

import com.arbr.graphql.lang.node.GraphQlLanguageNodeDocument
import java.nio.file.Path

interface ConverterTarget: BaseTarget {

    fun initialize(): ConverterProcess
    fun getBuildOutputDir(): Path?
}