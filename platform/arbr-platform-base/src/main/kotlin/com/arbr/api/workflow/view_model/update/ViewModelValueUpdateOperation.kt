package com.arbr.api.workflow.view_model.update

import com.fasterxml.jackson.annotation.JsonValue

enum class ViewModelValueUpdateOperation(@JsonValue val serializedName: String) {
    /**
     * Add entries matching the provided value(s).
     */
    ADD("add"),

    /**
     * Remove entries matching the provided value(s).
     */
    REMOVE("remove"),

    /**
     * Update entries matching the provided value(s).
     * I.e., add provided values not already present, update-in-place matching values, leave other values alone.
     */
    UPDATE("update"),

    /**
     * Replace entries entirely with provided value(s).
     */
    SET("set");
}