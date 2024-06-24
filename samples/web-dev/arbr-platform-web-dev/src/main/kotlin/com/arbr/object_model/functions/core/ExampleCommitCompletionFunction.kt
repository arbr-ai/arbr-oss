package com.arbr.object_model.functions.core

import com.arbr.object_model.core.view.ArbrFileOpView
import com.arbr.object_model.core.view.ArbrFileView
import com.arbr.og.object_model.common.functions.api.ResourceFunction
import com.arbr.og.object_model.common.functions.spec.base.HeterogeneousGraphObjectQuerySpec
import com.arbr.og.object_model.common.functions.spec.base.inputting
import com.arbr.og.object_model.common.functions.spec.base.returning
import com.arbr.og.object_model.common.values.SourcedValue

class ExampleCommitCompletionFunction : ResourceFunction({
    val fileOpTarget by actingOnResource(ArbrFileOpView::class.java) {
        val embeddingSearch by embedding {
            val config by configuring {
                model = "embedding_model_name"
                similarityThreshold = 0.8
                maxResults = 10
                // ... Set other configuration properties ...
            }

            val query by querying(ArbrFileView::class.java) {
                val inputQuerySpec = view(ArbrFileOpView::class.java)
                    .map { fileOpView ->
                        // Ported directly from legacy impl: search on task query as a string
                        element(
                            fileOpView.parent.parent.parent.taskQuery as SourcedValue<String>
                        )
                    }
                    .querySpec()

                val candidateQuerySpec = view(ArbrFileView::class.java)
                    .map { fileView ->
                        val fileContent = fileView.content.nonnull()
                        val stringBody = fileView.filePath.zip(fileContent).mapValue { (filePath, content) ->
                            "File: $filePath\nContent:\n$content"
                        }

                        element(stringBody)
                    }
                    .querySpec()

                HeterogeneousGraphObjectQuerySpec(inputQuerySpec, candidateQuerySpec)
            }

            query
        }

        val evaluateCommitCompletion by completion {
            val input by inputting { fileOpView ->
                struct(
                    element(fileOpView.implementedFile.filePath),
                    struct(
                        element(fileOpView.description)
                    )
                )
            }

            complete {
                // Configure the completion using the completionConfig object
                // Assume we have a CompletionConfig class with necessary properties and methods
                model = "completion_model_name"
                temperature = 0.7
                maxTokens = 100
                // Set other configuration properties as needed
            }

            returning { fileOpView ->
                element(
                    fileOpView.description,
                )
            }
        }

        val mutator by mutating { fileOp ->
            val commit = fileOp.parent
            val subtaskResource = commit.parent
            val task = subtaskResource.parent
            val project = task.parent

            val projectFullName = project.fullName
            val listenResource = fileOp
            val taskQuery = task.taskQuery
            val projectFiles = project.files

            val fileOpsContainer = commit.fileOps
            val existingCommitEvalsContainer = commit.commitEvals

            val commitMessage = commit.commitMessage
            val diffSummary = commit.diffSummary
            val subtask = subtaskResource.subtask

            val commitRelevantFiles = commit.commitRelevantFiles

            val relevantFilesWithContents = commitRelevantFiles.map { commitRelevantFileView ->
                val commitRelevantFile = commitRelevantFileView.file
                projectFiles.find(commitRelevantFile)
            }
                .filter { file ->
                    fileOpsContainer.filter { updatedFileOp ->
                        updatedFileOp.implementedFile.filePath.valueEquals(file.filePath)
                    }.isEmpty()
                }

//            val result = embeddingSearch()
        }
    }
})
