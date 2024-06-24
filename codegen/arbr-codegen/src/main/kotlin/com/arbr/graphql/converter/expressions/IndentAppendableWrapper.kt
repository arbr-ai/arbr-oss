package com.arbr.graphql.converter.expressions

class IndentAppendableWrapper(
    private val innerAppendable: Appendable,
) : Appendable {
    private var didAppendIndent: Boolean = false

    override fun append(csq: CharSequence): IndentAppendableWrapper {
        if (!didAppendIndent) {
            innerAppendable.append('\t')
            didAppendIndent = true
        }
        innerAppendable.append(csq)
            .also {
                if (csq.endsWith('\n')) {
                    didAppendIndent = false
                }
            }
        return this
    }

    override fun append(csq: CharSequence, start: Int, end: Int): IndentAppendableWrapper {
        innerAppendable.append(csq, start, end)
        return this
    }

    override fun append(c: Char): IndentAppendableWrapper {
        if (!didAppendIndent) {
            innerAppendable.append('\t')
            didAppendIndent = true
        }
        innerAppendable.append(c)
            .also {
                if (c == '\n') {
                    didAppendIndent = false
                }
            }
        return this
    }
}