package com.arbr.graphql.converter.expressions

fun interface BlockAppendable: Appendable {
    fun handleString(string: String)

    override fun append(csq: CharSequence?): BlockAppendable {
        csq?.let {
            handleString(it.toString())
        }
        return this
    }

    override fun append(csq: CharSequence?, start: Int, end: Int): BlockAppendable {
        csq?.let {
            handleString(it.toString().substring(start, end))
        }
        return this
    }

    override fun append(c: Char): BlockAppendable {
        handleString(c.toString())
        return this
    }

}