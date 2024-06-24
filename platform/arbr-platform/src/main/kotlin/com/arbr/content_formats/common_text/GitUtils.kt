package com.arbr.content_formats.common_text

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.time.Instant

object GitUtils {

    fun makePatch(fromDocument: String, toDocument: String): String {
        val nowMs = Instant.now().toEpochMilli().toString()
        val fname0 = "tmp_${nowMs}_0.txt"
        File(fname0).writeText(fromDocument)
        val fname1 = "tmp_${nowMs}_1.txt"
        File(fname1).writeText(toDocument)
        val commandTokens = arrayOf("git", "diff", "--no-index", fname0, fname1)
        val proc = Runtime.getRuntime().exec(commandTokens)
        val reader = BufferedReader(InputStreamReader(proc.inputStream))
        val lines = reader.lines().toList()
        File(fname0).delete()
        File(fname1).delete()
        return lines.joinToString("\n")
    }

    private fun sanitizePatch(patchText: String, documentFilePath: String): String {
        val patchLines = patchText.split("\n").toMutableList()
        while (patchLines.isNotEmpty() && (patchLines.first().startsWith("+++") || patchLines.first()
                .startsWith("---"))
        ) {
            patchLines.removeAt(0)
        }

        val newStartLines = listOf("--- a/", "+++ b/").map { it + documentFilePath }
        var outText = (newStartLines + patchLines).joinToString("\n")
        if (!outText.endsWith("\n")) {
            outText += "\n"
        }
        return outText
    }

    fun applyPatch(fromDocument: String, patch: String, reverse: Boolean): String {
        val nowMs = Instant.now().toEpochMilli().toString()
        val fname0 = "tmp_${nowMs}_0.txt"
        File(fname0).writeText(fromDocument)
        val fname1 = "tmp_${nowMs}_patch_0.txt"
        File(fname1).writeText(sanitizePatch(patch, fname0))
        val proc = Runtime.getRuntime().exec("git apply $fname1" + (if (reverse) " -R" else ""))
        val reader = BufferedReader(InputStreamReader(proc.inputStream))
        reader.lines().toList()
        val newDocumentText = File(fname0).readText()

        File(fname0).delete()
        File(fname1).delete()
        return newDocumentText
    }
}