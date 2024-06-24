package com.arbr.model_suite.predictive_models.linear_tree_indent

import com.arbr.platform.ml.linear.typed.impl.TypedMatrices
import com.arbr.platform.ml.linear.typed.shape.Dim

interface DocumentIndentEncoder {
    @Suppress("UNCHECKED_CAST")
    fun <SampleSize: Dim, DocumentSize: Dim, FeatureDimensions: Dim> encodeMany(
        documentModels: List<DocumentModel>,
        sampleSizeShape: SampleSize,
        documentSampleSizeShape: DocumentSize,
        featureDimensionsShape: FeatureDimensions,
    ): TypedMatrices.Sample<Dim.ProductOf<SampleSize, DocumentSize>, FeatureDimensions> {
        val samples = documentModels.map { encode(it, sampleSizeShape, featureDimensionsShape) }

        // Have to coerce types here
        // TODO("Migrate multiplicity logic into ml lib")
        return TypedMatrices.concatSamples(
            samples,
        ) as TypedMatrices.Sample<Dim.ProductOf<SampleSize, DocumentSize>, FeatureDimensions>
    }

    fun <DocumentSize: Dim, FeatureDimensions: Dim> encode(
        documentModel: DocumentModel,
        documentSampleSizeShape: DocumentSize,
        featureDimensionsShape: FeatureDimensions,
    ): TypedMatrices.Sample<DocumentSize, FeatureDimensions>
}
