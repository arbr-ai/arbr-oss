package com.arbr.engine.util

import com.arbr.content_formats.tokens.TokenizationUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

private const val text = """<html><p style="white-space: pre"><span style="background-color:#ddffdd;">class</span><span style="background-color:#ddddff;"> </span><span style="background-color:#ddffdd;">ApplicationManager</span><span style="background-color:#ddddff;"> </span><span style="background-color:#ddffdd;">"""

class TokenizationUtilsTest {

    @Test
    fun `gets cl100k_base token count`() {
        Assertions.assertEquals(73, TokenizationUtils.getTokenCount(text))
    }
}
