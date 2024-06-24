package com.arbr.engine.services.differential_content.diff_alignment

import com.arbr.content_formats.mapper.Mappers
import com.arbr.engine.services.differential_content.formatter.DefaultDocumentSerializerFactory
import com.arbr.engine.services.differential_content.formatter.JsonLenientPostProcessor
import com.arbr.model_loader.indents.IndentAlignmentService
import com.arbr.model_loader.loader.ParameterLoaderFactory
import com.arbr.test_util.CodeEditDiffAlignmentTestCase
import com.arbr.ml.optimization.base.NamedMetricKind
import com.arbr.ml.optimization.model.BindingParameter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.io.File

class DiffApplicatorServiceTest {

    private val helper = DocumentDiffAlignmentConfig(
        Mockito.mock(ParameterLoaderFactory::class.java),
        0.0,
    ).documentDiffAlignmentHelper()

    private val indentAlignmentService = IndentAlignmentService(
        Mockito.mock(),
        Mockito.mock(),
        BindingParameter(
            NamedMetricKind("TRAINING_SCORE_ADMISSIBLE_THRESHOLD"),
            0.8,
        ),
    )
        .also { it.init() }
    private val mapper = Mappers.mapper
    private val defaultDocumentSerializerFactory = DefaultDocumentSerializerFactory(indentAlignmentService, JsonLenientPostProcessor(mapper))
    private val diffApplicatorService = DiffApplicatorService(helper, defaultDocumentSerializerFactory)

    private fun getNewContent(testCase: CodeEditDiffAlignmentTestCase): String {
        return diffApplicatorService.extractAndApplyAlignedDiff(
            "0",
            testCase.filePath,
            testCase.baseDocument,
            testCase.rawModelOutput,
        ).block()!!
    }

    @Test
    fun `aligns replay case`() {
        val file = File("src/test/resources/patch_replay_test_cases/tc_1726bd09_f168b70b.json")
        val testCase = mapper.readValue(file, CodeEditDiffAlignmentTestCase::class.java)

        val resultText = getNewContent(testCase)

        println(resultText)
        Assertions.assertTrue("<JobListing />" in resultText)
    }

    @Test
    fun `aligns case duplicate content`() {
        val file = File("src/test/resources/patch_replay_test_cases/tc_fe8ec0c4_a544911b.json")
        val testCase = mapper.readValue(file, CodeEditDiffAlignmentTestCase::class.java)

        val resultText = getNewContent(testCase)
        val numLines = resultText.split("\n").size

        Assertions.assertEquals(37, numLines)
    }

    @Test
    fun `aligns case duplicate content minus leading space`() {
        val file = File("src/test/resources/patch_replay_test_cases/tc_fe8ec0c4_a544911b.json")
        val testCase = mapper.readValue(file, CodeEditDiffAlignmentTestCase::class.java)
            .let {
                it.copy(
                    rawModelOutput = it.rawModelOutput.split("\n").joinToString("\n") { line ->
                        if (line.startsWith("+ ")) {
                            line.replaceFirst("+ ", "+")
                        } else {
                            line
                        }
                    }
                )
            }

        val resultText = getNewContent(testCase)
        val numLines = resultText.split("\n").size

        println(resultText)
        Assertions.assertEquals(37, numLines)
    }

    @Test
    fun `aligns package json`() {
        val file = File("src/test/resources/patch_replay_test_cases/tc_pkgjson.json")
        val testCase = mapper.readValue(file, CodeEditDiffAlignmentTestCase::class.java)
            .let {
                it.copy(
                    rawModelOutput = it.rawModelOutput.split("\n").joinToString("\n") { line ->
                        if (line.startsWith("+ ")) {
                            line.replaceFirst("+ ", "+")
                        } else {
                            line
                        }
                    }
                )
            }

        val resultText = getNewContent(testCase)
        val numLines = resultText.split("\n").size

        println(resultText)
        Assertions.assertEquals(30, numLines)
    }

}
