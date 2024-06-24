package com.arbr.graphql.converter.model

import java.lang.reflect.Modifier

enum class JavaTopLevelObjectSpecifier {
    INTERFACE,
    ABSTRACT_CLASS,
    OPEN_CLASS,
    FINAL_CLASS;

    companion object {
        fun ofClass(clazz: Class<*>): JavaTopLevelObjectSpecifier {
            if (clazz.isEnum) {
                throw Exception(clazz.canonicalName)
            }

            return when {
                clazz.isInterface -> INTERFACE
                Modifier.isAbstract(clazz.modifiers) -> ABSTRACT_CLASS
                Modifier.isFinal(clazz.modifiers) -> FINAL_CLASS
                else -> OPEN_CLASS
            }
        }
    }
}

internal data class NodeBaseClass(
    val canonicalName: String,
    val simpleName: String,
    val fields: List<NodeBaseField>,
    val implements: List<String>,
    val superclass: String?,
    val objectKind: JavaTopLevelObjectSpecifier,
    val immediateChildren: List<String>,
)
