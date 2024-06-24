package com.arbr.content_formats.jsonb

import com.fasterxml.jackson.databind.ObjectMapper
import org.jooq.JSONB

private const val ESCAPED_NULL = "\\u0000"
private const val EMPTY_STRING = ""

/**
 * Serialize to JSONB and strip nulls.
 */
fun serializeToJsonb(mapper: ObjectMapper, obj: Any): JSONB {
    return JSONB.jsonb(mapper.writeValueAsString(obj).replace(ESCAPED_NULL, EMPTY_STRING))
}

fun JSONB.stripNulls(): JSONB {
    return JSONB.jsonb(data().replace("\\u0000", ""))
}
