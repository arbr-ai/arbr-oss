package com.arbr.platform.alignable.util

sealed interface Either<S, T> {
    data class Left<S, T>(val value: S): Either<S, T>
    data class Right<S, T>(val value: T): Either<S, T>

    fun <V> mapEither(mapLeft: (S) -> V, mapRight: (T) -> V): V {
        return when (this) {
            is Left -> mapLeft(this.value)
            is Right -> mapRight(this.value)
        }
    }

    fun leftOrNull(): S? = mapEither({ it }, { null })
    fun rightOrNull(): T? = mapEither({ null }, { it })
}
