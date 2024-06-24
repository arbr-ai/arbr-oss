package com.arbr.data_common.impl.fetch

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGroupDescriptor
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.format.DataVolumeObjectSourceScheme
import com.arbr.data_common.base.functional.DataRecordGroupFormatRecognizer
import com.arbr.data_common.base.functional.DataRecordObjectParsingConverter
import com.arbr.data_common.base.storage.DataRecordCollection
import com.arbr.data_common.base.storage.DataVolume
import com.arbr.data_common.impl.files.ClasspathResourceRecordGroupDescriptor
import com.arbr.util_common.uri.UriModel
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.nio.file.Paths

class DataRecordGroupClasspathClient<
        Obj : DataRecordObject,
        Grp : RecordGrouping,
        >(
    private val dataVolume: DataVolume,
    recordCollection: DataRecordCollection<Obj, Grp>,
    override val dataRecordGroupFormatRecognizer: DataRecordGroupFormatRecognizer<Obj, Grp>,
    override val dataRecordConverter: DataRecordObjectParsingConverter<Obj>,

    ) : DefaultDataRecordGroupClient<
        DataVolumeObjectSourceScheme.Classpath,
        Obj,
        Grp,
        >(recordCollection) {

    private val resolver = PathMatchingResourcePatternResolver()

    /**
     * Append the base volume path and the record collection component to get the interior search dir.
     */
    private val recordCollectionPattern = Paths.get(
        dataVolume.baseVolumePathString,
        recordCollection.uriComponent.uriComponent,
    )
        .toString()
        .let { interiorPathString ->
            // Trim any trailing separators to normalize for building patterns
            // Assuming separator is '/' should be fine for classpath
            interiorPathString.dropLastWhile { it == '/' }
        }
        .let { interiorPathString ->
            val tailPattern = if (recordCollection.crawlSubdirs) {
                // Items in any subdir
                "**/*"
            } else {
                // Top-level items only
                "*"
            }

            if (interiorPathString.isBlank()) {
                "classpath*:${tailPattern}"
            } else {
                "classpath*:${interiorPathString}/${tailPattern}"
            }
        }

    private fun listResources(): Flux<Resource> {
        return Mono.fromCallable {
            resolver.getResources(recordCollectionPattern)
        }.subscribeOn(Schedulers.boundedElastic())
            .flatMapIterable { it.toList() }
    }

    override fun getRecordText(completeUri: UriModel): Mono<String> {
        val url = completeUri.toUri().toURL()

        return Mono.fromCallable {
            url.readText()
        }.subscribeOn(Schedulers.boundedElastic())
    }

    override fun loadRecordGroupDescriptors(
        maxGroupSize: Int,
        maxNumGroupDescriptors: Int
    ): Flux<RecordGroupDescriptor<Obj, Grp>> {
        return listResources()
            .map { resource ->
                ClasspathResourceRecordGroupDescriptor(
                    dataVolume.uriComponent,
                    recordCollection.uriComponent,
                    resource.url
                )
            }
    }

}
