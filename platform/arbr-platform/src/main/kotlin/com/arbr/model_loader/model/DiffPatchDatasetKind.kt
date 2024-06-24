package com.arbr.model_loader.model

import com.fasterxml.jackson.annotation.JsonValue

enum class DiffPatchDatasetKind(@JsonValue val serializedName: String) {
    CLEAN("clean"),
    SIMPLE_TEXT_DIFF("simple_text_diff"),
    HARDCODED("hardcoded"),
    SYNTHETIC_NOISE_V0("synthetic_noise_v0"),
    SYNTHETIC_NOISE_V1("synthetic_noise_v1"),
    SYNTHETIC_NOISE_V2("synthetic_noise_v2"),
}
