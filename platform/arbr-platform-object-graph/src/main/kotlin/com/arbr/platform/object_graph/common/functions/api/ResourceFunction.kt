package com.arbr.platform.object_graph.common.functions.api

import com.arbr.platform.object_graph.common.functions.spec.base.RFunctionFactory

/**
 * External function config entrypoint
 */
abstract class ResourceFunction(val configure: RFunctionFactory.() -> Unit)
