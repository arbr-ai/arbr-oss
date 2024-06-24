package com.arbr.codegen.base.generator

data class DatabaseModel(
    val schemata: List<SchemaModel>,
) {

    fun getSchema(qualifiedName: String): SchemaModel {
        return try {
            schemata.first { s ->
                s.qualifiedName == qualifiedName
            }
        } catch (e: NoSuchElementException) {
            println("Schema not found: $qualifiedName")
            throw e
        }
    }

    fun getTable(qualifiedName: String): TableModel {
        return try {
            schemata.firstNotNullOf { s ->
                s.tables.firstOrNull { t ->
                    t.qualifiedName == qualifiedName
                }
            }
        } catch (e: NoSuchElementException) {
            println("Table not found: $qualifiedName")
            throw e
        }
    }

    fun getField(qualifiedName: String): FieldModel {
        return try {
            schemata.firstNotNullOf { s ->
                s.tables.firstNotNullOfOrNull { t ->
                    t.fields.firstOrNull { f ->
                        f.qualifiedName == qualifiedName
                    }
                }
            }
        } catch (e: NoSuchElementException) {
            println("Field not found: $qualifiedName")
            throw e
        }
    }
}