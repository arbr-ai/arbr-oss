package com.arbr.platform

import com.arbr.engine.services.completions.base.ChatCompletionProvider
import com.arbr.engine.services.workflow.state.WorkflowStateService
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(
    exclude = [
        R2dbcAutoConfiguration::class,
    ],
    scanBasePackages = [
        // TODO: Centralize dependencies
        "com.arbr.platform.autoconfigure",
        "com.arbr.object_model.functions.internal.tree_parse",
        "com.arbr.object_model.functions.internal.code_eval",
        "com.arbr.object_model.functions.inference.embedding",
        "com.arbr.model_loader.loader",
        "com.arbr.engine.services.differential_content.diff_alignment",
        "com.arbr.engine.services.differential_content.formatter",
        "com.arbr.prompt_library.config",
        "com.arbr.api_server_base.service.github",
        "com.arbr.object_model.processor.config",
        "com.arbr.platform.object_graph.core",
    ]
)
@EnableScheduling
class WebDevMain

fun main(args: Array<String>) {
    val context = runApplication<WebDevMain>(*args)
    println("Hello world\n$context")

    val provider = context.getBean(WorkflowStateService::class.java)
    println(provider)

//    val runner = context.getBean(EngineRunner::class.java)
//
//    runner.runEngine(null)
//        .subscribeOn(Schedulers.boundedElastic())
//        .subscribe()
}
