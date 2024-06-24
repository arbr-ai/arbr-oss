package com.arbr.graphql_compiler.model.python

data class PythonListValueType<T : PythonValueType>(
    private val innerValueType: T,
) : PythonValueType {
    override val literalNonNullForm: String = "List[${innerValueType.literalNonNullForm}]"

    override fun getImportSources(): PythonImportFromStatements {
        return super
            .getImportSources()
            .mergeWith(listImports)
    }

    companion object {
        private val listImports = PythonImportFromStatements(
            PythonImportFromStatement("typing", setOf("List"))
        )
    }
}