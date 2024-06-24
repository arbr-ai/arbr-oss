package com.arbr.model_loader.model

import com.arbr.content_formats.format.DiffLiteralPatch
import com.arbr.content_formats.format.DiffLiteralSourceDocument
import com.arbr.data_common.base.DataRecordObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DiffPatchTestCase(
    val hash: String,
    val name: String,
    val datasetName: DiffPatchDatasetKind,
    val baseDocument: DiffLiteralSourceDocument,
    val patch: DiffLiteralPatch,
    val expectedResult: DiffLiteralSourceDocument,
    val metadata: Map<String, Any>,
): DataRecordObject

