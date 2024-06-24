package com.arbr.alignable.helper.diff

//import com.fasterxml.jackson.annotation.JsonIgnoreProperties
//import com.fasterxml.jackson.databind.PropertyNamingStrategies
//import com.fasterxml.jackson.databind.annotation.JsonNaming
//import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
//import com.arbr.platform.alignable.alignable.diff.DiffOperationKind
//import com.arbr.data_structures_common.partial_order.PartialOrderAlignmentKind
//import com.arbr.platform.alignable.alignable.struct.Struct3
//import com.arbr.alignable.util.GitUtils
//import com.arbr.model_trainer.model.GitHub
//import org.apache.commons.io.IOUtils
//import org.junit.jupiter.api.Test
//import java.io.BufferedReader
//import java.io.File
//import java.io.InputStreamReader
//import java.nio.charset.StandardCharsets
//import java.nio.file.Files
//import java.nio.file.Path
//import java.nio.file.Paths
//import java.time.Duration
//import java.time.Instant
//import java.util.zip.ZipFile
//import kotlin.math.absoluteValue
//import kotlin.random.Random
//import kotlin.test.assertEquals
//
//class DocumentDiffAlignmentHelperTest {
//
//    companion object {
//        private val helper = DocumentDiffAlignmentHelper.load()
//        private val config = helper.config
//        private val operationAlignmentConfig = helper.operationConfig
//    }
//
//    private val document0 = DiffLiteralSourceDocument(
//        """
//            Hello world
//            print("hi world")
//            return world
//        """.trimIndent()
//    )
//
//    private val diff0 = DiffLiteralPatch(
//        """
//              Hello world
//            - print("hi world")
//            + print("welcome to heck")
//              return world
//        """.trimIndent()
//    )
//
//    private val expected0 = listOf(
//        PartialOrderAlignmentKind.MATCH to (DiffOperationKind.NOP to "Hello world"),
//        PartialOrderAlignmentKind.EDIT to (DiffOperationKind.DEL to "print(\"hi world\")"),
//        PartialOrderAlignmentKind.APPLY to (DiffOperationKind.ADD to "print(\"welcome to heck\")"),
//        PartialOrderAlignmentKind.MATCH to (DiffOperationKind.NOP to "return world"),
//    )
//
//    private val jsDocument = File("src/test/resources/content/js_document_0.js").readText()
//
////    @Test
////    fun `aligns diff`() {
////        val alignedPatch = helper.alignAndRecomputePatch(
////            diff0,
////            document0,
////        )!!
////
////        println(alignedPatch.joinToString("\n") { it.toString() })
////    }
//
//    private val testDataDir = File("src/test/resources/test_data/")
//
//    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
//    @JsonIgnoreProperties(ignoreUnknown = true)
//    internal data class FileRepoRecordPairWithPatch(
//        val pullRequestId: Long,
//        val commitSha: String,
//        val fileSha: String,
//        val fileContent: String,
//        val repoRecord: GitHub.GitHubRepoRecord
//    )
//
//    private fun filesInZip(zipFilePath: Path): Map<String, String> {
//        val zipFile = ZipFile(zipFilePath.toFile())
//
//        val interiorPathToContent = mutableMapOf<String, String>()
//        for (zipEntry in zipFile.entries()) {
//            val content = if (zipEntry.isDirectory) {
//                // println(zipEntry.name)
//                continue
//            } else {
//                val zipInputStream = zipFile.getInputStream(zipEntry)
//                val content = IOUtils.toString(zipInputStream, StandardCharsets.UTF_8)
//                // println("  " + zipEntry.name + " " + content.length.toString())
//                content
//            }
//
//            // Drop first path token
//            val interiorPath = zipEntry.name.split("/").drop(1).joinToString("/")
//
//            interiorPathToContent[interiorPath] = content
//        }
//
//        return interiorPathToContent
//    }
//
//    private fun loadFiles(maxN: Int) {
//        val mapper = jacksonObjectMapper()
//        val filePathStream = Files.list(Paths.get("/Volumes/ssd/datasets/github_public/"))
//        val numFilesToConsider = 100
//        val extensions = listOf("js", "jsx", "html", "css")
//
//        val filePaths = mutableListOf<Path>()
//        var i = 0
//        for (filePath in filePathStream) {
//            filePaths.add(filePath)
//            i += 1
//            if (i >= numFilesToConsider) {
//                break
//            }
//        }
//        filePathStream.close()
//
//        val nowMs = Instant.now().toEpochMilli()
//        val fileWithModified = filePaths.map {
//            val file = it.toFile()
//            val secondsSinceModified = Duration.ofMillis(nowMs - file.lastModified()).toSeconds()
//            // println("${file.name} $secondsSinceModified")
//            secondsSinceModified to file
//        }
//
//        val numFilesToUse = 25
//        val filesToUse = fileWithModified
//            .sortedBy { it.first }
//            .map { it.second }
//
//
//        val repoRecordsAndFiles = mutableListOf<Struct3<GitHub.GitHubRepoRecord, String, String>>()
//        var j = 0
//        for (file in filesToUse) {
//            var found = false
//            val repoRecord = mapper.readValue(file, GitHub.GitHubRepoRecord::class.java)
//
//            for (pullRequest in repoRecord.pullRequests) {
//                for (commit in pullRequest.commits) {
//                    val sha = commit.commit.sha
//                    if (commit.commit.files.any { f ->
//                            extensions.any {
//                                f.status == "modified" && f.filename.lowercase().endsWith(it)
//                            }
//                        }) {
//                        val zipFilePath = "/Volumes/datasets/github_public_repos/${repoRecord.repo.fullName}/$sha.zip"
//                        val zipFile = File(zipFilePath)
//                        if (zipFile.exists() && zipFile.length() > 128) {
//                            // println("${repoRecord.repo.fullName} $sha")
//                            repoRecordsAndFiles.add(Struct3(repoRecord, sha, zipFilePath))
//                            found = true
//                        }
//                    }
//                }
//
//                if (found) {
//                    break
//                }
//            }
//
//            if (found) {
//                j += 1
//            }
//            if (j >= numFilesToUse) {
//                break
//            }
//        }
//
//        var numFilesFoundToUse = 0
//        for ((repoRecord, sha, zipFilePath) in repoRecordsAndFiles) {
//            // println("\n${repoRecord.repo.fullName} $sha ${file.length()}")
//
////            if (repoRecord.repo.id != 931135) {
////                continue
////            }
//
//            println(zipFilePath)
//
//            val fileContentMap = filesInZip(Paths.get(zipFilePath))
//                .mapNotNull { (interiorFilePath, content) ->
//                    if (content.isNotEmpty() && extensions.any { interiorFilePath.endsWith(it) }) {
//                        val fullPath = Paths.get(repoRecord.repo.fullName, interiorFilePath).toString()
//                        val averageContentLineLength = content.length / content.split("\n").size
//                        // println("Average content line length: $averageContentLineLength")
//                        if (averageContentLineLength in 10..200) {
//                            fullPath to content
//                        } else {
//                            null
//                        }
//                    } else {
//                        null
//                    }
//                }
//                .toMap()
//
////            for ((fp, fc) in fileContentMap.entries) {
////                println("$fp: ${fc.length}")
////                val lines = fc.split("\n")
////                for (line in lines.take(16)) {
////                    println("|$line".take(128))
////                }
////                println("=".repeat(50))
////            }
//
//            println()
//
//            for (pullRequest in repoRecord.pullRequests) {
//                val commits = pullRequest.commits
//                    .filter { it.commit.sha == sha }
//
//                commits.forEach { c ->
//                    println("Repo: ${repoRecord.repo.fullName}")
//                    println("Commit ${c.commit.commit.message.split("\n").firstOrNull()?.take(128) ?: ""}")
//                    for (f in c.commit.files) {
//                        val filePath = Paths.get(repoRecord.repo.fullName, f.filename).toString()
//
//                        if (filePath in fileContentMap) {
//                            val fileContent = fileContentMap[filePath]!!
//                            println("  " + f.filename + " " + fileContent.length)
//                            val patchLength = f.patch?.length ?: 0
//                            println("  Patch length: $patchLength")
//                            val (averageLineLength, averageContentLineLength) = if (fileContent.isNotEmpty() && patchLength > 0) {
//                                val lines = f.patch!!.split("\n")
//                                val numLines = lines.size
//                                val averageLineLength = patchLength / numLines
//                                val averageContentLineLength = fileContent.length / fileContent.split("\n").size
//                                println("Average patch line length: $averageLineLength")
//                                println("Average content line length: $averageContentLineLength")
//                                averageLineLength to averageContentLineLength
//                            } else {
//                                0 to 0
//                            }
//
//                            if (fileContent.isNotEmpty() && patchLength > 0 && (averageLineLength in (10..200)) && (averageContentLineLength in (10..200))) {
//                                run {
//                                    val fname = (sha + fileContent).hashCode().absoluteValue.toString() + ".json"
//                                    val outPath = Paths.get(testDataDir.path, fname)
//                                    println(outPath)
//                                    outPath.toFile().createNewFile()
//
//                                    val model = FileRepoRecordPairWithPatch(
//                                        pullRequest.pullRequest.id,
//                                        c.commit.sha,
//                                        f.sha!!,
//                                        fileContent,
//                                        repoRecord,
//                                    )
//                                    mapper.writeValue(outPath.toFile(), model)
//                                    println("Wrote to $outPath")
//                                    numFilesFoundToUse++
//                                    if (numFilesFoundToUse >= maxN) {
//                                        return
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    println()
//                }
//            }
//
//            if (numFilesFoundToUse >= maxN) {
//                break
//            }
//        }
//    }
//
//    @Test
//    fun `loads files`() {
//        // loadFiles(100)
//    }
//
//    @Test
//    fun `correctly aligns correct diffs`() {
//        val mapper = jacksonObjectMapper()
//
//        val reposAndPatches = mutableListOf<FileRepoRecordPairWithPatch>()
//        Files.list(Paths.get(testDataDir.path)).forEach {
//            if (it.toString().endsWith(".json")) {
//                reposAndPatches.add(mapper.readValue(it.toFile(), FileRepoRecordPairWithPatch::class.java))
//            }
//        }
//
//        println("Loaded ${reposAndPatches.size}")
//
////        reposAndPatches.groupBy { it.repoRecord.repo.fullName }
////            .mapValues { (_, l) ->
////                l.size
////            }
////            .forEach { (k, v) ->
////                println("$k: $v")
////            }
//
//        reposAndPatches
//            .drop(9)
//            .take(1)
//            .forEachIndexed { index, rr ->
//                println("\nTest case $index\n")
//
//                val ff = rr.repoRecord.pullRequests.first {
//                    it.pullRequest.id == rr.pullRequestId
//                }.commits.first {
//                    it.commit.sha == rr.commitSha
//                }.commit.files.first {
//                    it.sha == rr.fileSha
//                }
//                println("${ff.status} ${ff.filename} ${rr.fileContent.length} ${ff.patch!!.length}")
//
//                val reversedDocumentContent = GitUtils.applyPatch(
//                    rr.fileContent, ff.patch, reverse = true,
//                )
//
//                File("patch_in.txt").writeText(ff.patch)
//                File("document_in.txt").writeText(reversedDocumentContent)
//
//                val patch = DiffLiteralPatch(ff.patch)
//                val document = DiffLiteralSourceDocument(reversedDocumentContent)
//
//                val alignedDocument = helper.alignBySectionAsync(
//                    patch,
//                    document,
//                ).block()!!
//
//                // println(alignedDocument.joinToString("\n") { it.toString() })
//
//                val serializedResult = DiffLiteralSourceDocumentSerializer().serialize(alignedDocument)
//                File("document_out.txt").writeText(serializedResult.text)
//
//                // Check that documentIn + invertedPatch = documentOut since this patch is already applied
//
////                val expectedResult = helperStrict.alignBySection(
////                    invertedPatch,
////                    document,
////                )!!
////                val serializedExpectedResult = DiffLiteralSourceDocumentSerializer().serialize(expectedResult)
//                assertEquals(rr.fileContent, serializedResult.text)
//
//                println("\nPassed test case $index\n")
//            }
//
//    }
//
//    private fun testAlignment(patchText: String, baseDocumentText: String, expectedResult: String) {
//        val helper =DocumentDiffAlignmentHelperImpl(
//            config.copy(numTrailingOperationsRequired = 0),
//            operationAlignmentConfig
//        )
//
//        val startMs = Instant.now().toEpochMilli()
//        val alignedPatch = helper.alignBySectionAsync(
//            DiffLiteralPatch(patchText),
//            DiffLiteralSourceDocument(baseDocumentText),
//        ).block()!!
//        val endMs = Instant.now().toEpochMilli()
//        println("Aligned in ${endMs - startMs}ms")
//
//        val serializedDocument = DiffLiteralSourceDocumentSerializer().serialize(alignedPatch).text
//        assertEquals(expectedResult, serializedDocument)
//    }
//
//    private val patch0 = """
//        --- a/tmp_document.txt
//        +++ b/document_out.txt
//        @@ -1,8 +1,8 @@
//         /*
//          * This file is part of the Symfony Webpack Encore package.
//          *
//        + * (c) Fabien Potencier <fabien@symfony.com>
//          *
//        + * For the full copyright and license information, please view the LICENSE
//        - * file that was distributed with this source code.
//          */
//
//         'use strict';
//    """.trimIndent()
//
//    @Test
//    fun `aligns small patch file`() {
//        // Correct patch content
//        val lines = jsDocument.split("\n").take(10)
//
//        val baseDocumentIndicesRemoved = listOf(3, 5)
//
//        val patchText = patch0
//        val baseDocumentText =
//            lines.indices.filter { it !in baseDocumentIndicesRemoved }.joinToString("\n") { lines[it] }
//
//        val expected = patchText
//            .split("\n")
//            .mapNotNull {
//                val firstChar = it.take(1)
//                if (it.startsWith("@@") || it.startsWith("+++") || it.startsWith("---")) {
//                    null
//                } else if (firstChar == "+" || firstChar == " ") {
//                    it.drop(1)
//                } else {
//                    null
//                }
//            }
//            .joinToString("\n")
//
//        testAlignment(patchText, baseDocumentText, expected)
//    }
//
//    private fun makePatch(fromDocument: String, toDocument: String): String {
//        val nowMs = Instant.now().toEpochMilli().toString()
//        val fname0 = "tmp_${nowMs}_0.txt"
//        File(fname0).writeText(fromDocument)
//        val fname1 = "tmp_${nowMs}_1.txt"
//        File(fname1).writeText(toDocument)
//        val proc = Runtime.getRuntime().exec("git diff --no-index $fname0 $fname1")
//        val reader = BufferedReader(InputStreamReader(proc.inputStream))
//        val lines = reader.lines().toList()
//        println(lines.joinToString("\n"))
//        File(fname0).delete()
//        File(fname1).delete()
//        return lines.joinToString("\n")
//    }
//
//    @Test
//    fun `aligns small patch file with mistakes`() {
//        val nLines = 10
//        val lines = jsDocument.split("\n").take(nLines)
//        val baseDocumentText = lines.joinToString("\n")
//
//        // Remove lines from patch baseline to simulate mistake
//        val random = Random(123182411)
//        val changedIndicesRemoved = lines.indices.shuffled(random).take(lines.size / 4)
//        val baseDocumentIndicesRemoved = lines.indices.shuffled(random).take(lines.size / 4)
//
//        val baseAlteredDocumentText =
//            lines.indices.filter { it !in baseDocumentIndicesRemoved }.joinToString("\n") { lines[it] }
//        val changedDocumentText = lines.indices.filter { it !in changedIndicesRemoved }.joinToString("\n") { lines[it] }
//
//        val patchText = makePatch(baseAlteredDocumentText, changedDocumentText)
//
//        // 10k evals!
//        // This one is very sensitive to NOP->ADD cost
//        testAlignment(patchText, baseDocumentText, changedDocumentText)
//    }
//
//    @Test
//    fun `aligns larger correct patch`() {
//        val lines = jsDocument.split("\n") // .take(nLines)
//        val baseDocumentText = lines.joinToString("\n")
//
//        // Remove lines from patch baseline to simulate mistake
//        val random = Random(123182411)
//        val changedIndicesRemoved = lines.indices.shuffled(random).take(lines.size / 4)
//
//        val changedDocumentText = lines.indices.filter { it !in changedIndicesRemoved }.joinToString("\n") { lines[it] }
//
//        val patchText = makePatch(baseDocumentText, changedDocumentText)
//
//        // This one splits into two sections
//        testAlignment(patchText, baseDocumentText, changedDocumentText)
//    }
//
//}