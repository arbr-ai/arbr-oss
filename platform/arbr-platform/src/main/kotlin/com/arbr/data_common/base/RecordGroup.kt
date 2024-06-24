package com.arbr.data_common.base

sealed interface RecordGroup<
        Elt,
        Grp : RecordGrouping,
        > {
    fun <U> map(f: (Elt) -> U): RecordGroup<U, Grp>

    fun flatten(): List<Elt> {
        return when (this) {
            is Batch -> records
            is BatchFixed -> records
            is Single -> listOf(record)
        }
    }

    data class Single<
            Elt,
            >(
        val record: Elt,
    ) : RecordGroup<Elt, RecordGrouping.Single> {
        override fun <U> map(f: (Elt) -> U): RecordGroup<U, RecordGrouping.Single> {
            return Single(f(record))
        }
    }

    data class Batch<
            Elt,
            >(
        val records: List<Elt>,
    ) : RecordGroup<Elt, RecordGrouping.Batch> {
        override fun <U> map(f: (Elt) -> U): RecordGroup<U, RecordGrouping.Batch> {
            return Batch(
                records.map(f)
            )
        }
    }

    data class BatchFixed<
            Elt,
            >(
        val records: List<Elt>,
    ) : RecordGroup<Elt, RecordGrouping.BatchFixed> {
        override fun <U> map(f: (Elt) -> U): RecordGroup<U, RecordGrouping.BatchFixed> {
            return BatchFixed(
                records.map(f)
            )
        }
    }
}
