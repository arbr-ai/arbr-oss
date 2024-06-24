package com.arbr.platform.alignable.alignable.struct

sealed class DoubleNullable<T: Any> {
    data class Some<T: Any>(val value: T): DoubleNullable<T>()
    class Null<T: Any>: DoubleNullable<T>()
    class Empty<T: Any>: DoubleNullable<T>()
}