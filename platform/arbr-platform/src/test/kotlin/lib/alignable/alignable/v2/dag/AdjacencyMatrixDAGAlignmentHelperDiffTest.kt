package com.arbr.alignable.alignable.v2.dag

import com.arbr.content_formats.format.*
import com.arbr.data_structures_common.partial_order.LinearOrderList
import com.arbr.model_loader.loader.CleanDiffDatasetLoader
import com.arbr.model_loader.loader.HardcodedDiffDatasetLoader
import com.arbr.model_loader.loader.NoisyRecordLoader
import com.arbr.model_suite.parameters.ParameterValueProviderImpl
import com.arbr.model_suite.predictive_models.document_diff_alignment.DocumentDiffAlignmentCache
import com.arbr.model_suite.predictive_models.document_diff_alignment.DocumentDiffAlignmentHelperImpl
import com.arbr.model_suite.predictive_models.document_diff_alignment.DocumentDiffAlignmentInducer
import com.arbr.platform.alignable.alignable.*
import com.arbr.platform.alignable.alignable.diff.AlignableDiffOperation
import com.arbr.ml.optimization.base.AsyncBoundaryEvaluator
import com.arbr.ml.optimization.base.NamedMetricKind
import com.arbr.ml.optimization.base.ParameterSetListener
import com.arbr.ml.optimization.base.ParameterValueProvider
import com.arbr.ml.optimization.grid.InitialPassingValueFinder
import com.arbr.ml.optimization.model.AsyncBoundaryEvaluation
import com.arbr.ml.optimization.model.BindingParameter
import org.apache.commons.text.similarity.LevenshteinDistance
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.math.exp


class AdjacencyMatrixDAGAlignmentHelperDiffTest {

    private fun linearOrderMatrix(n: Int): List<List<Boolean>> {
        return List(n) { i ->
            List(n) { j ->
                i < j
            }
        }
    }

    private val documentDiffAlignmentHelper =
       DocumentDiffAlignmentHelperImpl(
            ParameterValueProviderImpl(
                mutableMapOf(),
                defaultValue = 0.0,
            ) // TODO
        )
    private val parameterValueProvider = documentDiffAlignmentHelper.parameterValueProvider

    private val alignmentCache = DocumentDiffAlignmentCache()
    private val inducer = DocumentDiffAlignmentInducer(
        alignmentCache,
        documentDiffAlignmentHelper.parameterValueProvider,
    )

    fun parameterValueProviderWithOverrides(
        parameterMap: Map<NamedMetricKind, BindingParameter<Double>>
    ): ParameterValueProvider {
        val params = parameterValueProvider.getParameterMap().toMutableMap()
        params.putAll(parameterMap)
        return ParameterValueProviderImpl(
            params,
            defaultValue = 1.321,
        )
    }

    @Test
    fun `aligns diff`() {
        val result = documentDiffAlignmentHelper.alignBySectionAsync(
            diff0,
            document0,
        ).block()!!

        println(result.joinToString("\n") { it.toString() })
        println("\n==\n")
        println(DiffLiteralSourceDocumentSerializer().serialize(result.map { it.diffOperation }).text)
    }

