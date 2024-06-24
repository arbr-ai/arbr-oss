package com.arbr.model_loader.loader

//import com.arbr.aws.s3.S3Service
//import com.arbr.content_formats.common_text.GitUtils
//import com.arbr.model_loader.model.GitHubPublicNoisedPatchInfo
//import com.fasterxml.jackson.databind.JsonMappingException
//import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
//import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
//import org.slf4j.LoggerFactory
//import reactor.core.publisher.Flux
//import reactor.core.publisher.Mono
//import java.nio.file.Files
//import java.nio.file.Paths
//import java.util.*
//import java.util.stream.Stream
//import kotlin.streams.asStream
//
//class NoisyRecordFileLoader(
//    private val s3Service: S3Service,
//) : DatasetRecordLoader<GitHubPublicNoisedPatchInfo> {
//    private val dataLoadingConfiguration = DataLoadingConfiguration...
//    private val weightsDir by lazy {
//        dataLoadingConfiguration.weightsDir!!
//    }
//    private val noisedPatchDir by lazy {
//        dataLoadingConfiguration.noisedPatchDir!!
//    }
//
//    /**
//     * Folder within bucket
//     */
//    private val s3Folder = "com.arbr.alignable-diffs/noised/0/"
//
//    private val mapper = jacksonObjectMapper()
//
//    private fun recordStream(
//        recordFilter: DatasetRecordFilter<GitHubPublicNoisedPatchInfo>,
//        manifestFileName: String,
//        numFiles: Int,
//        random: Random,
//    ): Stream<GitHubPublicNoisedPatchInfo> {
//        val noisedPatchDirPath = Paths.get(noisedPatchDir.path)
//        val manifestFile = Paths.get(weightsDir.path, manifestFileName).toFile()
//
//        val reposAndPatches = mutableListOf<GitHubPublicNoisedPatchInfo>()
//
//        val manifestFileNames = if (manifestFile.exists() && manifestFile.isFile) {
//            val fileNames = mapper.readValue(manifestFile, jacksonTypeRef<List<String>>())
//
//            for (fn in fileNames) {
//                val path = Paths.get(noisedPatchDirPath.toString(), fn)
//                try {
//                    val patchInfo = mapper.readValue(path.toFile(), GitHubPublicNoisedPatchInfo::class.java)
//                        .let { loadedPatchInfo ->
//                            val revertedDocumentContent = GitUtils
//                                .applyPatch(
//                                    loadedPatchInfo.fileContent,
//                                    loadedPatchInfo.patchContent,
//                                    reverse = true,
//                                )
//                            loadedPatchInfo.copy(
//                                baseDocument = revertedDocumentContent
//                            )
//                        }
//
//                    if (recordFilter.shouldInclude(patchInfo)) {
//                        reposAndPatches.add(patchInfo)
//                        if (reposAndPatches.size >= numFiles) {
//                            break
//                        }
//                    }
//                } catch (e: Exception) {
//                    logger.warn("Failed to unmap listed manifest file $path - skipping")
//                }
//            }
//
//            fileNames
//        } else {
//            emptyList()
//        }
//
//        if (reposAndPatches.size >= numFiles) {
//            logger.info("Loaded ${reposAndPatches.size} files from manifest file $manifestFileName")
//            return reposAndPatches.stream()
//        }
//
//        logger.info("Loading ${numFiles - reposAndPatches.size} files not from manifest")
//        val repoDataFilePaths = Files.list(Paths.get(noisedPatchDir.path))
//            .toList()
//            .shuffled(random)
//        var repoDataFilePathsIndex = 0
//
//        return generateSequence {
//            if (reposAndPatches.size < numFiles) {
//                var foundPatchInfo: GitHubPublicNoisedPatchInfo? = null
//
//                while (repoDataFilePathsIndex < repoDataFilePaths.size) {
//                    val path = repoDataFilePaths[repoDataFilePathsIndex]
//                    repoDataFilePathsIndex++
//
//                    val pathFile = path.toFile()
//                    val fileName = pathFile.name
//
//                    if (path.toString().endsWith(".json") && fileName !in manifestFileNames) {
//                        try {
//                            val patchInfo = mapper.readValue(pathFile, GitHubPublicNoisedPatchInfo::class.java)
//                                .let { loadedPatchInfo ->
//                                    val revertedDocumentContent = GitUtils
//                                        .applyPatch(
//                                            loadedPatchInfo.fileContent,
//                                            loadedPatchInfo.patchContent,
//                                            reverse = true,
//                                        )
//                                    loadedPatchInfo.copy(
//                                        baseDocument = revertedDocumentContent
//                                    )
//                                }
//
//                            if (recordFilter.shouldInclude(patchInfo)) {
//                                foundPatchInfo = patchInfo
//                                break
//                            }
//                        } catch (e: JsonMappingException) {
//                            // logger.warn("Failed to unmap file $path - skipping")
//                        }
//                    }
//                }
//
//                foundPatchInfo
//            } else {
//                null
//            }
//        }.asStream()
//    }
//
//    fun uploadRecordsToS3(): Mono<Void> {
//        return Flux.fromStream(
//            recordStream(
//                recordFilter = { true },
//                manifestFileName = "",
//                numFiles = Int.MAX_VALUE,
//                random = Random(),
//            )
//        )
//            .flatMap { record ->
//                val testCaseName = listOf(
//                    "n",
//                    record.repoId,
//                    record.pullRequestId,
//                    record.commitSha,
//                    record.fileSha,
//                ).joinToString("-") { it.toString() }
//
//                s3Service.uploadObject(s3Folder + testCaseName, record)
//            }
//            .then()
//    }
//
//    override fun loadRecords(
//        recordFilter: DatasetRecordFilter<GitHubPublicNoisedPatchInfo>,
//        manifestFileName: String,
//        numFiles: Int,
//        random: Random
//    ): Flux<GitHubPublicNoisedPatchInfo> {
//        return Flux.fromStream(recordStream(recordFilter, manifestFileName, numFiles, random))
//    }
//
//    companion object {
//        private val logger = LoggerFactory.getLogger(NoisyRecordFileLoader::class.java)
//    }
//}