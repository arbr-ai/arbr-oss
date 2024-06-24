package com.arbr.data_common.impl

//import com.arbr.data_common.base.etl.transform.DataProcessor
//import com.fasterxml.jackson.databind.JsonMappingException
//import com.fasterxml.jackson.databind.ObjectMapper
//import org.apache.commons.io.FileUtils
//import org.slf4j.LoggerFactory
//import reactor.core.publisher.Flux
//import reactor.core.publisher.Mono
//import reactor.core.scheduler.Schedulers
//import java.io.File
//import java.nio.file.Path
//import java.nio.file.Paths
//import java.util.*
//import java.util.concurrent.ConcurrentLinkedDeque
//
//abstract class FileIngestor<I>(
//
//    private val mapper: ObjectMapper,
//    private val rootDataDir: String,
//    override val enabled: Boolean,
//
//    /**
//     * Whether to load input files as lines, such as JSONL
//     */
//    private val lines: Boolean = false,
//) : DataProcessor {
//
//    /**
//     * Whether to crawl subdirs for files to load.
//     */
//    open val crawlSubDirs = false
//
//    open fun shouldInclude(file: File): Mono<Boolean> = Mono.just(true)
//
//    open fun loadInputFile(file: File): Flux<I> {
//        return if (lines) {
//            Flux
//                .fromStream(file.bufferedReader().lines())
//                .mapNotNull {
//                    try {
//                        mapper.readValue(it, inputDataFileKind.parsedClass)
//                    } catch (jmex: JsonMappingException) {
//                        logger.error("Failed to parse a line in ${file.name}")
//                        null
//                    }
//                }
//        } else {
//            Mono.fromCallable {
//                Optional.ofNullable(
//                    try {
//                        mapper.readValue(file, inputDataFileKind.parsedClass)
//                    } catch (e: Exception) {
//                        null
//                    }
//                )
//            }
//                .subscribeOn(Schedulers.boundedElastic())
//                .flatMapMany { inputOpt ->
//                if (inputOpt.isEmpty) {
//                    logger.error("Failed to parse ${file.name}")
//                    Flux.empty()
//                } else {
//                    Flux.just(inputOpt.get())
//                }
//            }
//        }
//    }
//
//    open fun shouldIncludeLoaded(parsedInput: I): Mono<Boolean> = Mono.just(true)
//
//    abstract fun ingestEntry(entry: I): Mono<Void>
//
//    private fun filesWithUpdates(
//        dirPath: Path,
//    ): Flux<File> {
//        val filesMono = Mono.fromCallable {
//            FileUtils.listFiles(dirPath.toFile(), null, crawlSubDirs)
//        }.subscribeOn(Schedulers.boundedElastic())
//
//        val allFiles = ConcurrentLinkedDeque<File>()
//
//        val fileFlux = filesMono
//            .flatMapIterable { it }
//            .doOnNext {
//                allFiles.add(it)
//            }
//
//        return fileFlux
//
////        return Flux.concat(
////            fileFlux,
////            FileWatchdogFlux
////                .newFiles(dirPath)
////                .flatMap { // TODO: This doesn't acknowledge new files after the first one (Or does it?) - seems to work on Mac OS but not Linux
////                    if (it !in allFiles) {
////                        allFiles.add(it)
////                        Mono.just(it)
////                    } else {
////                        Mono.empty()
////                    }
////                }
////        )
//    }
//
//    protected open val ingestFileParallelism = 16
//
//    protected fun ingest(): Flux<I> {
//        val inputDirPath = Paths.get(rootDataDir, inputDataFileKind.getRelativePath().toString())
//        logger.info("${this::class.java.simpleName} Looking for files to ingest in $inputDirPath")
//        val loadFileParallelism = 16
//        val loadFilePrefetch = 128
//
//        return filesWithUpdates(inputDirPath)
//            .filterWhen(this::shouldInclude)
//            .flatMap({
//                logger.debug("Loading input file ${it.path}")
//                loadInputFile(it)
//                    .onErrorResume { ex ->
//                        logger.error("Error loading file ${it.path} to ingest, skipping", ex)
//                        Mono.empty()
//                    }
//            }, loadFileParallelism, loadFilePrefetch)
//            .filterWhen(this::shouldIncludeLoaded)
//            .flatMap({
//                ingestEntry(it)
//                    .thenReturn(it)
//            }, ingestFileParallelism)
//            .onErrorResume(FileIngestorFinishedException::class.java) {
//                logger.info("Ingestor signaled completion (${this::class.java.simpleName})")
//                Mono.empty()
//            }
//            .onErrorResume { ex ->
//                logger.error("Error during ingesting", ex)
//                Mono.empty()
//            }
//            .doOnSubscribe {
//                logger.info("Ingesting data (${this::class.java.simpleName})")
//            }
//            .doOnComplete {
//                logger.info("Finished ingesting data (${this::class.java.simpleName})")
//            }
//    }
//
//    override fun process(): Flux<Void> {
//        return ingest().flatMap { Mono.empty() }
//    }
//
//    companion object {
//        private val logger = LoggerFactory.getLogger(FileIngestor::class.java)
//    }
//}