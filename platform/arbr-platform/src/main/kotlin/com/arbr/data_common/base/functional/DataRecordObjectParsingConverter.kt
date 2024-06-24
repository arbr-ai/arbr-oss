package com.arbr.data_common.base.functional

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.serialized.DataRecordMap
import reactor.core.publisher.Mono

interface DataRecordObjectParsingConverter<Obj: DataRecordObject> {
    val targetObjectClass: Class<Obj>

    /**
     * Convert a value from the intermediate map form into the given target class, potentially async to make room for
     * lenient / stochastic parsers
     */
    fun convertValue(
        dataRecordMap: DataRecordMap,
    ): Mono<Obj>
}

