@file:Suppress("DEPRECATION")

package com.arbr.gradle

import com.arbr.graphql_compiler.util.StringUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyResolveDetails
import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.sources.DefaultKotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*
import javax.inject.Inject

class ArbrPlugin @Inject constructor(
    private val providers: ProviderFactory,
    private val objects: ObjectFactory,
    private val fileSystemOperations: FileSystemOperations,
) : Plugin<Project> {

    private fun sanitizePackageComponent(projectName: String): String {
        // Simple allowable replacements; violations will be checked before generation
        return projectName.lowercase().replace("-", "_").replace(" ", "_")
    }

    override fun apply(project: Project) {
        val extension = project.extensions.create("arbr", ArbrExtension::class.java)
        createArbrGeneratorRuntimeConfiguration(
            fileSystemOperations,
            extension,
            objects,
            project,
        )

        enforceArbrVersion(project, extension)
    }

    companion object {
        private const val OBJECT_MODEL_SOURCE_TARGET =
            "com.arbr.codegen.target.compiler.om.GraphQlSchemaToObjectModelCompiler"
        private const val SCHEMA_TASK_NAME = "generateObjectModelSchema"
        private const val SOURCE_TASK_NAME = "generateObjectModelSource"

        private fun createArbrSourceDirectorySet(
            parentDisplayName: String,
            objectFactory: ObjectFactory
        ): SourceDirectorySet {
            val name = "$parentDisplayName.arbr"
            val displayName = "$parentDisplayName Arbr schema source"

            val arbrSourceSet: SourceDirectorySet = objectFactory.sourceDirectorySet(name, displayName)
            ArbrCompileSchemaTask.defaultIncludesPatterns.forEach { patternString ->
                arbrSourceSet.filter.include(patternString)
            }

            return arbrSourceSet
        }

        private fun getBuildOutputDirForSourceSet(
            project: Project,
            sourceSetName: String
        ): Provider<Directory> {
            return project.layout.buildDirectory.dir("generated-src/arbr/$sourceSetName")
        }

        private fun getBuildOutputSubdirForTask(
            project: Project,
            sourceSetName: String,
            taskName: String,
        ): Provider<Directory> {
            val definitionCaseSuite = StringUtils.getCaseSuite(taskName)
            val outputSubDir = definitionCaseSuite.snakeCase
            return getBuildOutputDirForSourceSet(project, sourceSetName)
                .map { outDir ->
                    outDir.dir(outputSubDir)
                }
        }

        private fun configureTasksForDataType(
            project: Project,
            arbrDataType: ArbrDataType,
            arbrConfiguration: Configuration,
            sourceSetName: String,
            extension: ArbrExtension
        ) {
            val existingTask = project.tasks.findByName(SCHEMA_TASK_NAME)

            if (arbrDataType.name.contentEquals("root", ignoreCase = true)) {
                // Don't trigger task creation from a root type alone
            } else if (existingTask == null) {
                val gen0 = project.tasks.create(
                    SCHEMA_TASK_NAME,
                    ArbrObjectModelDslToSchemaTask::class.java
                ) { dslTask ->
                    dslTask.runtimeClasspath.setFrom(arbrConfiguration)
                    dslTask.description = "Processes the $sourceSetName Arbr schema"

                    val taskOutputDir = getBuildOutputSubdirForTask(
                        project, sourceSetName, dslTask.name
                    )
                    dslTask.outputBaseDir.set(taskOutputDir)

                    dslTask.setDataTypeInputs(extension.dataTypes.toList())
                }

                project.tasks.create(
                    SOURCE_TASK_NAME,
                    ArbrCompileSchemaTask::class.java
                ) { dslTask ->
                    dslTask.dependsOn(gen0)
                    dslTask.filter {
                        it
                            .include("**/*.graphql")
                            .include("**/*.graphqls")
                    }
                    dslTask.target.set(
                        OBJECT_MODEL_SOURCE_TARGET
                    )
                }
            } else if (existingTask is ArbrObjectModelDslToSchemaTask) {
                existingTask.setDataTypeInputs(extension.dataTypes.toList())
            }
        }

        private fun configureKotlinMainSourceSet(
            ktSourceSet: KotlinSourceSet,
            project: Project,
            extension: ArbrExtension,
            arbrConfiguration: Configuration,
            objectFactory: ObjectFactory
        ) {
            (ktSourceSet as DefaultKotlinSourceSet)

            val sourceSetName = ktSourceSet.name

            extension.dataTypes.configureEach { arbrDataType ->
                configureTasksForDataType(
                    project,
                    arbrDataType,
                    arbrConfiguration,
                    sourceSetName,
                    extension
                )
            }

            val displayName = ktSourceSet.displayName
            val arbrSourceSet = createArbrSourceDirectorySet(displayName, objectFactory)
            val srcDir = "src/${sourceSetName}/arbr"
            arbrSourceSet.srcDir(srcDir)
            ktSourceSet.kotlin.source(arbrSourceSet)
            ktSourceSet.resources.source(arbrSourceSet)

            project.tasks.withType(ArbrCompileSchemaTask::class.java) { arbrTask ->
                arbrTask.description = "Processes the $sourceSetName Arbr schema"
                arbrTask.runtimeClasspath.setFrom(arbrConfiguration)
                arbrTask.arbrSourceSet.set(arbrSourceSet)

                val taskOutputDir = getBuildOutputSubdirForTask(
                    project, sourceSetName, arbrTask.name
                )
                arbrTask.outputBaseDir.set(taskOutputDir)

                ktSourceSet.kotlin.srcDir(taskOutputDir)

                project.tasks.withType(KotlinCompile::class.java) { ktCompileTask ->
                    ktCompileTask.dependsOn(arbrTask)
                }
            }
        }

        private fun createArbrGeneratorRuntimeConfiguration(
            fileSystemOperations: FileSystemOperations,
            extension: ArbrExtension,
            objectFactory: ObjectFactory,
            project: Project,
        ): Configuration? {
            val arbrConfiguration = project.configurations.create("arbr")
            arbrConfiguration.description =
                "The classpath used to invoke the Arbr code generator."
            project.dependencies.add(arbrConfiguration.name, "com.arbr:arbr-codegen")

            project.extensions.findByType(KotlinProjectExtension::class.java)
                ?.sourceSets
                ?.configureEach { ktSourceSet ->
                    if (ktSourceSet.name == "main") {
                        createRootGenerateTask(
                            fileSystemOperations,
                            project
                        )
                        configureKotlinMainSourceSet(
                            ktSourceSet,
                            project,
                            extension,
                            arbrConfiguration,
                            objectFactory,
                        )
                    }
                }

            return arbrConfiguration
        }

        private fun createRootGenerateTask(
            fileSystemOperations: FileSystemOperations,
            project: Project
        ): Task {
            val rootArbrTask = project.tasks.maybeCreate("generateMain")
            rootArbrTask.group = "arbr"

            val outDirMain = getBuildOutputDirForSourceSet(project, "main")

            project.tasks.withType(ArbrCompileSchemaTask::class.java) { arbrTask ->
                rootArbrTask.dependsOn(arbrTask)

                arbrTask.doFirst {
                    fileSystemOperations.delete { spec ->
                        spec.delete(outDirMain)
                    }
                }
            }
            project.tasks.withType(ArbrObjectModelDslToSchemaTask::class.java) { dslTask ->
                rootArbrTask.dependsOn(dslTask)

                dslTask.doFirst {
                    fileSystemOperations.delete { spec ->
                        spec.delete(outDirMain)
                    }
                }
            }

            return rootArbrTask
        }

        private fun enforceArbrVersion(project: Project, arbrExtension: ArbrExtension) {
            val group = ArbrExtension.DEFAULT_ARBR_GROUP
            val version = arbrExtension.version.get()
            project.configurations.configureEach { configuration: Configuration ->
                configuration.resolutionStrategy.eachDependency { details: DependencyResolveDetails ->
                    val requested = details.requested
                    if (requested.group == group && requested.name.startsWith("arbr")) {

                        details.useTarget(group + ":" + requested.name + ":" + version)
                    }
                }
            }
        }
    }
}
