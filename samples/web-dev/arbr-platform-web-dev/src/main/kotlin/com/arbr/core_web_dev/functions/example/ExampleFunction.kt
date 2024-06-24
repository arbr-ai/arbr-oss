package com.arbr.core_web_dev.functions.example

import com.arbr.core_web_dev.functions.into
import com.arbr.object_model.core.resource.ArbrTask
import com.arbr.object_model.core.view.ArbrFileView
import com.arbr.object_model.core.view.ArbrTaskView
import com.arbr.og.object_model.common.functions.api.ResourceFunction

object ExampleFunction : ResourceFunction({
    val fileTarget by actingOnResource(ArbrFileView::class.java) {
        val m0 by mutating { file ->
            file.summary.nonnull()

            val project = file.parent
            project.tasks += file.subtaskRelevantFiles.map {
                new<ArbrTaskView> {
                    taskVerbosePlan = it.parent.subtask.into(ArbrTask.TaskVerbosePlan)
                }
            }
        }
    }
})
