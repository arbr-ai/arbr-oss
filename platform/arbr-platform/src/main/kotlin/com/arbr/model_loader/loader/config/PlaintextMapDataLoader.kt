package com.arbr.model_loader.loader.config

import com.arbr.content_formats.mapper.Mappers
import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.etl.load.DataLoader
import com.arbr.data_common.base.etl.load.DataLoaderImpl
import com.arbr.data_common.base.etl.load.RecordGroupDescribedPair
import com.arbr.data_common.base.format.DataRecordObjectFormat
import com.arbr.data_common.base.format.DataRecordObjectSerializer
import com.arbr.data_common.base.serialized.DataRecordMap
import com.arbr.data_common.base.storage.DataRecordCollection
import com.arbr.data_common.base.storage.DataVolume
import com.arbr.data_common.impl.functional.DataRecordObjectSerializingJacksonConverterFactory
import com.arbr.data_common.impl.functional.MemoryMapRecordGroupWriter
import com.arbr.data_common.impl.serialized.PlainStringSerializedRecord
import com.arbr.data_common.spec.element.DataRecordCollectionSpec
import com.arbr.data_common.spec.element.DataVolumeSpec
import com.arbr.data_common.spec.model.DataStorageMedium
import com.arbr.data_common.spec.model.RecordGroupingValue
import com.arbr.data_common.spec.uri.DataRecordCollectionUriComponent
import com.arbr.data_common.spec.uri.DataRecordFullyQualifiedUri
import com.arbr.data_common.spec.uri.DataVolumeUriComponent
import com.arbr.util_common.uri.UriModel
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.net.URI

/**
 * One-time use loader into a string map
 */
class PlaintextMapDataLoader<
        Obj : DataRecordObject,
        Grp : RecordGrouping,
        >(
    private val objClass: Class<Obj>,
) : DataLoader<Obj, Grp> {
    private val mapper = Mappers.mapper

    private var loadFlux: Flux<DataRecordFullyQualifiedUri>? = null

    override val outputVolume = DataVolume.ofSpec(
        DataVolumeSpec(
            relativeId = "arbr.null",
            name = "Null Data Volume for no-op writes",
            uriComponent = DataVolumeUriComponent(URI("memory:///")),
            priority = 0,
            allowWrites = true,
            storageMedium = DataStorageMedium.FILE_SYSTEM,
        )
    )

    override val outputRecordCollection = DataRecordCollection.ofSpec<Obj, Grp>(
        DataRecordCollectionSpec(
            relativeId = "arbr.null",
            name = "Null record collection for no-op writes",
            uriComponent = DataRecordCollectionUriComponent(""),
            crawlSubdirs = false,
            grouping = RecordGroupingValue.SINGLE,
            recordFullyQualifiedClassName = objClass.kotlin.qualifiedName!!,
        )
    )

    private val dataRecordWriter =
        MemoryMapRecordGroupWriter<Obj, Grp, DataRecordObjectFormat.FilePlaintext, PlainStringSerializedRecord<DataRecordObjectFormat.FilePlaintext>>()

    private val innerDataLoader = DataLoaderImpl(
        outputVolume,
        outputRecordCollection,
        DataRecordObjectSerializingJacksonConverterFactory(mapper).makeConverter(),
        object :
            DataRecordObjectSerializer<DataRecordObjectFormat.FilePlaintext, PlainStringSerializedRecord<DataRecordObjectFormat.FilePlaintext>> {
            override val format: DataRecordObjectFormat.FilePlaintext = DataRecordObjectFormat.FilePlaintext

            override fun serializeRecord(
                recordMap: DataRecordMap
            ): PlainStringSerializedRecord<DataRecordObjectFormat.FilePlaintext> {
                return PlainStringSerializedRecord(
                    format,
                    mapper.writeValueAsString(recordMap.value)
                )
            }
        },
        dataRecordWriter,
    )

    @Synchronized
    override fun loadToUris(outputElements: Flux<RecordGroupDescribedPair<Obj, Grp>>): Flux<DataRecordFullyQualifiedUri> {
        val existingLoadFlux = loadFlux
        if (existingLoadFlux != null) {
            return existingLoadFlux
        }

        val uriFlux = innerDataLoader.loadToUris(outputElements)
            .share().cache()
        loadFlux = uriFlux
        return uriFlux
    }

    fun loadTextMap(outputElements: Flux<RecordGroupDescribedPair<Obj, Grp>>): Mono<Map<UriModel, String>> {
        return loadToUris(outputElements)
            .then()
            .then(
                Mono.fromCallable {
                    dataRecordWriter.getMap()
                }.subscribeOn(Schedulers.boundedElastic())
            )
    }

    /**
     * Get the text map, relying on having been completed already.
     */
    fun getTextMap(): Mono<Map<UriModel, String>> {
        return Mono.fromCallable {
            dataRecordWriter.getMap()
        }.subscribeOn(Schedulers.boundedElastic())
    }
}
