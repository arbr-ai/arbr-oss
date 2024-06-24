package com.arbr.graphql.converter.model

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Object modifier sets which we map to, in practice, from Java object definitions.
 */
enum class MappedKotlinTopLevelObjectSpecifier {
    /**
     * Anything with enumerable subtypes and no superclass.
     */
    SEALED_INTERFACE,

    /**
     * Anything with enumerable subtypes and a superclass.
     */
    SEALED_CLASS,

    /**
     * Anything with no subtypes and a non-zero number of fields.
     */
    DATA_CLASS,

    /**
     * Anything with no subtypes and no fields.
     */
    DATA_OBJECT;

    fun getModifierString(): String {
        return when (this) {
            SEALED_INTERFACE -> "sealed interface"
            SEALED_CLASS -> "sealed class"
            DATA_CLASS -> "data class"
            DATA_OBJECT -> "data object"
        }
    }

    fun isInstantiable(): Boolean {
        return when (this) {
            SEALED_INTERFACE,
            SEALED_CLASS -> false
            DATA_CLASS,
            DATA_OBJECT -> true
        }
    }

    fun usesConstructor(): Boolean {
        return when (this) {
            SEALED_INTERFACE,
            DATA_OBJECT -> false

            SEALED_CLASS,
            DATA_CLASS -> true
        }
    }
}

internal data class MappedNodeSuperclassConstructor(
    /**
     * Simple constructor class name for constructor invocation
     */
    val constructorName: String,
    val arguments: List<String>,
)

internal data class MappedNodeClassModel(
    val simpleName: String,
    val packageName: String,
    val fieldModels: List<MappedNodeFieldModel>,

    val superclassConstructor: MappedNodeSuperclassConstructor?,
    val implements: List<String>,
    val objectKind: MappedKotlinTopLevelObjectSpecifier,

    /**
     * Class of origin - not unique!
     */
    val originClass: NodeBaseClass,
) {

    /**
     * Get the target dir path relative to a source set path such as .../src/main/kotlin/
     */
    fun getTargetDir(
        relativeToSourceSet: Path,
    ): Path {
        return Paths.get(
            relativeToSourceSet.toString(),
            *packageName.split(".").toTypedArray()
        )
    }

    /**
     * Get the target file path relative to a source set path such as .../src/main/kotlin/
     */
    fun getTargetFilePath(
        relativeToSourceSet: Path,
    ): Path {
        return Paths.get(
            getTargetDir(relativeToSourceSet).toString(),
            "$simpleName.kt"
        )
    }
}