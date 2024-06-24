package com.arbr.data_common.base.etl

import com.arbr.content_formats.mapper.Mappers
import com.arbr.data_common.base.DataRecordObject
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.etl.load.DataLoaderImpl
import com.arbr.data_common.base.etl.load.RecordGroupDescribedPair
import com.arbr.data_common.base.format.DataRecordObjectFormat
import com.arbr.data_common.base.format.DataRecordObjectSerializer
import com.arbr.data_common.base.functional.*
import com.arbr.data_common.base.serialized.DataRecordMap
import com.arbr.data_common.base.storage.DataRecordCollection
import com.arbr.data_common.base.storage.DataVolume
import com.arbr.data_common.impl.etl.extract.DataExtractorImpl
import com.arbr.data_common.impl.etl.transform.DataTransformerImpl
import com.arbr.data_common.impl.fetch.DataRecordGroupClasspathClient
import com.arbr.data_common.impl.fetch.DataRecordGroupLocalFileSystemClient
import com.arbr.data_common.impl.files.ClasspathResourceRecordGroupDescriptor
import com.arbr.data_common.impl.files.FileRecordGroupDescriptor
import com.arbr.data_common.impl.functional.DataRecordGroupSimpleExtensionFormatRecognizer
import com.arbr.data_common.impl.functional.DataRecordObjectSerializingJacksonConverterFactory
import com.arbr.data_common.impl.functional.FileRecordGroupWriter
import com.arbr.data_common.impl.serialized.PlainStringSerializedRecord
import com.arbr.data_common.spec.element.DataRecordCollectionSpec
import com.arbr.data_common.spec.element.DataVolumeSpec
import com.arbr.data_common.spec.model.DataStorageMedium
import com.arbr.data_common.spec.model.RecordGroupingValue
import com.arbr.data_common.spec.uri.DataRecordCollectionUriComponent
import com.arbr.data_common.spec.uri.DataVolumeUriComponent
import org.junit.jupiter.api.Assertions
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant
import kotlin.io.path.absolute
import kotlin.io.path.deleteIfExists
import kotlin.math.absoluteValue

internal object TestEtlObjects {

    private val mapper = Mappers.mapper

    init {
        val mapper = Mappers.mapper

        val collectionsDirPath = Paths.get("./tmp/collections/cl0/")
        val m0 = TestRecordObjectModel("abc", 5)
        val m1 = TestRecordObjectModel("def", 1)

        Files.createDirectories(collectionsDirPath)
        Files.list(collectionsDirPath)
            .forEach { it.deleteIfExists() }


        val file0 = Paths.get(collectionsDirPath.toString(), "hello.json")
        mapper.writeValue(file0.toFile(), m0)

        val file1 = Paths.get(collectionsDirPath.toString(), "goodbye.json")
        mapper.writeValue(file1.toFile(), m1)

        // Clear output dir
        val outputCollectionsDirPath = Paths.get("./tmp/collections/cl1/")
        Files.createDirectories(outputCollectionsDirPath)
        Files.list(outputCollectionsDirPath)
            .forEach { it.deleteIfExists() }
    }

    private fun <
            Obj : DataRecordObject,
            Grp : RecordGrouping,
            > dataRecordGroupFormatRecognizer(): DataRecordGroupFormatRecognizer<Obj, Grp> {
        return DataRecordGroupSimpleExtensionFormatRecognizer()
    }

    private val fileDataVolumeUri = Paths.get("./tmp").absolute().toUri().normalize()
    private val fileDataVolume = DataVolume.ofSpec(
        DataVolumeSpec(
            relativeId = "data-volume",
            name = "Data Volume",
            uriComponent = DataVolumeUriComponent(fileDataVolumeUri),
            priority = 0,
            allowWrites = true,
            storageMedium = DataStorageMedium.FILE_SYSTEM,
        )
    )

    object FileExtractor {

        private val recordCollection =
            DataRecordCollection.ofSpec<TestRecordObjectModel, RecordGrouping.Single>(
                DataRecordCollectionSpec(
                    relativeId = "collection-0",
                    name = "Record Collection",
                    DataRecordCollectionUriComponent(
                        "collections/cl0",
                    ),
                    crawlSubdirs = true,
                    grouping = RecordGroupingValue.SINGLE,
                    recordFullyQualifiedClassName = TestRecordObjectModel::class.qualifiedName!!
                )
            )

        private val dataExtractorFilter: DataExtractorFilter<TestRecordObjectModel, RecordGrouping.Single> =
            DataExtractorFilter {
                Mono.just(true)
            }

