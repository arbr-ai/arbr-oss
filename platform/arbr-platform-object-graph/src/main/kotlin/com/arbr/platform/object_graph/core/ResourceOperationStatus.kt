package com.arbr.platform.object_graph.core

import com.arbr.platform.object_graph.types.naming.NamedForeignKey
import com.arbr.platform.object_graph.common.model.PropertyIdentifier
import com.arbr.platform.object_graph.impl.ObjectModelResource
import com.arbr.platform.object_graph.impl.Partial
import com.arbr.platform.object_graph.concurrency.AtomicStack

data class ResourceOperationStatus<T : ObjectModelResource<T, P, ForeignKey>, P : Partial<T, P, ForeignKey>, ForeignKey: NamedForeignKey>(
    val f: WorkflowResourceUnaryFunction<T, P, *, *, *, *, ForeignKey>,
    val arg: UpdateArgument<T>,
    val state: AtomicStack<ResourceOperationState>,
    val resourceAcquireAttempts: Long,
    val blockedBy: String?,
    val dependencyIdentifier: PropertyIdentifier?,
)