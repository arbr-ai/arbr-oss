package com.arbr.data_common.impl

/**
 * Abstract parent class for processing data from DB records and writing to files
 * TODO: Port and re-enable
 */
//abstract class DbRecordProcessor<I, O>(
//    private val outputDataFileKind: DataFileKind<O>,
//    private val mapper: ObjectMapper,
//    private val rootDataDir: String,
//    enabled: Boolean,
//) : DbRecordIngestor<I>(enabled) {
//
//    private val counter = AtomicLong()
//
//    protected open val processRecordConcurrency = 8
//
//    protected val outputDir: Path = Paths.get(rootDataDir, outputDataFileKind.getRelativePath().toString())
//
//    init {
//        if (enabled) {
//            Files.createDirectories(outputDir)
//        }
//    }
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
//                    val file = outFilePath.toFile()
//                    file.createNewFile()
//                    mapper.writeValue(file, output)
//
//                    logger.info("Wrote output file $outFilePath")
//                }.subscribeOn(Schedulers.boundedElastic())
//            }
//        }.then()
//    }
//
//    override fun process(): Flux<Void> {
//        return ingest()
//            .flatMap(this::processEntry, processRecordConcurrency)
//            .flatMap { (input, output) ->
//                writeOutput(input, output)
//                    .doOnSuccess {
//                        val count = counter.incrementAndGet()
//                        if (count % 100L == 0L) {
//                            logger.info("${this::class.java.simpleName} processed $count")
//                        }
//                    }
//            }
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
//        private val logger = LoggerFactory.getLogger(DbRecordProcessor::class.java)
//    }
//}