package com.arbr.data_common.base.etl.transform

import com.arbr.data_common.base.RecordGrouping
import com.arbr.data_common.base.etl.TestEtlObjects
import com.arbr.data_common.base.etl.TestRecordObjectModel2
import com.arbr.data_common.impl.files.FileRecordGroupDescriptor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DataTransformerTest {

    private val extractor = TestEtlObjects.FileExtractor.fileDataExtractor
    private val transformer = TestEtlObjects.transformer

    @Test
    fun `constructs transformer`() {
        val transformedFlux = extractor.extract()
            .run(transformer::transform)

        val result = transformedFlux
            .doOnNext {
                println(it)
            }
            .collectList()
            .block()!!

        Assertions.assertEquals(2, result.size)
        val fileRecordGroupDescriptors = result.map {
            (it.recordGroupDescriptor as FileRecordGroupDescriptor<TestRecordObjectModel2, RecordGrouping.Single>).fileRecordRelativePathString
        }.toSet()
        Assertions.assertEquals(2, fileRecordGroupDescriptors.size)

        Assertions.assertTrue("goodbye.txtx" in fileRecordGroupDescriptors)
        Assertions.assertTrue("hello.txtx" in fileRecordGroupDescriptors)
    }

}