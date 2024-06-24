package com.arbr.data_common.base.etl.load

import com.arbr.data_common.base.RecordGroupDescriptor
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGroup

data class RecordGroupDescribedPair<
        Obj: DataRecordObject,
        Grp : RecordGrouping,
        >(
    val recordGroup: RecordGroup<Obj, Grp>,
    val recordGroupDescriptor: RecordGroupDescriptor<Obj, Grp>,
)
