package com.arbr.graphql_compiler.model.python

data class PythonImportFromStatement(
    val source: String,
    val symbols: Set<String>
) : RenderableExpression {

    fun mergeWithSameSource(
        importFromStatement: PythonImportFromStatement
    ): PythonImportFromStatement {
        check(importFromStatement.source == source) {
            "Import source mismatch for merge: ${importFromStatement.source} and $source"
        }

        return copy(symbols = symbols + importFromStatement.symbols)
    }

    override fun render(): String {
        val symbolsString = symbols.sorted().joinToString(", ")
        return "from $source import $symbolsString"
    }
}