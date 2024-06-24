package com.arbr.engine.services.differential_content.segmenter

import com.arbr.content_formats.mapper.Mappers
import com.arbr.model_suite.predictive_models.linear_tree_indent.SegmenterService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SegmenterServiceTest {

    private val segmenterService = SegmenterService()

    @Test
    fun `segments JSX`() {
        val content = "import React, { useState } from 'react';"

        val parseResult = segmenterService.segmentFileContentUniversal(
            "src/Calculator.jsx",
            content,
        )

        println(Mappers.yamlMapper.writeValueAsString(parseResult.segmentReprTree))

        Assertions.assertTrue(parseResult.segmentReprTree.childElements.isNotEmpty())
    }

    @Test
    fun `segments react component`() {
        // Note: test passes but this logs errors for the `=` inside the button tags
        val content = """
            import React, { useState } from 'react';

            function Calculator() {
              const [input, setInput] = useState('');
              const [result, setResult] = useState(0);

              // Rest of the function...
            }
            
            function Calculator() {
              const [input, setInput] = useState("");
              const [result, setResult] = useState(0);

              return (
                <div className="calculator">
                  <div className="display">{result}</div>
                  <div className="buttons">
                    <button>1</button>
                    <button>2</button>
                    <button>3</button>
                    <button>+</button>
                    <button>-</button>
                    <button>*</button>
                    <button>/</button>
                    <button>=</button>
                    <button onClick={() => setInput("")}>Clear</button>
                  </div>
                </div>
              );
            }
        """.trimIndent()

        val parseResult = segmenterService.segmentFileContentUniversal(
            "src/Calculator.jsx",
            content,
        )

        println(Mappers.yamlMapper.writeValueAsString(parseResult.segmentReprTree))

        Assertions.assertTrue(parseResult.segmentReprTree.childElements.isNotEmpty())
    }

    @Test
    fun `segments react component 2`() {
        // TODO: Improve labeling some content as miscellaneous, for example child of first Calculator function with
        // function header
        val content = """
            import React, { useState } from 'react';

            function Calculator() {
              const [input, setInput] = useState('');
              const [result, setResult] = useState(0);

              // Rest of the function...
            }
            
            function Calculator() {
              const [input, setInput] = useState("");
              const [result, setResult] = useState(0);

              return (
                <div className="calculator">
                  <div className="display">{result}</div>
                  <div className="buttons">
                    <button>1</button>
                    <button>2</button>
                    <button>3</button>
                    <button>+</button>
                    <button>-</button>
                    <button>*</button>
                    <button>/</button>
                    <button onClick={() => setInput("")}>Clear</button>
                  </div>
                </div>
              );
            }
        """.trimIndent()

        val parseResult = segmenterService.segmentFileContentUniversal(
            "src/Calculator.jsx",
            content,
        )

        println(Mappers.yamlMapper.writeValueAsString(parseResult.segmentReprTree))

        Assertions.assertTrue(parseResult.segmentReprTree.childElements.isNotEmpty())
    }

    @Test
    fun `identifies adjacent blocks`() {
        val content = """
            import React, { useState } from 'react';

            function First() {
              const [input, setInput] = useState('');
              const [result, setResult] = useState(0);

              console.log("Hello World");
            }function Second() {
              const [input, setInput] = useState("");
              const [result, setResult] = useState(0);

              console.log("Hello again World");
            }
        """.trimIndent()

        val parseResult = segmenterService.segmentFileContentUniversal(
            "src/App.jsx",
            content,
        )

        println(Mappers.yamlMapper.writeValueAsString(parseResult.segmentReprTree))

        Assertions.assertTrue(parseResult.segmentReprTree.childElements.isNotEmpty())
    }

    @Test
    fun `segments bad css and corrects`() {
        val contents = """
            #root {
              max-width: 1280px;
              margin: 0 auto;
              padding: 2rem;
              text-align: center;
            }
            
            @keyframes logo-spin {
              from {
                fun hello() {
                    console.log("hello");
                }
              }
              to {
                transform: rotate(360deg);
              }
            }

            .logo {
              height: 6em;
              padding: 1.5em;
              will-change: filter;
              transition: filter 300ms;
            }
            .logo:hover {
              filter: drop-shadow(0 0 2em #646cffaa);
            }
            .logo.react:hover {
              filter: drop-shadow(0 0 2em #61dafbaa);
            }
        """.trimIndent()

        val parseResult = segmenterService.segmentFileContentUniversal(
            "src/App.css",
            contents,
        )

        println(Mappers.yamlMapper.writeValueAsString(parseResult.segmentReprTree))

        Assertions.assertTrue(parseResult.segmentReprTree.childElements.isEmpty())
    }

}
