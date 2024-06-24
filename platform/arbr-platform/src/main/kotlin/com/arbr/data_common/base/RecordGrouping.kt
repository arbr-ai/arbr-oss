package com.arbr.data_common.base

import com.arbr.data_common.spec.model.RecordGroupingValue

sealed interface RecordGrouping {
    val recordGroupingValue: RecordGroupingValue

    data object Single: RecordGrouping {
        override val recordGroupingValue: RecordGroupingValue = RecordGroupingValue.SINGLE
    }
    data object Batch: RecordGrouping {
        override val recordGroupingValue: RecordGroupingValue = RecordGroupingValue.BATCH
    }
    data object BatchFixed: RecordGrouping {
        override val recordGroupingValue: RecordGroupingValue = RecordGroupingValue.BATCH_FIXED
    }

    companion object {
        fun forGroupingValue(
            recordGroupingValue: RecordGroupingValue
        ): RecordGrouping {
            return when (recordGroupingValue) {
                RecordGroupingValue.SINGLE -> Single
                RecordGroupingValue.BATCH -> Batch
                RecordGroupingValue.BATCH_FIXED -> BatchFixed
            }
        }
    }
}
