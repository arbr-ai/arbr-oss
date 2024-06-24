package com.arbr.graphql_compiler.model.python

data class PythonImportFromStatements(
    val statementMap: Map<String, PythonImportFromStatement>
) : RenderableExpression {

    /**
     * Construct an empty collection of import-from statements.
     */
    constructor() : this(emptyMap())

    /**
     * Construct a singleton collection of import-from statements.
     */
    constructor(pythonImportFromStatement: PythonImportFromStatement) : this(
        mapOf(pythonImportFromStatement.source to pythonImportFromStatement)
    )

    fun mergeWith(
        importFromStatements: PythonImportFromStatements
    ): PythonImportFromStatements {
        val combinedMap = statementMap.toMutableMap()
        for ((source, statement) in importFromStatements.statementMap) {
            combinedMap.compute(source) { _, existingStatement ->
                existingStatement?.mergeWithSameSource(statement) ?: statement
            }
        }
        return PythonImportFromStatements(combinedMap)
    }

    override fun render(): String {
        return statementMap.values
            .map { it.render() }
            .sorted()
            .joinToString("\n")
    }

}