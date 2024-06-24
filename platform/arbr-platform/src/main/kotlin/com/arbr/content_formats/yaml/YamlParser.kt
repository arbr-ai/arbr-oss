@file:Suppress("DEPRECATION")

package com.arbr.content_formats.yaml

import com.arbr.content_formats.code.LenientCodeParser
import com.arbr.content_formats.mapper.Mappers
import com.arbr.relational_prompting.layers.object_translation.PropertySchema
import com.arbr.relational_prompting.services.ai_application.config.OutputFormatException
import com.arbr.util_common.collections.lazyMap
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.slf4j.LoggerFactory
import kotlin.math.abs

class YamlParser {
    private val logger = LoggerFactory.getLogger(YamlParser::class.java)

    private fun normalizeIndentation(content: String, roundUp: Boolean): String {
        val lines = content.split("\n")
        val indentationCounts = lines.map { it.replaceFirst("- ", "  ") }
            .map { it.length - it.trimStart().length }

        val mostCommonIndent = indentationCounts.zip(indentationCounts.drop(1)).map { (indent, nextIndent) ->
            abs(nextIndent - indent)
        }
            .filter { it > 0 }
            .groupBy { it }
            .maxByOrNull { it.value.size }
            ?.key ?: 0

        if (mostCommonIndent == 0) {
            return content
        }

        return lines.zip(indentationCounts).joinToString("\n") { (line, indentation) ->
            if (indentation % mostCommonIndent == 0) {
                line
            } else if (roundUp) {
                val target = (indentation / mostCommonIndent + 1) * mostCommonIndent
                " ".repeat(target - indentation) + line
            } else {
                val target = (indentation / mostCommonIndent - 1) * mostCommonIndent
                line.drop(indentation - target)
            }
        }
    }

    private fun unnestBacktickYaml(content: String): String {
        val prefixes = listOf(
            "```yaml",
            "```yml",
            "```"
        )
        val suffixes = listOf(
            "```"
        )

        var runningContent = content.trim()

        for (prefix in prefixes) {
            if (runningContent.startsWith(prefix)) {
                runningContent = runningContent
                    .drop(prefix.length)
                    .trimStart()
            }
        }

        for (suffix in suffixes) {
            if (runningContent.endsWith(suffix)) {
                runningContent = runningContent
                    .dropLast(suffix.length)
                    .trimEnd()
            }
        }

        return runningContent
    }

    private fun handleSingleElementOut(
        propertySchemas: List<PropertySchema<*, *>>,
        content: String,
    ): String {
        return if (propertySchemas.size == 1) {
            val propertyKey = propertySchemas.first().property().first
            if (content.startsWith(propertyKey)) {
                content
            } else {
                val lenientUnwrapped = content
                    .split("\n")
                    .joinToString("\n") { "  $it" }
                "$propertyKey: |\n$lenientUnwrapped"
            }
        } else {
            content
        }
    }

    private fun allParseableForms(
        propertySchemas: List<PropertySchema<*, *>>,
        content: String,
    ): Iterable<String> {
        val operations: List<(String) -> String> = listOf(
            { it },
            { unnestBacktickYaml(it) },
            { handleSingleElementOut(propertySchemas, it) },
            { it + "\"" },
            { normalizeIndentation(it, true) },
            { normalizeIndentation(it, false) },
            { LenientCodeParser.parse(it) },
        )

        return Iterable {
            object : Iterator<String> {
                val maxAppliedOps = 3
                val indices = MutableList(maxAppliedOps) { 0 }
                val numOps = operations.size
                var hasNext = true

                private fun getCurrentString(): String {
                    var string = content
                    for (opIndex in indices) {
                        string = operations[opIndex](string)
                    }
                    return string
                }

                private fun increment() {
                    var ii = 0
                    while (ii < maxAppliedOps && indices[ii] + 1 == numOps) {
                        indices[ii] = 0
                        ii++
                    }

                    if (ii < maxAppliedOps) {
                        indices[ii] = indices[ii] + 1
                    } else {
                        // Exhausted all operations
                        hasNext = false
                    }
                }

                override fun hasNext(): Boolean {
                    return hasNext
                }

                override fun next(): String {
                    val result = getCurrentString()
                    increment()

                    return result
                }

            }
        }
    }

    fun parseMap(
        propertySchemas: List<PropertySchema<*, *>>,
        content: String,
    ): Iterable<Pair<LinkedHashMap<String, Any>?, Exception?>> {
        val forms = allParseableForms(propertySchemas, content)

        /**
         * Lazily attempt to parse everything
         */
        return forms.lazyMap { form ->
            try {
                yamlMapper.readValue(form, jacksonTypeRef<LinkedHashMap<String, Any>>()) to null
            } catch (e: Exception) {
                null to OutputFormatException(form, e)
            }
        }
    }

    companion object {
        private val yamlMapper = Mappers.yamlMapper
    }
}
