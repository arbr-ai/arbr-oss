package com.arbr.og.object_model.common.functions.platform

import com.arbr.og.object_model.common.properties.NonNullRequirementsProvider

class ResourceFunctionContextImpl(
    override val resourceViewInstantiators: ResourceViewInstantiators,
    override val nonNullRequirementsProvider: NonNullRequirementsProvider,
): ResourceFunctionContext()