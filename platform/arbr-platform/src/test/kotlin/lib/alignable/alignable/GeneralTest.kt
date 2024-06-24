package com.arbr.alignable.alignable

import org.junit.jupiter.api.Test

private class DidSet {

    var name: String? = null
        set(value) {
            println(value)
            field = value
        }

}

class GeneralTest {

    @Test
    fun didset() {
        val ds = DidSet()
        ds.name = "Bob"
        println("Set to: ${ds.name}")
        ds.name = null
        println("Set to: ${ds.name}")
        ds.name = "Dave"
        println("Set to: ${ds.name}")
    }

}