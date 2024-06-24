package com.arbr.data_common.impl.fetch

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.format.DataRecordObjectFormat
import com.arbr.data_common.base.format.DataVolumeObjectSourceScheme
import com.arbr.data_common.base.serialized.SerializedRecord
import com.arbr.data_common.base.storage.DataRecordCollection
import com.arbr.data_common.impl.functional.DataRecordCollectionGroupInitializer
import com.arbr.data_common.impl.serialized.PlainStringSerializedRecord
import reactor.core.publisher.Mono

abstract class DefaultDataRecordGroupClient<
        Sch : DataVolumeObjectSourceScheme,
        Obj : DataRecordObject,
        Grp : RecordGrouping,
        >(
    protected val recordCollection: DataRecordCollection<Obj, Grp>
        ) : DataRecordGroupClient<Sch, Obj, Grp> {

    override val grouping: Grp = recordCollection.grouping

    override val recordGroupInitializer: DataRecordCollectionGroupInitializer<Obj, Grp> =
        DataRecordCollectionGroupInitializer.forGrouping(recordCollection.grouping)

    override fun <Fmt : DataRecordObjectFormat> makeSerializedRecord(
        format: Fmt,
        resourceText: String
    ): SerializedRecord<Fmt> {
        return PlainStringSerializedRecord(format, resourceText)
    }

    override fun isEmpty(): Mono<Boolean> {
        return loadRecordGroupDescriptors(
            maxGroupSize = 1,
            maxNumGroupDescriptors = 1,
        )
            .next()
            .materialize()
            .flatMap {
                if (it.isOnNext) {
                    Mono.just(false)  // Nonempty
                } else if (it.isOnComplete) {
                    Mono.just(true)  // Empty
                } else {
                    Mono.error(it.throwable!!)
                }
            }
    }
}