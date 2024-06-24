package com.arbr.gradle

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.CommandLineArgumentProvider
import org.gradle.process.ExecOperations
import org.gradle.process.ExecResult
import org.gradle.process.JavaExecSpec
import java.io.File
import javax.inject.Inject

interface ArbrBaseTask {
    @get:Input
    val mainClass: Provider<String>

    @get:Input
    val workingDir: Provider<File>

    @get:InputFiles
    val runtimeClasspath: ConfigurableFileCollection

    @get:Inject
    val execOperations: ExecOperations

    @get:OutputDirectory
    val outputBaseDir: DirectoryProperty

    @get:Input
    val jvmArgListProperty: ListProperty<String>

    @get:Input
    val argListProperty: ListProperty<String>

    private fun executeArbr(
        mainClass: String,
        workingDir: File,
        jvmArgProvider: CommandLineArgumentProvider,
        argProvider: CommandLineArgumentProvider,
    ): ExecResult {
        return execOperations.javaexec { spec: JavaExecSpec ->
            spec.mainClass.set(mainClass)
            spec.classpath = runtimeClasspath
            spec.workingDir = workingDir
            spec.jvmArgumentProviders.add(jvmArgProvider)
            spec.argumentProviders.add(argProvider)
        }
    }

    @TaskAction
    fun execute() {
        executeArbr(
            mainClass.get(),
            workingDir.get(),
            jvmArgListProperty::get,
            argListProperty::get,
        )
    }

    companion object {
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
