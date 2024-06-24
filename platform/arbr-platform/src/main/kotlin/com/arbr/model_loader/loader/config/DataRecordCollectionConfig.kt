package com.arbr.model_loader.loader.config

import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.storage.DataRecordCollection
import com.arbr.data_common.spec.element.DataRecordCollectionSpec
import com.arbr.data_common.spec.model.RecordGroupingValue
import com.arbr.data_common.spec.uri.DataRecordCollectionUriComponent
import com.arbr.model_loader.loader.SerializedScoredParameterSet
import com.arbr.model_loader.model.DiffPatchTestCase
import com.arbr.model_loader.model.GitHubPublicNoisedPatchInfo
import com.arbr.model_loader.model.LanguageVocabularyWords
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataRecordCollectionConfig {

    @Bean("noisedPatchRecordCollection")
    fun noisedPatchRecordCollection(): DataRecordCollection<GitHubPublicNoisedPatchInfo, RecordGrouping.Single> {
        return DataRecordCollection.ofSpec(
            DataRecordCollectionSpec(
                relativeId = "arbr.github-public-patches.web-noised",
                name = "GitHub Public Patches: Web, Noised",
                DataRecordCollectionUriComponent(
                    "topdown/github_public_patches_noised/web/",
                ),
                crawlSubdirs = false,  // Flat
                grouping = RecordGroupingValue.SINGLE,
                recordFullyQualifiedClassName = GitHubPublicNoisedPatchInfo::class.qualifiedName!!
            )
        )
    }

    @Bean("hardcodedDataRecordCollection")
    fun hardcodedDataRecordCollection(): DataRecordCollection<DiffPatchTestCase, RecordGrouping.Single> {
        // Note needs filter to JSON
        // Meant to pair with classpath resources
        return DataRecordCollection.ofSpec(
            DataRecordCollectionSpec(
                relativeId = "arbr.diff-patches.hardcoded",
                name = "Hardcoded Diff Alignment test cases",
                DataRecordCollectionUriComponent(
                    "hardcoded_data/",
                ),
                crawlSubdirs = true,  // Nested
                grouping = RecordGroupingValue.SINGLE,
                recordFullyQualifiedClassName = DiffPatchTestCase::class.qualifiedName!!
            )
        )
    }

    @Bean("alignableWeightsProductionRecordCollection")
    fun alignableWeightsProductionRecordCollection(): DataRecordCollection<SerializedScoredParameterSet, RecordGrouping.Single> {
        return DataRecordCollection.ofSpec(
            DataRecordCollectionSpec(
                relativeId = "arbr.alignable.weights-primary-prod",
                name = "Alignable Weights for source code diff alignment (primary use case, prod)",
                DataRecordCollectionUriComponent(
                    "weights_diff_alignment/prod/",
                ),
                crawlSubdirs = true,  // Nested
                grouping = RecordGroupingValue.SINGLE,
                recordFullyQualifiedClassName = SerializedScoredParameterSet::class.qualifiedName!!
            )
        )
    }

    @Bean("alignableWeightsRecordCollection")
    fun alignableWeightsRecordCollection(): DataRecordCollection<SerializedScoredParameterSet, RecordGrouping.Single> {
        return DataRecordCollection.ofSpec(
            DataRecordCollectionSpec(
                relativeId = "arbr.alignable.weights-primary-working",
                name = "Alignable Weights for source code diff alignment (primary use case, training and eval)",
                DataRecordCollectionUriComponent(
                    "weights_diff_alignment/working/",
                ),
                crawlSubdirs = true,  // Nested
                grouping = RecordGroupingValue.SINGLE,
                recordFullyQualifiedClassName = SerializedScoredParameterSet::class.qualifiedName!!
            )
        )
    }

    @Bean("alignableWeightsSimpletextProductionRecordCollection")
    fun alignableWeightsSimpletextProductionRecordCollection(): DataRecordCollection<SerializedScoredParameterSet, RecordGrouping.Single> {
        return DataRecordCollection.ofSpec(
            DataRecordCollectionSpec(
                relativeId = "arbr.alignable.weights-simpletext-prod",
                name = "Alignable Weights for source code diff alignment (simpletext case, prod)",
                DataRecordCollectionUriComponent(
                    "weights_diff_alignment/simpletext_prod/",
                ),
                crawlSubdirs = true,  // Nested
                grouping = RecordGroupingValue.SINGLE,
                recordFullyQualifiedClassName = SerializedScoredParameterSet::class.qualifiedName!!
            )
        )
    }

    @Bean("alignableWeightsSimpletextRecordCollection")
    fun alignableWeightsSimpletextRecordCollection(): DataRecordCollection<SerializedScoredParameterSet, RecordGrouping.Single> {
        return DataRecordCollection.ofSpec(
            DataRecordCollectionSpec(
                relativeId = "arbr.alignable.weights-simpletext-working",
                name = "Alignable Weights for source code diff alignment (simpletext case, training and eval)",
                DataRecordCollectionUriComponent(
                    "weights_diff_alignment/simpletext_working/",
                ),
                crawlSubdirs = true,  // Nested
                grouping = RecordGroupingValue.SINGLE,
                recordFullyQualifiedClassName = SerializedScoredParameterSet::class.qualifiedName!!
            )
        )
    }

    @Bean("documentModelVocabularyRecordCollection")
    fun documentModelVocabularyRecordCollection(): DataRecordCollection<LanguageVocabularyWords, RecordGrouping.Single> {
        return DataRecordCollection.ofSpec(
            DataRecordCollectionSpec(
                relativeId = "arbr.indents.vocab",
                name = "Language vocabularies for indent models",
                DataRecordCollectionUriComponent(
                    "vocab/",
                ),
                crawlSubdirs = true,  // Nested
                grouping = RecordGroupingValue.SINGLE,
                recordFullyQualifiedClassName = LanguageVocabularyWords::class.qualifiedName!!
            )
        )
    }

}