    @Test
    fun `aligns sample diff with deletion`() {
        val sampleTarget = AdjacencyMatrixDAGValued<AlignableDiffOperation>(
            linearOrderMatrix(4),
            listOf(
                AlignableDiffOperation(DiffOperation(DiffOperationKind.NOP, "A", 0), parameterValueProvider),
                AlignableDiffOperation(DiffOperation(DiffOperationKind.NOP, "B", 1), parameterValueProvider),
                AlignableDiffOperation(DiffOperation(DiffOperationKind.DEL, "C", 2), parameterValueProvider),
                AlignableDiffOperation(DiffOperation(DiffOperationKind.NOP, "D", 3), parameterValueProvider),
            ),
        )
        val sampleSource = AdjacencyMatrixDAGValued<AlignableDiffOperation>(
            linearOrderMatrix(4),
            listOf(
                AlignableDiffOperation(DiffOperation(DiffOperationKind.NOP, "A", 0), parameterValueProvider),
                AlignableDiffOperation(DiffOperation(DiffOperationKind.NOP, "B", 1), parameterValueProvider),
                AlignableDiffOperation(DiffOperation(DiffOperationKind.NOP, "C", 2), parameterValueProvider),
                AlignableDiffOperation(DiffOperation(DiffOperationKind.NOP, "D", 3), parameterValueProvider),
            ),
        )

        val (path, cost) = AdjacencyMatrixDAGAlignmentHelper(
            sampleTarget,
            sampleSource,
            inducer,
            alignmentCache,
            parameterValueProvider,
        ).alignmentPath()!!

        val docSerialized =
            DiffLiteralSourceDocumentSerializer().serialize(LinearOrderList(path.last().constructionState.toList()).map { it.diffOperation })

        val lines = docSerialized.text.trim().split("\n")
        Assertions.assertEquals(3, lines.size)
        Assertions.assertEquals("A", lines[0])
        Assertions.assertEquals("B", lines[1])
        Assertions.assertEquals("D", lines[2])
    }

    @Test
    fun `aligns sample diff with addition`() {
        val adoConfig = parameterValueProvider
//            .copy(stringEditWeight = 99.9) // Avoid edit of line text

        val sampleTarget = AdjacencyMatrixDAGValued<AlignableDiffOperation>(
            linearOrderMatrix(5),
            listOf(
                AlignableDiffOperation(DiffOperation(DiffOperationKind.NOP, "A", 0), parameterValueProvider),
                AlignableDiffOperation(DiffOperation(DiffOperationKind.NOP, "B", 1), parameterValueProvider),
                AlignableDiffOperation(DiffOperation(DiffOperationKind.NOP, "C", 2), parameterValueProvider),
                AlignableDiffOperation(DiffOperation(DiffOperationKind.ADD, "E", 3), parameterValueProvider),
                AlignableDiffOperation(DiffOperation(DiffOperationKind.NOP, "D", 4), parameterValueProvider),
            ),
        )
        val sampleSource = AdjacencyMatrixDAGValued<AlignableDiffOperation>(
            linearOrderMatrix(4),
            listOf(
                AlignableDiffOperation(DiffOperation(DiffOperationKind.NOP, "A", 0), parameterValueProvider),
                AlignableDiffOperation(DiffOperation(DiffOperationKind.NOP, "B", 1), parameterValueProvider),
                AlignableDiffOperation(DiffOperation(DiffOperationKind.NOP, "C", 2), parameterValueProvider),
                AlignableDiffOperation(DiffOperation(DiffOperationKind.NOP, "D", 3), parameterValueProvider),
            ),
        )

        val (path, cost) = AdjacencyMatrixDAGAlignmentHelper(
            sampleTarget,
            sampleSource,
            inducer,
            alignmentCache,
            parameterValueProvider,
        ).alignmentPath()!!

        val docSerialized =
            DiffLiteralSourceDocumentSerializer().serialize(LinearOrderList(path.last().constructionState.toList()).map { it.diffOperation })
        println(path.last().constructionState.toList().joinToString("\n") { it.toString() })

        val lines = docSerialized.text.trim().split("\n")
        println(lines)

        Assertions.assertEquals(5, lines.size)
        Assertions.assertEquals("A", lines[0])
        Assertions.assertEquals("B", lines[1])
        Assertions.assertEquals("C", lines[2])
        Assertions.assertEquals("E", lines[3])
        Assertions.assertEquals("D", lines[4])
    }

