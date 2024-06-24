package com.arbr.data_common.spec.uri

import com.arbr.util_common.uri.UriModel
import java.net.URI

data class DataVolumeUriComponent(
    val uri: UriModel
) {

    constructor(uri: URI): this(UriModel.ofUri(uri))
}
