package com.topdown.parsers.lang.universal.tree_parsing

import com.topdown.parsers.lang.html.base.HTMLParser

class HtmlTreeParsingListener(
    contentLength: Long,
): TreeParsingListener(
    GlobalTreeParsingTerminalRules.htmlRules,
    contentLength,
)