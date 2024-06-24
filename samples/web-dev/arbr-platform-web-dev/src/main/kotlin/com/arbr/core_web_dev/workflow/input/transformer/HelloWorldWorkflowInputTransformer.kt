package com.arbr.core_web_dev.workflow.input.transformer

import com.arbr.core_web_dev.workflow.input.model.IterationTaskContainer
import com.arbr.core_web_dev.workflow.input.model.WorkflowInputModelBase
import com.arbr.engine.services.workflow.input.WorkflowInputTransformer
import com.arbr.object_model.core.resource.ArbrProject
import com.arbr.object_model.core.resource.ArbrTask
import com.arbr.og_engine.artifact.Artifact
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono

@Component
class HelloWorldWorkflowInputTransformer : WorkflowInputTransformer<
        WorkflowInputModelBase,
        IterationTaskContainer,
        > {

    override fun transformInput(
        workflowFormInput: WorkflowInputModelBase,
        artifactSink: FluxSink<Artifact>
    ): Mono<IterationTaskContainer> {
        return Mono.just(
            IterationTaskContainer(
                ArbrProject.FullName.input(workflowFormInput.projectFullName),
                ArbrProject.Platform.constant("Web"),
                ArbrProject.Description.constant("A simple Web application."),
                ArbrTask.TaskQuery.constant(
                    "Add a greeting message to the README file."
                ),
                workflowFormInput.baseBranch?.let { baseBranch -> ArbrTask.BranchName.input(baseBranch) },
            )
        )
    }
}
