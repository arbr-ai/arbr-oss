package com.arbr.og.object_model.common.functions.spec.base

class ResourceHelperFunctionCompleteSpecContext {
    var model: String = ""
    var temperature: Double = 0.7
        set(value) {
            field = value.coerceIn(0.0, 2.0)
        }
    var maxTokens: Int = 100
        set(value) {
            field = value.coerceAtLeast(1)
        }
    var topP: Double = 1.0
        set(value) {
            field = value.coerceIn(0.0, 1.0)
        }
    var frequencyPenalty: Double = 0.0
        set(value) {
            field = value.coerceIn(-2.0, 2.0)
        }
    var presencePenalty: Double = 0.0
        set(value) {
            field = value.coerceIn(-2.0, 2.0)
        }
    var stop: List<String> = emptyList()
}
