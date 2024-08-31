package com.arbr.platform.object_graph.common.model.view

import com.arbr.platform.object_graph.types.ResourceViewProviderFactory
import com.arbr.platform.object_graph.types.TypedResourceViewProvider
import com.arbr.platform.object_graph.types.naming.NamedResourceKey
import com.arbr.platform.object_graph.types.suites.ResourceAssociatedObjectCollectionBuilder
import com.arbr.platform.object_graph.common.requirements.RequirementsProvider

class ProposedValueStreamAcceptedViewProvider<RK : NamedResourceKey>(
    requirementsProvider: RequirementsProvider,
    resourceViewProviderFactory: ResourceViewProviderFactory,
    resourceKeyResolver: ResourceKeyResolver<RK>,
    resourceAssociatedObjectCollectionBuilder: ResourceAssociatedObjectCollectionBuilder<RK, TypedResourceViewProvider<*, *>>,
): ProposedValueStreamAccessContextViewProvider<RK>(
    requirementsProvider.acceptContext(),
    requirementsProvider,
    resourceViewProviderFactory,
    resourceKeyResolver,
    resourceAssociatedObjectCollectionBuilder,
)