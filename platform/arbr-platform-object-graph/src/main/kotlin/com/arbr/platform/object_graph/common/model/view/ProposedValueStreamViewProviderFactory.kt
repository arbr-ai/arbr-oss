package com.arbr.og.object_model.common.model.view

import com.arbr.object_model.core.types.ResourceViewProviderFactory
import com.arbr.object_model.core.types.TypedResourceViewProvider
import com.arbr.object_model.core.types.naming.NamedResourceKey
import com.arbr.object_model.core.types.suites.ResourceAssociatedObjectCollectionBuilder
import com.arbr.og.object_model.common.properties.ReadDependencyTracingProvider
import com.arbr.og.object_model.common.properties.ProposedValueAccessTier
import com.arbr.og.object_model.common.properties.WriteDependencyTracingProvider
import com.arbr.og.object_model.common.requirements.RequirementsProvider

class ProposedValueStreamViewProviderFactory<RK : NamedResourceKey>(
    private val requirementsProvider: RequirementsProvider,
    private val resourceViewProviderFactory: ResourceViewProviderFactory,
    private val readDependencyTracingProvider: ReadDependencyTracingProvider,
    private val writeDependencyTracingProvider: WriteDependencyTracingProvider,
    private val resourceKeyResolver: ResourceKeyResolver<RK>,
    private val resourceAssociatedObjectCollectionBuilder: ResourceAssociatedObjectCollectionBuilder<RK, TypedResourceViewProvider<*, *>>,
) {

    fun provider(
        proposedValueAccessTier: ProposedValueAccessTier,
    ): ProposedValueStreamViewProvider<RK> {
        return when (proposedValueAccessTier) {
            ProposedValueAccessTier.LATEST -> ProposedValueStreamLatestViewProvider(
                requirementsProvider,
                resourceViewProviderFactory,
                resourceKeyResolver,
                resourceAssociatedObjectCollectionBuilder,
            )
            ProposedValueAccessTier.ACCEPTED -> ProposedValueStreamAcceptedViewProvider(
                requirementsProvider,
                resourceViewProviderFactory,
                resourceKeyResolver,
                resourceAssociatedObjectCollectionBuilder,
            )
            ProposedValueAccessTier.TRACE -> ProposedValueStreamTraceViewProvider(
                readDependencyTracingProvider,
                writeDependencyTracingProvider,
            )
        }
    }
}
