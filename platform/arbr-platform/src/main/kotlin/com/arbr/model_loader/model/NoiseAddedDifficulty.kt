package com.arbr.model_loader.model

import com.fasterxml.jackson.annotation.JsonValue

enum class NoiseAddedDifficulty(@JsonValue val serializedName: String) {
    EASY("easy"),
    NORMAL("normal"),
    HARD("hard");
}