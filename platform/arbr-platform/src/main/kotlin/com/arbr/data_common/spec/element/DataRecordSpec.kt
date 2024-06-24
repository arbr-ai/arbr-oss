package com.arbr.data_common.spec.element

import com.arbr.data_common.spec.model.DataRecordFormat
import com.arbr.data_common.spec.uri.DataRecordUriComponent

data class DataRecordSpec(
    /**
     * ID of the record independent of which volume and record collection it is used with.
     */
    val relativeId: String,

    /**
     * Readable name for the record.
     */
    val name: String,

    /**
     * Component of the URI for the record collection.
     */
    val uriComponent: DataRecordUriComponent,

    /**
     * Storage format of the record.
     */
    val storageFormat: DataRecordFormat,

    /**
     * Class name of the record model, fully qualified.
     */
    val recordFullyQualifiedClassName: String,
)
