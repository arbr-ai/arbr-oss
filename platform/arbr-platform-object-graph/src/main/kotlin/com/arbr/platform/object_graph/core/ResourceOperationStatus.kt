package com.arbr.platform.object_graph.core

import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.og.object_model.common.model.PropertyIdentifier
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