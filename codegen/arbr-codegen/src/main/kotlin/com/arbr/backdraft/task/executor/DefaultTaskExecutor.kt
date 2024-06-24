package com.arbr.backdraft.task.executor

import com.arbr.backdraft.task.handler.DefaultCompilationTaskHandler
import com.arbr.backdraft.task.handler.DefaultConversionTaskHandler
import com.arbr.graphql_compiler.core.SchemaSource

class DefaultTaskExecutor(
    schemaSources: List<SchemaSource>
): DelegatingTaskExecutor(
    DefaultCompilationTaskHandler(schemaSources),
    DefaultConversionTaskHandler(schemaSources),
)