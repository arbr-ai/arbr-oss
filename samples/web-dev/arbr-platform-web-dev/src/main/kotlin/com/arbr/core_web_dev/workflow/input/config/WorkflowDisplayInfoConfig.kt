package com.arbr.core_web_dev.workflow.input.config

import com.arbr.api.user_project.core.WorkflowDisplayInfo
import com.arbr.api.workflow.core.WorkflowType
import com.arbr.core_web_dev.workflow.input.model.WorkflowInputBaseSchema
import com.arbr.core_web_dev.workflow.input.model.WorkflowInputFeatureQuerySchema
import com.arbr.core_web_dev.workflow.input.model.WorkflowInputProjectStarterSchema
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class WorkflowDisplayInfoConfig {

    @Bean
    fun displayInfoMigrateRedux() = WorkflowDisplayInfo(
        WorkflowType.MIGRATE_REDUX.serializedName,
        title = "Migrate to Redux",
        description = "Migrate the project to Redux.",
        estimatedDuration = Duration.ofMinutes(60L),
        showOnProjectPage = true,
        workflowInputSchema = WorkflowInputBaseSchema,
    )

    @Bean
    fun displayInfoMultiFeature() = WorkflowDisplayInfo(
        WorkflowType.MULTI_FEATURE.serializedName,
        title = "Implement Feature",
        description = "Implement a multi-part feature within the project.",
        estimatedDuration = Duration.ofMinutes(40L),
        showOnProjectPage = true,
        workflowInputSchema = WorkflowInputFeatureQuerySchema,
    )

    @Bean
    fun displayInfoHelloWorld() = WorkflowDisplayInfo(
        WorkflowType.HELLO_WORLD.serializedName,
        title = "Hello World",
        description = "Add a greeting to the project's README.",
        estimatedDuration = Duration.ofSeconds(20L),
        showOnProjectPage = true,
        workflowInputSchema = WorkflowInputBaseSchema,
    )

    @Bean
    fun displayInfoStarterProject() = WorkflowDisplayInfo(
        WorkflowType.STARTER_PROJECT.serializedName,
        title = "Create a new project",
        description = "Create a new project as a GitHub repository.",
        estimatedDuration = Duration.ofMinutes(40L),
        showOnProjectPage = false,
        workflowInputSchema = WorkflowInputProjectStarterSchema,
    )

    @Bean
    fun displayInfoProjectAnalyzeStatic() = WorkflowDisplayInfo(
        WorkflowType.PROJECT_ANALYZE_STATIC.serializedName,
        title = "Analyze Project",
        description = "Analyze the static project.",
        estimatedDuration = Duration.ofMinutes(10L),
        showOnProjectPage = false,
        workflowInputSchema = WorkflowInputBaseSchema,
    )

}
