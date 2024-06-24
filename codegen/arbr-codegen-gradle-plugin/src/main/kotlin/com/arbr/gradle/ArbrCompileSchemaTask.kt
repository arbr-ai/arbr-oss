package com.arbr.gradle

import com.arbr.codegen.base.dependencies.MapperConfig
import com.arbr.codegen.config.ArbrSchemaTaskDefinitionConfigModel
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.FileCollectionFactory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet
import org.gradle.internal.Factory
import org.gradle.process.ExecOperations
import java.nio.file.Files
import java.nio.file.Paths
import javax.inject.Inject
import kotlin.io.path.exists

inline fun <reified T : Any> ObjectFactory.constantProvider(value: T): Provider<T> {
    return this.property(T::class.java)
        .also {
            it.set(value)
        }
        .map { it }
}

abstract class ArbrCompileSchemaTask @Inject constructor(
    objects: ObjectFactory,
    fileCollectionFactory: FileCollectionFactory,
    override val execOperations: ExecOperations,
    patternSetFactory: Factory<PatternSet>,
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

    @InputFiles
    val arbrSourceSet: Property<SourceDirectorySet> = objects
        .property(SourceDirectorySet::class.java)

    private val sourcePatternSet: Property<PatternFilterable> = objects
        .property(PatternFilterable::class.java)
        .value(patternSetFactory.create())

    @get:Input
    abstract val target: Property<String>

    @InputFiles
    val inputFiles: Provider<FileCollection> = arbrSourceSet.flatMap { inputSourceSet ->
        sourcePatternSet.map {
            inputSourceSet.matching(it)
        }
    }

    override fun getGroup(): String? {
        return "arbr"
    }

    fun filter(spec: (PatternFilterable) -> PatternFilterable) {
        sourcePatternSet.set(spec(sourcePatternSet.get()))
    }

    private fun getConfigModel(): ArbrSchemaTaskDefinitionConfigModel {
        return ArbrSchemaTaskDefinitionConfigModel(
            this.name,
            0,
            inputFiles.get().map { it.path },
            outputBaseDir.get().asFile.path,
            target.get(),
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
                buildDir.asFile.toPath().toString(),
                "arbr-config-${name}.json"
            ).toFile()
            val configModel = getConfigModel()

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

        val defaultIncludesPatterns = listOf(
            "**/*.json",
            "**/*.yaml",
            "**/*.yml",
            "**/*.graphql",
            "**/*.graphqls",
            "**/*.mustache",
            "**/*.g4",
            "**/*.kttmpl",
        )
    }
}
