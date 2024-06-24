package com.arbr.data_common.spec.element

import com.arbr.data_common.spec.model.RecordGroupingValue
import com.arbr.data_common.spec.uri.DataRecordCollectionUriComponent

data class DataRecordCollectionSpec(
    /**
     * ID of the record collection independent of which volume and records it is used with.
     */
    val relativeId: String,

    /**
     * Readable name for the record collection.
     */
    val name: String,

    /**
     * Component of the URI for the record collection.
     */
    val uriComponent: DataRecordCollectionUriComponent,

    /**
     * Whether the collection requires crawling subdirectories recursively for records.
     */
    val crawlSubdirs: Boolean,

    /**
     * Storage format of the collection.
     */
    val grouping: RecordGroupingValue,

    /**
     * Class name of the record model, fully qualified.
     */
    val recordFullyQualifiedClassName: String,
)
