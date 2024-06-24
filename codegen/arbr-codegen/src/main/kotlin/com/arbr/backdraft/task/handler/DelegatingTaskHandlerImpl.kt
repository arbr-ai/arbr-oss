package com.arbr.backdraft.task.handler

class DelegatingTaskHandlerImpl(
    override val compilationTaskHandler: CompilationTaskHandler,
    override val conversionTaskHandler: ConversionTaskHandler,
): DelegatingTaskHandler