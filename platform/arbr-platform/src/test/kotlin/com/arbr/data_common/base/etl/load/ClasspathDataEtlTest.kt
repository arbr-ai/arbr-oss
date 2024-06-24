package com.arbr.data_common.base.etl.load

import com.arbr.data_common.base.etl.TestEtlObjects
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class ClasspathDataEtlTest {

    private val extractor = TestEtlObjects.ClasspathExtractor.classpathDataExtractor
    private val transformer = TestEtlObjects.transformer3
    private val loader = TestEtlObjects.Output.loader

    @Test
    fun `loads outputs`() {
        val loadFlux = extractor.extract()
            .run(transformer::transform)
            .run(loader::loadToUris)
            .doOnNext { uri ->
                val destinationUri = uri.concat()
                println("Loaded to $destinationUri")
                val destinationFile = Paths.get(destinationUri.lenientEffectivePath).toFile()
                Assertions.assertTrue(destinationFile.exists() && destinationFile.isFile && destinationFile.length() > 0)
            }

        val loadedUris = loadFlux
            .collectList()
            .block()!!

        Assertions.assertEquals(3, loadedUris.size)
    }

}