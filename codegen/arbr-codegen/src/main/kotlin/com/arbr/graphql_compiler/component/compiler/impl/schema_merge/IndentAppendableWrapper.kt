package com.arbr.graphql_compiler.component.compiler.impl.schema_merge

class IndentAppendableWrapper(
    private val innerAppendable: Appendable,
    private val indentString: String = "\t",
) : Appendable {
    private var didAppendIndent: Boolean = false

    override fun append(csq: CharSequence): IndentAppendableWrapper {
        if (!didAppendIndent) {
            innerAppendable.append(indentString)
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
            innerAppendable.append(indentString)
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

    companion object {
        const val INDENT4 = "    "
    }
}