        private val dataRecordFileSystemClient: DataRecordGroupLocalFileSystemClient<TestRecordObjectModel, RecordGrouping.Single> =
            DataRecordGroupLocalFileSystemClient(
                fileDataVolume,
                recordCollection,
                dataRecordGroupFormatRecognizer(),
                object : DataRecordObjectParsingConverter<TestRecordObjectModel> {
                    override val targetObjectClass: Class<TestRecordObjectModel>
                        get() = TestRecordObjectModel::class.java

                    override fun convertValue(dataRecordMap: DataRecordMap): Mono<TestRecordObjectModel> {
                        return Mono.just(
                            mapper.convertValue(dataRecordMap.value, targetObjectClass)
                        )
                    }

                }
            )

        val fileDataExtractor = DataExtractorImpl(
            fileDataVolume,
            recordCollection,
            dataRecordFileSystemClient,
            dataExtractorFilter,
            dataRecordFileSystemClient,
        )
    }

    private val classpathDataVolumeUri = URI("classpath:///")
    private val classpathDataVolume = DataVolume.ofSpec(
        DataVolumeSpec(
            relativeId = "data-volume-cp",
            name = "Data Volume Classpath",
            uriComponent = DataVolumeUriComponent(classpathDataVolumeUri),
            priority = 0,
            allowWrites = false,
            storageMedium = DataStorageMedium.FILE_SYSTEM,
        )
    )

    object ClasspathExtractor {
        private const val recordCollectionUriComponent = "**/patch_replay_test_cases/"

        private val recordCollection =
            DataRecordCollection.ofSpec<TestRecordObjectModel3, RecordGrouping.Single>(
                DataRecordCollectionSpec(
                    relativeId = "collection-cp-1",
                    name = "Record Collection Classpath",
                    DataRecordCollectionUriComponent(
                        recordCollectionUriComponent,
                    ),
                    crawlSubdirs = false,
                    grouping = RecordGroupingValue.SINGLE,
                    recordFullyQualifiedClassName = TestRecordObjectModel3::class.qualifiedName!!
                )
            )

        private val dataExtractorFilter: DataExtractorFilter<TestRecordObjectModel3, RecordGrouping.Single> =
            DataExtractorFilter {
                Mono.just(true)
            }

        private val dataRecordClasspathClient =
            DataRecordGroupClasspathClient(
                classpathDataVolume,
                recordCollection,
                dataRecordGroupFormatRecognizer(),
                object : DataRecordObjectParsingConverter<TestRecordObjectModel3> {
                    override val targetObjectClass: Class<TestRecordObjectModel3>
                        get() = TestRecordObjectModel3::class.java

                    override fun convertValue(dataRecordMap: DataRecordMap): Mono<TestRecordObjectModel3> {
                        return Mono.just(
                            mapper.convertValue(dataRecordMap.value, targetObjectClass)
                        )
                    }

                }
            )

        val classpathDataExtractor = DataExtractorImpl(
            classpathDataVolume,
            recordCollection,
            dataRecordClasspathClient,
            dataExtractorFilter,
            dataRecordClasspathClient,
        )
    }

    private val recordGroupDescriptorTransformer =
        RecordGroupDescriptorTransformer<TestRecordObjectModel, RecordGrouping.Single, TestRecordObjectModel2, RecordGrouping.Single> { descriptor ->
            val relPathString =
                if (descriptor is FileRecordGroupDescriptor<TestRecordObjectModel, RecordGrouping.Single>) {
                    descriptor.fileRecordRelativePathString
                } else {
                    throw IllegalArgumentException()
                }

            Flux.just(
                FileRecordGroupDescriptor(
                    relPathString + "x"
                )
            )
        }

    private val recordGroupTransformer = RecordGroupTransformer<
            TestRecordObjectModel,
            RecordGrouping.Single,
            TestRecordObjectModel2,
            RecordGrouping.Single>
    { recordGroup, inputDescriptor, outputDescriptors ->

        Flux.fromIterable(outputDescriptors)
            .map { outputDescriptor ->
                val outGroup = recordGroup.map { testRecordObjectModel ->
                    TestRecordObjectModel2(
                        Instant.now().toEpochMilli() - testRecordObjectModel.x1,
                        testRecordObjectModel.hashCode().toLong().absoluteValue,
                        testRecordObjectModel.x0 + "-name",
                    )
                }

                RecordGroupDescribedPair(
                    outGroup,
                    outputDescriptor,
                )
            }
    }

