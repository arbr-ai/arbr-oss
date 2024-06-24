package com.arbr.data_common.base.etl.load

import com.arbr.data_common.base.etl.TestEtlObjects
import org.junit.jupiter.api.Test

class DataLoaderTest {

    private val extractor = TestEtlObjects.FileExtractor.fileDataExtractor
    private val transformer = TestEtlObjects.transformer
    private val loader = TestEtlObjects.Output.loader

    @Test
    fun `loads outputs`() {
        val loadFlux = extractor.extract()
            .run(transformer::transform)
            .run(loader::load)

        loadFlux
            .collectList()
            .block()
    }

}