    @Test
    fun `aligns hardcoded 5`() {
        val loader = HardcodedDiffDatasetLoader(
            Mono.error(NotImplementedError())
        )
        val dataset = loader.loadDataset(8, 0, 1.0, 23147634)
        val testCase = dataset.trainingData.collectList().block()!!.first { it.name.contains("5") }

        val ivpf = InitialPassingValueFinder(0.9, valuesPerParameter = 4, 250000000)

        val eval = AsyncBoundaryEvaluator { parameterMaps, targetParameterKind ->
            Flux.fromIterable(parameterMaps).flatMap { parameterMap ->

                val helper =DocumentDiffAlignmentHelperImpl(ParameterValueProviderImpl(
                    parameterMap,
                    defaultValue = 1.321,
                ))

                helper.alignBySectionAsync(
//                    DiffLiteralPatch(testCase.baseDocument.text.split("\n").joinToString("\n") { " $it" }),
                    testCase.patch,
                    testCase.baseDocument,
                ).flatMap { result ->
//                println(result.joinToString("\n") { it.toString() })
//                println("\n==\n")
                    val resultText = DiffLiteralSourceDocumentSerializer().serialize(result.map { it.diffOperation }).text
                    val resultDist = LevenshteinDistance().apply(resultText, testCase.expectedResult.text)
                    println(resultDist)

//                Assertions.assertTrue(resultDist <= 1)

                    Mono.just(
                        0 to AsyncBoundaryEvaluation(
                            true,
                            exp(-resultDist * 0.01),
                            emptyList(),
                        )
                    )
                }

            }
        }

        ivpf.optimizeBoundariesAsync(
            "",
            documentDiffAlignmentHelper.parameterValueProvider.getParameterMap(),
            emptyList(),
            eval,
            eval,
            ParameterSetListener {
                println(it)
                Mono.empty()
            },
            0.01,
            true,
        ).block()!!
    }

    @Test
    fun `aligns single clean test case multi section`() {
        val testCaseId = "c-63760777-1224909137-d994-a3b3"
        val loader = CleanDiffDatasetLoader(
            NoisyRecordLoader(
                Mono.error(NotImplementedError())
            )
        ).loadDataset(50, 0, 1.0, 123)
        val testCase = loader.trainingData.collectList().block()!!
            .first {
                it.name == testCaseId
            }

        val result = documentDiffAlignmentHelper.alignBySectionAsync(
            testCase.patch,
            testCase.baseDocument,
        ).block()!!

        val resultText = DiffLiteralSourceDocumentSerializer().serialize(result.map { it.diffOperation }).text
        val resultDist = LevenshteinDistance().apply(resultText, testCase.expectedResult.text)

        Assertions.assertTrue(resultDist <= 1)

//        println(result.joinToString("\n") { it.toString() })
        println(resultText)
        println("\n==\n")

        println(testCase.name)
        println(resultDist)
    }

    @Test
    fun `aligns single clean test case`() {
        val testCaseId = "c-13527761-965003857-9cfc-0632"
        val loader = CleanDiffDatasetLoader(
            NoisyRecordLoader(
                Mono.error(NotImplementedError())
            )
        ).loadDataset(50, 0, 1.0, 123)
        val testCase = loader.trainingData.collectList().block()!!
            .first {
                it.name == testCaseId
            }

        val paramProvider = parameterValueProviderWithOverrides(
            mapOf(
//                SKIP_PRE to BindingParameter(SKIP_PRE, 0.0),
//                SKIP_POST to BindingParameter(SKIP_POST, 0.0),
//                SKIP to BindingParameter(SKIP_POST, 1.0),
                STR_WEIGHT to BindingParameter(STR_WEIGHT, 0.00),

                TKW_SKIP_PRE to BindingParameter(TKW_SKIP_PRE, 0.001),
                TKW_SKIP to BindingParameter(TKW_SKIP, 1.0),
                TKW_SKIP_POST to BindingParameter(TKW_SKIP_POST, 0.001),

                OP_MATRIX_NOP_NOP to BindingParameter(OP_MATRIX_NOP_NOP, 0.0),
            )
        )

        // Error was a matter of weight
        val helper =DocumentDiffAlignmentHelperImpl(
            paramProvider,
//            documentDiffAlignmentHelper.config.copy(
//                preSkipCost = 0.0,
//                postSkipCost = 0.0,
//            ),
//            documentDiffAlignmentHelper.generalDagAlignmentConfig,
//            documentDiffAlignmentHelper.operationConfig,
        )

        val result = helper.alignBySectionAsync(
            testCase.patch,
            testCase.baseDocument,
        ).block()!!

        val resultText = DiffLiteralSourceDocumentSerializer().serialize(result.map { it.diffOperation }).text
        val resultDist = LevenshteinDistance().apply(resultText, testCase.expectedResult.text)

//        println(result.joinToString("\n") { it.toString() })
        println(resultText)
        println("\n==\n")

        println(testCase.name)
        println(resultDist)

        Assertions.assertTrue(resultDist <= 1)
    }

