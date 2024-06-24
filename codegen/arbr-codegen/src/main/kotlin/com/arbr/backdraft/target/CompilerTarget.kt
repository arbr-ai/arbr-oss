package com.arbr.backdraft.target

import com.arbr.graphql.lang.common.GraphQlNodeProcessor

interface CompilerTarget: BaseTarget {

    /**
     * Create a _new_ node processor.
     */
    fun nodeProcessor(): GraphQlNodeProcessor

    fun initialize(): CompilationProcess {
        return CompilationProcess(nodeProcessor())
    }
}

