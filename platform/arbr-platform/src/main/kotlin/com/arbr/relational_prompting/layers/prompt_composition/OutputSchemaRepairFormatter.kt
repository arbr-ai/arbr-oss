package com.arbr.relational_prompting.layers.prompt_composition

import com.arbr.content_formats.mapper.Mappers
import com.arbr.content_formats.json_schematized.JsonSchemaViolation

fun interface OutputSchemaRepairFormatter {

    fun process(
        violations: List<JsonSchemaViolation>
    ): String

    companion object {
        private val yamlMapper = Mappers.yamlMapper

        val default = OutputSchemaRepairFormatter { violations ->
            val violationString = violations.joinToString("\n\n") { violation ->
                val valueString = if (violation.localObject is List<*> || violation.localObject is Map<*, *>) {
                    yamlMapper.writeValueAsString(violation.localObject)
                } else {
                    violation.localObject.toString()
                }.trim()

                "At ${violation.selector}:\nValue:\n$valueString\n\nViolation: " + violation.message
            }

            """
Your output resulted in the following schema violations:

$violationString

Repair the output to fix the violations and make it interpretable.
            """.trimIndent()
        }
    }
}
