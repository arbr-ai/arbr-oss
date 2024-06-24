package com.arbr.data_common.base.storage

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.spec.element.DataRecordCollectionSpec
import com.arbr.data_common.spec.uri.DataRecordCollectionUriComponent

interface DataRecordCollection<
        Obj : DataRecordObject,
        Grp : RecordGrouping,
        > {
    /**
     * ID of the record collection independent of which volume and records it is used with.
     */
    val relativeId: String

    /**
     * Readable name for the record collection.
     */
    val name: String

    /**
     * Component of the URI for the record collection.
     */
    val uriComponent: DataRecordCollectionUriComponent

    /**
     * Grouping strategy for records into pages.
     */
    val grouping: Grp

    /**
     * Whether the collection requires crawling subdirectories recursively for records.
     */
    val crawlSubdirs: Boolean

    /**
     * Class of the record model
     */
    val recordObjectClass: Class<Obj>

    companion object {

        private data class DataRecordCollectionImpl<Obj : DataRecordObject, Grp : RecordGrouping>(
            override val relativeId: String,
            override val name: String,
            override val uriComponent: DataRecordCollectionUriComponent,
            override val grouping: Grp,
            override val crawlSubdirs: Boolean,
            override val recordObjectClass: Class<Obj>
        ) : DataRecordCollection<Obj, Grp>

        private fun <T> getClass(
            recordFullyQualifiedClassName: String
        ): Class<T> {
            val loaderClass = DataRecordCollection::class.java

            @Suppress("UNCHECKED_CAST")
            return loaderClass.classLoader.loadClass(recordFullyQualifiedClassName) as Class<T>
        }

        fun <Obj : DataRecordObject, Grp : RecordGrouping> ofSpec(
            recordCollectionSpec: DataRecordCollectionSpec,
        ): DataRecordCollection<Obj, Grp> {
            return recordCollectionSpec.run {
                @Suppress("UNCHECKED_CAST")
                val grp = RecordGrouping.forGroupingValue(grouping) as Grp

                DataRecordCollectionImpl(
                    relativeId,
                    name,
                    uriComponent,
                    grp,
                    crawlSubdirs,
                    getClass(recordFullyQualifiedClassName),
                )
            }
        }

    }
}