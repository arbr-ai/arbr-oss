package com.arbr.platform

import com.arbr.engine.services.completions.base.ChatCompletionProvider
import com.arbr.engine.services.workflow.state.WorkflowStateService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(
    exclude = [
        R2dbcAutoConfiguration::class,
    ],
    scanBasePackages = ["com.arbr.object_model.processor.config"]
)
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
