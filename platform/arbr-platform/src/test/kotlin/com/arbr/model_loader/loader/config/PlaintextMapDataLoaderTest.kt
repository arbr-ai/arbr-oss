package com.arbr.model_loader.loader.config

import com.arbr.data_common.base.RecordGroup
import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.etl.load.RecordGroupDescribedPair
import com.arbr.data_common.impl.files.FileRecordGroupDescriptor
import com.arbr.model_loader.model.LanguageVocabularyWords
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux

class PlaintextMapDataLoaderTest {

    @Test
    fun `loads into memory map`() {
        val loader = PlaintextMapDataLoader<LanguageVocabularyWords, RecordGrouping.Single>(
            LanguageVocabularyWords::class.java
        )

        val textMap = loader.loadTextMap(
            Flux.fromIterable<RecordGroupDescribedPair<LanguageVocabularyWords, RecordGrouping.Single>>(
                listOf(
                    RecordGroupDescribedPair(
                        RecordGroup.Single(
                            LanguageVocabularyWords(listOf("a"))
                        ),
                        FileRecordGroupDescriptor("main/hats.txt"),
                    ),
                    RecordGroupDescribedPair(
                        RecordGroup.Single(
                            LanguageVocabularyWords(listOf("b", "c"))
                        ),
                        FileRecordGroupDescriptor("vats.txt"),
                    ),
                    RecordGroupDescribedPair(
                        RecordGroup.Single(
                            LanguageVocabularyWords(listOf("z", "x", "y"))
                        ),
                        FileRecordGroupDescriptor("abcd.txt"),
                    ),
                )
            )
        ).block()!!

        println(textMap)
        Assertions.assertEquals(3, textMap.size)
    }

}
