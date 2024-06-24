package com.arbr.relational_prompting.services.ai_application.config

import com.arbr.content_formats.yaml.YamlParser
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.arbr.content_formats.mapper.Mappers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.opentest4j.AssertionFailedError

class YamlParserTest2 {

    @Test
    fun `parses tree output`() {
        val yamlMapper = Mappers.yamlMapper

        // Indentation failure case from GPT output
            val malformattedTree = """
combined_source_element_tree:
  element_rule_name: "whole_file"
  element_name: "program"
  element_index: 0
  child_elements:
    - element_rule_name: "import"
      element_name: "[React, useState]"
      element_index: 0
      child_elements: []
    - element_rule_name: "miscellaneous"
      element_name: null
      element_index: 0
      child_elements: []
    - element_rule_name: "import"
      element_name: "viteLogo"
      element_index: 0
      child_elements: []
    - element_rule_name: "miscellaneous"
      element_name: null
      element_index: 1
      child_elements: []
    - element_rule_name: "import"
      element_name: "'./App.css'"
      element_index: 0
      child_elements: []
    - element_rule_name: "miscellaneous"
      element_name: null
      element_index: 2
      child_elements: []
    - element_rule_name: "import"
      element_name: "calculator"
      element_index: 0
      child_elements: []
    - element_rule_name: "miscellaneous"
      element_name: null
      element_index: 3
      child_elements: []
    - element_rule_name: "function"
      element_name: "App"
      element_index: 0
      child_elements:
        - element_rule_name: "miscellaneous"
          element_name: null
          element_index: 4
          child_elements: []
        - element_rule_name: "variable"
          element_name: "[count, setCount]"
          element_index: 0
          child_elements: []
        - element_rule_name: "miscellaneous"
          element_name: null
          element_index: 5
          child_elements: []
        - element_rule_name: "html_element"
          element_name: "react_component"
          element_index: 0
          child_elements: []
        - element_rule_name: "miscellaneous"
          element_name: null
          element_index: 6
          child_elements: []
      - element_rule_name: "miscellaneous"
        element_name: null
        element_index: 7
        child_elements: []
      - element_rule_name: "variable"
        element_name: "[count, setCount]"
        element_index: 0
        child_elements: []
      - element_rule_name: "miscellaneous"
        element_name: null
        element_index: 8
        child_elements: []
      - element_rule_name: "html_element"
        element_name: "react_component"
        element_index: 0
        child_elements: []
      - element_rule_name: "miscellaneous"
        element_name: null
        element_index: 9
        child_elements: []
      - element_rule_name: "miscellaneous"
        element_name: null
        element_index: 10
        child_elements: []
      - element_rule_name: "export_default"
        element_name: "App"
        element_index: 0
        child_elements: []
            """.trimIndent()


        assertThrows<Exception> {
            yamlMapper.readValue(
                malformattedTree,
                jacksonTypeRef()
            )
        }

        val yamlParser = YamlParser()
        val parsedTree = yamlParser.parseMap(emptyList(), malformattedTree)
        val result = parsedTree.firstOrNull { (resMap, _) ->
            resMap != null
        }?.first

        // Fails
        assertThrows<AssertionFailedError> {
            Assertions.assertNotNull(result)
            println(result)
        }

    }

}