package com.arbr.og_engine.core

import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.og.object_model.common.requirements.DefaultRequirementsProvider
import com.arbr.og.object_model.common.requirements.RequirementsProvider
import com.arbr.platform.object_graph.impl.ObjectModelResource
import com.arbr.platform.object_graph.impl.Partial

abstract class ArbrResourceFunction<
        T : ObjectModelResource<T, P, ArbrForeignKey>, P : Partial<T, P, ArbrForeignKey>, // Listen
        T1 : ObjectModelResource<T1, P1, ArbrForeignKey>, P1 : Partial<T1, P1, ArbrForeignKey>, // Read root
        T2 : ObjectModelResource<T2, P2, ArbrForeignKey>, P2 : Partial<T2, P2, ArbrForeignKey>, // Write root
        >(
    objectModelParser: ObjectModelParser,
    requirementsProvider: RequirementsProvider = DefaultRequirementsProvider(),
): WorkflowResourceUnaryFunction<T, P, T1, P1, T2, P2, ArbrForeignKey>(objectModelParser, requirementsProvider)
