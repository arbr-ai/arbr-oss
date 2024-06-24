package com.arbr.data_common.base.etl.extract

import com.arbr.data_common.base.etl.TestEtlObjects
import org.junit.jupiter.api.Test

class FileDataExtractorTest {

    private val dataExtractor = TestEtlObjects.FileExtractor.fileDataExtractor

    @Test
    fun `constructs file extractor`() {
        dataExtractor
            .extract()
            .doOnNext { recordGroupDescribedPair ->
                println(recordGroupDescribedPair.recordGroupDescriptor)
                println(recordGroupDescribedPair.recordGroup)
            }
            .collectList()
            .block()
    }

}