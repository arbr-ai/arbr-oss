package com.arbr.gradle

import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider

data class ArbrCompileSchemaTaskProvidedConfig(
    val sourceFiles: Provider<FileCollection>,
    val outputBaseDir: Provider<Directory>,
    val target: Provider<String>,
)