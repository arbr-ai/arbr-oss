package com.arbr.model_loader.loader

import com.fasterxml.jackson.annotation.JsonValue

enum class NoisyDiffDatasetVersion(@JsonValue val serializedName: String) {
    V0("nv0"),
    V1("nv1"),
    V2("nv2_cl100k"),
}