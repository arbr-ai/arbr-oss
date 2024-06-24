package com.arbr.core_web_dev.workflow.input.transformer

import com.arbr.core_web_dev.workflow.input.model.IterationTaskContainer
import com.arbr.core_web_dev.workflow.input.model.WorkflowInputModelFeatureQuery
import com.arbr.engine.services.workflow.input.WorkflowInputTransformer
import com.arbr.object_model.core.resource.ArbrProject
import com.arbr.object_model.core.resource.ArbrTask
import com.arbr.og_engine.artifact.Artifact
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono

@Component
class MultiFeatureWorkflowInputTransformer : WorkflowInputTransformer<
        WorkflowInputModelFeatureQuery,
        IterationTaskContainer,
        > {

    override fun transformInput(
        workflowFormInput: WorkflowInputModelFeatureQuery,
        artifactSink: FluxSink<Artifact>,
    ): Mono<IterationTaskContainer> {
        return Mono.just(
            IterationTaskContainer(
                ArbrProject.FullName.input(workflowFormInput.projectFullName),
                ArbrProject.Platform.constant("Web"),
                ArbrProject.Description.constant("A basic web application."),
                ArbrTask.TaskQuery.input(workflowFormInput.taskQuery),
                workflowFormInput.baseBranch?.let { baseBranch -> ArbrTask.BranchName.input(baseBranch) },
            )
        )
    }
}
