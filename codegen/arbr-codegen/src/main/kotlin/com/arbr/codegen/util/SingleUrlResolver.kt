package com.arbr.codegen.util

import com.github.mustachejava.MustacheResolver
import java.io.Reader
import java.net.URL

class SingleUrlResolver(
    private val url: URL,
) : MustacheResolver {

    override fun getReader(resourceName: String): Reader {
        if (resourceName.isBlank()) {
            throw Exception("Blank resource name")
        }

        check(url.path.endsWith(resourceName))

        return url.openStream().bufferedReader()
    }
}