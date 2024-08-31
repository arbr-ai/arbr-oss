package com.arbr.platform.object_graph.common.functions.spec.base

import com.arbr.platform.object_graph.common.functions.spec.base.GraphObjectQueryMapContext

interface GraphObjectQueryMapContextFactory {
    fun newContext(): GraphObjectQueryMapContext
}