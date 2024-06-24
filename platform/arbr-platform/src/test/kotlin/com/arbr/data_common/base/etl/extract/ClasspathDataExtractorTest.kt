package com.arbr.data_common.base.etl.extract

import com.arbr.data_common.base.etl.TestEtlObjects
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ClasspathDataExtractorTest {

    private val dataExtractor = TestEtlObjects.ClasspathExtractor.classpathDataExtractor

    @Test
    fun `constructs classpath extractor`() {
        val extracted = dataExtractor
            .extract()
            .doOnNext { recordGroupDescribedPair ->
                println(recordGroupDescribedPair.recordGroupDescriptor.toString().take(64) + "...")
                println(recordGroupDescribedPair.recordGroup.toString().take(64) + "...")
            }
            .collectList()
            .block()!!

        println(extracted.size)
        Assertions.assertEquals(3, extracted.size)
    }

}