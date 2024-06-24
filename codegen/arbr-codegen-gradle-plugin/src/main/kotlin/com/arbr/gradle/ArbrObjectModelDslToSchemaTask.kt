package com.arbr.gradle

import com.arbr.codegen.base.dependencies.MapperConfig
import com.arbr.codegen.base.inputs.ArbrPlainDataType
import com.arbr.codegen.config.ArbrSchemaTaskDefinitionConfigModel
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.file.FileCollectionFactory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.process.ExecOperations
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject
import kotlin.io.path.exists

/**
 * Task representing the first step in object model codegen:
 *   Generating GraphQL schema from Gradle DSL
 *
 * "CONVERTER"
 */
abstract class ArbrObjectModelDslToSchemaTask @Inject constructor(
    objects: ObjectFactory,
    fileCollectionFactory: FileCollectionFactory,
    override val execOperations: ExecOperations,
    project: Project,
) : DefaultTask(), ArbrBaseTask {
    private val mapper = MapperConfig().mapper

    override val mainClass = objects.constantProvider(DEFAULT_MAIN_CLASS)
    override val workingDir = objects.constantProvider(project.layout.projectDirectory.asFile)
    override val outputBaseDir: DirectoryProperty = objects.directoryProperty()
    override val runtimeClasspath: ConfigurableFileCollection = fileCollectionFactory
        .configurableFiles("runtimeClasspath")
    override val jvmArgListProperty: ListProperty<String> = objects
        .listProperty(String::class.java)
        .value(getJvmArgs())
    override val argListProperty: ListProperty<String> = objects
        .listProperty(String::class.java)
        .value(getArgs())

    @get:Input
    val domain: Property<String> = objects
        .property(String::class.java)
        .convention("arbr")

    private val plainDataTypes: ListProperty<ArbrPlainDataType> = objects
        .listProperty(ArbrPlainDataType::class.java)

    override fun getGroup(): String? {
        return "arbr"
    }

    fun setDataTypeInputs(
        dataTypes: List<ArbrDataType>
    ) {
        val convertedDataTypes = dataTypes.map(ArbrDataTypePlainConverter::convertDataType)
        plainDataTypes.set(convertedDataTypes)
    }

    private fun writeInputConfigFile(buildDirPath: Path): File {
        val inputFile = Paths.get(
            buildDirPath.toString(),
            "arbr-object-model-input-${name}.json"
        ).toFile()
        inputFile.writeText(
            mapper.writeValueAsString(plainDataTypes.get())
        )

        return inputFile
    }

    private fun getConfigModel(
        inputConfigFile: File
    ): ArbrSchemaTaskDefinitionConfigModel {
        return ArbrSchemaTaskDefinitionConfigModel(
            this.name,
            0,
            listOf(inputConfigFile.path),
            outputBaseDir.get().asFile.path,
            OBJECT_MODEL_CONVERTER_TARGET,
        )
    }

    private fun getJvmArgs(): Provider<List<String>> {
        return project.layout.buildDirectory.map {
            listOf(
                "-Dlogback.configurationFile=logback.xml",
            )
        }
    }

    private fun getArgs(): Provider<List<String>> {
        return project.layout.buildDirectory.map { buildDir ->
            val buildDirPath = buildDir.asFile.toPath()
            if (!buildDirPath.exists()) {
                Files.createDirectories(buildDirPath)
            }

            val configFile = Paths.get(
                buildDirPath.toString(),
                "arbr-config-${name}.json"
            ).toFile()
            val inputConfigFile = writeInputConfigFile(buildDirPath)
            val configModel = getConfigModel(inputConfigFile)

            configFile.writeText(
                mapper.writeValueAsString(configModel)
            )

            listOf(
                "-c", configFile.path,
            )
        }
    }

    companion object {
        private const val DEFAULT_MAIN_CLASS = "com.arbr.backdraft.TaskMainKt"
        private const val OBJECT_MODEL_CONVERTER_TARGET = "com.arbr.codegen.target.converter.dsl.ArbrObjectModelConverter"
    }
}
