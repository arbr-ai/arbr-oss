package com.arbr.data_common.base.functional

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGroupDescriptor
import com.arbr.data_common.base.RecordGrouping
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Retrieve sequential pages of record group descriptors to enumerate extractable inputs.
 */
interface DataRecordGroupDescriptorPaginator<
        Obj : DataRecordObject,
        Grp : RecordGrouping,
        > {

    /**
     * Return whether the collection of descriptors is empty.
     */
    fun isEmpty(): Mono<Boolean>

    /**
     * Load sequential record descriptor groups, each containing descriptors for at most `maxSize` resources.
     */
    fun loadRecordGroupDescriptors(
        maxGroupSize: Int,
        maxNumGroupDescriptors: Int,
    ): Flux<RecordGroupDescriptor<Obj, Grp>>

}