    val transformer = DataTransformerImpl(
        recordGroupDescriptorTransformer,
        recordGroupTransformer,
    )

    private val recordGroupDescriptorTransformer3 =
        RecordGroupDescriptorTransformer<TestRecordObjectModel3, RecordGrouping.Single, TestRecordObjectModel2, RecordGrouping.Single> { descriptor ->
            val url =
                if (descriptor is ClasspathResourceRecordGroupDescriptor<TestRecordObjectModel3, RecordGrouping.Single>) {
                    descriptor.classpathResourceUrl
                } else {
                    throw IllegalArgumentException()
                }

            val uri = url.toURI()
            if (uri.scheme != "file") {
                throw IllegalArgumentException(uri.scheme)
            }
            val filePath = Paths.get(uri.path)
            val containerFinalPathElement = Paths.get(
                descriptor.dataVolumeUriComponent.uri.lenientEffectivePath.replace(Regex("\\*+/*"), ""),
                descriptor.recordCollectionUriComponent.uriComponent.replace(Regex("\\*+/*"), ""),
            ).last().toString()
            val finalElementIndex = filePath.indexOfLast { it.toString() == containerFinalPathElement }
            Assertions.assertNotEquals(-1, finalElementIndex)
            val suffixPathTokens = filePath.drop(finalElementIndex + 1).map { it.toString() }
            val suffixPath = Paths.get(
                suffixPathTokens[0],
                *suffixPathTokens.drop(1).toTypedArray(),
            )

            Flux.just(
                FileRecordGroupDescriptor(
                    suffixPath.toString() + "x"
                )
            )
        }

    private val recordGroupTransformer3 = RecordGroupTransformer<
            TestRecordObjectModel3,
            RecordGrouping.Single,
            TestRecordObjectModel2,
            RecordGrouping.Single>
    { recordGroup, inputDescriptor, outputDescriptors ->

        Flux.fromIterable(outputDescriptors)
            .map { outputDescriptor ->
                val outGroup = recordGroup.map { testRecordObjectModel ->
                    TestRecordObjectModel2(
                        Instant.now().toEpochMilli() - testRecordObjectModel.alignedDocument.hashCode(),
                        testRecordObjectModel.hashCode().toLong().absoluteValue,
                        testRecordObjectModel.model + "-name",
                    )
                }

                RecordGroupDescribedPair(
                    outGroup,
                    outputDescriptor,
                )
            }
    }

    val transformer3 = DataTransformerImpl(
        recordGroupDescriptorTransformer3,
        recordGroupTransformer3,
    )

    object Output {

        private val recordCollectionOut =
            DataRecordCollection.ofSpec<TestRecordObjectModel2, RecordGrouping.Single>(
                DataRecordCollectionSpec(
                    relativeId = "collection-1",
                    name = "Record Collection",
                    DataRecordCollectionUriComponent(
                        "collections/cl1"
                    ),
                    crawlSubdirs = true,
                    grouping = RecordGroupingValue.SINGLE,
                    recordFullyQualifiedClassName = TestRecordObjectModel2::class.qualifiedName!!
                )
            )

        private val fileWriterOut =
            FileRecordGroupWriter<TestRecordObjectModel2, RecordGrouping.Single, DataRecordObjectFormat.FileJson,
                    PlainStringSerializedRecord<DataRecordObjectFormat.FileJson>>()

        private val dataRecordObjectSerializer: DataRecordObjectSerializer<DataRecordObjectFormat.FileJson, PlainStringSerializedRecord<DataRecordObjectFormat.FileJson>> =
            object : DataRecordObjectSerializer<DataRecordObjectFormat.FileJson, PlainStringSerializedRecord<DataRecordObjectFormat.FileJson>> {
                override val format: DataRecordObjectFormat.FileJson = DataRecordObjectFormat.FileJson

                override fun serializeRecord(
                    recordMap: DataRecordMap
                ): PlainStringSerializedRecord<DataRecordObjectFormat.FileJson> {
                    return PlainStringSerializedRecord(
                        format,
                        mapper.writeValueAsString(recordMap.value)
                    )
                }
            }

        private val dataRecordObjectSerializingConverter =
            DataRecordObjectSerializingJacksonConverterFactory.getInstance().makeConverter<TestRecordObjectModel2>()

        val loader = DataLoaderImpl(
            fileDataVolume,
            recordCollectionOut,
            dataRecordObjectSerializingConverter,
            dataRecordObjectSerializer,
            fileWriterOut
        )

    }
}