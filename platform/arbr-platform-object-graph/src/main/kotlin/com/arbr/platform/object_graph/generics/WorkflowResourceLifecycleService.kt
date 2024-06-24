package com.arbr.platform.object_graph.generics

import com.arbr.platform.object_graph.impl.ObjectModelResource

interface WorkflowResourceLifecycleService {

    fun hasStrongPathToProjectOwner(
        resource: ObjectModelResource<*, *, *>,
    ): Boolean

}