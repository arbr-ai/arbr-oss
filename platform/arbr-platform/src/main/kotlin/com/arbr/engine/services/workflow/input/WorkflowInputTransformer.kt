package com.arbr.engine.services.workflow.input

import com.arbr.api.workflow.input.WorkflowInputModel
import com.arbr.og_engine.artifact.Artifact
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono

fun interface WorkflowInputTransformer<WI : WorkflowInputModel, PI> {
    fun transformInput(
        workflowFormInput: WI,
        artifactSink: FluxSink<Artifact>,
    ): Mono<PI>

    companion object {
        fun <WI : WorkflowInputModel> identity(): WorkflowInputTransformer<WI, WI> = WorkflowInputTransformer { input, _ ->
            Mono.just(input)
        }
    }
}
