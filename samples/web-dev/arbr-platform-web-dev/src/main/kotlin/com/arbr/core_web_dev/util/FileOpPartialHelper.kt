package com.arbr.core_web_dev.util

import com.arbr.core_web_dev.workflow.model.FileOperation
import com.arbr.object_model.core.partial.PartialCommit
import com.arbr.object_model.core.partial.PartialCommitEval
import com.arbr.object_model.core.partial.PartialFile
import com.arbr.object_model.core.partial.PartialFileOp
import com.arbr.object_model.core.resource.ArbrCommitEval
import com.arbr.object_model.core.resource.ArbrFile
import com.arbr.object_model.core.resource.ArbrFileOp
import com.arbr.object_model.core.resource.field.ArbrFileContentValue
import com.arbr.object_model.core.resource.field.ArbrFileFilePathValue
import com.arbr.object_model.core.resource.field.ArbrFileOpDescriptionValue
import com.arbr.object_model.core.resource.field.ArbrFileOpFileOperationValue
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.platform.object_graph.impl.PartialRef
import org.slf4j.LoggerFactory
import java.util.*

object FileOpPartialHelper {
    private val logger = LoggerFactory.getLogger(FileOpPartialHelper::class.java)

    fun fileOpPartial(
        partialObjectGraph: PartialObjectGraph<*, *, ArbrForeignKey>,
        commit: PartialCommit,
        file: ArbrFile,
        fileOperation: ArbrFileOpFileOperationValue,
        description: ArbrFileOpDescriptionValue?,
        commitEval: PartialRef<out ArbrCommitEval, PartialCommitEval>?,
    ): PartialFileOp? {
        return fileOpPartial(
            partialObjectGraph,
            commit,
            file.uuid,
            file.filePath.getLatestAcceptedValue(),
            file.content.getLatestAcceptedValue(),
            fileOperation,
            description,
            commitEval,
        )
    }

    fun fileOpPartial(
        partialObjectGraph: PartialObjectGraph<*, *, ArbrForeignKey>,
        commit: PartialCommit,
        file: PartialFile,
        fileOperation: ArbrFileOpFileOperationValue,
        description: ArbrFileOpDescriptionValue?,
        commitEval: PartialRef<out ArbrCommitEval, PartialCommitEval>?,
    ): PartialFileOp? {
        return fileOpPartial(
            partialObjectGraph,
            commit,
            file.uuid,
            file.filePath,
            file.content,
            fileOperation,
            description,
            commitEval,
        )
    }

    private fun commitFilePartialInner(
        partialObjectGraph: PartialObjectGraph<*, *, ArbrForeignKey>,
        commit: PartialCommit,
        fileUuid: String,
        fileOperation: ArbrFileOpFileOperationValue,
        description: ArbrFileOpDescriptionValue?,
        // Callers set PartialRef(null)
        commitEval: PartialRef<out ArbrCommitEval, PartialCommitEval>?,
    ): PartialFileOp {
        return PartialFileOp(
            partialObjectGraph,
            UUID.randomUUID().toString(),
        ).apply {
            this.parent = PartialRef(commit.uuid)
            targetFile = PartialRef(fileUuid)
            baseFileContent = null
            this.fileOperation = fileOperation
            implementedFile = null
            this.description = description
            this.commitEval = commitEval
        }
    }

    fun fileOpPartial(
        partialObjectGraph: PartialObjectGraph<*, *, ArbrForeignKey>,
        commit: PartialCommit,
        fileUuid: String,
        filePath: ArbrFileFilePathValue?,
        fileContent: ArbrFileContentValue?,
        fileOperation: ArbrFileOpFileOperationValue,
        description: ArbrFileOpDescriptionValue?,
        commitEval: PartialRef<out ArbrCommitEval, PartialCommitEval>?,
    ): PartialFileOp? {
        val innerFileOpValue = fileOperation.value?.lowercase() ?: ""
        val fileOp = when (FileOperation.parseLine(innerFileOpValue)) {
            FileOperation.EDIT_FILE -> {
                if (fileContent == null) {
                    logger.warn("File to edit (${filePath?.value}) has no content, overriding to create")
                    ArbrFileOp.FileOperation.initialize(
                        fileOperation.kind,
                        "create_file",
                        fileOperation.generatorInfo,
                    )
                } else {
                    ArbrFileOp.FileOperation.initialize(
                        fileOperation.kind,
                        "edit_file",
                        fileOperation.generatorInfo,
                    )
                }
            }

            FileOperation.CREATE_FILE -> {
                if (fileContent == null) {
                    ArbrFileOp.FileOperation.initialize(
                        fileOperation.kind,
                        "create_file",
                        fileOperation.generatorInfo,
                    )
                } else {
                    logger.warn("File to create (${filePath?.value}) already exists, overriding to edit")
                    ArbrFileOp.FileOperation.initialize(
                        fileOperation.kind,
                        "edit_file",
                        fileOperation.generatorInfo,
                    )
                }
            }

            FileOperation.DELETE_FILE -> fileOperation
            null -> {
                logger.warn("Uninterpretable file op value (${innerFileOpValue}) - dropping")
                null
            }
        } ?: return null

        return commitFilePartialInner(
            partialObjectGraph,
            commit,
            fileUuid,
            fileOp,
            description,
            commitEval,
        )
    }
}