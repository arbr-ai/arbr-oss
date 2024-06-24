package com.arbr.api_server_base.service.github.client

import com.arbr.content_formats.mapper.Mappers
import org.springframework.http.codec.DecoderHttpMessageReader
import org.springframework.http.codec.EncoderHttpMessageWriter
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.util.MimeTypeUtils

object CustomCodecs {

    private val snakeCaseMapper = Mappers.snakeCaseMapper

    fun snakeCaseDecoderHttpMessageReader(): DecoderHttpMessageReader<Any> {
        return DecoderHttpMessageReader<Any>(
            Jackson2JsonDecoder(
                snakeCaseMapper,
                MimeTypeUtils.APPLICATION_JSON,
            )
        )
    }

    fun snakeCaseEncoderHttpMessageWriter(): EncoderHttpMessageWriter<Any> {
        return EncoderHttpMessageWriter<Any>(
            Jackson2JsonEncoder(
                snakeCaseMapper,
                MimeTypeUtils.APPLICATION_JSON,
            )
        )
    }

}
