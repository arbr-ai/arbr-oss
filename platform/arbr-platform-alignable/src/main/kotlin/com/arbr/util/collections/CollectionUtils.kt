package com.arbr.util.collections

inline fun <T> Iterable<T>.splitOn(predicate: (T) -> Boolean): Pair<List<T>, List<T>> {
    val arrayListSatisfied = ArrayList<T>()
    val arrayListUnsatisfied = ArrayList<T>()

    for (element in this) if (predicate(element)) arrayListSatisfied.add(element) else arrayListUnsatisfied.add(element)

    return arrayListSatisfied to arrayListUnsatisfied
}

inline fun <T, reified U> Array<T>.mapToArray(f: (T) -> U): Array<U> {
    return Array(this.size) { i ->
        f(this.elementAt(i))
    }
}

inline fun <reified U> DoubleArray.mapToArray(f: (Double) -> U): Array<U> {
    return Array(this.size) { i ->
        f(this.elementAt(i))
    }
}

inline fun <T, reified U> Collection<T>.mapToArray(f: (T) -> U): Array<U> {
    return Array(this.size) { i ->
        f(this.elementAt(i))
    }
}
