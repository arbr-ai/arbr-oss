package com.arbr.graphql_compiler.model.python

sealed interface PythonValueType : RenderableExpression {
    val literalNonNullForm: String

    /**
     * Whether the field is nullable.
     * GraphQL defaults to true since partial models dominate use cases.
     */
    val nullable: Boolean
        get() = true

    /**
     * from K import V_0, V_1, ...
     */
    private val nonNullImports: PythonImportFromStatements
        get() = emptyImports

    fun getImportSources(): PythonImportFromStatements {
        return if (nullable) {
            nonNullImports.mergeWith(nullableImport)
        } else {
            nonNullImports
        }
    }

    override fun render(): String {
        return if (nullable) {
            "Optional[$literalNonNullForm]"
        } else {
            literalNonNullForm
        }
    }

    companion object {
        private val emptyImports = PythonImportFromStatements()
        private val nullableImport = PythonImportFromStatements(
            PythonImportFromStatement("typing", setOf("Optional"))
        )
    }
}