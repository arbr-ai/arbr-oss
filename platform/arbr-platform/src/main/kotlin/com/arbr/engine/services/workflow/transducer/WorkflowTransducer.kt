package com.arbr.engine.services.workflow.transducer

import com.arbr.api.user_project.core.WorkflowDisplayInfo
import com.arbr.api.workflow.input.WorkflowInputModel
import com.arbr.engine.services.workflow.input.WorkflowInputDetailsProvider
import com.arbr.engine.services.workflow.input.WorkflowInputTransformer
import com.arbr.engine.services.workflow.setup.WorkflowSetterUpper
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.WorkflowResourceModel
import com.arbr.og_engine.file_system.VolumeState
import com.fasterxml.jackson.databind.ObjectMapper
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono

class WorkflowTransducer<WI : WorkflowInputModel, PI: Any> private constructor(
    private val mapper: ObjectMapper,
    private val inputDetailsProvider: WorkflowInputDetailsProvider<WI>,
    private val inputTransformer: WorkflowInputTransformer<WI, PI>,
    private val workflowSetup: WorkflowSetterUpper<PI>,
    private val workflowProjectTaskInitializer: WorkflowProjectTaskInitializer<PI>,
) {
    /**
     * Info for user-facing display of the workflow input.
     */
    val userFacingDisplayInfo: WorkflowDisplayInfo<WI>
        get() = inputDetailsProvider.userFacingDisplayInfo()

    fun setUp(
        workflowHandleId: String,
        workflowFormInput: WI,
        artifactSink: FluxSink<Artifact>,
    ): Mono<Pair<VolumeState, PI>> {
        return inputTransformer
            .transformInput(
                workflowFormInput,
                artifactSink,
            )
            .flatMap { input ->
                workflowSetup.setUp(
                    workflowHandleId,
                    userFacingDisplayInfo.id,
                    input,
                    artifactSink,
                )
                    .map { it to input }
            }
    }

    /**
     * Perform the complete workflow.
     */
    fun perform(
        workflowHandleId: String,
        workflowResourceModel: WorkflowResourceModel,
        initialVolumeState: VolumeState,
        input: PI,
        artifactSink: FluxSink<Artifact>,
    ): Mono<VolumeState> {
        return workflowProjectTaskInitializer.initialize(
            workflowHandleId,
            workflowResourceModel.root,
            initialVolumeState,
            input,
            artifactSink,
        )
            .thenReturn(initialVolumeState)
                }

    fun <V> parsing(
        paramMap: Map<String, String>,
        handler: (WI) -> V,
    ): V {
        val workflowInputModel = mapper.convertValue(paramMap, userFacingDisplayInfo.workflowInputSchema.modelClass)
        return handler(workflowInputModel)
    }

    class Builder(
        private val mapper: ObjectMapper,
    ) {
        fun <WI : WorkflowInputModel> withInputDetailsProvider(
            inputDetailsProvider: WorkflowInputDetailsProvider<WI>
        ): BuilderWithInputDetailsProvider<WI> = BuilderWithInputDetailsProvider(
            mapper,
            inputDetailsProvider,
        )
    }

    class BuilderWithInputDetailsProvider<WI : WorkflowInputModel>(
        private val mapper: ObjectMapper,
        private val inputDetailsProvider: WorkflowInputDetailsProvider<WI>,
    ) {
        fun <PI: Any> withInputTransformer(
            inputTransformer: WorkflowInputTransformer<WI, PI>
        ): BuilderWithInputTransformer<WI, PI> = BuilderWithInputTransformer(
            mapper,
            inputDetailsProvider,
            inputTransformer,
        )
    }

    class BuilderWithInputTransformer<WI : WorkflowInputModel, PI: Any>(
        private val mapper: ObjectMapper,
        private val inputDetailsProvider: WorkflowInputDetailsProvider<WI>,
        private val inputTransformer: WorkflowInputTransformer<WI, PI>,
    ) {

        fun withWorkflowSetup(
            workflowSetup: WorkflowSetterUpper<PI>
        ): BuilderWithSetup<WI, PI> = BuilderWithSetup(
            mapper,
            inputDetailsProvider,
            inputTransformer,
            workflowSetup
        )
    }

    class BuilderWithSetup<WI : WorkflowInputModel, PI: Any>(
        private val mapper: ObjectMapper,
        private val inputDetailsProvider: WorkflowInputDetailsProvider<WI>,
        private val inputTransformer: WorkflowInputTransformer<WI, PI>,
        private val workflowSetup: WorkflowSetterUpper<PI>,
    ) {
        fun withProjectTaskInitializer(
            workflowProjectTaskInitializer: WorkflowProjectTaskInitializer<PI>,
        ): BuilderWithProjectTaskInitializer<WI, PI> = BuilderWithProjectTaskInitializer(
            mapper,
            inputDetailsProvider,
            inputTransformer,
            workflowSetup,
            workflowProjectTaskInitializer,
        )
    }

    class BuilderWithProjectTaskInitializer<WI : WorkflowInputModel, PI: Any>(
        private val mapper: ObjectMapper,
        private val inputDetailsProvider: WorkflowInputDetailsProvider<WI>,
        private val inputTransformer: WorkflowInputTransformer<WI, PI>,
        private val workflowSetup: WorkflowSetterUpper<PI>,
        private val workflowProjectTaskInitializer: WorkflowProjectTaskInitializer<PI>,
    ) {

        fun build(): WorkflowTransducer<WI, PI> = WorkflowTransducer(
            mapper,
            inputDetailsProvider,
            inputTransformer,
            workflowSetup,
            workflowProjectTaskInitializer,
        )
    }

    companion object {
        fun builder(
            mapper: ObjectMapper,
        ): Builder = Builder(mapper)
    }
}