    @Test
    fun `aligns single clean test case 2`() {
        val testCaseId = "c-35658863-367282292-8b60-a420"
        val loader = CleanDiffDatasetLoader(
            NoisyRecordLoader(
                Mono.error(NotImplementedError())
            )
        ).loadDataset(50, 0, 1.0, 123)
        val testCase = loader.trainingData.collectList().block()!!
            .first {
                it.name == testCaseId
            }

        val helper =DocumentDiffAlignmentHelperImpl(
            parameterValueProvider,
//            documentDiffAlignmentHelper.config.copy(
//                preSkipCost = 0.0,
//                postSkipCost = 0.0,
//            ),
//            documentDiffAlignmentHelper.generalDagAlignmentConfig,
//            documentDiffAlignmentHelper.operationConfig,
        )

        val result = helper.alignBySectionAsync(
            testCase.patch,
            testCase.baseDocument,
        ).block()!!

        val resultText = DiffLiteralSourceDocumentSerializer().serialize(result.map { it.diffOperation }).text
        val resultDist = LevenshteinDistance().apply(resultText, testCase.expectedResult.text)

        Assertions.assertTrue(resultDist <= 1)

//        println(result.joinToString("\n") { it.toString() })
        println(resultText)
        println("\n==\n")

        println(testCase.name)
        println(resultDist)
    }

    @Test
    fun `aligns single hardcoded test case`() {
        val loader = HardcodedDiffDatasetLoader(
            Mono.error(NotImplementedError()),
        ).loadDataset(8, 0, 1.0, 123)
        val testCase = loader.trainingData.collectList().block()!!
            .first {
                it.name.contains("hardcoded_6_extra_leading_space")
            }

        val helper =DocumentDiffAlignmentHelperImpl(
            parameterValueProvider,
//            documentDiffAlignmentHelper.config.copy(
//                preSkipCost = 0.0,
//                postSkipCost = 0.0,
//            ),
//            documentDiffAlignmentHelper.generalDagAlignmentConfig,
//            documentDiffAlignmentHelper.operationConfig,
        )

        val result = helper.alignBySectionAsync(
            testCase.patch,
            testCase.baseDocument,
        ).block()!!

        val resultText = DiffLiteralSourceDocumentSerializer().serialize(result.map { it.diffOperation }).text
        val resultDist = LevenshteinDistance().apply(resultText, testCase.expectedResult.text)

        Assertions.assertTrue(resultDist <= 1)

//        println(result.joinToString("\n") { it.toString() })
        println(resultText)
        println("\n==\n")

        println(testCase.name)
        println(resultDist)
    }

