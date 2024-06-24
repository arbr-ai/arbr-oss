package com.arbr.backdraft.task.executor

import com.arbr.backdraft.TaskMain
import com.arbr.backdraft.task.resolver.TargetResolver
import com.arbr.backdraft.task.handler.TaskHandler
import com.arbr.backdraft.task.result.TaskWorkResult
import com.arbr.graphql_compiler.core.*
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Paths

abstract class BaseTaskExecutor(
    override val taskHandler: TaskHandler
) : TaskExecutor {

    override fun executeSingleTask(
        commandLineArgs: TaskCommandLineArguments,
    ): TaskWorkResult {
        val logger = LoggerFactory.getLogger(TaskMain::class.java)
        logger.info("Beginning process '${commandLineArgs.name}'...")

        val resolvedBuildDir = Paths.get(commandLineArgs.outputDirString)
        if (!resolvedBuildDir.toFile().exists()) {
            Files.createDirectories(resolvedBuildDir)
        }

        val targetResolver = TargetResolver()
        val target = targetResolver.resolve(commandLineArgs.targetId)
        target.setBuildOutputDir(Paths.get(commandLineArgs.outputDirString))

        return taskHandler.handleBaseTargetTask(target)
    }
}