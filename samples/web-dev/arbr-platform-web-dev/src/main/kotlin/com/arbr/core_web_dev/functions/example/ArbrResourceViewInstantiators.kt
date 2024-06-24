package com.arbr.core_web_dev.functions.example

import com.arbr.object_model.core.types.*
import com.arbr.object_model.core.view.*
import com.arbr.og.object_model.common.functions.platform.ResourceViewDomainInstantiators
import com.arbr.og.object_model.common.functions.platform.ResourceViewInstantiator
import com.arbr.og.object_model.common.functions.platform.ResourceViewInstantiators
import com.arbr.og.object_model.common.model.view.ProposedValueStreamViewProvider
import java.util.*

class ArbrResourceViewInstantiators(
    private val resourceViewProviderFactory: ResourceViewProviderFactory,
    private val resourceStreamProviderFactory: ResourceStreamProviderFactory,
    private val proposedValueStreamViewProvider: ProposedValueStreamViewProvider<ArbrResourceKey>,
) : ResourceViewDomainInstantiators<ArbrResourceKey> {
    private fun getArbrResource(resourceViewClass: Class<out ResourceView<*>>): ArbrResource {
        return when (resourceViewClass) {
            ArbrCommitView::class.java -> Arbr.Commit
            ArbrCommitEvalView::class.java -> Arbr.CommitEval
            ArbrCommitRelevantFileView::class.java -> Arbr.CommitRelevantFile
            ArbrFileView::class.java -> Arbr.File
            ArbrFileOpView::class.java -> Arbr.FileOp
            ArbrFileSegmentView::class.java -> Arbr.FileSegment
            ArbrFileSegmentOpView::class.java -> Arbr.FileSegmentOp
            ArbrFileSegmentOpDependencyView::class.java -> Arbr.FileSegmentOpDependency
            ArbrProjectView::class.java -> Arbr.Project
            ArbrRootView::class.java -> Arbr.Root
            ArbrSubtaskView::class.java -> Arbr.Subtask
            ArbrSubtaskEvalView::class.java -> Arbr.SubtaskEval
            ArbrSubtaskRelevantFileView::class.java -> Arbr.SubtaskRelevantFile
            ArbrTaskView::class.java -> Arbr.Task
            ArbrTaskEvalView::class.java -> Arbr.TaskEval
            ArbrTaskRelevantFileView::class.java -> Arbr.TaskRelevantFile
            ArbrVectorResourceView::class.java -> Arbr.VectorResource
            else -> throw Exception("Unrecognized resource view $resourceViewClass")
        }
    }

    private fun makeView(
        resource: ArbrResource,
    ): ResourceView<ArbrResource> {
        val viewProvider = resourceViewProviderFactory.resourceViewProvider(
            proposedValueStreamViewProvider,
            resource
        )
        val streamProvider = resourceStreamProviderFactory.resourceStreamProvider(resource)
        val uuid = UUID.randomUUID().toString()
        return viewProvider.provideResourceView(streamProvider.provideEmptyResource(uuid))
    }

    override fun <RV : ResourceView<*>> instantiator(resourceViewClass: Class<out ResourceView<*>>): ResourceViewInstantiator<RV> {
        val resource = getArbrResource(resourceViewClass)

        return ResourceViewInstantiator {
            @Suppress("UNCHECKED_CAST")
            makeView(resource) as RV
        }
    }
}