    @Test
    fun `aligns clean test cases`() {
        val loader = CleanDiffDatasetLoader(
            NoisyRecordLoader(
                Mono.error(NotImplementedError())
            )
        )
            .loadDataset(50, 0, 1.0, 123)

        // Error was a matter of weight
        val helper =DocumentDiffAlignmentHelperImpl(
            parameterValueProvider,
//            documentDiffAlignmentHelper.config.copy(
//                preSkipCost = 0.0,
//                postSkipCost = 0.0,
//            ),
//            documentDiffAlignmentHelper.generalDagAlignmentConfig,
//            documentDiffAlignmentHelper.operationConfig,
        )

        for (testCase in loader.trainingData.collectList().block()!!) {
            val result = documentDiffAlignmentHelper.alignBySectionAsync(
                testCase.patch,
                testCase.baseDocument,
            ).block()!!

//            println(result.joinToString("\n") { it.toString() })
//            println("\n==\n")
            val resultText = DiffLiteralSourceDocumentSerializer().serialize(result.map { it.diffOperation }).text
            val resultDist = LevenshteinDistance().apply(resultText, testCase.expectedResult.text)

            println(testCase.name)
            println(resultDist)
        }
//        val testCase = loader.trainingData.first {
//            it.name == testCaseId
//        }


    }

}

private val document0 = DiffLiteralSourceDocument(
    """
import React, { useEffect, useState } from 'react';
import fs from 'fs';
import path from 'path';
import matter from 'gray-matter';
import Link from 'next/link';

const Careers = () => {
  const [jobListings, setJobListings] = useState([]);

  useEffect(() => {
    const jobFiles = fs.readdirSync(path.join(process.cwd(), 'careers')).filter(file => file.endsWith('.md'));
    const jobs = jobFiles.map((file) => {
      const markdownWithMetadata = fs.readFileSync(path.join(process.cwd(), 'careers', file)).toString();
      const { data } = matter(markdownWithMetadata);
      return data;
    });
    setJobListings(jobs);
  }, []);

  return (
    <div>
      <h1>Careers</h1>
      {jobListings.map((job, index) => (
        <div key={index}>
          <h2>{job.title}</h2>
          <p>{job.location}</p>
          <Link href={`/careers/$\{job.slug}`}>
            <a>Read More</a>
          </Link>
        </div>
      ))}
    </div>
  );
};

export default Careers;
        """.trimIndent()
)

private val diff0 = DiffLiteralPatch(
    """
+ import { Card, CardFooter, Image, Button, Divider } from '@nextui-org/react';
+ import linkedinLogo from '../assets/about/linkedinLogo.png';
+ import githubLogo from '../assets/about/githubLogo.png';

  import React, { useEffect, useState } from 'react';
  import fs from 'fs';
  import path from 'path';
  import matter from 'gray-matter';
  import Link from 'next/link';

  const Careers = () => {
    const [jobListings, setJobListings] = useState([]);

    useEffect(() => {
      const jobFiles = fs.readdirSync(path.join(process.cwd(), 'careers')).filter(file => file.endsWith('.md'));
      const jobs = jobFiles.map((file) => {
        const markdownWithMetadata = fs.readFileSync(path.join(process.cwd(), 'careers', file)).toString();
        const { data } = matter(markdownWithMetadata);
        return data;
      });
      setJobListings(jobs);
    }, []);

    return (
      <div>
        <h1>Careers</h1>
-       {jobListings.map((job, index) => (
-         <div key={index}>
-           <h2>{job.title}</h2>
-           <p>{job.location}</p>
-           <Link href={`/careers/$\{job.slug}`}>
-             <a>Read More</a>
-           </Link>
-         </div>
-       ))}
+       {jobListings.map((job, index) => {
+         return (
+           <Card key={index} shadow>
+             <h2>{job.title}</h2>
+             <p>{job.location}</p>
+             <Divider />
+             <CardFooter>
+               <Link href={`/careers/$\{job.slug}`}>
+                 <Button auto color="primary" ghost>Read More</Button>
+               </Link>
+             </CardFooter>
+           </Card>
+         );
+       })}
      </div>
    );
  };

  export default Careers;
        """.trimIndent()
)
