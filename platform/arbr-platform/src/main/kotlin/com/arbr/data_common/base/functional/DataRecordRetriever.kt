package com.arbr.data_common.base.functional

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGroup
import com.arbr.data_common.base.RecordGroupDescriptor
import com.arbr.data_common.base.RecordGrouping
import com.arbr.util_common.uri.UriModel
import reactor.core.publisher.Mono

fun interface DataRecordRetriever<
        Obj : DataRecordObject,
        Grp : RecordGrouping,
        > {

    fun retrieveRecordGroup(
        recordGroupDescriptor: RecordGroupDescriptor<Obj, Grp>,
        completeUri: UriModel,
    ): Mono<RecordGroup<Obj, Grp>>

}