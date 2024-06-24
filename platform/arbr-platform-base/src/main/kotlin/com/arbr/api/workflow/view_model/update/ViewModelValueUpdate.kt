package com.arbr.api.workflow.view_model.update

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * In-place update to a view model value. Specification of "matching" is informal and left to context, for example ID
 * or name depending on the resource, or simply the singleton entry in the case of non-collection values.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ViewModelValueUpdate<T>(
    val operation: ViewModelValueUpdateOperation,
    val value: T?,
)
