package com.arbr.model_loader.loader

import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.etl.extract.DataExtractor
import com.arbr.model_loader.model.DiffPatchDatasetKind
import com.arbr.model_loader.model.DiffPatchTestCase
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class HardcodedDiffDatasetLoader(
    @Qualifier("hardcodedDataExtractor")
    private val dataExtractorMono: Mono<DataExtractor<DiffPatchTestCase, RecordGrouping.Single>>,
): DatasetLoader<DiffPatchTestCase, DiffPatchTestCase> {
    private val mapper = jacksonObjectMapper()

    override val datasetKind = DiffPatchDatasetKind.HARDCODED

    /**
     * Clean diff loads from noisy and just uses the base / clean patch / result values
     */
    override val recordLoader = DatasetRecordLoader { _, _, totalRecords, _ ->
        dataExtractorMono
            .flatMapMany { dataExtractor ->
                dataExtractor
                    .extract()
            }
            .flatMapIterable {
                it.recordGroup.flatten()
            }
            .filter(recordFilter::shouldInclude)
            .repeat()
            .take(totalRecords.toLong())
    }
    override val recordFilter = DatasetRecordFilter<DiffPatchTestCase> {
        true
    }
    override val recordMapper = DatasetRecordMapper<DiffPatchTestCase, DiffPatchTestCase> { it }

//    fun saveTestCases() {
//        val hardcodedTestCases = loadTestCasesFromDisk()
//
//        FileUtils.deleteDirectory(hardcodedDataDir)
//        Files.createDirectories(hardcodedDataDir.toPath())
//        for (testCase in hardcodedTestCases) {
//            val hash = HashUtils.sha1Hash(
//                testCase.baseDocument.text,
//                testCase.patch.text,
//                testCase.expectedResult.text,
//            )
//            val testCaseDir = Paths.get(hardcodedDataDir.toString(), testCase.name)
//            Files.createDirectories(testCaseDir)
//
//            Paths.get(testCaseDir.toString(), "base.txt").toFile().writeText(
//                testCase.baseDocument.text,
//            )
//            Paths.get(testCaseDir.toString(), "patch.txt").toFile().writeText(
//                testCase.patch.text,
//            )
//            Paths.get(testCaseDir.toString(), "aligned.txt").toFile().writeText(
//                testCase.expectedResult.text,
//            )
//
//            val updatedTestCase = testCase.copy(hash = hash)
//            val jsonFile = Paths.get(testCaseDir.toString(), "record.json").toFile()
//            mapper.writeValue(jsonFile, updatedTestCase)
//        }
//    }

//    private fun loadTestCasesFromDisk(): List<DiffPatchTestCase> {
//        val testCaseSubdirs = Files.list(hardcodedDataDir.toPath())
//            .filter { it.isDirectory() }
//            .toList()
//
//        val testCases = mutableListOf<DiffPatchTestCase>()
//        for (subdir in testCaseSubdirs) {
//            val testCaseName = subdir.name
//
//            val filePaths = Files.list(subdir)
//                .filter { it.isRegularFile() }
//                .toList()
//
//            val jsonFile = filePaths.firstOrNull { it.fileName.toString() == "record.json" }
//            if (jsonFile == null) {
//                val baseFilePath = Paths.get(subdir.toString(), "base.txt")
//                val patchFilePath = Paths.get(subdir.toString(), "patch.txt")
//                val alignedFilePath = Paths.get(subdir.toString(), "aligned.txt")
//
//                if (baseFilePath.exists() && patchFilePath.exists() && alignedFilePath.exists()) {
//                    val baseDocument = DiffLiteralSourceDocument(baseFilePath.toFile().readText())
//                    val patch = DiffLiteralPatch(patchFilePath.toFile().readText())
//                    val alignedDocument = DiffLiteralSourceDocument(alignedFilePath.toFile().readText())
//                    val hash = HashUtils.sha1Hash(
//                        baseDocument.text,
//                        patch.text,
//                        alignedDocument.text,
//                    )
//
//                    val testCase = DiffPatchTestCase(
//                        hash,
//                        testCaseName,
//                        DiffPatchDatasetKind.HARDCODED,
//                        baseDocument,
//                        patch,
//                        alignedDocument,
//                        emptyMap()
//                    )
//
//                    testCases.add(testCase)
//                }
//            } else {
//                testCases.add(mapper.readValue(jsonFile.toFile(), DiffPatchTestCase::class.java))
//            }
//        }
//
//        return testCases
//    }
}
