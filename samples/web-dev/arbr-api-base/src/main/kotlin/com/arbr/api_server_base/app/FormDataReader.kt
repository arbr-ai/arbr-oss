package com.arbr.api_server_base.app

import com.fasterxml.jackson.databind.ObjectMapper
import org.reactivestreams.Publisher
import org.springframework.context.annotation.Configuration
import org.springframework.core.ResolvableType
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.MediaType
import org.springframework.http.ReactiveHttpInputMessage
import org.springframework.http.codec.HttpMessageReader
import org.springframework.lang.Nullable
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.util.StringUtils
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.URLDecoder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

@Configuration
class FormDataReader(
    private val mapper: ObjectMapper
) : HttpMessageReader<Any> {
    override fun canRead(elementType: ResolvableType, @Nullable mediaType: MediaType?): Boolean {
        return supportsMediaType(mediaType)
    }

    override fun read(
        elementType: ResolvableType,
        message: ReactiveHttpInputMessage,
        hints: MutableMap<String, Any>
    ): Flux<Any> {
        return Flux.from(this.readMono(elementType, message, hints))
    }

    override fun readMono(
        elementType: ResolvableType,
        message: ReactiveHttpInputMessage,
        hints: MutableMap<String, Any>
    ): Mono<Any> {
        val elementClass = elementType.toClass()

        val contentType = message.headers.contentType
        val charset = this.getMediaTypeCharset(contentType)
        return DataBufferUtils.join(message.body as Publisher<DataBuffer>).map { buffer ->
            val body: String = buffer.toString(charset)
            DataBufferUtils.release(buffer)
            val resultElement = parseFormData(charset, body, elementClass)
            resultElement
        }
    }

    private fun supportsMediaType(@Nullable mediaType: MediaType?): Boolean {
        return mediaType == FORM_DATA_ALT
    }

    private fun getMediaTypeCharset(@Nullable mediaType: MediaType?): Charset {
        return (if (mediaType != null && mediaType.charset != null) mediaType.charset else null)
            ?: StandardCharsets.UTF_8
    }

    private fun <T> parseFormData(charset: Charset?, body: String, clazz: Class<T>): T? {
        val multiMap = parseFormDataMulti(charset, body)

        val stringMap = multiMap
            .entries
            .mapNotNull { (k, v) ->
                val firstElt = v?.firstNotNullOfOrNull { it }
                if (k == null || firstElt == null) {
                    null
                } else {
                    k to firstElt
                }
            }
            .associate { it.first to it.second }

        return mapper.convertValue(stringMap, clazz)
    }

    private fun parseFormDataMulti(charset: Charset?, body: String): MultiValueMap<String?, String?> {
        val pairs = StringUtils.tokenizeToStringArray(body, "&")
        val result: MultiValueMap<String?, String?> = LinkedMultiValueMap(pairs.size)

        pairs.forEach { pair ->
            val idx = pair.indexOf('=')
            if (idx == -1) {
                result.add(URLDecoder.decode(pair, charset), null)
            } else {
                val name = URLDecoder.decode(pair.substring(0, idx), charset)
                val value = URLDecoder.decode(pair.substring(idx + 1), charset)
                result.add(name, value)
            }
        }

        return result
    }

    override fun getReadableMediaTypes(): List<MediaType> {
        return listOf(FORM_DATA_ALT)
    }

    companion object {
        /**
         * Alternate form data media type enabling Jackson parsing of form data into a request body.
         */
        val FORM_DATA_ALT = MediaType("application", "x-www-form-urlencoded-alt")

        const val FORM_DATA_ALT_VALUE = "application/x-www-form-urlencoded-alt"
    }
}
