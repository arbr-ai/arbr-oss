package com.arbr.platform.alignable.alignable

import com.arbr.platform.ml.optimization.base.NamedMetricKind

val MCONSTANT = NamedMetricKind("MCONSTANT")
val STR_WEIGHT = NamedMetricKind("STR_WEIGHT")
val STR_NORM = NamedMetricKind("STR_NORM")
val DIFF_OP_BASELINE = NamedMetricKind("DIFF_OP_BASELINE")

val ADJ_MERGE = NamedMetricKind("ADJ_MERGE")
val ADJ_INDUCE = NamedMetricKind("ADJ_INDUCE")
val ADJ_DEDUCE = NamedMetricKind("ADJ_DEDUCE")
val ADJ_ROOT_NODE = NamedMetricKind("ADJ_ROOT_NODE")
val ADJ_CHILD_NODE = NamedMetricKind("ADJ_CHILD_NODE")
val ADJ_EDGE_GENERAL = NamedMetricKind("ADJ_EDGE_GENERAL")

val TKW_DROP = NamedMetricKind("TKW_DROP")
val TKW_MATCH = NamedMetricKind("TKW_MATCH")
val TKW_EDIT = NamedMetricKind("TKW_EDIT")
val TKW_APPLY = NamedMetricKind("TKW_APPLY")
val TKW_SKIP = NamedMetricKind("TKW_SKIP")
val TKW_SKIP_PRE = NamedMetricKind("TKW_SKIP_PRE")
val TKW_SKIP_POST = NamedMetricKind("TKW_SKIP_POST")

val OP_MATRIX_NUL_NUL = NamedMetricKind("OP_MATRIX_NUL_NUL")
val OP_MATRIX_NUL_ADD = NamedMetricKind("OP_MATRIX_NUL_ADD")
val OP_MATRIX_NUL_NOP = NamedMetricKind("OP_MATRIX_NUL_NOP")
val OP_MATRIX_NUL_DEL = NamedMetricKind("OP_MATRIX_NUL_DEL")
val OP_MATRIX_ADD_NUL = NamedMetricKind("OP_MATRIX_ADD_NUL")
val OP_MATRIX_ADD_ADD = NamedMetricKind("OP_MATRIX_ADD_ADD")
val OP_MATRIX_ADD_NOP = NamedMetricKind("OP_MATRIX_ADD_NOP")
val OP_MATRIX_ADD_DEL = NamedMetricKind("OP_MATRIX_ADD_DEL")
val OP_MATRIX_NOP_NUL = NamedMetricKind("OP_MATRIX_NOP_NUL")
val OP_MATRIX_NOP_ADD = NamedMetricKind("OP_MATRIX_NOP_ADD")
val OP_MATRIX_NOP_NOP = NamedMetricKind("OP_MATRIX_NOP_NOP")
val OP_MATRIX_NOP_DEL = NamedMetricKind("OP_MATRIX_NOP_DEL")
val OP_MATRIX_DEL_NUL = NamedMetricKind("OP_MATRIX_DEL_NUL")
val OP_MATRIX_DEL_ADD = NamedMetricKind("OP_MATRIX_DEL_ADD")
val OP_MATRIX_DEL_NOP = NamedMetricKind("OP_MATRIX_DEL_NOP")
val OP_MATRIX_DEL_DEL = NamedMetricKind("OP_MATRIX_DEL_DEL")

//val ADJ_COST_METRICS = listOf(
//    ADJ_MERGE,
//    ADJ_INDUCE,
//    ADJ_DEDUCE,
////    ADJ_ROOT_NODE,
////    ADJ_CHILD_NODE,
////    ADJ_EDGE_GENERAL,
//)
//
//val TKW_COST_METRICS = listOf(
//    TKW_DROP,
//    TKW_MATCH,
//    TKW_EDIT,
//    TKW_APPLY,
//    TKW_SKIP,
//    TKW_SKIP_PRE,
//    TKW_SKIP_POST,
//)

//val ADJ_TKW_COMBINATION_METRIC_MAP = ADJ_COST_METRICS.associate { am ->
//    am.name to TKW_COST_METRICS.associate { tkw ->
//        tkw.name to NamedMetricKind(am.name + "_" + tkw.name)
//    }
//}
//val ADJ_TKW_COMBINATION_METRICS = ADJ_TKW_COMBINATION_METRIC_MAP.values.flatMap { it.values }

val BASE_COST_METRIC_KINDS = listOf(
    MCONSTANT,
    STR_WEIGHT,
    STR_NORM,
    DIFF_OP_BASELINE,

    ADJ_MERGE,
    ADJ_INDUCE,
    ADJ_DEDUCE,
    ADJ_ROOT_NODE,
    ADJ_CHILD_NODE,
    ADJ_EDGE_GENERAL,

    TKW_DROP,
    TKW_MATCH,
    TKW_EDIT,
    TKW_APPLY,
    TKW_SKIP,
    TKW_SKIP_PRE,
    TKW_SKIP_POST,

    OP_MATRIX_NUL_NUL,
    OP_MATRIX_NUL_ADD,
    OP_MATRIX_NUL_NOP,
    OP_MATRIX_NUL_DEL,
    OP_MATRIX_ADD_NUL,
    OP_MATRIX_ADD_ADD,
    OP_MATRIX_ADD_NOP,
    OP_MATRIX_ADD_DEL,
    OP_MATRIX_NOP_NUL,
    OP_MATRIX_NOP_ADD,
    OP_MATRIX_NOP_NOP,
    OP_MATRIX_NOP_DEL,
    OP_MATRIX_DEL_NUL,
    OP_MATRIX_DEL_ADD,
    OP_MATRIX_DEL_NOP,
    OP_MATRIX_DEL_DEL,
) // + ADJ_TKW_COMBINATION_METRICS

val DIFF_OP_COST_METRIC_MATRIX = listOf(
    listOf(
        OP_MATRIX_NUL_NUL,
        OP_MATRIX_NUL_ADD,
        OP_MATRIX_NUL_NOP,
        OP_MATRIX_NUL_DEL,
    ),
    listOf(
        OP_MATRIX_ADD_NUL,
        OP_MATRIX_ADD_ADD,
        OP_MATRIX_ADD_NOP,
        OP_MATRIX_ADD_DEL,
    ),

    listOf(
        OP_MATRIX_NOP_NUL,
        OP_MATRIX_NOP_ADD,
        OP_MATRIX_NOP_NOP,
        OP_MATRIX_NOP_DEL,
    ),

    listOf(
        OP_MATRIX_DEL_NUL,
        OP_MATRIX_DEL_ADD,
        OP_MATRIX_DEL_NOP,
        OP_MATRIX_DEL_DEL,
    ),
)
