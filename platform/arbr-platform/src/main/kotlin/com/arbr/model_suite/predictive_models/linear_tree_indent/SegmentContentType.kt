package com.arbr.model_suite.predictive_models.linear_tree_indent

import com.fasterxml.jackson.annotation.JsonValue

/**
 * Source language content type, i.e. what dictates the parser used
 */
enum class SegmentContentType(@JsonValue val serializedName: String) {
    JSX("JSX"),
    JavaScript("JavaScript"),
    TSX("TSX"),
    TypeScript("TypeScript"),
    HTML("HTML"),
    CSS("CSS"),

    /**
     * Plain text and catch-all for unsupported content types
     */
    Plaintext("plaintext");
}
