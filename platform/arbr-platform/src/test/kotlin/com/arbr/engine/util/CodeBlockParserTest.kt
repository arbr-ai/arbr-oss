package com.arbr.engine.util

import com.arbr.content_formats.code.CodeBlockParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CodeBlockParserTest {

    @Test
    fun `parse single code block without tag`() {
        val input = """
            ```
            Line 1
            Line 2
            ```
        """.trimIndent()

        val expected = listOf(
            CodeBlockParser.Block(
                tag = null,
                lines = listOf("Line 1", "Line 2")
            )
        )

        val result = CodeBlockParser.parse(input)
        assertEquals(expected, result)
    }

    @Test
    fun `parse single code block with tag`() {
        val input = """
            ```kotlin
            fun main() {
                println("Hello, World!")
            }
            ```
        """.trimIndent()

        val expected = listOf(
            CodeBlockParser.Block(
                tag = "kotlin",
                lines = listOf(
                    "fun main() {",
                    "    println(\"Hello, World!\")",
                    "}"
                )
            )
        )

        val result = CodeBlockParser.parse(input)
        assertEquals(expected, result)
    }

    @Test
    fun `parse multiple code blocks`() {
        val input = """
            ```python
            def hello():
                print("Hello, Python!")
            ```
            Some text between code blocks.
            ```java
            public class HelloWorld {
                public static void main(String[] args) {
                    System.out.println("Hello, Java!");
                }
            }
            ```
        """.trimIndent()

        val expected = listOf(
            CodeBlockParser.Block(
                tag = "python",
                lines = listOf(
                    "def hello():",
                    "    print(\"Hello, Python!\")"
                )
            ),
            CodeBlockParser.Block(
                tag = "java",
                lines = listOf(
                    "public class HelloWorld {",
                    "    public static void main(String[] args) {",
                    "        System.out.println(\"Hello, Java!\");",
                    "    }",
                    "}"
                )
            )
        )

        val result = CodeBlockParser.parse(input)
        assertEquals(expected, result)
    }

    @Test
    fun `parse nested code blocks`() {
        val input = """
            ```
            Outer block start
            ```nested
            Nested block content
            ```
            Outer block end
            ```
        """.trimIndent()

        val expected = listOf(
            CodeBlockParser.Block(
                tag = null,
                lines = listOf(
                    "Outer block start",
                    "Outer block end"
                )
            ),
            CodeBlockParser.Block(
                tag = "nested",
                lines = listOf(
                    "Nested block content",
                )
            )
        )

        val result = CodeBlockParser.parse(input)
        assertEquals(expected, result)
    }

    @Test
    fun `parse with incomplete code block`() {
        val input = """
            ```incomplete
            Line in incomplete block
        """.trimIndent()

        val expected = listOf(
            CodeBlockParser.Block(
                tag = "incomplete",
                lines = listOf(
                    "Line in incomplete block",
                )
            ),
        )

        val result = CodeBlockParser.parse(input)
        assertEquals(expected, result)
    }

    @Test
    fun `real output example`() {
        val content = """
            The code changes required are to update the CSS rules in the 'src/App.css' file to style the HomePage component. This may include adding or modifying CSS classes or selectors to target the elements within the HomePage component. Add or modify the necessary CSS properties to achieve the desired styling. 

            Since the specific changes are not provided, I'll assume that we need to add a new CSS class for the HomePage component. Here is a simplified diff:

            ```diff
             #root {
               max-width: 1280px;
               margin: 0 auto;
               padding: 2rem;
               text-align: center;
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

             @keyframes logo-spin {
               from {
                 transform: rotate(0deg);
               }
               to {
                 transform: rotate(360deg);
               }
             }

             @media (prefers-reduced-motion: no-preference) {
               a:nth-of-type(2) .logo {
                 animation: logo-spin infinite 20s linear;
               }
             }

             .card {
               padding: 2em;
             }

             .read-the-docs {
               color: #888;
             }
             
            + .home-page {
            +   background-color: #f0f0f0;
            +   border-radius: 5px;
            +   padding: 20px;
            +   margin: 10px;
            + }
            ```

            Please note that the actual changes may vary based on the specific design specifications or style guide.
        """.trimIndent()

        val codeBlocks = CodeBlockParser.parse(content)

        assertEquals(1, codeBlocks.size)
    }
}
