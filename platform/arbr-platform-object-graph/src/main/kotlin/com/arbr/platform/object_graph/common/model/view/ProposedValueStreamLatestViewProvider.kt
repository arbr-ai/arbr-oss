package com.arbr.og.object_model.common.model.view

import com.arbr.object_model.core.types.ResourceViewProviderFactory
import com.arbr.object_model.core.types.TypedResourceViewProvider
import com.arbr.object_model.core.types.naming.NamedResourceKey
import com.arbr.object_model.core.types.suites.ResourceAssociatedObjectCollectionBuilder
import com.arbr.og.object_model.common.requirements.RequirementsProvider

class ProposedValueStreamLatestViewProvider<RK : NamedResourceKey>(
    requirementsProvider: RequirementsProvider,
    resourceViewProviderFactory: ResourceViewProviderFactory,
    resourceKeyResolver: ResourceKeyResolver<RK>,
    resourceAssociatedObjectCollectionBuilder: ResourceAssociatedObjectCollectionBuilder<RK, TypedResourceViewProvider<*, *>>,
): ProposedValueStreamAccessContextViewProvider<RK>(
    requirementsProvider.latestContext(),
    requirementsProvider,
    resourceViewProviderFactory,
    resourceKeyResolver,
    resourceAssociatedObjectCollectionBuilder,
)