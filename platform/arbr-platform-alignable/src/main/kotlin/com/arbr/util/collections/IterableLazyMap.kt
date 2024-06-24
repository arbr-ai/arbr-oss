package com.arbr.util.collections


fun <T, U> Iterable<T>.lazyMap(f: (T) -> U): Iterable<U> {
    val outerIterator = iterator()

    return Iterable {
        object : Iterator<U> {
            override fun hasNext(): Boolean {
                return outerIterator.hasNext()
            }

            override fun next(): U {
                return f(outerIterator.next())
            }
        }
    }
}
