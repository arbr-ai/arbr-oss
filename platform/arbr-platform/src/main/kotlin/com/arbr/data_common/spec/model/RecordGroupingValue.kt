package com.arbr.data_common.spec.model

enum class RecordGroupingValue {
    /**
     * Records are stored individually in materialized storage so that fetching them individually is roughly the same
     * as doing so in batches.
     */
    SINGLE,

    /**
     * Records can be retrieved in batches of a parametrized size, e.g. database queries.
     */
    BATCH,

    /**
     * Records are stored in materialized batches of a predetermined size, e.g. JSONL format.
     */
    BATCH_FIXED,
}