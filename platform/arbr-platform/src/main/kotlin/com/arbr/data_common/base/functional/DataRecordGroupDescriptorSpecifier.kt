package com.arbr.data_common.base.functional

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGroup
import com.arbr.data_common.base.RecordGroupDescriptor
import com.arbr.data_common.base.RecordGrouping
import reactor.core.publisher.Mono

/**
 * For a record group without a descriptor / location, determine a suitable location as a descriptor.
 */
fun interface DataRecordGroupDescriptorSpecifier<
        Obj : DataRecordObject,
        Grp : RecordGrouping,
        > {

    fun specifyDescriptor(
        recordGroup: RecordGroup<Obj, Grp>,
    ): Mono<RecordGroupDescriptor<Obj, Grp>>

}
