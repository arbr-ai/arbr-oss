package com.arbr.model_loader.loader.config

import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.etl.extract.DataExtractorConfig
import com.arbr.data_common.base.etl.load.DataLoader
import com.arbr.model_loader.model.LanguageVocabularyWords
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataLoaderConfig {

    @Bean("plaintextVocabularyMapDataLoader")
    fun plaintextVocabularyMapDataLoader(): PlaintextMapDataLoader<LanguageVocabularyWords, RecordGrouping.Single> {
        return PlaintextMapDataLoader<LanguageVocabularyWords, RecordGrouping.Single>(
            LanguageVocabularyWords::class.java
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DataExtractorConfig::class.java)
    }
}
