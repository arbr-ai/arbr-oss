package com.arbr.prompt_library

import com.arbr.og.object_model.common.values.collections.SourcedStruct
import com.arbr.prompt_library.PromptLibrary.Companion.DelegateProvider
import com.arbr.relational_prompting.services.ai_application.application.AiApplication
import com.arbr.relational_prompting.services.ai_application.config.AiApplicationConfig
import com.arbr.relational_prompting.services.ai_application.config.AiApplicationFactory
import kotlin.reflect.KProperty

class PromptLibrary(
    private val aiApplicationFactory: AiApplicationFactory,
) {
    private val applicationConfig = ApplicationConfig(aiApplicationFactory)

    val commitReassignmentToSubtask by delegateProvider { config -> config.commitReassignmentToSubtask() }
    val commitSummarizationApplication by delegateProvider { config -> config.commitSummarizationApplication() }
    val contextualCommitCompletionApplication by delegateProvider { config -> config.contextualCommitCompletionApplication() }
    val contextualTaskCompletionApplication by delegateProvider { config -> config.contextualTaskCompletionApplication() }
    val dependencyGraphApplication by delegateProvider { config -> config.dependencyGraphApplication() }
    val dependencyReassignApplication by delegateProvider { config -> config.dependencyReassignApplication() }
    val editExistingFileOpSegApplication by delegateProvider { config -> config.editExistingFileOpSegApplication() }
    val featurePullRequestDetailsApplication by delegateProvider { config -> config.featurePullRequestDetailsApplication() }
    val fileOpEditToFileSegOps by delegateProvider { config -> config.fileOpEditToFileSegOps() }
    val fileOpNewToFileSegOps by delegateProvider { config -> config.fileOpNewToFileSegOps() }
    val fileOpsDetailApplication by delegateProvider { config -> config.fileOpsDetailApplication() }
    val fileSearchApplication by delegateProvider { config -> config.fileSearchApplication() }
    val githubExistingProjectDescriber by delegateProvider { config -> config.githubExistingProjectDescriber() }
    val githubFileSearchApplication by delegateProvider { config -> config.githubFileSearchApplication() }
    val githubFileSummarizer by delegateProvider { config -> config.githubFileSummarizer() }
    val githubNewFileSummarizer by delegateProvider { config -> config.githubNewFileSummarizer() }
    val githubNewProjectPlatformer by delegateProvider { config -> config.githubNewProjectPlatformer() }
    val githubProjectSpecifier by delegateProvider { config -> config.githubProjectSpecifier() }
    val localCodeEditDiffApplication by delegateProvider { config -> config.localCodeEditDiffApplication() }
    val localCodeEditPackageJsonApplication by delegateProvider { config -> config.localCodeEditPackageJsonApplication() }
    val localCodeEditResolveTodosApplication by delegateProvider { config -> config.localCodeEditResolveTodosApplication() }
    val localCodeGenApplication by delegateProvider { config -> config.localCodeGenApplication() }
    val newFileOpSegApplication by delegateProvider { config -> config.newFileOpSegApplication() }
    val newSegmentIncorporateApplication by delegateProvider { config -> config.newSegmentIncorporateApplication() }
    val repairAddFileOpDescriptionsApplication by delegateProvider { config -> config.repairAddFileOpDescriptionsApplication() }
    val repairFileSelectApplication by delegateProvider { config -> config.repairFileSelectApplication() }
    val subtaskCompletionApplication by delegateProvider { config -> config.subtaskCompletionApplication() }
    val subtaskFileSearchApplication by delegateProvider { config -> config.subtaskFileSearchApplication() }
    val taskBreakdownIterationApplication by delegateProvider { config -> config.taskBreakdownIterationApplication() }
    val taskBreakdownReduceCommitsApplication by delegateProvider { config -> config.taskBreakdownReduceCommitsApplication() }
    val taskBreakdownSynthesizeApplication by delegateProvider { config -> config.taskBreakdownSynthesizeApplication() }
    val taskBreakdownSynthesizeCommitsApplication by delegateProvider { config -> config.taskBreakdownSynthesizeCommitsApplication() }
    val taskCompletionApplication by delegateProvider { config -> config.taskCompletionApplication() }
    val taskCompletionPipelineOnlySummaries by delegateProvider { config -> config.taskCompletionPipelineOnlySummaries() }
    val taskCompletionPipelineWithSummaries by delegateProvider { config -> config.taskCompletionPipelineWithSummaries() }
    val taskDeduplicateCommits by delegateProvider { config -> config.taskDeduplicateCommits() }
    val taskFileSearchApplication by delegateProvider { config -> config.taskFileSearchApplication() }
    val taskPlanCommitDescriptionsPipelineYaml by delegateProvider { config -> config.taskPlanCommitDescriptionsPipelineYaml() }
    val taskVerbosePlanApplication by delegateProvider { config -> config.taskVerbosePlanApplication() }
    val todoApplication by delegateProvider { config -> config.todoApplication() }
    val todoImplPlanningApplication by delegateProvider { config -> config.todoImplPlanningApplication() }

    private fun <InputModel : SourcedStruct, OutputModel : SourcedStruct> delegateProvider(
        makeConfig: (applicationConfig: ApplicationConfig) -> AiApplicationConfig<InputModel, OutputModel>,
    ): DelegateProvider<AiApplication<InputModel, OutputModel>> {
        return DelegateProvider { _, _ ->
            Delegate {
                val config = makeConfig(applicationConfig)
                aiApplicationFactory.makeApplication(config)
            }
        }
    }

    companion object {
        private fun interface DelegateProvider<T> {
            operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): Delegate<T>
        }

        private class Delegate<V>(private val get: () -> V) {
            operator fun getValue(view: Any?, property: KProperty<*>): V {
                return get()
            }
        }
    }
}