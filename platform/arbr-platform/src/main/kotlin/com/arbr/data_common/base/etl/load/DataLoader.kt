package com.arbr.data_common.base.etl.load

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.storage.DataRecordCollection
import com.arbr.data_common.base.storage.DataVolume
import com.arbr.data_common.spec.uri.DataRecordFullyQualifiedUri
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface DataLoader<
        Obj : DataRecordObject,
        Grp : RecordGrouping,
        > {
    val outputVolume: DataVolume
    val outputRecordCollection: DataRecordCollection<Obj, Grp>

    /**
     * Load (write) elements and return URIs for each.
     */
    fun loadToUris(
        outputElements: Flux<RecordGroupDescribedPair<Obj, Grp>>,
    ): Flux<DataRecordFullyQualifiedUri>

    fun load(
        outputElements: Flux<RecordGroupDescribedPair<Obj, Grp>>,
    ): Flux<Void> {
        return loadToUris(outputElements)
            .flatMap {
                Mono.empty()
            }
    }
}

