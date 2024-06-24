package com.arbr.platform.alignable.alignable.v2.dag

import com.fasterxml.jackson.annotation.JsonIgnore
import com.arbr.platform.ml.optimization.base.NamedMetricKind
import com.arbr.platform.alignable.alignable.*

enum class TokenWindowDAGAlignmentKind {
    DROP,
    MATCH,
    EDIT,
    APPLY,
    SKIP,
    SKIP_PRE,
    SKIP_POST;

    @JsonIgnore
    fun costMetricKind(): NamedMetricKind {
        return when (this) {
            DROP -> TKW_DROP
            MATCH -> TKW_MATCH
            EDIT -> TKW_EDIT
            APPLY -> TKW_APPLY
            SKIP -> TKW_SKIP
            SKIP_PRE -> TKW_SKIP_PRE
            SKIP_POST -> TKW_SKIP_POST
        }
    }
}