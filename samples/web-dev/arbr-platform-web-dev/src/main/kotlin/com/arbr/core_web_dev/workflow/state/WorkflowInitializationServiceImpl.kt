package com.arbr.core_web_dev.workflow.state

//import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
//import com.arbr.engine.services.workflow.state.WorkflowInitializationService
//import com.arbr.object_model.core.partial.PartialRoot
//import com.arbr.object_model.core.resource.ArbrProject
//import com.arbr.object_model.core.resource.ArbrRoot
//import com.arbr.object_model.core.resource.provider.ArbrProjectStreamProvider
//import com.arbr.object_model.core.resource.provider.ArbrRootStreamProvider
//import com.arbr.object_model.core.resource.provider.ArbrTaskStreamProvider
//import com.arbr.object_model.core.types.ArbrForeignKey
//import com.arbr.object_model.core.types.ArbrResourceKey
//import com.arbr.object_model.core.types.naming.NamedPropertyKey
//import com.arbr.object_model.engine.delegator.WorkflowRootDelegator
//import com.arbr.og.object_model.common.model.PropertyIdentifier
//import com.arbr.og.object_model.common.model.PropertyKeyRelationship
//import com.arbr.og.object_model.common.model.ProposedForeignKeyCollectionStream
//import com.arbr.og.object_model.common.model.collections.OneToManyResourceMap
//import com.arbr.platform.object_graph.impl.ObjectRef
//import com.arbr.og_engine.artifact.Artifact
//import com.arbr.og_engine.core.WorkflowResourceModel
//import com.arbr.og_engine.file_system.VolumeState
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.ComponentScan
//import org.springframework.context.annotation.Configuration
//import reactor.core.publisher.FluxSink
//import reactor.core.publisher.Mono
//import java.util.*
//
//class WorkflowInitializationServiceImpl(
//    private val workflowRootDelegator: WorkflowRootDelegator,
//) : WorkflowInitializationService {
//    private fun uuid() = UUID.randomUUID().toString()
//
//    override fun beginUpdates(
//        workflowResourceModel: WorkflowResourceModel,
//        volumeState: VolumeState,
//        artifactSink: FluxSink<Artifact>
//    ): Mono<Void> {
//        val rootUuid = workflowResourceModel.root.uuid
//
//        // Fake collection to initiate notification polling
//        val rootCollection = OneToManyResourceMap<ArbrRoot, PartialRoot, ArbrForeignKey>(
//            UUID.randomUUID().toString(),
//            ProposedForeignKeyCollectionStream(
//                object : PropertyIdentifier {
//                    override val resourceKey = ArbrResourceKey.ARBR__ROOT
//                    override val resourceUuid: String = rootUuid
//                    override val propertyKey = object : NamedPropertyKey {
//                        override val name: String
//                            get() = "root"
//                        override val ordinal: Int
//                            get() = 0
//                    }
//                    override val relationship: PropertyKeyRelationship =
//                        PropertyKeyRelationship.CHILD_COLLECTION
//                },
//                ArbrForeignKey.ARBR__TASK__FK__PARENT,
//            )
//        )
//
//        // Begin propagating notifications
//        return workflowRootDelegator.subscribeToUpdates(
//            volumeState,
//            workflowResourceModel,
//            parentResourceUuid = null, // Root object
//            rootCollection,
//            artifactSink
//        ).then()
//    }
//
//    override fun createWorkflowResourceModel(
//        userId: Long,
//        workflowHandleId: String,
//        projectFullName: String,
//        volumeState: VolumeState,
//        artifactSink: FluxSink<Artifact>,
//        preloadFromWorkflowHandleId: Long?
//    ): WorkflowResourceModel {
//        val rootResourceStreamProvider = ArbrRootStreamProvider()
//        val projectResourceStreamProvider = ArbrProjectStreamProvider()
//        val taskResourceStreamProvider = ArbrTaskStreamProvider()
//
//        // Create a workflow root
//        val root = rootResourceStreamProvider.provide(
//            uuid = UUID.randomUUID().toString(),
//        )
//
//        val projectFullNameValue = ArbrProject.FullName.materialized(projectFullName)
//
//        val project = projectResourceStreamProvider.provide(
//            uuid = uuid(),
//            fullName = projectFullNameValue,
//            title = null,
//            platform = null,
//            primaryLanguage = null,
//            techStackDescription = null,
//            description = null,
//            parent = ObjectRef.OfResource(root),
//        )
//
//        val task = taskResourceStreamProvider.provide(
//            uuid = uuid(),
//            taskQuery = null,
//            taskVerbosePlan = null,
//            pullRequestTitle = null,
//            pullRequestBody = null,
//            branchName = null,
//            pullRequestHtmlUrl = null,
//            parent = ObjectRef.OfResource(project),
//        )
//        project.tasks.items.propose(
//            ImmutableLinkedMap(
//                task.uuid to ObjectRef.OfResource(task)
//            )
//        )
//
//        return WorkflowResourceModel(
//            workflowHandleId,
//            root,
//        )
//    }
//}