package com.arbr.codegen.base.generator

import com.arbr.codegen.base.dependencies.MapperConfig
import com.arbr.codegen.base.generator.targets.GeneratorTarget
import com.arbr.codegen.base.generator.targets.GeneratorTargetConfig
import com.arbr.codegen.base.generator.targets.GeneratorTargetOutput
import com.arbr.codegen.base.generator.targets.SealedGeneratorTarget
import org.apache.commons.io.FileUtils
import org.apache.commons.io.file.PathUtils
import org.apache.commons.io.filefilter.FileFileFilter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream
import kotlin.io.path.exists
import kotlin.io.path.moveTo
import kotlin.io.path.relativeTo
import kotlin.io.path.writeText


class ObjectModelGenerator(
    private val didWriteFilesIntoSubpaths: (List<String>) -> Unit,
) {
    private val generatorTargets: List<GeneratorTarget>
        get() {
            val subclassTargets = SealedGeneratorTarget::class.sealedSubclasses
                .map {
                    it.objectInstance
                        ?: throw Exception("Generator target is not an object: ${it.simpleName}")
                }

            val coveredTemplateFiles = subclassTargets.mapNotNull { it.templateFileName }

            val fileTargets = GeneratorTargetConfig.generatorTargets()
                .filter {
                    val templateFileName = it.templateFileName
                    templateFileName != null && templateFileName !in coveredTemplateFiles
                }

            return subclassTargets + fileTargets
        }

    private fun generateInner(
        packageDomain: String,
        databaseModel: DatabaseModel,
        tempGenerationDirSubproject: Path,
    ) {
        val databaseDisplayModel = DisplayModelConverter(
            MapperConfig().mapper
        )
            .databaseDisplayModel(packageDomain, databaseModel)

        fun writeOutput(output: GeneratorTargetOutput) {
            val writePath = Paths.get(tempGenerationDirSubproject.toString(), output.subPath)

            // Create sequence of dirs if necessary
            val containingDir = writePath.normalize().parent
            Files.createDirectories(containingDir)

            writePath.writeText(output.content)
        }

        generatorTargets.forEach { target ->
            val subpaths = target.generate(databaseDisplayModel).map { output ->
                writeOutput(output)
                output.subPath
            }
            didWriteFilesIntoSubpaths(subpaths.toList())
        }
    }

    private fun listFilesRecursively(
        dirPath: Path,
    ): Stream<Path> {
        val pathFilter = FileFileFilter.INSTANCE
        val maxDepth = Int.MAX_VALUE

        return PathUtils.walk(dirPath, pathFilter, maxDepth, false)
    }

    private fun moveFilesRecursively(
        sourceDirPath: Path,
        targetDirPath: Path,
    ) {
        listFilesRecursively(sourceDirPath)
            .forEach { path ->
                val subpath = path.relativeTo(sourceDirPath).toString()
                val newPath = Paths.get(targetDirPath.toString(), subpath)

                // Create sequence of dirs if necessary
                val containingDir = newPath.normalize().parent
                Files.createDirectories(containingDir)

                path.moveTo(newPath, overwrite = true)
            }
    }

    private fun moveToTargetDirs(
        tempGenerationDirSubproject: Path,
        targetGenerationDirSubproject: Path,
    ) {
        moveFilesRecursively(tempGenerationDirSubproject, targetGenerationDirSubproject)
    }

    fun generate(
        packageDomain: String,
        databaseModel: DatabaseModel,
        subprojectDir: Path,
    ) {
        val tmpDirSubproject = Files.createTempDirectory("ogcp")

        try {
            generateInner(packageDomain, databaseModel, tmpDirSubproject)
            moveToTargetDirs(tmpDirSubproject, subprojectDir)
        } catch (e: Exception) {
            throw e
        } finally {
            if (tmpDirSubproject.exists()) {
                FileUtils.deleteDirectory(tmpDirSubproject.toFile())
            }
        }
    }
}
