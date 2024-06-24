package com.arbr.core_web_dev.workflow.input.model

import com.arbr.core_web_dev.workflow.input.base.TaskInfoBearer
import com.arbr.object_model.core.resource.ArbrProject
import com.arbr.object_model.core.resource.ArbrTask
import com.arbr.og.object_model.common.values.SourcedValue
import com.arbr.og.object_model.common.values.SourcedValueGeneratorInfo
import com.arbr.og.object_model.common.values.SourcedValueKind
import com.fasterxml.jackson.annotation.JsonIgnore

data class ProjectStarter(
    val organizationName: SourcedValue<String>,
    val projectShortName: SourcedValue<String>,
    val projectQuery: SourcedValue<String>,
) : TaskInfoBearer {

    @JsonIgnore
    val projectFullName =
        ArbrProject.FullName.initialize(
            listOf(organizationName.kind, projectShortName.kind).max(),
            "${organizationName.value}/${projectShortName.value}",
            SourcedValueGeneratorInfo((organizationName.generatorInfo.generators + projectShortName.generatorInfo.generators).distinctBy { it.completionCacheKey })
        )

    @JsonIgnore
    override fun getTaskInfo(): IterationTaskContainer = IterationTaskContainer(
        projectFullName,
        ArbrProject.Platform.initialize(
            SourcedValueKind.CONSTANT,
            "Web",
            SourcedValueGeneratorInfo(emptyList()),
        ), // TODO: Get from platform choice
        ArbrProject.Description.initialize(
            projectQuery.kind,
            projectQuery.value,
            projectQuery.generatorInfo,
        ),
        ArbrTask.TaskQuery.initialize(
            projectQuery.kind,
            projectQuery.value,
            projectQuery.generatorInfo,
        ),
        baseBranch = null, // Default
    )

}
