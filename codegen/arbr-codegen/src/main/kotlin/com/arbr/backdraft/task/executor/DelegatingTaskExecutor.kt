package com.arbr.backdraft.task.executor

import com.arbr.backdraft.task.handler.CompilationTaskHandler
import com.arbr.backdraft.task.handler.ConversionTaskHandler
import com.arbr.backdraft.task.handler.DelegatingTaskHandler
import com.arbr.backdraft.task.handler.DelegatingTaskHandlerImpl

abstract class DelegatingTaskExecutor(
    override val taskHandler: DelegatingTaskHandler,
): BaseTaskExecutor(taskHandler) {

    constructor(
        compilationTaskHandler: CompilationTaskHandler,
        conversionTaskHandler: ConversionTaskHandler,
    ): this(DelegatingTaskHandlerImpl(compilationTaskHandler, conversionTaskHandler))
}

