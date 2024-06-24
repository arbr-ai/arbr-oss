package com.arbr.data_common.impl.fetch

import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGroupDescriptor
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.format.DataVolumeObjectSourceScheme
import com.arbr.data_common.base.functional.DataRecordGroupFormatRecognizer
import com.arbr.data_common.base.functional.DataRecordObjectParsingConverter
import com.arbr.data_common.base.storage.DataRecordCollection
import com.arbr.data_common.base.storage.DataVolume
import com.arbr.data_common.impl.files.FileRecordGroupDescriptor
import com.arbr.util_common.uri.UriModel
import org.apache.commons.io.file.PathUtils
import org.apache.commons.io.filefilter.FileFileFilter
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.notExists
import kotlin.io.path.relativeToOrSelf
import kotlin.io.path.toPath

class DataRecordGroupLocalFileSystemClient<
        Obj : DataRecordObject,
        Grp : RecordGrouping,
        >(
    dataVolume: DataVolume,
    recordCollection: DataRecordCollection<Obj, Grp>,
    override val dataRecordGroupFormatRecognizer: DataRecordGroupFormatRecognizer<Obj, Grp>,
    override val dataRecordConverter: DataRecordObjectParsingConverter<Obj>,

    ) : DefaultDataRecordGroupClient<
        DataVolumeObjectSourceScheme.LocalFileSystem,
        Obj,
        Grp,
        >(recordCollection) {

    private val recordCollectionUriModel: UriModel = dataVolume.uriComponent.uri
        .lenientExtendPath(
            recordCollection.uriComponent.uriComponent
        )

    private fun listFiles(
        dirPath: Path,
        crawlSubDirs: Boolean,
    ): Flux<Path> {
        if (dirPath.notExists() || !dirPath.isDirectory()) {
            return Flux.empty()
        }

        val pathFilter = FileFileFilter.INSTANCE
        val maxDepth = if (crawlSubDirs) Int.MAX_VALUE else 1

        val pathStream = PathUtils.walk(dirPath, pathFilter, maxDepth, false)
        return Flux.fromStream(pathStream)
    }

    override fun getRecordText(completeUri: UriModel): Mono<String> {
        val filePath = completeUri.toUri().toPath()

        return Mono.fromCallable {
            filePath
                .toFile()
                .readText()
        }.subscribeOn(Schedulers.boundedElastic())
    }

    override fun loadRecordGroupDescriptors(
        maxGroupSize: Int,
        maxNumGroupDescriptors: Int
    ): Flux<RecordGroupDescriptor<Obj, Grp>> {
        val crawlSubDirs = recordCollection.crawlSubdirs
        val recordCollectionPath = recordCollectionUriModel.toUri().toPath()

        return listFiles(recordCollectionPath, crawlSubDirs)
            .map { path ->
                val fileRelativePathString = path.relativeToOrSelf(recordCollectionPath).toString()
                FileRecordGroupDescriptor(fileRelativePathString)
            }
    }

}