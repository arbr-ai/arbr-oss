package com.arbr.gradle

import com.arbr.graphql_compiler.util.StringUtils
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.internal.jvm.ClassDirectoryBinaryNamingScheme
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class ArbrExtension @Inject constructor(
    private val project: Project,
    objects: ObjectFactory,
) {
    private var rootDataType: ArbrDataType? = null

    @get:Suppress("unused")
    val version: Property<String> = objects.property(String::class.java)
        .also {
            it.finalizeValueOnRead()
            it.convention(DEFAULT_ARBR_VERSION)
        }

    @get:Suppress("unused")
    val domain: Property<String> = objects.property(String::class.java)
        .also {
            it.finalizeValueOnRead()
        }

    @get:Suppress("unused")
    val compileSchema: NamedDomainObjectContainer<ArbrCompileSchemaTask> = objects.domainObjectContainer(
        ArbrCompileSchemaTask::class.java
    ) { name ->
        val verb = "generate"
        val baseName = if (name.startsWith(verb, ignoreCase = true)) name.drop(verb.length) else name
        val titleCase = StringUtils.getCaseSuite(baseName).titleCase
        val namingScheme = ClassDirectoryBinaryNamingScheme("main")
        val taskNameWrapped = namingScheme.getTaskName(verb, titleCase)

        project.tasks.create(taskNameWrapped, ArbrCompileSchemaTask::class.java)
    }

    @get:Suppress("unused")
    val dataTypes: NamedDomainObjectContainer<ArbrDataType> = objects.domainObjectContainer(
        ArbrDataType::class.java
    ) { name ->
        objects.newInstance(ArbrDataType::class.java, name)
            .also { newDataType ->
                newDataType.parent.convention(rootDataType)
            }
    }.also { dataTypeContainer ->
        val newRootDataType = dataTypeContainer.create(ROOT_DATA_TYPE_NAME)
        newRootDataType.parent.set(newRootDataType)
        rootDataType = newRootDataType
    }

    companion object {
        const val DEFAULT_ARBR_GROUP = "com.arbr"
        private const val DEFAULT_ARBR_VERSION = "1.0"

        const val ROOT_DATA_TYPE_NAME = "root"
    }
}
