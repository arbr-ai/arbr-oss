package com.arbr.api.user.core

import com.fasterxml.jackson.annotation.JsonValue

enum class UserEmailSource(@JsonValue val serializedValue: String) {
    FIRST_PARTY("first_party"),
    GITHUB("github"),
    STRIPE("stripe"),
}
