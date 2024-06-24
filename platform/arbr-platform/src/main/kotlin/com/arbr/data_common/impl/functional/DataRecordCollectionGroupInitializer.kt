package com.arbr.data_common.impl.functional

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGroup
import com.arbr.data_common.base.RecordGrouping

sealed interface DataRecordCollectionGroupInitializer<
        Obj : DataRecordObject,
        Grp : RecordGrouping,
        > {

    fun makeRecordGroup(obj: Obj): RecordGroup<Obj, Grp>

    fun makeRecordGroup(objList: List<Obj>): RecordGroup<Obj, Grp>

    class Single<
            Obj : DataRecordObject,
            >: DataRecordCollectionGroupInitializer<Obj, RecordGrouping.Single> {
        override fun makeRecordGroup(obj: Obj): RecordGroup<Obj, RecordGrouping.Single> {
            return RecordGroup.Single(obj)
        }

        override fun makeRecordGroup(objList: List<Obj>): RecordGroup<Obj, RecordGrouping.Single> {
            // Be lenient and allow length-1 lists
            if (objList.size == 1) {
                return makeRecordGroup(objList.first())
            } else {
                throw IllegalStateException("Calling single record group initializer with object list sized ${objList.size}")
            }
        }

    }

    class Batch<
            Obj : DataRecordObject,
            >: DataRecordCollectionGroupInitializer<Obj, RecordGrouping.Batch> {
        override fun makeRecordGroup(obj: Obj): RecordGroup<Obj, RecordGrouping.Batch> {
            return makeRecordGroup(listOf(obj))
        }

        override fun makeRecordGroup(objList: List<Obj>): RecordGroup<Obj, RecordGrouping.Batch> {
            return RecordGroup.Batch(objList)
        }

    }

    class BatchFixed<
            Obj : DataRecordObject,
            >: DataRecordCollectionGroupInitializer<Obj, RecordGrouping.BatchFixed> {
        override fun makeRecordGroup(obj: Obj): RecordGroup<Obj, RecordGrouping.BatchFixed> {
            return makeRecordGroup(listOf(obj))
        }

        override fun makeRecordGroup(objList: List<Obj>): RecordGroup<Obj, RecordGrouping.BatchFixed> {
            return RecordGroup.BatchFixed(objList)
        }

    }

    companion object {
        fun <
                Obj : DataRecordObject,
                Grp : RecordGrouping
                > forGrouping(
            grouping: Grp
        ): DataRecordCollectionGroupInitializer<Obj, Grp> {
            @Suppress("UNCHECKED_CAST") // This shouldn't be necessary :(
            return when (grouping) {
                RecordGrouping.Single -> {
                    Single<Obj>() as DataRecordCollectionGroupInitializer<Obj, Grp>
                }
                RecordGrouping.Batch -> {
                    Batch<Obj>() as DataRecordCollectionGroupInitializer<Obj, Grp>
                }
                RecordGrouping.BatchFixed -> {
                    BatchFixed<Obj>() as DataRecordCollectionGroupInitializer<Obj, Grp>
                }
                else -> throw IllegalStateException() // This shouldn't be necessary :(
            }
        }
    }
}