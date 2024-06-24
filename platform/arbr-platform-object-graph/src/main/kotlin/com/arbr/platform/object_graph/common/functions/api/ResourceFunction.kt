package com.arbr.og.object_model.common.functions.api

import com.arbr.og.object_model.common.functions.spec.base.RFunctionFactory

/**
 * External function config entrypoint
 */
abstract class ResourceFunction(val configure: RFunctionFactory.() -> Unit)
