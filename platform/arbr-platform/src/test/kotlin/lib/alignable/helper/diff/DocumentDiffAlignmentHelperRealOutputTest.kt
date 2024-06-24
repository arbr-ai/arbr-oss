package com.arbr.alignable.helper.diff

//import com.arbr.platform.alignable.alignable.diff.AlignableDiffOperationAlignmentConfig
//import com.arbr.data_structures_common.partial_order.PartialOrderAlignmentConfiguration
//import org.junit.jupiter.api.Assertions
//import org.junit.jupiter.api.Test
//import java.util.*
//import kotlin.math.abs
//import kotlin.math.min
//import kotlin.math.pow
//
//class DocumentDiffAlignmentHelperRealOutputTest {
//
//    private val helper = DocumentDiffAlignmentHelper.load()
//
//    private val document0 = DiffLiteralSourceDocument(
//        """
//import React, { useEffect, useState } from 'react';
//import fs from 'fs';
//import path from 'path';
//import matter from 'gray-matter';
//import Link from 'next/link';
//
//const Careers = () => {
//  const [jobListings, setJobListings] = useState([]);
//
//  useEffect(() => {
//    const jobFiles = fs.readdirSync(path.join(process.cwd(), 'careers')).filter(file => file.endsWith('.md'));
//    const jobs = jobFiles.map((file) => {
//      const markdownWithMetadata = fs.readFileSync(path.join(process.cwd(), 'careers', file)).toString();
//      const { data } = matter(markdownWithMetadata);
//      return data;
//    });
//    setJobListings(jobs);
//  }, []);
//
//  return (
//    <div>
//      <h1>Careers</h1>
//      {jobListings.map((job, index) => (
//        <div key={index}>
//          <h2>{job.title}</h2>
//          <p>{job.location}</p>
//          <Link href={`/careers/$\{job.slug}`}>
//            <a>Read More</a>
//          </Link>
//        </div>
//      ))}
//    </div>
//  );
//};
//
//export default Careers;
//        """.trimIndent()
//    )
//
//    private val diff0 = DiffLiteralPatch(
//        """
//+ import { Card, CardFooter, Image, Button, Divider } from '@nextui-org/react';
//+ import linkedinLogo from '../assets/about/linkedinLogo.png';
//+ import githubLogo from '../assets/about/githubLogo.png';
//
//  import React, { useEffect, useState } from 'react';
//  import fs from 'fs';
//  import path from 'path';
//  import matter from 'gray-matter';
//  import Link from 'next/link';
//
//  const Careers = () => {
//    const [jobListings, setJobListings] = useState([]);
//
//    useEffect(() => {
//      const jobFiles = fs.readdirSync(path.join(process.cwd(), 'careers')).filter(file => file.endsWith('.md'));
//      const jobs = jobFiles.map((file) => {
//        const markdownWithMetadata = fs.readFileSync(path.join(process.cwd(), 'careers', file)).toString();
//        const { data } = matter(markdownWithMetadata);
//        return data;
//      });
//      setJobListings(jobs);
//    }, []);
//
//    return (
//      <div>
//        <h1>Careers</h1>
//-       {jobListings.map((job, index) => (
//-         <div key={index}>
//-           <h2>{job.title}</h2>
//-           <p>{job.location}</p>
//-           <Link href={`/careers/$\{job.slug}`}>
//-             <a>Read More</a>
//-           </Link>
//-         </div>
//-       ))}
//+       {jobListings.map((job, index) => {
//+         return (
//+           <Card key={index} shadow>
//+             <h2>{job.title}</h2>
//+             <p>{job.location}</p>
//+             <Divider />
//+             <CardFooter>
//+               <Link href={`/careers/$\{job.slug}`}>
//+                 <Button auto color="primary" ghost>Read More</Button>
//+               </Link>
//+             </CardFooter>
//+           </Card>
//+         );
//+       })}
//      </div>
//    );
//  };
//
//  export default Careers;
//        """.trimIndent()
//    )
//
//    @Test
//    fun `aligns diff`() {
//        val alignedPatch = helper.alignBySectionAsync(
//            diff0,
//            document0,
//        ).block()!!
//
//        println(alignedPatch.joinToString("\n") { it.toString() })
//    }
//
//    private val document1 = DiffLiteralSourceDocument(
//        """
//#root {
//  max-width: 1280px;
//  margin: 0 auto;
//  padding: 2rem;
//  text-align: center;
//}
//
//.logo {
//  height: 6em;
//  padding: 1.5em;
//  will-change: filter;
//  transition: filter 300ms;
//}
//.logo:hover {
//  filter: drop-shadow(0 0 2em #646cffaa);
//}
//.logo.react:hover {
//  filter: drop-shadow(0 0 2em #61dafbaa);
//}
//
//@keyframes logo-spin {
//  from {
//    transform: rotate(0deg);
//  }
//  to {
//    transform: rotate(360deg);
//  }
//}
//
//@media (prefers-reduced-motion: no-preference) {
//  a:nth-of-type(2) .logo {
//    animation: logo-spin infinite 20s linear;
//  }
//}
//
//.card {
//  padding: 2em;
//}
//
//.read-the-docs {
//  color: #888;
//}
//        """.trimIndent()
//    )
//
//    private val diff1 = DiffLiteralPatch(
//        """
//#root {
//  max-width: 1280px;
//  margin: 0 auto;
//  padding: 2rem;
//  text-align: center;
//+  background-color: #f0f0f0;
//}
//
//.logo {
//  height: 6em;
//  padding: 1.5em;
//  will-change: filter;
//  transition: filter 300ms;
//+  border-radius: 50%;
//}
//.logo:hover {
//  filter: drop-shadow(0 0 2em #646cffaa);
//}
//.logo.react:hover {
//  filter: drop-shadow(0 0 2em #61dafbaa);
//}
//
//@keyframes logo-spin {
//  from {
//    transform: rotate(0deg);
//  }
//  to {
//    transform: rotate(360deg);
//  }
//}
//
//@media (prefers-reduced-motion: no-preference) {
//  a:nth-of-type(2) .logo {
//    animation: logo-spin infinite 20s linear;
//  }
//}
//
//.card {
//  padding: 2em;
//+  background-color: #fff;
//+  border-radius: 10px;
//+  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.15);
//}
//
//.read-the-docs {
//  color: #888;
//+  font-size: 0.8em;
//}
//        """.trimIndent()
//    )
//
//    @Test
//    fun `aligns diff 1`() {
//        val partialOrderAlignmentConfigurationConfig = PartialOrderAlignmentConfiguration(
//            dropCost = 0.749250,
//            matchCost = 0.1,
//            editCost = 0.818852,
//            applyCost = 0.5,
//            induceCost = 2.5,
//            deduceCost = 2.5,
//            skipCost = 0.25,
//            preSkipCost = 0.0,
//            postSkipCost = 0.0,
//            singleOperationCostLimit = null,
//            operationsLimitDocumentProportion = 2.5,
//            operationsLimitPatchProportion = 2.5,
//            operationsLimitProductProportion = 2.5,
//            numTrailingOperationsRequired = 3,
//            enableMemoization = true,
//            logLevel = null,
//        )
//
//        val alignableDiffOperationAlignmentConfig = AlignableDiffOperationAlignmentConfig(
//            listOf(
//                listOf(2.5, 2.5, 2.5, 2.5),
//                listOf(2.39, 2.5, 2.34, 2.5),
//                listOf(2.37, 2.5, 2.39, 2.34),
//                listOf(2.5, 2.5, 2.5, 2.5),
//            ),
//            100.0,
//            100.0,
//            100.0,
//        )
//        val helper =DocumentDiffAlignmentHelperImpl(
//            partialOrderAlignmentConfigurationConfig,
//            alignableDiffOperationAlignmentConfig,
//        )
//
//        val alignedPatch = helper.alignBySectionAsync(
//            diff1,
//            document1,
//        ).block()!!
//
//        println(alignedPatch.joinToString("\n") { it.toString() })
//    }
//
//    private val document2 = DiffLiteralSourceDocument(
//        """
//#root {
//  max-width: 1280px;
//  margin: 0 auto;
//  padding: 2rem;
//  text-align: center;
//}
//
//.logo {
//  height: 6em;
//  padding: 1.5em;
//  will-change: filter;
//  transition: filter 300ms;
//}
//.logo:hover {
//  filter: drop-shadow(0 0 2em #646cffaa);
//}
//.logo.react:hover {
//  filter: drop-shadow(0 0 2em #61dafbaa);
//}
//
//@keyframes logo-spin {
//  from {
//    transform: rotate(0deg);
//  }
//  to {
//    transform: rotate(360deg);
//  }
//}
//
//@media (prefers-reduced-motion: no-preference) {
//  a:nth-of-type(2) .logo {
//    animation: logo-spin infinite 20s linear;
//  }
//}
//
//.card {
//  padding: 2em;
//}
//
//.read-the-docs {
//  color: #888;
//}
//        """.trimIndent()
//    )
//
//    private val diff2 = DiffLiteralPatch(
//        """
// #root {
//   max-width: 1280px;
//   margin: 0 auto;
//   padding: 2rem;
//   text-align: center;
//+  background-color: #f0f0f0;
// }
//
// .logo {
//   height: 6em;
//   padding: 1.5em;
//   will-change: filter;
//   transition: filter 300ms;
//+  border-radius: 50%;
// }
// .logo:hover {
//   filter: drop-shadow(0 0 2em #646cffaa);
// }
// .logo.react:hover {
//   filter: drop-shadow(0 0 2em #61dafbaa);
// }
//
// @keyframes logo-spin {
//   from {
//     transform: rotate(0deg);
//   }
//   to {
//     transform: rotate(360deg);
//   }
// }
//
// @media (prefers-reduced-motion: no-preference) {
//   a:nth-of-type(2) .logo {
//     animation: logo-spin infinite 20s linear;
//   }
// }
//
// .card {
//   padding: 2em;
//+  background-color: #fff;
//+  border-radius: 10px;
//+  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.15);
// }
//
// .read-the-docs {
//   color: #888;
//+  font-size: 0.8em;
// }
//        """.trimIndent()
//    )
//
//    @Test
//    fun `aligns diff 2`() {
//        val partialOrderAlignmentConfigurationConfig = PartialOrderAlignmentConfiguration(
//            dropCost = 0.749250,
//            matchCost = 0.1,
//            editCost = 0.818852,
//            applyCost = 0.5,
//            induceCost = 2.5,
//            deduceCost = 2.5,
//            skipCost = 0.25,
//            preSkipCost = 0.0,
//            postSkipCost = 0.0,
//            singleOperationCostLimit = null,
//            operationsLimitDocumentProportion = 2.5,
//            operationsLimitPatchProportion = 2.5,
//            operationsLimitProductProportion = 2.5,
//            numTrailingOperationsRequired = 3,
//            enableMemoization = true,
//            logLevel = null,
//        )
//
//        val alignableDiffOperationAlignmentConfig = AlignableDiffOperationAlignmentConfig(
//            listOf(
//                listOf(2.5, 2.5, 2.5, 2.5),
//                listOf(2.39, 2.5, 2.34, 2.5),
//                listOf(2.37, 2.5, 2.39, 2.34),
//                listOf(2.5, 2.5, 2.5, 2.5),
//            ),
//            100.0,
//            100.0,
//            100.0,
//        )
//        val helper =DocumentDiffAlignmentHelperImpl(
//            partialOrderAlignmentConfigurationConfig,
//            alignableDiffOperationAlignmentConfig,
//        )
//
//        val alignedPatch = helper.alignBySectionAsync(
//            diff2,
//            document2,
//        ).block()!!
//
//        val result = DiffLiteralSourceDocumentSerializer().serialize(alignedPatch)
//        println(result.text)
//    }
//
//    private val diff3 = """
// #root {
//   max-width: 1280px;
//   margin: 0 auto;
//   padding: 2rem;
//   text-align: center;
// }
//
// .logo {
//   height: 6em;
//   padding: 1.5em;
//   will-change: filter;
//   transition: filter 300ms;
// }
// .logo:hover {
//   filter: drop-shadow(0 0 2em #646cffaa);
// }
// .logo.react:hover {
//   filter: drop-shadow(0 0 2em #61dafbaa);
// }
//
// @keyframes logo-spin {
//   from {
//     transform: rotate(0deg);
//   }
//   to {
//     transform: rotate(360deg);
//   }
// }
//
// @media (prefers-reduced-motion: no-preference) {
//   a:nth-of-type(2) .logo {
//     animation: logo-spin infinite 20s linear;
//   }
// }
//
// .card {
//   padding: 2em;
// }
//
// .read-the-docs {
//   color: #888;
// }
//+
//+ .home-page {
//+   background-color: #f0f0f0;
//+   color: #333;
//+   font-size: 1.2em;
//+   padding: 1em;
//+ }
//    """.trimIndent()
//
//    @Test
//    fun `aligns diff 3`() {
//        val partialOrderAlignmentConfigurationConfig = PartialOrderAlignmentConfiguration(
//            dropCost = 50.5,
//            matchCost = 0.1,
//            editCost = 50.4,
//            applyCost = 50.250,
//            induceCost = 2.5,
//            deduceCost = 2.5,
//            skipCost = 0.25,
//            preSkipCost = 0.0,
//            postSkipCost = 0.0,
//            singleOperationCostLimit = null,
//            operationsLimitDocumentProportion = 2.5,
//            operationsLimitPatchProportion = 2.5,
//            operationsLimitProductProportion = 2.5,
//            numTrailingOperationsRequired = 3,
//            enableMemoization = true,
//            logLevel = null,
//        )
//
//        val alignableDiffOperationAlignmentConfig = AlignableDiffOperationAlignmentConfig(
//            listOf(
//                listOf(2.5, 2.5, 2.5, 2.5),
//                listOf(5.449, 2.5, 2.5, 2.5),
//                listOf(51.200, 2.5, 51.200, 51.200),
//                listOf(51.168, 2.5, 2.5, 2.5),
//            ),
//            50.75,
//            26.125,
//            26.125,
//        )
//        val helper =DocumentDiffAlignmentHelperImpl(
//            partialOrderAlignmentConfigurationConfig,
//            alignableDiffOperationAlignmentConfig,
//        )
//
//        val alignedPatch = helper.alignBySectionAsync(
//            DiffLiteralPatch(diff3),
//            document2,
//        ).block()!!
//
//        val result = DiffLiteralSourceDocumentSerializer().serialize(alignedPatch)
//        Assertions.assertTrue(result.text.isNotBlank())
//        println(result.text)
//    }
//
//    private fun makeIndentDoc(t: Int): String {
//        val random = Random(123132L)
//
//        val doc = StringBuilder().run {
//            var ll = 0
//            val numLines = 50
//            val net = List(numLines) { 0 }.toMutableList()
//            for (i in 0 until numLines) {
//                val post = i + (random.nextDouble().pow(3) * (numLines - i)).toInt()
//                net[i]++
//                net[post]--
//            }
//            val cumulative = List(numLines) { 0 }.toMutableList()
//            var cumulativeSum = 0
//            for (i in 0 until numLines) {
//                cumulative[i] = cumulativeSum
//                cumulativeSum += net[i]
//            }
//
//            val indentUnit = " ".repeat(t)
//
//            for (i in 0 until numLines) {
//                ll = kotlin.math.max(0, cumulative[i])
//                val indent = indentUnit.repeat(ll)
//                val line = indent + "L"
//                append(line + "\n")
//            }
//
//            toString()
//        }
//
//        val docWithNoise = doc.lines().joinToString("\n") {
//            val r = random.nextDouble()
//            val net = if (r < 0.3) 1 else if (r > 0.7) -1 else 0
//
//            if (net == 1) {
//                " $it"
//            } else if (net == -1 && it.startsWith(" ")) {
//                it.drop(1)
//            } else {
//                it
//            }
//        }
//
//        return docWithNoise
//    }
//
//    @Test
//    fun `normalizes whitespace`() {
//        // T(t)
//        val tabDist = listOf(
//            1 to 0.0,
//            2 to 0.2,
//            3 to 0.1,
//            4 to 0.6,
//            5 to 0.05,
//            6 to 0.05,
//        ).toMap()
//
//        // X(x)
//        val rawIndentDist = listOf(
//            0 to 0.25,
//            1 to 0.1,
//            2 to 0.25,
//            3 to 0.1,
//            4 to 0.25,
//            5 to 0.05,
//        ).toMap()
//
//        for (t in 1..6) {
//
//        }
//
//        for (k in 0 until 6) {
//            for (t in 1..6) {
//                // P[T=t | X=k] = P[X=k | T=t] P[T=t] / P[X=k]
//
//            }
//        }
//    }
//
//    @Test
//    fun `minimizes marginal entropy`() {
//        val dist = mutableListOf(
//            0.1,
//            0.2,
//            0.3,
//            0.4,
//        )
//        val priorNumTrials = 50
//
//        val numObs = dist.map { p ->
//            (priorNumTrials * p).toInt()
//        }.toMutableList()
//
//        val lr0 = 0.8
//        val lrExpBase = (2.0).pow(1.0 / 100)
//        val m = dist.size
//        var roundNumber = 0
//
//        fun update(obs: Int) {
//            val lr = lr0 * lrExpBase.pow(-roundNumber)
//            println("lr=$lr")
//            roundNumber++
//
//            val maxDeltaLo = dist.filterIndexed { index, d -> index != obs }
//                .min() * (m - 1)
//            val maxDeltaHi = 1 - dist[obs]
//            val maxDelta = min(maxDeltaLo, maxDeltaHi)
//
//            fun infErr(delta: Double): Double {
//                var sum = 0.0
//                for (i in dist.indices) {
//                    if (i == obs) {
//                        continue
//                    }
//
//                    val obsI = numObs[i]
//                    val pI = dist[i]
//                    sum += obsI / ((m - 1) * pI - delta)
//                }
//
//                val constt = (numObs[obs] + 1) / ((1 - lr) * (dist[obs] + delta))
//
//                return abs(constt - sum)
//            }
//
//            val resolution = 100
//            val step = maxDelta / resolution
//            var ddelta = 1E-8
//            for (r in 1..5) {
//                ddelta = (0 until resolution).map { tt ->
//                    ddelta + tt * step.pow(r)
//                }.minBy { d ->
//                    infErr(d)
////                        .also { println("$d, $it") }
//                }
//            }
//
//            println("$ddelta, ${infErr(ddelta)}")
//
//            for (i in dist.indices) {
//                dist[i] = if (i == obs) {
//                    dist[i] + ddelta
//                } else {
//                    dist[i] - ddelta / (m - 1)
//                }
//            }
//            numObs[obs]++
//
//            println(dist)
//            println()
//        }
//
//        val random = Random(214894L)
//        // Want to converge to this dist
//        val sampleDist = listOf(
//            0.08,
//            0.29,
//            0.2,
//            0.43,
//        )
//        repeat(10000) {
//            val r = random.nextDouble()
//            var i = 0
//            var cumulative = 0.0
//            while (r > cumulative + sampleDist[i]) {
//                cumulative += sampleDist[i]
//                i++
//            }
//
//            println("Obs $i")
//            update(i)
//        }
//    }
//}