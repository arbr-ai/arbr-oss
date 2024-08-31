package com.arbr.platform.object_graph.common.functions.platform

import com.arbr.platform.object_graph.common.functions.platform.ResourceFunctionContext
import com.arbr.platform.object_graph.common.functions.platform.ResourceViewInstantiators
import com.arbr.platform.object_graph.common.properties.NonNullRequirementsProvider

class ResourceFunctionContextImpl(
    override val resourceViewInstantiators: ResourceViewInstantiators,
    override val nonNullRequirementsProvider: NonNullRequirementsProvider,
): ResourceFunctionContext()