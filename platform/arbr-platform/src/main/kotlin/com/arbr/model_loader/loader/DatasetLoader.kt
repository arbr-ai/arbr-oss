package com.arbr.model_loader.loader

import com.arbr.model_loader.model.Dataset
import com.arbr.model_loader.model.DiffPatchDatasetKind
import java.util.*

interface DatasetLoader<Record, Datum> {

    val datasetKind: DiffPatchDatasetKind

    val recordLoader: DatasetRecordLoader<Record>
    val recordFilter: DatasetRecordFilter<Record>
    val recordMapper: DatasetRecordMapper<Record, Datum>

    fun loadDataset(
        numFilesTraining: Int,
        numFilesTest: Int,
        subsampleRate: Double,
        randomSeed: Long
    ): Dataset<Datum> {
        val random = Random(randomSeed)

        val sanitizedName = datasetKind.serializedName.lowercase()
            .replace(Regex("\\s+"), "")
            .replace(Regex("([.,\\-])"), "_")

        val trainingManifestFilename = "manifest_${sanitizedName}_train_${randomSeed}"
        val trainingData = recordLoader.loadRecords(
            recordFilter,
            trainingManifestFilename,
            numFilesTraining,
            random,
        ).filter(recordFilter::shouldInclude)
            .map(recordMapper::mapRecord)

        val testManifestFilename = "manifest_${sanitizedName}_test_${randomSeed}"
        val testData = recordLoader.loadRecords(
            recordFilter,
            testManifestFilename,
            numFilesTest,
            random,
        ).filter(recordFilter::shouldInclude)
            .map(recordMapper::mapRecord)

        return Dataset(
            datasetKind.serializedName,
            trainingData,
            testData,
        )
    }
}