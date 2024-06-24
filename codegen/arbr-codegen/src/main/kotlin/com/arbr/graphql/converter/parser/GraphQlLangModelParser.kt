package com.arbr.graphql.converter.parser

import com.arbr.graphql.converter.model.*
import com.arbr.graphql.converter.regex.RegexNamedReplacementRule
import com.arbr.graphql.converter.regex.RegexReplacementNumberedRule
import com.arbr.graphql.converter.regex.RegexReplacementRule
import com.arbr.graphql.converter.regex.Regexes
import com.arbr.graphql.converter.util.StringUtils
import com.arbr.graphql.converter.util.topSortWith
import graphql.language.Node
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.reflect.Type
import java.net.URI
import java.net.URLClassLoader
import java.nio.file.Path
import java.nio.file.Paths
import java.util.jar.JarFile
import kotlin.io.path.extension
import kotlin.io.path.name

internal class GraphQlLangModelParser(
    private val outputSourceDir: Path,
) {

    private fun mappedTypeName(fieldValueType: Type, extraRules: List<RegexReplacementRule>): String {
        val originalName = fieldValueType.typeName
        return Regexes.replaceByRules(originalName, extraRules + rules)
    }

    private fun getClassNodeFields(nodeClass: Class<*>): List<NodeBaseField> {
        val methodBackedFields = nodeClass.methods
            .filter {
                it.name.startsWith("get")
                        && it.parameterCount == 0
            }
            .map {
                NodeBaseField(
                    StringUtils.getCaseSuite(it.name.drop(3)).camelCase,
                    StringUtils.getCaseSuite(it.name.drop(3)).camelCase,
//                    "${it.name}()",
                    nodeClass,
                    it.genericReturnType,
                )
            }

        val propertyFields = nodeClass.fields
            .filter { it.modifiers and 8 == 0 }  // Not static
            .map {
                NodeBaseField(
                    StringUtils.getCaseSuite(it.name).camelCase,
                    it.name,
                    nodeClass,
                    it.genericType,
                )
            }

        return (methodBackedFields + propertyFields)
            .filter { it.name !in excludedFields }
            .distinctBy { it.name }
    }

    private fun typeIsNullable(
        nodeBaseField: NodeBaseField,
    ): Boolean {
        val alwaysNonNullFieldTypes = setOf(
            "int", "boolean"
        )
        if (nodeBaseField.fieldValueType.typeName in alwaysNonNullFieldTypes) {
            return false
        }

        val alwaysNonNullFieldNames = setOf(
            "comments", "ignoredChars", "additionalData",
        )
        if (nodeBaseField.name in alwaysNonNullFieldNames) {
            return false
        }

        return true
    }

    private fun mappedClassName(classSimpleName: String): String {
        val simpleName = StringUtils.getCaseSuite(classSimpleName).titleCase
        return "GraphQlLanguageNode$simpleName"
    }

    private fun properSelfModel(
        selfParentModel: MappedNodeClassModel,
    ): MappedNodeClassModel {
        val superclassConstructor = when (selfParentModel.objectKind) {
            MappedKotlinTopLevelObjectSpecifier.SEALED_INTERFACE -> {
                null
            }
            MappedKotlinTopLevelObjectSpecifier.SEALED_CLASS -> {
                MappedNodeSuperclassConstructor(
                    selfParentModel.simpleName,
                    selfParentModel.fieldModels.map { it.name },
                )
            }
            MappedKotlinTopLevelObjectSpecifier.DATA_CLASS,
            MappedKotlinTopLevelObjectSpecifier.DATA_OBJECT -> throw Exception(selfParentModel.simpleName)
        }
        val implements = if (selfParentModel.objectKind == MappedKotlinTopLevelObjectSpecifier.SEALED_INTERFACE) {
            selfParentModel.simpleName
        } else {
            null
        }
        val finalObjectKind = if (selfParentModel.fieldModels.isNotEmpty()) {
            MappedKotlinTopLevelObjectSpecifier.DATA_CLASS
        } else {
            // Impossible by construction?
            MappedKotlinTopLevelObjectSpecifier.DATA_OBJECT
        }

        val properModelClassName = mappedClassName(
            "${selfParentModel.originClass!!.simpleName}Proper"
        )

        // Fields will be identical to parent but all overrides
        val mappedFields = selfParentModel.fieldModels.map { fieldModel ->
            fieldModel.copy(isOverride = true)
        }

        return MappedNodeClassModel(
            properModelClassName,
            selfParentModel.packageName,
            mappedFields,

            superclassConstructor = superclassConstructor,
            listOfNotNull(implements),
            finalObjectKind,

            selfParentModel.originClass,
        )
    }

    private fun mapNodeClassModels(
        mappedClassModels: List<MappedNodeClassModel>,
        conversionRules: List<RegexReplacementRule>,
        nodeClass: NodeBaseClass,
        nodeClassByCanonicalName: Map<String, NodeBaseClass>,
    ): List<MappedNodeClassModel> {
        logger.debug(nodeClass.simpleName)

        val newClassName = mappedClassName(nodeClass.simpleName)
        val packageName = "com.arbr.graphql.lang.node"

        val hasSubNodes = nodeClass.immediateChildren.isNotEmpty()

        val (superclassModel, interfaceModels) = run {
            val superclass = nodeClass.superclass?.let {
                nodeClassByCanonicalName[it]
            }
            val interfaces = nodeClass.implements.map {
                nodeClassByCanonicalName[it]!!
            }
            if (superclass != null) {
                check(mappedClassModels.any { it.originClass == superclass })
            }
            check(
                interfaces.all { intface ->
                    mappedClassModels.any { it.originClass == intface }
                }
            )

            val superClassModel = superclass?.let { superclazz ->
                mappedClassModels.first { it.originClass == superclazz }
            }
            val superclassInterface = if (superClassModel?.objectKind == MappedKotlinTopLevelObjectSpecifier.SEALED_INTERFACE) {
                // Converted to interface
                superClassModel
            } else {
                null
            }
            val superClassUpdatedModel = superClassModel?.takeIf { superclassInterface == null }

            val interfaceModels = listOfNotNull(superClassModel) + interfaces.map { intface ->
                mappedClassModels.first { it.originClass == intface }
            }

            superClassUpdatedModel to interfaceModels
        }

        val superclassFields = superclassModel
            ?.fieldModels
            ?.map { it.name }
            ?.toSet()
            ?: emptySet()

        val interfaceFields = interfaceModels.flatMap { interfaceModel ->
            interfaceModel.fieldModels.map { it.name }.toSet()
        }.toSet()

        // Note assumptions about field name not changed
        val nodeFields = nodeClass.fields.map { it.name }.toSet()
        val overrideFields = superclassFields.plus(interfaceFields)
        val properFields = nodeFields.minus(overrideFields)
        val hasFields = nodeClass.fields.isNotEmpty()

        val mappedFields = nodeClass.fields.map { nodeField ->
            MappedNodeFieldModel(
                nodeField.name,
                mappedTypeName(nodeField.fieldValueType, conversionRules),
                nullable = typeIsNullable(nodeField),
                isOverride = nodeField.name !in properFields,
            )
        }

        fun model(
            specifier: MappedKotlinTopLevelObjectSpecifier,
        ): MappedNodeClassModel {
            val superclassConstructor = superclassModel?.let { superclazzModel ->
                MappedNodeSuperclassConstructor(
                    mappedClassName(superclazzModel.simpleName),
                    superclassModel.fieldModels.map { it.name },
                )
            }

            return MappedNodeClassModel(
                newClassName,
                packageName,
                mappedFields,

                superclassConstructor = superclassConstructor,
                interfaceModels.map { it.simpleName },
                specifier,

                nodeClass,
            )
        }
        fun sealedInterface(): MappedNodeClassModel {
            return model(
                MappedKotlinTopLevelObjectSpecifier.SEALED_INTERFACE,
            )
        }
        fun sealedClass(): MappedNodeClassModel {
            return model(
                MappedKotlinTopLevelObjectSpecifier.SEALED_CLASS,
            )
        }
        fun dataClass(): MappedNodeClassModel {
            return model(
                MappedKotlinTopLevelObjectSpecifier.DATA_CLASS,
            )
        }
        fun dataObject(): MappedNodeClassModel {
            return model(
                MappedKotlinTopLevelObjectSpecifier.DATA_OBJECT,
            )
        }

        return when (nodeClass.objectKind) {
            JavaTopLevelObjectSpecifier.INTERFACE -> {
                check(hasSubNodes) {
                    "${nodeClass.objectKind} with no sub-nodes: ${nodeClass.simpleName}"
                }
                // Sealed Interface
                listOf(
                    sealedInterface(),
                )
            }
            JavaTopLevelObjectSpecifier.ABSTRACT_CLASS -> {
                check(hasSubNodes) {
                    "${nodeClass.objectKind} with no sub-nodes: ${nodeClass.simpleName}"
                }

                // Sealed Interface or Class
                val sealedModel = if (superclassModel == null) {
                    sealedInterface()
                } else {
                    sealedClass()
                }

                listOf(
                    sealedModel,
                )
            }
            JavaTopLevelObjectSpecifier.OPEN_CLASS -> {
                if (hasSubNodes) {
                    // Sealed Interface or Class + Proper Sub-Node
                    val sealedModel = if (superclassModel == null) {
                        sealedInterface()
                    } else {
                        sealedClass()
                    }

                    listOf(
                        sealedModel,
                        properSelfModel(sealedModel),
                    )
                } else {
                    // Data Class or Data Object
                    listOf(
                        if (hasFields) dataClass() else dataObject()
                    )
                }
            }
            JavaTopLevelObjectSpecifier.FINAL_CLASS -> {
                // Data Class or Data Object
                listOf(
                    if (hasFields) dataClass() else dataObject()
                )
            }
        }
    }

    fun parseBaseNodeClassModels(): List<NodeBaseClass> {
        val rootClass = Node::class.java
        val jarClasses = getJarClasses(rootClass)

        val nodeClasses = jarClasses
            .filter { p -> rootClass.isAssignableFrom(p) }
        val classes = nonNodeTargetConversionClasses + nodeClasses

        logger.info("Target classes for conversion:\n")
        classes.forEach { logger.info(it.canonicalName) }

        val classNames = classes.map { it.canonicalName!! }.toSet()

        val nodeToImplementsMap = classes.associate { sc ->
            sc.canonicalName!! to sc.interfaces
                .filterNotNull()
                .map { it.canonicalName!! }
                .filter { it in classNames }
        }

        val nodeToSubNodeMap = classes.flatMap { sc ->
            val superNodes = (sc.interfaces + sc.superclass).filterNotNull().filter { c -> c.canonicalName!! in classNames }

            superNodes.map { sn ->
                sn.canonicalName!! to sc.canonicalName!!
            }
        }.groupBy { it.first }
            .mapValues { (_, subPairs) -> subPairs.map { it.second } }

        return classes
            .distinctBy { it.canonicalName!! }
            .map { nodeClass ->
            val canonicalName = nodeClass.canonicalName!!

            NodeBaseClass(
                canonicalName,
                nodeClass.simpleName,
                getClassNodeFields(nodeClass),
                nodeToImplementsMap[canonicalName] ?: emptyList(),
                nodeClass.superclass?.canonicalName,
                JavaTopLevelObjectSpecifier.ofClass(nodeClass),
                nodeToSubNodeMap[canonicalName] ?: emptyList(),
            )
        }
    }

    fun mapBaseNodeModels(
        classes: List<NodeBaseClass>
    ): List<MappedNodeClassModel> {
        val conversionRules = getClassNameConversionRules(classes)
        val nodeClassByCanonicalName = classes.associateBy { it.canonicalName }

        val sortedClasses = classes.topSortWith(
            compareBy { it.canonicalName },
        ) { nodeBaseClass ->
            // Get dependencies
            (nodeBaseClass.implements + listOfNotNull(nodeBaseClass.superclass))
                .mapNotNull { nodeClassByCanonicalName[it] }
        }

        return sortedClasses.fold(
            mutableListOf<MappedNodeClassModel>()
        ) { mappedClasses, nodeBaseClass ->
            val newModels = mapNodeClassModels(
                mappedClasses,
                conversionRules,
                nodeBaseClass,
                nodeClassByCanonicalName
            )
            mappedClasses.addAll(newModels)
            mappedClasses
        }
    }

    private fun getJarClasses(rootClass: Class<Node<*>>): List<Class<*>> {
        val packageUrl = rootClass.getResource("")!!
        val jarEntries = JarFile(File(URI(packageUrl.file.split("!")[0]).path)).entries().toList()

        val entryNames = jarEntries.mapNotNull { jarEntry ->
            if (jarEntry.isDirectory) {
                null
            } else {
                jarEntry.realName
            }
        }
        val urls = entryNames.map { rootClass.classLoader.getResource(it) }
        val fqcNames = entryNames.mapNotNull {
            val path = Paths.get(it)
            if (path.extension == "class") {
                val pathTokens = path.map { t -> t.name }
                val packageTokens =
                    pathTokens.dropLast(1) + pathTokens.takeLast(1).map { t -> File(t).nameWithoutExtension }
                packageTokens.joinToString(".")
            } else {
                null
            }
        }

        val urlClassLoader = URLClassLoader(urls.toTypedArray())
        val jarClasses = fqcNames.map {
            urlClassLoader.loadClass(it)
        }
        return jarClasses
    }

    private fun getClassNameConversionRules(
        targetConversionClasses: List<NodeBaseClass>,
    ): List<RegexReplacementRule> {
        return targetConversionClasses.map { clazz ->
            val fromName = clazz.canonicalName
            val simpleName = StringUtils.getCaseSuite(clazz.simpleName).titleCase
            val newClassName = "GraphQlLanguageNode$simpleName"

            RegexNamedReplacementRule(
                Regex("(?<prefix>^.*)(?<clsName>${fromName})(?<terminal>[,>\\s]|$)(?<suffix>.*)"),
            ) { _, matchGroupMap ->
                val prefix = matchGroupMap["prefix"]!!.value
                val terminal = matchGroupMap["terminal"]!!.value
                val suffix = matchGroupMap["suffix"]!!.value

                prefix + newClassName + terminal + suffix
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GraphQlLangModelParser::class.java)

        private val excludedFields = setOf(
            "children",
            "namedChildren",
            "class",
            "ignoredChars",
            "argumentsByName",
            "directivesByName",
        )

        private val nonNodeTargetConversionClasses = listOf(
            graphql.language.Comment::class.java,
            graphql.language.SourceLocation::class.java,
            graphql.language.Definition::class.java,
            graphql.language.Description::class.java,
        )

        private val rules = listOf(
            RegexReplacementNumberedRule(
                Regex("(java\\.math\\.BigDecimal)"),
                { "Double" }
            ),
            /**
             * GraphQL Int types are 32-bit integers despite this strange usage of BigInteger
             * See "Specification.graphql"
             */
            RegexReplacementNumberedRule(
                Regex("(java\\.math\\.BigInteger)"),
                { "Int" }
            ),
            RegexReplacementNumberedRule(
                Regex("(\\$)"),
                { "." }
            ),
            RegexReplacementNumberedRule(
                Regex("(java\\.util\\.)Map"),
                { "" }
            ),
            RegexReplacementNumberedRule(
                Regex("(java\\.util\\.)List"),
                { "" }
            ),
            RegexReplacementNumberedRule(
                Regex("(java\\.lang\\.)String"),
                { "" }
            ),
            RegexReplacementNumberedRule(
                Regex("(graphql\\.language\\.Value)"),
                { "com.arbr.graphql.lang.common.GraphQlLanguageNodeValue" }
            ),
            RegexReplacementNumberedRule(
                Regex("(graphql\\.language\\.OperationDefinition\\.Operation)"),
                { "com.arbr.graphql.lang.common.GraphQlOperationValue" }
            ),
            RegexReplacementNumberedRule(
                Regex("graphql\\.language\\.Definition([,>\\s]|$)"),
                { "<*>$it" }
            ),
            RegexReplacementNumberedRule(
                Regex("graphql\\.language\\.Type([,>\\s]|$)"),
                { "<*>$it" }
            ),
            RegexReplacementNumberedRule(
                Regex("graphql\\.language\\.Selection([,>\\s]|$)"),
                { "<*>$it" }
            ),
            RegexNamedReplacementRule(
                Regex("(?<pre>[,<\\s]|^)int(?<terminal>[,>\\s]|$)"),
            ) { _, m ->
                val pre = m["pre"]!!.value
                val terminal = m["terminal"]!!.value
                "${pre}Int$terminal"
            },
            RegexNamedReplacementRule(
                Regex("(?<pre>[,<\\s]|^)boolean(?<terminal>[,>\\s]|$)"),
            ) { _, m ->
                val pre = m["pre"]!!.value
                val terminal = m["terminal"]!!.value
                "${pre}Boolean$terminal"
            },
        )
    }
}
