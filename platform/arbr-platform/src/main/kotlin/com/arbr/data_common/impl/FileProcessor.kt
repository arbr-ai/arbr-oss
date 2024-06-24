package com.arbr.data_common.impl

//import com.arbr.data_common.base.etl.transform.DataProcessor
//import com.arbr.dataloaders.util.nonBlocking
//import com.fasterxml.jackson.databind.ObjectMapper
//import org.slf4j.LoggerFactory
//import reactor.core.publisher.Flux
//import reactor.core.publisher.Mono
//import reactor.core.scheduler.Schedulers
//import java.nio.file.Files
//import java.nio.file.Path
//import java.nio.file.Paths
//import java.util.concurrent.atomic.AtomicLong
//import kotlin.io.path.exists
//
//abstract class FileProcessor<I, O>(
//    inputDataFileKind: DataFileKind<I>,
//    private val outputDataFileKind: DataFileKind<O>,
//    private val mapper: ObjectMapper,
//    private val rootDataDir: String,
//    enabled: Boolean,
//
//    /**
//     * Whether to load input files as lines, such as JSONL
//     */
//    lines: Boolean = false,
//) : FileIngestor<I>(
//    inputDataFileKind, mapper, rootDataDir, enabled, lines
//), DataProcessor {
//
//    private val numResourcesWritten = AtomicLong()
//
//    private val counter = AtomicLong()
//
//    protected open val processFileConcurrency = 8
//    protected open val writeFileConcurrency = 8
//
//    protected val outputDir: Path = Paths.get(rootDataDir, outputDataFileKind.getRelativePath().toString())
//
//    init {
//        if (enabled) {
//            Files.createDirectories(outputDir)
//        }
//    }
//
//    /**
//     * Load any initial content and perform any initialization necessary.
//     */
//    open fun loadInitialContent(): Mono<Void> = Mono.empty()
//
//    override fun ingestEntry(entry: I): Mono<Void> = Mono.empty()
//
//    abstract fun processEntry(entry: I): Flux<Pair<I, O>>
//
//    abstract fun getFileName(input: I, output: O): Mono<String>
//
//    open fun getFileOutputDir(input: I, output: O): Mono<Path> {
//        return Mono.just(outputDir)
//    }
//
//    open fun writeOutput(input: I, output: O): Mono<Void> {
//        return getFileName(input, output).flatMap { fileName ->
//            getFileOutputDir(input, output).flatMap { fileOutputDir ->
//                Mono.fromCallable {
//                    Files.createDirectories(fileOutputDir)
//
//                    val outFilePath = Paths.get(fileOutputDir.toString(), fileName)
//                    val didExist = outFilePath.exists()
//
//                    val file = outFilePath.toFile()
//                    file.createNewFile()
//                    mapper.writeValue(file, output)
//
//                    val numWritten = numResourcesWritten.incrementAndGet()
//                    val numWrittenString = numWritten.toString().padStart(8, ' ')
//                    val suffix = if (didExist) " (existed)" else ""
//                    logger.info("[$numWrittenString] Wrote output file $outFilePath$suffix")
//                }.subscribeOn(Schedulers.boundedElastic())
//            }
//        }.then()
//    }
//
//    override fun process(): Flux<Void> {
//        return loadInitialContent()
//            .thenMany(Flux.defer {
//                ingest()
//            })
//            .flatMap(this::processEntry, processFileConcurrency)
//            .flatMap({ (fileName, output) ->
//                writeOutput(fileName, output)
//                    .doOnSuccess {
//                        val count = counter.incrementAndGet()
//                        if (count % 100L == 0L) {
//                            logger.info("${this::class.java.simpleName} processed $count")
//                        }
//                    }
//            }, writeFileConcurrency)
//            .onErrorResume { ex ->
//                logger.error("Error during processing", ex)
//                Mono.empty()
//            }
//            .doOnSubscribe {
//                logger.info("Processing data (${this::class.java.simpleName})")
//            }
//    }
//
//    companion object {
//        private val logger = LoggerFactory.getLogger(FileProcessor::class.java)
//    }
//}
