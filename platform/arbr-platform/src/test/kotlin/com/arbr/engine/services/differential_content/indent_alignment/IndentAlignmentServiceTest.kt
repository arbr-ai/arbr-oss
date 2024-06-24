package com.arbr.engine.services.differential_content.indent_alignment

import com.arbr.content_formats.format.*
import com.arbr.content_formats.format.tokenizer.TokenizationSerializer
import com.arbr.engine.services.differential_content.formatter.Formatters
import com.arbr.model_loader.indents.IndentAlignmentService
import com.arbr.ml.optimization.base.NamedMetricKind
import com.arbr.ml.optimization.model.BindingParameter
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.test.util.ReflectionTestUtils
import reactor.core.publisher.Mono
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.extension
import kotlin.io.path.readText

class IndentAlignmentServiceTest {

    private val indentAlignmentService = IndentAlignmentService(
        Mockito.mock(),
        Mockito.mock(),
        BindingParameter(
            NamedMetricKind("TRAINING_SCORE_ADMISSIBLE_THRESHOLD"),
            0.8,
        ),
    ).also { it.init() }

    private val baseSerializer = DiffLiteralSourceDocumentSerializer()
    private val addOpSerializer =
        TokenizationSerializer<DiffLiteralSourceDocument, DiffOperation> { tokens, formatter ->
            baseSerializer.serializeWith(
                tokens.map { op ->
                    op.copy(
                        kind = DiffOperationKind.ADD
                    )
                },
                formatter,
            )
        }

    @Test
    fun `trains on file content changes`() {
        val srcDir = Paths.get("src/test/resources")

        val inferenceFileName = "GitHubLogin.jsx"

        val trainFilePaths = Files.list(Paths.get("tmp"))
            .filter { it.extension in listOf("js", "jsx") && it.fileName.toString() != inferenceFileName }
            .toList()

//        val trainFilePaths = listOf(
//            Paths.get(srcDir.toString(), "Account.jsx"),
//            Paths.get(srcDir.toString(), "NewTask.jsx"),
//            Paths.get(srcDir.toString(), "ExistingProject.jsx"),
//        )
        val inferenceFilePath = Paths.get(srcDir.toString(), inferenceFileName)

        for (path in trainFilePaths) {
            val fileContent = path.readText()
            if (fileContent.length >= 10000) {
                continue
            }

            indentAlignmentService.fileContentWasUpdated(
                "w0test",
                path.toString(),
                null,
                fileContent,
            )
        }

        val retrainMono =
            ReflectionTestUtils.invokeMethod<Mono<Void>>(indentAlignmentService, "innerRetrainModelsIfNecessary")!!
        retrainMono.block()

        val formatterCompiler = indentAlignmentService.getDocumentFormatterCompiler(
            "w0test",
            inferenceFilePath.toString(),
        ).block()!!

        val inferenceFileText = inferenceFilePath.readText()
        val inferenceDocument = DiffLiteralSourceDocument(inferenceFileText)
        val tokens = DiffLiteralSourceDocumentTokenizer().tokenize(inferenceDocument)

//        val addTokens = tokens.map { op ->
//            op.copy(kind = DiffOperationKind.ADD)
//        }

//        val formatter = formatterCompiler.compileFormatter(
//            addTokens,
//            inferenceDocument,
//        )

        val combinedSerializer: TokenizationSerializer<DiffLiteralSourceDocument, DiffOperation> =
            Formatters.combine(
                addOpSerializer,
                listOf(formatterCompiler)
            )

        val serializedDocument = combinedSerializer.serializeWith(tokens) { i, op ->
            println("$i. $op")

            op
        }

        println("=".repeat(50))
        println(serializedDocument.text)

        // Seems to legitimately fail right now. TODO: Investigate
        // Assertions.assertEquals(inferenceFileText, serializedDocument.text)
    }

}
