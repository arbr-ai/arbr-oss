package com.arbr.data_common.spec.uri

import com.arbr.util_common.uri.UriModel

data class DataRecordFullyQualifiedUri(
    val volumeUriComponent: DataVolumeUriComponent,
    val recordCollectionUriComponent: DataRecordCollectionUriComponent,
    val recordUriComponent: DataRecordUriComponent,
) {

    /**
     * Concatenate components to a URI
     */
    fun concat(): UriModel {
        val volumeUri = volumeUriComponent.uri
        val middlePart = recordCollectionUriComponent.uriComponent
        val suffixPart = recordUriComponent.uriComponent

        if (middlePart.isBlank() && suffixPart.isBlank()) {
            return volumeUri
        }

        return volumeUri.lenientExtendPath(middlePart, suffixPart)
    }
}
