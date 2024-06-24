package com.arbr.prompt_library

import com.arbr.content_formats.code.LenientCodeParser
import com.arbr.content_formats.mapper.Mappers
import com.arbr.object_model.core.resource.ArbrCommit
import com.arbr.object_model.core.resource.ArbrCommitEval
import com.arbr.object_model.core.resource.ArbrFile
import com.arbr.object_model.core.resource.ArbrFileOp
import com.arbr.object_model.core.resource.ArbrFileSegment
import com.arbr.object_model.core.resource.ArbrFileSegmentOp
import com.arbr.object_model.core.resource.ArbrProject
import com.arbr.object_model.core.resource.ArbrSubtask
import com.arbr.object_model.core.resource.ArbrSubtaskEval
import com.arbr.object_model.core.resource.ArbrTask
import com.arbr.object_model.core.resource.ArbrTaskEval
import com.arbr.object_model.core.resource.field.ArbrCommitEvalErrorContentValue
import com.arbr.object_model.core.resource.field.ArbrFileSegmentContainsTodoValue
import com.arbr.object_model.core.resource.field.ArbrFileSegmentOpContentValue
import com.arbr.object_model.core.resource.field.ArbrFileSegmentRuleNameValue
import com.arbr.object_model.core.resource.field.ArbrProjectDescriptionValue
import com.arbr.object_model.core.resource.field.ArbrProjectFullNameValue
import com.arbr.object_model.core.resource.field.ArbrProjectPlatformValue
import com.arbr.object_model.core.resource.field.ArbrProjectPrimaryLanguageValue
import com.arbr.object_model.core.resource.field.ArbrProjectTechStackDescriptionValue
import com.arbr.object_model.core.resource.field.ArbrProjectTitleValue
import com.arbr.object_model.core.resource.field.ArbrSubtaskSubtaskValue
import com.arbr.object_model.core.resource.field.ArbrTaskTaskQueryValue
import com.arbr.object_model.core.resource.field.ArbrTaskTaskVerbosePlanValue
import com.arbr.og.object_model.common.values.SourcedValueGeneratorInfo
import com.arbr.og.object_model.common.values.SourcedValueKind
import com.arbr.og.object_model.common.values.collections.SourcedStruct1
import com.arbr.og.object_model.common.values.collections.SourcedStruct2
import com.arbr.og.object_model.common.values.collections.SourcedStruct3
import com.arbr.og.object_model.common.values.collections.SourcedStruct4
import com.arbr.og.object_model.common.values.collections.SourcedStruct5
import com.arbr.og.object_model.common.values.collections.SourcedStruct6
import com.arbr.og.object_model.common.values.collections.SourcedStruct7
import com.arbr.prompt_library.template.PromptTemplates
import com.arbr.prompt_library.transform.CodeSerializer
import com.arbr.prompt_library.transform.PropertyListParsers
import com.arbr.prompt_library.transform.TrailingContentPropertySerializers
import com.arbr.prompt_library.util.CommitDetailsAndFileOps
import com.arbr.prompt_library.util.CommitDetailsAndFileSegOps
import com.arbr.prompt_library.util.CommitMessageSubtaskPair
import com.arbr.prompt_library.util.CommitMessages
import com.arbr.prompt_library.util.DiffSummaries
import com.arbr.prompt_library.util.FileOperationsAndTargetFilePaths
import com.arbr.prompt_library.util.FileOperationsAndTargetFilePathsWithDescriptions
import com.arbr.prompt_library.util.FilePaths
import com.arbr.prompt_library.util.FilePathsAndContents
import com.arbr.prompt_library.util.FilePathsAndSummaries
import com.arbr.prompt_library.util.FilePathsContainer
import com.arbr.prompt_library.util.FileSegmentContents
import com.arbr.prompt_library.util.FileSegmentOpDependencyEdges
import com.arbr.prompt_library.util.FileSegmentOperations
import com.arbr.prompt_library.util.FileSegmentOperationsInFile
import com.arbr.prompt_library.util.FileSegmentOperationsInFileWithContent
import com.arbr.prompt_library.util.FileSegments
import com.arbr.prompt_library.util.SubtaskPlans
import com.arbr.prompt_library.util.Subtasks
import com.arbr.prompt_library.util.commitDetailsAndFileOpsInnerValue
import com.arbr.prompt_library.util.makeFileOperationsAndTargetFilePathsInnerValue
import com.arbr.prompt_library.util.makeFilePathsAndSummariesInnerValue
import com.arbr.relational_prompting.generics.model.OpenAiChatCompletionModel
import com.arbr.relational_prompting.layers.object_translation.PropertySchema
import com.arbr.relational_prompting.layers.prompt_composition.PropertyListParser
import com.arbr.relational_prompting.services.ai_application.config.AiApplicationConfigFactory
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.math.max

internal class ApplicationConfig(
    private val aiApplicationConfigFactory: AiApplicationConfigFactory,
) {
    private val yamlMapper: ObjectMapper = Mappers.yamlMapper

    fun repairFileSelectApplication() =
        aiApplicationConfigFactory
            .builder("repair-file-select")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrProject.Platform)
                    .p(ArbrProject.Description)
                    .p(FilePaths)
                    .p(ArbrCommitEval.ErrorContent)
            }
            .withInputSerializer(TrailingContentPropertySerializers.errorContentsPropertySerializer)
            .withOutputSchema {
                p(FilePaths) // errored files
                    .withKey("errored_file_paths")
                    .withDescription("File(s) in the project which should be edited to fix the error.")
                    .p(FilePaths) // other files
                    .withKey("other_file_paths")
                    .withDescription("Other project files referenced in the error message.")
            }
            .withInstructions { inputSchemaDescription, outputSchemaDescription ->
                (
                        "You are an assistant who, given an input of the following form:\n"
                                + CodeSerializer.serializeCode(inputSchemaDescription, languageIndicator = "yaml")
                                + "\n\nProduces an output of the following form:\n"
                                + CodeSerializer.serializeCode(outputSchemaDescription, languageIndicator = "yaml")
                                + "\n\nYou should use the provided message to decide which files in the project " +
                                "should be edited to fix the issue, as well as any other files that may be relevant to" +
                                " debugging and fixing the error."
                        )
            }
            .withExample(
                SourcedStruct5(
                    ArbrProjectFullNameValue.constant("podtown-research/minesweeper"),
                    ArbrProjectPlatformValue.constant("Web"),
                    ArbrProjectDescriptionValue.constant("A Minesweeper game implemented in JavaScript using React."),
                    FilePaths.initializeMerged(
                        listOf(
                            FilePathsContainer(ArbrFile.FilePath.constant(".gitignore")),
                            FilePathsContainer(ArbrFile.FilePath.constant("README.md")),
                            FilePathsContainer(ArbrFile.FilePath.constant("index.html")),
                            FilePathsContainer(ArbrFile.FilePath.constant("package-lock.json")),
                            FilePathsContainer(ArbrFile.FilePath.constant("package.json")),
                            FilePathsContainer(ArbrFile.FilePath.constant("public/vite.svg")),
                            FilePathsContainer(ArbrFile.FilePath.constant("src/App.css")),
                            FilePathsContainer(ArbrFile.FilePath.constant("src/App.jsx")),
                            FilePathsContainer(ArbrFile.FilePath.constant("src/assets/react.svg")),
                            FilePathsContainer(ArbrFile.FilePath.constant("src/index.css")),
                            FilePathsContainer(ArbrFile.FilePath.constant("src/main.jsx")),
                            FilePathsContainer(ArbrFile.FilePath.constant("src/styles.css")),
                            FilePathsContainer(ArbrFile.FilePath.constant("vite.config.js")),
                        ),
                    ),
                    ArbrCommitEvalErrorContentValue.constant(
                        "[vite]: Rollup failed to resolve import \"react-router-dom\" from \"/mnt_ga_2/repos/863/topdown-ai/minesweeper-9/src/App.jsx\".\n" +
                                "This is most likely unintended because it can break your application at runtime.\n" +
                                "If you do want to externalize this module explicitly add it to\n" +
                                "`build.rollupOptions.external`\n" +
                                "error during build:\n" +
                                "Error: [vite]: Rollup failed to resolve import \"react-router-dom\" from \"/mnt_ga_2/repos/863/topdown-ai/minesweeper-9/src/App.jsx\".\n" +
                                "This is most likely unintended because it can break your application at runtime.\n" +
                                "If you do want to externalize this module explicitly add it to\n" +
                                "`build.rollupOptions.external`\n" +
                                "    at viteWarn (file:///mnt_ga_2/repos/863/topdown-ai/minesweeper-9/node_modules/vite/dist/node/chunks/dep-3b8eb186.js:48088:27)\n" +
                                "    at onwarn (file:///mnt_ga_2/repos/863/topdown-ai/minesweeper-9/node_modules/@vitejs/plugin-react/dist/index.mjs:238:9)\n" +
                                "    at onRollupWarning (file:///mnt_ga_2/repos/863/topdown-ai/minesweeper-9/node_modules/vite/dist/node/chunks/dep-3b8eb186.js:48117:9)\n" +
                                "    at onwarn (file:///mnt_ga_2/repos/863/topdown-ai/minesweeper-9/node_modules/vite/dist/node/chunks/dep-3b8eb186.js:47848:13)\n" +
                                "    at file:///mnt_ga_2/repos/863/topdown-ai/minesweeper-9/node_modules/rollup/dist/es/shared/node-entry.js:24070:13\n" +
                                "    at Object.logger [as onLog] (file:///mnt_ga_2/repos/863/topdown-ai/minesweeper-9/node_modules/rollup/dist/es/shared/node-entry.js:25742:9)\n" +
                                "    at ModuleLoader.handleInvalidResolvedId (file:///mnt_ga_2/repos/863/topdown-ai/minesweeper-9/node_modules/rollup/dist/es/shared/node-entry.js:24656:26)"
                    ),
                ),
                SourcedStruct2(
                    FilePaths.initializeMerged(
                        listOf(
                            FilePathsContainer(ArbrFile.FilePath.constant("package.json")),
                            FilePathsContainer(ArbrFile.FilePath.constant("src/App.jsx")),
                        ),
                    ),
                    FilePaths.initializeMerged(
                        emptyList(),
                    ),
                )
            )
            .build()

    fun repairAddFileOpDescriptionsApplication() =
        aiApplicationConfigFactory
            .builder("repair-add-file-op-descriptions")
            .withInputSchema {
                p(ArbrCommitEval.ErrorContent)
                    .p(FilePaths)
                    .withKey("files_not_found")
                    .withDescription(
                        "The paths of files from the error which were not found in the project." +
                                " Check whether these files not existing is the reason for the error and decide whether" +
                                " it is better to add them or remove the reference."
                    )
                    .p(FilePathsAndContents)
                    .withKey("other_relevant_files")
                    .withDescription(
                        "The paths and contents of other files that have been determined to" +
                                " be relevant to the error."
                    )
                    .p(FileOperationsAndTargetFilePaths)
                    .p(ArbrFile.Content)
            }
            .withOutputSchema {
                p(ArbrFileOp.Description)
            }
            .withInstructions { inputSchemaDescription, outputSchemaDescription ->
                (
                        "You are a code project planning assistant. Given an input of the following form:\n\n"
                                + inputSchemaDescription.trim()
                                + "\n\nPlan the code changes needed to resolve the given error messages by providing " +
                                " a descriptions for the given file explaining what should be done in the file" +
                                " in order to fix the errors." +
                                " The files included in the user's input are existing files in the repository which" +
                                " have been judged to be relevant to the error.\n" +
                                "Your output has the format:\n\n"
                                + outputSchemaDescription
                        )
            }
            .build()

    fun editExistingFileOpSegApplication() =
        aiApplicationConfigFactory
            .builder("file-seg-op-edit-impl")
            .withInputSchema {
                p(FileSegmentOperationsInFileWithContent)
                    .p(ArbrFileSegmentOp.Operation)
                    .p(ArbrFileSegmentOp.ContentType)
                    .p(ArbrFileSegmentOp.RuleName)
                    .p(ArbrFileSegmentOp.Name)
                    .p(ArbrFileSegmentOp.ElementIndex)
                    .p(ArbrFileSegmentOp.Content)
                    .p(ArbrFileSegmentOp.Description)
            }
            .withOutputSchema {
                p(ArbrFileSegmentOp.Content)
            }
            .withModel(OpenAiChatCompletionModel.GPT_4_0125_PREVIEW)
            .build()

    fun newFileOpSegApplication() =
        aiApplicationConfigFactory
            .builder("file-seg-op-new-impl")
            .withInputSchema {
                p(FileSegmentOperationsInFileWithContent)
                    .p(ArbrFileSegmentOp.Operation)
                    .p(ArbrFileSegmentOp.ContentType)
                    .p(ArbrFileSegmentOp.RuleName)
                    .p(ArbrFileSegmentOp.Name)
                    .p(ArbrFileSegmentOp.ElementIndex)
                    .p(ArbrFileSegmentOp.Description)
            }
            .withOutputSchema {
                p(ArbrFileSegmentOp.Content)
                    .withKey("content")
                    .withDescription("New Source Element Content: Source code to add for the new source element to implement the described change.")
            }
            .withModel(OpenAiChatCompletionModel.GPT_4_0125_PREVIEW)
            .build()

    fun dependencyGraphApplication() =
        aiApplicationConfigFactory
            .builder("file-seg-ops-dependencies")
            .withInputSchema {
                p(FileSegmentOperationsInFile)
            }
            .withOutputSchema {
                p(FileSegmentOpDependencyEdges)
            }
            .withOutputParser(
                PropertyListParsers
                    .OutputParser(yamlMapper)
            )
            .withModel(OpenAiChatCompletionModel.GPT_4_0125_PREVIEW)
            .withMaxTokens(3 * 1024)
            .withInstructions { inputSchemaDescription, _ ->
                (
                        "You are a code planning assistant who maps out the dependencies of code changes within a" +
                                " software project. Given an input of the form:\n\n" + inputSchemaDescription +
                                "\n\nYou should output each operation and its dependencies by ID. Change X is" +
                                " dependent on Change Y when Change X is expected to contain a source code" +
                                " reference to Change Y such that it will not compile (or otherwise be interpretable" +
                                " as valid source code) unless Change Y is complete. You should include only" +
                                " dependencies that are expected to represent such strong relationships. Your output" +
                                " should be in the following YAML format:" +
                                "\n\n```yaml\nfile_operation_id_a: [{dependency_id_a_0}, {dependency_id_a_1}, ...] # List of dependency IDs\n" +
                                "file_operation_id_b: [{dependency_id_b_0}, {dependency_id_b_1}, ...]\n" +
                                "# ... Continued for each operation ...\n```"
                        )
            }
            .build()

    fun dependencyReassignApplication() =
        aiApplicationConfigFactory
            .builder("dependency-commit-reassign")
            .withInputSchema {
                p(FileSegmentOperationsInFile)
            }
            .withOutputSchema {
                p(CommitDetailsAndFileSegOps)
            }
            // .withModel(OpenAiChatCompletionModel.GPT_4_0125_PREVIEW)
            .withMaxTokens(2048)
            .build()

    fun githubFileSearchApplication() =
        aiApplicationConfigFactory
            .builder("github-file-search")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrTask.TaskQuery)
                    .p(FilePathsAndSummaries)
            }
            .withOutputSchema {
                p(FilePaths)
            }
            .withPromptShortener { record, _ ->
                record.copy(
                    t3 = record.t3.let { filePaths ->
                        filePaths.copy(
                            value = filePaths.value.dropLast(1)
                        )
                    }
                )
            }
            .build()

    fun githubNewProjectPlatformer() =
        aiApplicationConfigFactory
            .builder("github-new-project-describer")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrTask.TaskQuery)
            }
            .withOutputSchema {
                p(ArbrProject.Description)
                    .p(ArbrProject.Platform)
            }
            .build()

    fun githubExistingProjectDescriber() =
        aiApplicationConfigFactory
            .builder("project-file-based-details")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(FilePaths)
            }
            .withOutputSchema {
                p(ArbrProject.Title)
                    .p(ArbrProject.PrimaryLanguage)
                    .p(ArbrProject.Platform)
                    .p(ArbrProject.Description)
                    .p(ArbrProject.TechStackDescription)
            }
            .build()

    fun githubProjectSpecifier() =
        aiApplicationConfigFactory
            .builder("github-project-specifier-3")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrProject.Title)
                    .p(ArbrProject.Platform)
                    .p(ArbrProject.Description)
                    .p(FilePathsAndContents)
            }
            .withOutputSchema {
                p(ArbrProject.PrimaryLanguage)
                    .p(ArbrProject.TechStackDescription)
            }
            .withPromptShortener { record, _ ->
                record.copy(
                    t5 = record.t5.let { fileSummaries ->
                        fileSummaries.copy(
                            value = fileSummaries.value.dropLast(1)
                        )
                    }
                )
            }
            .withExample(
                SourcedStruct5(
                    ArbrProjectFullNameValue.constant("podtown-research/minesweeper"),
                    ArbrProjectTitleValue.constant("Minesweeper"),
                    ArbrProjectPlatformValue.constant("Web"),
                    ArbrProjectDescriptionValue.constant("A simple web implementation of a minesweeper game"),
                    FilePathsAndContents.initializeMerged(
                        listOf(
                            SourcedStruct2(
                                ArbrFile.FilePath.constant("README.md"),
                                ArbrFile.Content.constant("# React + Vite\n\nThis template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.\n\nCurrently, two official plugins are available:\n\n- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react/README.md) uses [Babel](https://babeljs.io/) for Fast Refresh\n- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react-swc) uses [SWC](https://swc.rs/) for Fast Refresh\n"),
                            )
                        )
                    ),
                ),
                SourcedStruct2(
                    ArbrProjectPrimaryLanguageValue.constant("JavaScript"),
                    ArbrProjectTechStackDescriptionValue.constant("This web-based Minesweeper game is built using React and Vite. The project also utilizes ESLint for code linting."),
                )
            )
            .build()

    fun githubNewFileSummarizer() =
        aiApplicationConfigFactory
            .builder("github-new-file-summarizer-yaml")
            .withInputSchema {
                p(ArbrProject.Title)
                    .p(ArbrProject.Platform)
                    .p(ArbrProject.PrimaryLanguage)
                    .p(ArbrProject.TechStackDescription)
                    .p(ArbrProject.Description)
                    .p(ArbrTask.TaskQuery)
                    .p(ArbrCommit.CommitMessage)
                    .p(ArbrFile.FilePath)
            }
            .withOutputSchema {
                p(ArbrFile.SourceLanguage)
                    .p(ArbrFile.IsBinary)
                    .p(ArbrFile.Summary)
            }
            .build()

    fun githubFileSummarizer() =
        aiApplicationConfigFactory
            .builder("file-summarizer")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrProject.Title)
                    .p(ArbrProject.Platform)
                    .p(ArbrProject.PrimaryLanguage)
                    .p(ArbrProject.TechStackDescription)
                    .p(ArbrProject.Description)
                    .p(ArbrFile.FilePath)
                    .p(ArbrFile.Content)
            }
            .withInputSerializer(TrailingContentPropertySerializers.fileContentsPropertyListSerializer)
            .withOutputSchema {
                p(ArbrFile.SourceLanguage)
                    .p(ArbrFile.IsBinary)
                    .p(ArbrFile.Summary)
            }
            .withInstructions { inputSchemaDescription, outputSchemaDescription ->
                "You are an assistant who summarizes the contents of a file. The user will give an input of the form:\n" +
                        inputSchemaDescription +
                        "\n\nYou produce an output of the form:\n" +
                        outputSchemaDescription +
                        "\n\nEnsure your output is formatted as valid YAML."
            }
            .build()

    /**
     * Currently same as regular diff
     */
    fun localCodeEditPackageJsonApplication() =
        aiApplicationConfigFactory
            .builder("code-edit-diff-package-json")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrTask.TaskQuery)
                    .withKey("main_task")
                    .withDescription("Main Task: The main task being accomplished, which this code change is a part of.")
                    .p(ArbrSubtask.Subtask)
                    .withKey("subtask")
                    .withDescription("Subtask: A subtask of the main task being accomplished, which this code change is a part of.")
                    .p(CommitDetailsAndFileOps)
                    .withKey("all_subtask_commit_info")
                    .withDescription("All Subtask Commit Info: Info for all the commits currently planned for the subtask.")
                    .p(ArbrCommit.CommitMessage)
                    .withKey("current_commit_message")
                    .withDescription("Current Commit Message: The commit message for the commit currently being implemented, which this code change is a part of.")
                    .p(FileOperationsAndTargetFilePathsWithDescriptions) // Singleton
                    .withKey("current_change_details")
                    .withDescription("Current Code Change Details: Details for the code change currently being implemented.")
                    .p(ArbrFile.Summary)
                    .p(ArbrFile.Content)
            }
            .withInputSerializer(TrailingContentPropertySerializers.trailingContentPropertyListSerializer)
            .withOutputSchema {
                p(ArbrFile.Content) // Note description unused in this case, important since not exactly file content
            }
            .withInstructions { inputSchemaDescription, _ ->
                PromptTemplates
                    .iterativeCodeEditDiffInstructions
                    .replace("{input_schema_description}", inputSchemaDescription)
            }
            .withMaxTokens(2048)
            .withModel(OpenAiChatCompletionModel.GPT_4_0125_PREVIEW)
            .withNumExamplesToIncludeInPrompt(0)
            .build()

    fun localCodeEditDiffApplication() =
        aiApplicationConfigFactory
            .builder("code-edit-diff")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrTask.TaskQuery)
                    .withKey("main_task")
                    .withDescription("Main Task: The main task being accomplished, which this code change is a part of.")
                    .p(ArbrSubtask.Subtask)
                    .withKey("subtask")
                    .withDescription("Subtask: A subtask of the main task being accomplished, which this code change is a part of.")
                    .p(CommitDetailsAndFileOps)
                    .withKey("all_subtask_commit_info")
                    .withDescription("All Subtask Commit Info: Info for all the commits currently planned for the subtask.")
                    .p(ArbrCommit.CommitMessage)
                    .withKey("current_commit_message")
                    .withDescription("Current Commit Message: The commit message for the commit currently being implemented, which this code change is a part of.")
                    .p(FileOperationsAndTargetFilePathsWithDescriptions) // Singleton
                    .withKey("current_change_details")
                    .withDescription("Current Code Change Details: Details for the code change currently being implemented.")
                    .p(ArbrFile.Summary)
                    .p(ArbrFile.Content)
            }
            .withInputSerializer(TrailingContentPropertySerializers.trailingContentPropertyListSerializer)
            .withOutputSchema {
                p(ArbrFile.Content) // Note description unused in this case, important since not exactly file content
            }
            .withInstructions { inputSchemaDescription, _ ->
                PromptTemplates
                    .iterativeCodeEditDiffInstructions
                    .replace("{input_schema_description}", inputSchemaDescription)
            }
            .withMaxTokens(2048)
            .withModel(OpenAiChatCompletionModel.GPT_4_0125_PREVIEW)
            .withNumExamplesToIncludeInPrompt(0)
            .build()


    fun localCodeEditResolveTodosApplication() =
        aiApplicationConfigFactory
            .builder("code-edit-resolve-todos")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrTask.TaskQuery)
                    .p(ArbrFile.FilePath)
                    .p(ArbrFile.Summary)
                    .p(FileSegmentContents)
                    .withKey("target_file_segments")
                    .withDescription("The segments of the file content identified to contain outstanding TODO tasks.")
                    .p(ArbrFile.Content)
            }
            .withInputSerializer(TrailingContentPropertySerializers.trailingContentPropertyListSerializer)
            .withOutputSchema {
                p(ArbrFile.Content)
            }
            .withOutputParser(PropertyListParsers.trailingContentPropertyListParser)
            .withInstructions { inputSchemaDescription, _ ->
                (
                        "You are an assistant who edits the contents of an existing file in a GitHub project as part of "
                                + "implementing in a feature in a software project. Given an input of the following form:\n\n"
                                + inputSchemaDescription
                                + "\n\nYou output the new, updated contents of the file."
                                + "\nYour goal is to edit the file to implement the remaining TODOs, while working" +
                                " towards accomplishing the main task. You should give"
                                + " the complete contents of the file without any additional description or context." +
                                " If no changes are necessary, simply output the original contents of the file." +
                                "You must implement the source code for all TODOs present in the file. Wrap the contents in backticks such as:\n\n"
                                + "```\n... file content ...\n```"
                        )
            }
            .withOutputProcessor { chatMessage ->
                chatMessage.copy(
                    content = LenientCodeParser.parse(chatMessage.content.trim())
                )
            }
            .withModel(OpenAiChatCompletionModel.GPT_4_0125_PREVIEW)
            .build()

    fun localCodeGenApplication() =
        aiApplicationConfigFactory
            .builder("pipeline-iterative-code-gen-raw")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrTask.TaskQuery)
                    .p(CommitDetailsAndFileOps)
                    .p(ArbrCommit.CommitMessage)
                    .p(ArbrCommit.DiffSummary)
                    .p(ArbrFile.FilePath)
                    .p(ArbrFile.SourceLanguage)
                    .p(ArbrFile.Summary)
            }
            .withOutputSchema {
                p(ArbrFile.Content)
            }
            .withInstructions { inputSchemaDescription, _ ->
                (
                        "You are an assistant who, given an input of the following form:\n"
                                + inputSchemaDescription
                                + "\n\nYou output the new, updated contents of the file."
                                + "\n\nYour goal is to implement the file to accomplish the project task. You should give"
                                + " the complete contents of the file without any additional description or context. If"
                                + " there are ambiguities that limit your ability to implement the complete file, you should"
                                + " make your best attempt anyway, always outputting syntactically correct content, but you"
                                + " may leave a comment pointing out such ambiguities."
                        )
            }
            .withOutputProcessor { chatMessage ->
                chatMessage.copy(
                    content = LenientCodeParser.parse(chatMessage.content.trim())
                )
            }
            .withModel(OpenAiChatCompletionModel.GPT_4_0125_PREVIEW)
            .build()

    fun newSegmentIncorporateApplication() =
        aiApplicationConfigFactory
            .builder("incorporate-new-segment")
            .withInputSchema {
                p(ArbrFileSegmentOp.RuleName) // HACK: THIS IS THE FILE SEGMENT TREE
                    .p(ArbrFileSegmentOp.RuleName)
                    .withKey("new_element_rule_name")
                    .withDescription("New Source Element Kind: The kind of the new source element, such as class, function, etc.")
                    .p(ArbrFileSegmentOp.Name)
                    .withKey("new_element_name")
                    .withDescription("New Source Element Name: The name of the new source element.")
            }
            .withInputSerializer(
                TrailingContentPropertySerializers
                    .fileSegmentTreePropertyListSerializer(yamlMapper)
            )
            .withOutputSchema {
                p(ArbrFileSegment.RuleName) // HACK: THIS IS THE FILE SEGMENT TREE
            }
            .withOutputParser(object :
                PropertyListParser<SourcedStruct1<ArbrFileSegmentRuleNameValue>> {
                override fun parseValue(
                    propertySchemas: List<PropertySchema<*, *>>,
                    messageContent: String,
                    sourcedValueGeneratorInfo: SourcedValueGeneratorInfo,
                    objectClass: Class<SourcedStruct1<ArbrFileSegmentRuleNameValue>>
                ): SourcedStruct1<ArbrFileSegmentRuleNameValue>? {
                    return SourcedStruct1(
                        ArbrFileSegment.RuleName.generated(
                            messageContent,
                            sourcedValueGeneratorInfo,
                        )
                    )
                }
            })
            .withInstructions { inputSchemaDescription, _ ->
                val treeDescription = """
            ```yaml
            combined_source_element_tree: # Tree of source element data with the new element added in the appropriate place.
              element_rule_name: # Source Element Kind: The kind of source element, such as class, function, etc.
              element_name: # Source Element Name: The name of the source element.
              child_elements:
                - element_rule_name: #
                  element_name: #
                  child_elements:
                    - element_rule_name: #
                      element_name: #
                      child_elements: [] # ...
                - element_rule_name: #
                  element_name: #
                  child_elements: [] # ...
                # ...
            ```
        """.trimIndent()

                (
                        "You are a code planning assistant helping to determine the correct point to inject an element of" +
                                " source code in a file. Given an input of the following form:\n" +
                                inputSchemaDescription +
                                "\n\nYou should incorporate the new source element into the existing tree, giving an output of the following form:\n" +
                                treeDescription
                        )
            }
            .withModel(OpenAiChatCompletionModel.GPT_4_0125_PREVIEW)
            .withOutputProcessor { chatMessage ->
                chatMessage.copy(
                    content = LenientCodeParser.parse(chatMessage.content.trim())
                )
            }
            .withMaxTokens(2048)
            .build()

    fun taskBreakdownIterationApplication() =
        aiApplicationConfigFactory
            .builder("task-breakdown-iteration-4")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrProject.Platform)
                    .p(ArbrProject.Description)
                    .p(ArbrTask.TaskVerbosePlan)
                    .p(FilePaths)
                    .p(ArbrTask.TaskQuery)
            }
            .withOutputSchema {
                p(Subtasks)
            }
            .withInstructions { inputSchemaDescription, outputSchemaDescription ->
                (
                        "You are a code project planning assistant. The user will give an input of the following form:\n"
                                + inputSchemaDescription
                                + "\n\nYour output has the format:\n"
                                + outputSchemaDescription
                                + "\n\nYou should divide the user-inputted task if the task comprises multiple steps," +
                                " where a step is appropriately-sized for a single commit. If the user-inputted task is " +
                                "already simple enough, you should just output the same task as a singleton subtask." +
                                "\n\nSubtasks should include only those that correspond directly to code changes made inside the git repository." +
                                "\nYou should remove any subtasks which do not correspond to code changes. For example, " +
                                "subtasks regarding designing, planning, or pushing the code change should be removed. " +
                                "Also remove any subtasks regarding testing and documentation."
                        )
            }
            .withMaxTokens(1024)
            .withNumExamplesToIncludeInPrompt(0)
            .build()

    fun taskBreakdownSynthesizeApplication() =
        aiApplicationConfigFactory
            .builder("task-breakdown-refinement-3")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrProject.Platform)
                    .p(ArbrProject.Description)
                    .p(ArbrTask.TaskVerbosePlan)
                    .p(ArbrTask.TaskQuery)
                    .p(Subtasks)
            }
            .withOutputSchema {
                p(Subtasks)
            }
            .withInstructions { inputSchemaDescription, outputSchemaDescription ->
                (
                        "You are a code project planning assistant. The user will give an input of the following form:\n"
                                + inputSchemaDescription
                                + "\n\nYour output has the format:\n"
                                + outputSchemaDescription
                                + "\n\nYou should rewrite the unrefined list of subtasks to include only those " +
                                "tasks which are essential to the core user-facing functionality of the main task. You" +
                                " should remove any subtasks which do not correspond to code changes. For example, " +
                                "subtasks regarding planning or pushing the code change should be removed. Also remove" +
                                " any subtasks regarding testing and documentation. The " +
                                "resulting list of subtasks should closely resemble a list of commits in a pull request" +
                                " for the main task. Phrase each subtask in the imperative."
                        )
            }
            .withMaxTokens(1024)
            .build() // TODO: Too long for GPT-4

    fun taskPlanCommitDescriptionsPipelineYaml() =
        aiApplicationConfigFactory
            .builder("task-project-plan-commit-descriptions-yaml-4") // TODO: Good candidate for simplification
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrProject.Platform)
                    .p(ArbrProject.Description)
                    .p(ArbrTask.TaskQuery)
                    .p(FilePathsAndSummaries)
                    .p(ArbrSubtask.Subtask)
            }
            .withOutputSchema {
                p(CommitDetailsAndFileOps)
            }
            .withInstructions { inputSchemaDescription, outputSchemaDescription ->
                (
                        "You are a code project planning assistant. Given an input of the following form:\n"
                                + inputSchemaDescription
                                + "\n\nPlan the " +
                                "commits needed to accomplish the provided subtask by outputting summaries of the " +
                                "planned change associated with each commit. The given files summarized in the user's" +
                                " input are existing files in the repository.\nYour output has the format:\n"
                                + outputSchemaDescription
                        )
            }
            .withExample(
                SourcedStruct6(
                    ArbrProjectFullNameValue.constant("podtown-research/minesweeper"),
                    ArbrProjectPlatformValue.constant("Web"),
                    ArbrProjectDescriptionValue.constant("A simple web implementation of a minesweeper game"),
                    ArbrTaskTaskQueryValue.constant("Implement the Minesweeper game with easy difficulty"),
                    FilePathsAndSummaries.initializeMerged(
                        listOf(
                            makeFilePathsAndSummariesInnerValue(
                                "index.html",
                                "The index.html file is the main HTML file for the Minesweeper web app. It contains the HTML structure and elements required for the app to run in a web browser. The file starts with the doctype declaration and the opening html tag. Inside the head tag, there are meta tags for character encoding, viewport configuration, and the page title. The file also includes a link tag for the favicon and a closing head tag. Inside the body tag, there is plain text 'Minesweeper' followed by a div element with the id 'root', which serves as the root element for the app. Inside this div, there is a custom component called 'App'. The file also includes two script tags with the type 'module' and src attributes pointing to the main.jsx and GameBoard.jsx files in the /src directory. These scripts are responsible for loading and running the app. Overall, the index.html file provides the necessary structure and dependencies for the Minesweeper web app to function in a web browser.",
                            ),
                            makeFilePathsAndSummariesInnerValue(
                                "src/App.jsx",
                                "The App.jsx file is a React component that represents the main application of the Minesweeper web app. It imports the React library, the useState hook, and the GameBoard component from the './GameBoard' file. The App component is a functional component that returns a JSX element. It uses the useState hook to create a state variable called 'gameOver' and a function called 'setGameOver' to update the state. It also defines a function called 'handleGameOver' that sets the 'gameOver' state to true. The App component renders a div element with the className 'App'. Inside the div, it conditionally renders either a div element with the text 'Game Over' if the 'gameOver' state is true, or the GameBoard component with the 'onGameOver' prop set to the 'handleGameOver' function if the 'gameOver' state is false. The App component is exported as the default export of the file, making it accessible to other parts of the application. This file is responsible for rendering the main structure of the Minesweeper web app and handling the game over state.",
                            ),
                            makeFilePathsAndSummariesInnerValue(
                                "src/Cell.jsx",
                                "The Cell.jsx file is a functional component in the Minesweeper game. It imports the React library and the useState hook from React. The Cell component takes two props: isMine and isRevealed. It also uses the useState hook to manage the state of the isFlagged variable, which is initially set to false. The handleRightClick function is triggered when the user right-clicks on the cell, preventing the default context menu and toggling the value of isFlagged. The handleClick function is triggered when the user clicks on the cell. If the cell is not flagged, the reveal cell logic can be implemented. The Cell component returns a div element that listens for the onContextMenu event to trigger the handleRightClick function and the onClick event to trigger the handleClick function. The div element displays a flag emoji if isFlagged is true. Other cell rendering logic can be added within the div element. The Cell component is exported as the default export of the file.",
                            ),
                            makeFilePathsAndSummariesInnerValue(
                                "src/GameBoard.jsx",
                                "The GameBoard.jsx file is a functional component that represents the game board in the Minesweeper game. It imports the React library, the useState and useEffect hooks, and the Cell component from the './Cell' file. The GameBoard component is a functional component that creates a 2D array 'board' and a state variable 'mineCount' using the useState hook. The 'board' array is initialized with objects that have properties 'mine' set to false and 'adjacentMines' set to 0. The component also defines a function 'distributeMines' that randomly distributes mines on the game board. It uses a while loop to generate random coordinates and checks if the cell at that coordinate already has a mine. If not, it sets the 'mine' property of that cell to true and increments the 'mineCount' variable. The 'board' and 'mineCount' state variables are updated using the setBoard and setMineCount functions respectively. The component then defines a function 'calculateAdjacentMines' that calculates the number of adjacent mines for each cell on the board using nested for loops. The count is stored in the 'adjacentMines' property of each cell. The 'calculateAdjacentMines' function updates the 'board' state variable using the setBoard function. The component uses the useEffect hook to call the 'distributeMines' and 'calculateAdjacentMines' functions when the component is first rendered. Finally, the component returns a div element that maps over the board array and renders each row as a div element and each cell as a Cell component. The Cell component is passed the 'mine' and 'adjacentMines' properties of each cell as props. The GameBoard component is exported as the default export of the file.",
                            ),
                            makeFilePathsAndSummariesInnerValue(
                                "src/main.jsx",
                                "The main.jsx file is the entry point of the Minesweeper web application. It imports the React and ReactDOM libraries, as well as the App and GameBoard components from their respective files. The ReactDOArbrrender method is used to render the App and GameBoard components inside the 'root' element of the HTML document. Both components are wrapped in a React.StrictMode component. This file serves as the starting point for the Minesweeper web app and includes the main components and their rendering logic.",
                            ),
                            makeFilePathsAndSummariesInnerValue(
                                "src/utils.jsx",
                                "The utils.js file contains a single function named 'distributeMines'. This function takes three parameters: 'rows', 'cols', and 'mines'. It returns a 2D array 'board' that represents the distribution of mines on the game board. The function initializes the 'board' array with 'rows' number of rows and 'cols' number of columns, filled with 'false' values. It then uses a for loop to randomly distribute 'mines' number of mines on the board. It generates random row and column indices using the Math.random() function and assigns 'true' to the corresponding cell in the 'board' array. If the cell already has a mine, the function continues to find another cell until an empty cell is found. Finally, the function returns the 'board' array. The 'distributeMines' function is exported as a named export 'distributeMines' from the file.",
                            ),
                        ),
                    ),
                    ArbrSubtaskSubtaskValue.constant("Develop logic to flag cells as potential mines.")
                ),
                SourcedStruct1(
                    CommitDetailsAndFileOps.initialize(
                        SourcedValueKind.CONSTANT,
                        listOf(
                            commitDetailsAndFileOpsInnerValue(
                                "Add flagging logic to Cell component",
                                "Updated the `Cell.jsx` file to include logic for flagging cells as potential mines. This includes adding a new state variable `isFlagged` and a function `toggleFlag` that toggles the `isFlagged` state when the cell is right-clicked. The cell's display is also updated to show a flag emoji when `isFlagged` is true.",
                                listOf(
                                    makeFileOperationsAndTargetFilePathsInnerValue(
                                        "edit_file",
                                        "src/Cell.jsx",
                                    )
                                )
                            ),
                            commitDetailsAndFileOpsInnerValue(
                                "Update GameBoard to handle flagged cells",
                                "Updated the `GameBoard.jsx` file to handle flagged cells. This includes passing a `flagCell` function as a prop to the Cell component, which is called when a cell is right-clicked. The `flagCell` function updates the `board` state to reflect the flagged status of the cell.",
                                listOf(
                                    makeFileOperationsAndTargetFilePathsInnerValue(
                                        "edit_file",
                                        "src/GameBoard.jsx"
                                    )
                                )
                            ),
                            commitDetailsAndFileOpsInnerValue(
                                "Add flag count state to GameBoard",
                                "Updated the `GameBoard.jsx` file to include a new state variable `flagCount`. This state will be used to track the number of flags placed on the board. Initialized `flagCount` to 0.",
                                listOf(
                                    makeFileOperationsAndTargetFilePathsInnerValue(
                                        "edit_file",
                                        "src/GameBoard.jsx"
                                    )
                                )
                            ),
                            commitDetailsAndFileOpsInnerValue(
                                "Update flagCell to update flag count",
                                "Updated the `flagCell` function in `GameBoard.jsx` to increment or decrement the `flagCount` state depending on whether a cell is being flagged or unflagged.",
                                listOf(
                                    makeFileOperationsAndTargetFilePathsInnerValue(
                                        "edit_file",
                                        "src/GameBoard.jsx"
                                    )
                                )
                            ),
                            commitDetailsAndFileOpsInnerValue(
                                "Display flag count in App component",
                                "Updated the `App.jsx` file to display the current flag count. This includes adding a new prop `flagCount` to the GameBoard component, and displaying the value of `flagCount` in the App component's render method.",
                                listOf(
                                    makeFileOperationsAndTargetFilePathsInnerValue(
                                        "edit_file",
                                        "src/App.jsx"
                                    )
                                )
                            ),
                        ),
                        SourcedValueGeneratorInfo(emptyList()),
                    )
                )
            )
            .withPromptShortener { record, _ ->
                record.copy(
                    t5 = record.t5.let { fileSummaries ->
                        fileSummaries.copy(
                            value = fileSummaries.value.dropLast(1)
                        )
                    }
                )
            }
            .withMaxTokens(1024) // Warning: this prompt gets very close to max
            .withNumExamplesToIncludeInPrompt(0)
            .build()

    fun taskBreakdownSynthesizeCommitsApplication() =
        aiApplicationConfigFactory
            .builder("task-breakdown-synthesize-commits-5")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrProject.Platform)
                    .p(ArbrProject.Description)
                    .p(ArbrTask.TaskVerbosePlan)
                    .p(ArbrSubtask.Subtask)
                    .p(CommitDetailsAndFileOps)
            }
            .withOutputSchema {
                p(CommitDetailsAndFileOps)
            }
            .withInstructions { inputSchemaDescription, outputSchemaDescription ->
                (
                        "You are a code project planning assistant. The user will give an input of the following form:\n"
                                + inputSchemaDescription
                                + "\n\nYour output has the format:\n"
                                + outputSchemaDescription
                                + "\n\nYou should rewrite and combine the unrefined list of commits to include only those " +
                                "code changes which are essential to the core user-facing functionality of the given subtask. You" +
                                " should remove any commits which do not correspond to code changes. For example, " +
                                "commits regarding planning or pushing the code change should be removed. Also remove" +
                                " any commits that mention testing and documentation. Combine related commits such that" +
                                " the output contains at most 3 commits, and update the summaries to reflect the combined change."
                        )
            }
            .withMaxTokens(2048)
            .withNumExamplesToIncludeInPrompt(0)
            .build()

    fun taskBreakdownReduceCommitsApplication() =
        aiApplicationConfigFactory
            .builder("task-breakdown-reduce-commits")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrProject.Platform)
                    .p(ArbrProject.Description)
                    .p(ArbrTask.TaskQuery)
                    .p(CommitDetailsAndFileOps)
            }
            .withOutputSchema {
                p(CommitDetailsAndFileOps)
            }
            .withInstructions { inputSchemaDescription, outputSchemaDescription ->
                (
                        "You are a code project planning assistant. The user will give an input of the following form:\n"
                                + inputSchemaDescription
                                + "\n\nYour output has the format:\n"
                                + outputSchemaDescription
                                + "\n\nYou should rewrite and combine the unrefined list of commits to include only those " +
                                "code changes which are essential to the core user-facing functionality of the main task. " +
                                "Combine related commits such that" +
                                " the output contains at most 8 commits, and update the summaries to reflect the combined change."
                        )
            }
            .withMaxTokens(2048)
            .withNumExamplesToIncludeInPrompt(1)
            .build()

    fun taskDeduplicateCommits() =
        aiApplicationConfigFactory
            .builder("task-breakdown-deduplicate-commits-3")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrProject.Platform)
                    .p(ArbrProject.Description)
                    .p(ArbrTask.TaskQuery)
                    .p(CommitDetailsAndFileOps) // planned
                    .withKey("planned_commit_details_and_file_operations")
                    .p(CommitDetailsAndFileOps) // proposed
                    .withKey("proposed_commit_details_and_file_operations")
            }
            .withOutputSchema {
                p(CommitDetailsAndFileOps)
            }
            .withInstructions { inputSchemaDescription, outputSchemaDescription ->
                (
                        "You are a code project planning assistant. The user will give an input of the following form:\n"
                                + inputSchemaDescription
                                + "\n\nYour output has the format:\n"
                                + outputSchemaDescription
                                + "\n\nYou should rewrite and filter the proposed list of commits to include only those " +
                                "code changes which introduce new work on top of the already-planned commits. The " +
                                "resulting list will be used in place of the proposed list and appended to the planned" +
                                " commits."
                        )
            }
            .withMaxTokens(2048)
            .withNumExamplesToIncludeInPrompt(1)
            .withExample(
                SourcedStruct6(
                    ArbrProjectFullNameValue.constant("podtown-research/minesweeper"),
                    ArbrProjectPlatformValue.constant("Web"),
                    ArbrProjectDescriptionValue.constant("This project is about implementing a basic Minesweeper game as a Web app. Minesweeper is a classic puzzle game where the player must uncover cells on a grid without hitting any mines. The game involves strategic thinking and careful decision-making to avoid mines and uncover all safe cells. The Web app will provide a user-friendly interface for playing the game and will include features such as customizable grid sizes and difficulty levels."),
                    ArbrTaskTaskQueryValue.constant("Implement a basic Minesweeper game as a Web app"),
                    CommitDetailsAndFileOps.initialize(
                        SourcedValueKind.CONSTANT,
                        listOf(
                            commitDetailsAndFileOpsInnerValue(
                                "Create function to generate game board",
                                "This commit adds a new function called `generateGameBoard` to the `src/App.jsx` file. The function takes two parameters: `gridSize` and `difficultyLevel`. It generates a game board based on the provided grid size and difficulty level. The function first calculates the total number of cells based on the grid size. It then generates an array of objects representing each cell on the game board. Each object has properties such as `row`, `column`, `isMine`, `isRevealed`, and `adjacentMines`. The `isMine` property is randomly assigned based on the difficulty level. The `isRevealed` property is initially set to `false` for all cells. The `adjacentMines` property is calculated by iterating over each cell and counting the number of adjacent cells that contain mines. The function returns the generated game board as an array of objects.",
                                listOf(
                                    makeFileOperationsAndTargetFilePathsInnerValue(
                                        "edit_file",
                                        "src/App.jsx",
                                    )
                                )
                            ),
                            commitDetailsAndFileOpsInnerValue(
                                "Create initial game board layout in App component",
                                "This commit adds the initial game board layout to the App component. The game board is represented by a grid of cells, each cell containing an initial state. The layout is implemented using HTML and CSS. The App component is updated to render the grid of cells, and each cell is styled using CSS to provide the desired appearance. The initial state of each cell is set to a default value, which will be updated later based on user interactions. This commit also includes any necessary changes to the index.css file to style the cells and the grid layout.",
                                listOf(
                                    makeFileOperationsAndTargetFilePathsInnerValue(
                                        "edit_file",
                                        "src/App.jsx",
                                    ),
                                    makeFileOperationsAndTargetFilePathsInnerValue(
                                        "edit_file",
                                        "src/index.css",
                                    )
                                )
                            ),
                            commitDetailsAndFileOpsInnerValue(
                                "Add logic to randomly place mines on the game board",
                                "This commit adds the logic to randomly place mines on the game board based on the selected difficulty level. The logic is implemented in the `src/App.jsx` file. The `placeMines` function is added, which takes the difficulty level as a parameter and generates a random placement of mines on the game board. The function uses the `Math.random` method to generate random coordinates within the grid size. It then checks if the generated coordinates are already occupied by a mine or the starting cell. If not, it places a mine at the coordinates. The number of mines placed is determined by the difficulty level. The `placeMines` function is called in the `useEffect` hook to initialize the game board with mines when the component mounts.",
                                listOf(
                                    makeFileOperationsAndTargetFilePathsInnerValue(
                                        "edit_file",
                                        "src/App.jsx",
                                    ),
                                )
                            ),
                        ),
                        SourcedValueGeneratorInfo(emptyList()),
                    ),
                    CommitDetailsAndFileOps.initialize(
                        SourcedValueKind.CONSTANT,
                        listOf(
                            commitDetailsAndFileOpsInnerValue(
                                "Add game over logic",
                                "This commit adds the necessary logic to handle game over conditions in the Minesweeper game web application. The logic is implemented in the `App.jsx` file. The `handleCellClick` function is modified to check if the clicked cell contains a mine. If it does, the game is over and the `gameOver` state is set to `true`. The `handleCellClick` function is also modified to check if all safe cells have been uncovered. If they have, the game is over and the `gameOver` state is set to `true`. Additionally, a new function `handleGameOver` is added to handle the game over state. This function displays a message to the user indicating whether they won or lost the game. The `handleGameOver` function is called when the game over conditions are met.",
                                listOf(
                                    makeFileOperationsAndTargetFilePathsInnerValue(
                                        "edit_file",
                                        "src/App.jsx"
                                    )
                                )
                            ),
                            commitDetailsAndFileOpsInnerValue(
                                "Add logic to place mines on the board",
                                "This commit adds the logic to randomly place mines on the game board. The logic is implemented in the `src/App.jsx` file. The `placeMines` function is added, which takes the difficulty level as a parameter and generates a random placement of mines on the game board. The number of mines placed is determined by the difficulty level.",
                                listOf(
                                    makeFileOperationsAndTargetFilePathsInnerValue(
                                        "edit_file",
                                        "src/App.jsx",
                                    ),
                                )
                            ),
                        ),
                        SourcedValueGeneratorInfo(emptyList()),
                    ),
                ),
                SourcedStruct1(
                    CommitDetailsAndFileOps.initialize(
                        SourcedValueKind.CONSTANT,
                        listOf(
                            commitDetailsAndFileOpsInnerValue(
                                "Add game over logic",
                                "This commit adds the necessary logic to handle game over conditions in the Minesweeper game web application. The logic is implemented in the `App.jsx` file. The `handleCellClick` function is modified to check if the clicked cell contains a mine. If it does, the game is over and the `gameOver` state is set to `true`. The `handleCellClick` function is also modified to check if all safe cells have been uncovered. If they have, the game is over and the `gameOver` state is set to `true`. Additionally, a new function `handleGameOver` is added to handle the game over state. This function displays a message to the user indicating whether they won or lost the game. The `handleGameOver` function is called when the game over conditions are met.",
                                listOf(
                                    makeFileOperationsAndTargetFilePathsInnerValue(
                                        "edit_file",
                                        "src/App.jsx"
                                    )
                                )
                            ),
                        ),
                        SourcedValueGeneratorInfo(emptyList()),
                    ),
                )
            )
            .withExample(
                SourcedStruct6(
                    ArbrProjectFullNameValue.constant("podtown-research/minesweeper"),
                    ArbrProjectPlatformValue.constant("Web"),
                    ArbrProjectDescriptionValue.constant("This project is about implementing a basic Minesweeper game as a Web app. Minesweeper is a classic puzzle game where the player must uncover cells on a grid without hitting any mines. The game involves strategic thinking and careful decision-making to avoid mines and uncover all safe cells. The Web app will provide a user-friendly interface for playing the game and will include features such as customizable grid sizes and difficulty levels."),
                    ArbrTaskTaskQueryValue.constant("Implement a basic Minesweeper game as a Web app"),
                    CommitDetailsAndFileOps.initialize(
                        SourcedValueKind.CONSTANT,
                        listOf(
                            commitDetailsAndFileOpsInnerValue(
                                "Implement logic to flag cells as potential mines",
                                "This commit adds the logic to flag cells as potential mines in the Minesweeper game web application. The changes are made to the `App.jsx` file. The logic is implemented using React state and event handlers. When a cell is right-clicked, the `handleCellRightClick` function is called, which prevents the default context menu behavior and toggles the `isFlagged` state of the cell. The `isFlagged` state is used to determine the appearance of the cell, with a flag icon indicating that the cell is flagged as a potential mine. The `handleCellRightClick` function is added as an event listener to each cell element in the `renderGrid` function.",
                                listOf(
                                    makeFileOperationsAndTargetFilePathsInnerValue(
                                        "edit_file",
                                        "src/App.jsx",
                                    )
                                )
                            ),
                        ),
                        SourcedValueGeneratorInfo(emptyList()),
                    ),
                    CommitDetailsAndFileOps.initialize(
                        SourcedValueKind.CONSTANT,
                        listOf(
                            commitDetailsAndFileOpsInnerValue(
                                "Add game over logic",
                                "This commit adds the logic to flag cells as potential mines in the Minesweeper game web application. The changes are made to the `App.jsx` file. The logic is implemented using React state and event handlers. When a cell is right-clicked, the `handleCellRightClick` function is called, which prevents the default context menu behavior and toggles the `isFlagged` state of the cell. The `isFlagged` state is used to determine the appearance of the cell, with a flag icon indicating that the cell is flagged as a potential mine. The `handleCellRightClick` function is added as an event listener to each cell element in the `renderGrid` function.",
                                listOf(
                                    makeFileOperationsAndTargetFilePathsInnerValue(
                                        "edit_file",
                                        "src/App.jsx",
                                    )
                                )
                            ),
                        ),
                        SourcedValueGeneratorInfo(emptyList()),
                    ),
                ),
                SourcedStruct1(
                    CommitDetailsAndFileOps.initialize(
                        SourcedValueKind.CONSTANT,
                        listOf(
                            commitDetailsAndFileOpsInnerValue(
                                "Add game over logic",
                                "This commit adds the logic to flag cells as potential mines in the Minesweeper game web application. The changes are made to the `App.jsx` file. The logic is implemented using React state and event handlers. When a cell is right-clicked, the `handleCellRightClick` function is called, which prevents the default context menu behavior and toggles the `isFlagged` state of the cell. The `isFlagged` state is used to determine the appearance of the cell, with a flag icon indicating that the cell is flagged as a potential mine. The `handleCellRightClick` function is added as an event listener to each cell element in the `renderGrid` function.",
                                listOf(
                                    makeFileOperationsAndTargetFilePathsInnerValue(
                                        "edit_file",
                                        "src/App.jsx",
                                    )
                                )
                            )
                        ),
                        SourcedValueGeneratorInfo(emptyList()),
                    ),
                )
            )
            .build()

    fun taskVerbosePlanApplication() =
        aiApplicationConfigFactory
            .builder("plan-task-verbose-v2")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrProject.Platform)
                    .p(ArbrProject.Description)
                    .p(FilePaths)
                    .p(ArbrTask.TaskQuery)
            }
            .withOutputSchema {
                p(ArbrTask.TaskVerbosePlan)
            }
            .withInstructions { inputSchemaDescription, outputSchemaDescription ->
                (
                        "You are an assistant who, given an input of the following form:\n" +
                                inputSchemaDescription +
                                "\n\nProduces an output of the following form:\n" +
                                outputSchemaDescription +
                                "\n\nThe outputted verbose plan should contain a sequence of step-by-step instructions for implementing the given task, taking into account the provided contextual information for the project. The plan should include only steps corresponding to code changes such as those that would be done by an engineer to implement the feature. Omit steps for creating directories, as creating files within them will create the directories automatically."
                        )
            }
            .withExample(
                SourcedStruct5(
                    ArbrProjectFullNameValue.constant("podtown-research/minesweeper"),
                    ArbrProjectPlatformValue.constant("Web"),
                    ArbrProjectDescriptionValue.constant("A simple web implementation of a minesweeper game"),
                    FilePaths.initializeMerged(
                        listOf(
                            FilePathsContainer(ArbrFile.FilePath.constant(".gitignore")),
                            FilePathsContainer(ArbrFile.FilePath.constant("README.md")),
                            FilePathsContainer(ArbrFile.FilePath.constant("index.html")),
                            FilePathsContainer(ArbrFile.FilePath.constant("package-lock.json")),
                            FilePathsContainer(ArbrFile.FilePath.constant("package.json")),
                            FilePathsContainer(ArbrFile.FilePath.constant("public/vite.svg")),
                            FilePathsContainer(ArbrFile.FilePath.constant("src/App.css")),
                            FilePathsContainer(ArbrFile.FilePath.constant("src/App.jsx")),
                            FilePathsContainer(ArbrFile.FilePath.constant("src/assets/react.svg")),
                            FilePathsContainer(ArbrFile.FilePath.constant("src/index.css")),
                            FilePathsContainer(ArbrFile.FilePath.constant("src/main.jsx")),
                            FilePathsContainer(ArbrFile.FilePath.constant("vite.config.js")),
                        ),
                    ),
                    ArbrTaskTaskQueryValue.constant("Implement the Minesweeper game")
                ),
                SourcedStruct1(
                    ArbrTaskTaskVerbosePlanValue.constant(
                        """
To implement the Minesweeper game for the Minesweeper project, follow these steps:
1. Create a function to generate the game board with the specified difficulty.
2. Display the basic game board layout in the App component.
3. Add logic to randomly place mines on the game board.
4. Calculate the number of adjacent mines for each cell on the game board.
5. Implement the logic to reveal cells when clicked by the user.
6. Implement the logic to flag cells as potential mines.
7. Add logic to handle game over conditions, such as revealing a mine.
                        """.trimIndent()
                    ),
                )
            )
            .withNumExamplesToIncludeInPrompt(0)
            .withModel(OpenAiChatCompletionModel.GPT_4_0125_PREVIEW)
            .build()


    fun todoApplication() =
        aiApplicationConfigFactory
            .builder("segment-contains-todo")
            .withInputSchema {
                p(ArbrFileSegmentOp.Content)
            }
            .withOutputSchema {
                p(ArbrFileSegment.ContainsTodo)
            }
            .withInstructions { inputSchemaDescription, outputSchemaDescription ->
                "You are a coding assistant who identifies whether a code segment contains a placeholder for a full" +
                        " implementation, such as a TODO comment or a similar indication of a stub. The user gives the" +
                        " input in the format:\n" + inputSchemaDescription + "\n\nYou reply" +
                        " in the format:\n" + outputSchemaDescription + "\n\nGive a true result only if " +
                        "the file segment content contains an explicit placeholder. For an implementation that is " +
                        "simply incomplete, give a false result"
            }
            .withExample(
                SourcedStruct1(
                    ArbrFileSegmentOpContentValue.constant(
                        """
class Cell extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      // Initialize cell state
    };
  }

  render() {
    return (
      <div className='cell' onClick={this.props.onClick} onContextMenu={this.props.onContextMenu}>
        {/* Render cell here */}
      </div>
    );
  }
}
                        """.trimIndent()
                    )
                ),
                SourcedStruct1(ArbrFileSegmentContainsTodoValue.constant(true))
            )
            .withExample(
                SourcedStruct1(
                    ArbrFileSegmentOpContentValue.constant(
                        """
export default defineConfig({
  plugins: [react()],
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'game-board': ['src/GameBoard.jsx'],
        },
      },
    },
  },
}
                        """.trimIndent()
                    )
                ),
                SourcedStruct1(ArbrFileSegmentContainsTodoValue.constant(false))
            )
            .withExample(
                SourcedStruct1(
                    ArbrFileSegmentOp.Content.constant(
                        """
(
    <React.StrictMode>
        <App/>
                        """.trimIndent()
                    )
                ),
                SourcedStruct1(ArbrFileSegmentContainsTodoValue.constant(false))
            )
            .withExample(
                SourcedStruct1(
                    ArbrFileSegmentOp.Content.constant(
                        """
function calculateAdjacentMines(board) {
    // TODO: Implement calculation of adjacent mines for each cell on the game board
    // ...
}
                        """.trimIndent()
                    )
                ),
                SourcedStruct1(ArbrFileSegmentContainsTodoValue.constant(true))
            )
            .withNumExamplesToIncludeInPrompt(0)
            .build()

    fun todoImplPlanningApplication() =
        aiApplicationConfigFactory
            .builder("pipeline-impl-planning")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrProject.Platform)
                    .p(ArbrProject.Description)
                    .p(ArbrTask.TaskQuery)
                    .p(FilePathsAndSummaries)
                    .p(ArbrFileSegmentOp.Content)
            }
            .withOutputSchema {
                p(CommitDetailsAndFileOps)
            }
            .withInstructions { inputSchemaDescription, outputSchemaDescription ->
                (
                        "You are a code project planning assistant. Given an input of the following form:\n"
                                + inputSchemaDescription
                                + "\n\nPlan the commits needed to fill in the incomplete stubbed implementation given" +
                                " as the file segment content.\nYour output has the format:\n"
                                + outputSchemaDescription
                        )
            }
            .withPromptShortener { record, _ ->
                record.copy(
                    t5 = record.t5.copy(value = record.t5.value.dropLast(1))
                )
            }
            .withMaxTokens(1024) // Warning: this prompt gets very close to max
            .build()

    fun featurePullRequestDetailsApplication() =
        aiApplicationConfigFactory
            .builder("pipeline-feature-pull-request-details-inner")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrProject.Platform)
                    .p(ArbrProject.Description)
                    .p(ArbrTask.TaskQuery)
                    .p(CommitMessages)
            }
            .withOutputSchema {
                p(ArbrTask.PullRequestTitle)
                    .p(ArbrTask.PullRequestBody)
            }
            .withInstructions { inputDescription, outputDescription ->
                (
                        "You are a code project planning assistant. The user will give an input of the following form:\n"
                                + inputDescription
                                + "\n\nYour output has the format:\n"
                                + outputDescription
                                + "\n\nYou should give a pull request title and the contents of the pull request body" +
                                " suitable for GitHub."
                        )
            }
            .build()

    fun contextualCommitCompletionApplication() =
        aiApplicationConfigFactory
            .builder("contextual-commit-completion-4")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrTask.TaskQuery)
                    .withKey("main_task")
                    .withDescription("Main Task: The main task being accomplished, which this commit is a part of.")
                    .p(ArbrSubtask.Subtask)
                    .withKey("subtask")
                    .withDescription("Subtask: A subtask of the main task being accomplished, which this commit is a part of.")
                    .p(ArbrCommit.CommitMessage)
                    .withKey("current_commit_message")
                    .withDescription("Current Commit Message: The commit message for this commit being evaluated.")
                    .p(ArbrCommit.DiffSummary)
                    .withKey("current_commit_summary")
                    .withDescription("Current Commit Summary: A summary of the goal of this commit.")
                    .p(FilePathsAndContents) // relevant other file contents
                    .withKey("relevant_other_file_contents")
                    .p(FilePathsAndContents) // updated file contents
                    .withKey("updated_file_contents")
            }
            .withInputSerializer(TrailingContentPropertySerializers.commitCompletionPropertyListSerializer)
            .withOutputSchema {
                p(ArbrCommitEval.PartiallyComplete)
                    .p(ArbrCommitEval.MostlyComplete)
                    .p(ArbrCommitEval.Complete)
                    .p(FileOperationsAndTargetFilePathsWithDescriptions) // TODO: Make optional
                    .withKey("necessary_remaining_operations")
            }
            .withInstructions { inputSchemaDescription, outputSchemaDescription ->
                (
                        "You are an assistant who, given an input of the following form:\n"
                                + inputSchemaDescription
                                + "\n\nProduces an output of the following form:\n"
                                + outputSchemaDescription
                                + "\n\nYour goal is to decide if the goal of the current commit has been accomplished given" +
                                " the current state of the provided files in the project." +
                                " If the code reflects significant, non-trivial progress towards accomplishing the goal, then you " +
                                "should flag the commit as at least partially complete. If the code reflects complete " +
                                "accomplishment the commit, you should flag the commit as complete. If the commit is not completely" +
                                " accomplished, you should describe at most 2 remaining code changes. " +
                                "Always give a boolean value \"true\" or \"false\" for each of the flags. If the commit is " +
                                "only partially complete, always give at least one remaining change. Ensure the output is formatted as YAML." +
                                "\nEach remaining commit should be given as an imperative statement similar to a commit message." +
                                " If there are unrelated changes included, ignore them for the sake of evaluating the completion progress of the commit." +
                                " You may act optimistically about the completion status of the commit; if it is nearly complete, mark it as complete."
                        )
            }
            .withExample(
                SourcedStruct7(
                    ArbrProject.FullName.constant("podtown-research/minesweeper"),
                    ArbrTask.TaskQuery.constant("Implement a simple Minesweeper game as a Web app"),
                    ArbrSubtask.Subtask.constant("Calculate the number of adjacent mines for each cell on the game board."),
                    ArbrCommit.CommitMessage.constant("Implement mine adjacency calculation"),
                    ArbrCommit.DiffSummary.constant("This commit implements the calculation of the number of adjacent mines for each cell on the game board. The changes are made to the `src/App.jsx` file. The `calculateAdjacentMines` function is added, which takes the current game board as input and returns a new board with the number of adjacent mines calculated for each cell. The function iterates over each cell in the board and checks its neighboring cells to count the number of mines. The result is stored in a new board, which is then returned by the function. The `calculateAdjacentMines` function is called in the `App` component's render method to update the game board with the calculated values. This commit also includes necessary imports and updates to the component's state and rendering logic."),
                    FilePathsAndContents.initializeMerged(
                        listOf(
                            SourcedStruct2(
                                ArbrFile.FilePath.constant("src/main.jsx"),
                                ArbrFile.Content.constant(
                                    "import React from 'react'\n" +
                                            "import ReactDOM from 'react-dom/client'\n" +
                                            "import App from './App.jsx'\n" +
                                            "import './index.css'\n" +
                                            "\n" +
                                            "ReactDOM.createRoot(document.getElementById('root')).render(\n" +
                                            "    <React.StrictMode>\n" +
                                            "        <App/>\n" +
                                            "    </React.StrictMode>,\n" +
                                            ")\n" +
                                            "\n" +
                                            "// Add logic to randomly place mines on the game board\n" +
                                            "function placeMines(numMines) {\n" +
                                            "    // TODO: Implement logic to randomly place mines on the game board\n" +
                                            "}\n" +
                                            "\n" +
                                            "export default placeMines\n"
                                ),
                            ),
                            SourcedStruct2(
                                ArbrFile.FilePath.constant("src/index.css"),
                                ArbrFile.Content.constant(
                                    ":root {\n" +
                                            "  font-family: Inter, system-ui, Avenir, Helvetica, Arial, sans-serif;\n" +
                                            "  line-height: 1.5;\n" +
                                            "  font-weight: 400;\n" +
                                            "\n" +
                                            "  color-scheme: light dark;\n" +
                                            "  color: rgba(255, 255, 255, 0.87);\n" +
                                            "  background-color: #242424;\n" +
                                            "\n" +
                                            "  font-synthesis: none;\n" +
                                            "  text-rendering: optimizeLegibility;\n" +
                                            "  -webkit-font-smoothing: antialiased;\n" +
                                            "  -moz-osx-font-smoothing: grayscale;\n" +
                                            "  -webkit-text-size-adjust: 100%;\n" +
                                            "}\n" +
                                            "\n" +
                                            "a {\n" +
                                            "  font-weight: 500;\n" +
                                            "  color: #646cff;\n" +
                                            "  text-decoration: inherit;\n" +
                                            "}\n" +
                                            "a:hover {\n" +
                                            "  color: #535bf2;\n" +
                                            "}\n" +
                                            "\n" +
                                            "body {\n" +
                                            "  margin: 0;\n" +
                                            "  display: flex;\n" +
                                            "  place-items: center;\n" +
                                            "  min-width: 320px;\n" +
                                            "  min-height: 100vh;\n" +
                                            "}\n" +
                                            "\n" +
                                            "h1 {\n" +
                                            "  font-size: 3.2em;\n" +
                                            "  line-height: 1.1;\n" +
                                            "}\n" +
                                            "\n" +
                                            "button {\n" +
                                            "  border-radius: 8px;\n" +
                                            "  border: 1px solid transparent;\n" +
                                            "  padding: 0.6em 1.2em;\n" +
                                            "  font-size: 1em;\n" +
                                            "  font-weight: 500;\n" +
                                            "  font-family: inherit;\n" +
                                            "  background-color: #1a1a1a;\n" +
                                            "  cursor: pointer;\n" +
                                            "  transition: border-color 0.25s;\n" +
                                            "}\n" +
                                            "button:hover {\n" +
                                            "  border-color: #646cff;\n" +
                                            "}\n" +
                                            "button:focus,\n" +
                                            "button:focus-visible {\n" +
                                            "  outline: 4px auto -webkit-focus-ring-color;\n" +
                                            "}\n" +
                                            "\n" +
                                            "@media (prefers-color-scheme: light) {\n" +
                                            "  :root {\n" +
                                            "    color: #213547;\n" +
                                            "    background-color: #ffffff;\n" +
                                            "  }\n" +
                                            "  a:hover {\n" +
                                            "    color: #747bff;\n" +
                                            "  }\n" +
                                            "  button {\n" +
                                            "    background-color: #f9f9f9;\n" +
                                            "  }\n" +
                                            "}\n"
                                ),
                            ),
                        )
                    ),
                    FilePathsAndContents.initializeMerged(
                        listOf(
                            SourcedStruct2(
                                ArbrFile.FilePath.constant("src/App.jsx"),
                                ArbrFile.Content.constant(
                                    "import React, { useState } from 'react'\n" +
                                            "import viteLogo from '/vite.svg'\n" +
                                            "import './App.css'\n" +
                                            "\n" +
                                            "function App() {\n" +
                                            "    const [count, setCount] = useState(0)\n" +
                                            "\n" +
                                            "    function generateGameBoard(difficulty) {\n" +
                                            "        // TODO: Implement game board generation logic\n" +
                                            "    }\n" +
                                            "\n" +
                                            "    function placeMines(numMines) {\n" +
                                            "        for (let i = 0; i < numMines; i++) {\n" +
                                            "            // Generate random coordinates for each mine\n" +
                                            "            const row = Math.floor(Math.random() * 10)\n" +
                                            "            const col = Math.floor(Math.random() * 10)\n" +
                                            "\n" +
                                            "            // Update the game board accordingly\n" +
                                            "            // TODO: Implement logic to place mines on the game board\n" +
                                            "        }\n" +
                                            "    }\n" +
                                            "\n" +
                                            "    function calculateAdjacentMines(board) {\n" +
                                            "        // TODO: Calculate adjacent mines\n" +
                                            "    }\n" +
                                            "\n" +
                                            "    // Call placeMines when the component is mounted\n" +
                                            "    React.useEffect(() => {\n" +
                                            "        placeMines(10) // Change the number of mines as desired\n" +
                                            "    }, [])\n" +
                                            "\n" +
                                            "    return (\n" +
                                            "        <>\n" +
                                            "            <div className=\"game-board\">\n" +
                                            "            </div>\n" +
                                            "        </>\n" +
                                            "    )\n" +
                                            "}\n" +
                                            "\n" +
                                            "export default App\n" +
                                            "\n"
                                ),
                            ),
                        ),
                    ),
                ),
                SourcedStruct4(
                    ArbrCommitEval.PartiallyComplete.constant(true),
                    ArbrCommitEval.MostlyComplete.constant(false),
                    ArbrCommitEval.Complete.constant(false),
                    FileOperationsAndTargetFilePathsWithDescriptions.initializeMerged(
                        listOf(
                            SourcedStruct3(
                                ArbrFileOp.FileOperation.constant("edit_file"),
                                ArbrFile.FilePath.constant("src/App.jsx"),
                                ArbrFileOp.Description.constant(
                                    "Update the `generateGameBoard` function to generate the game board layout based on the difficulty level. Currently, the function is empty and needs to be implemented. Then, implement the logic in the `placeMines` function to place the specified number of mines on the game board. Currently, the function generates random coordinates for each mine, but does not update the game board accordingly. Also, implement the `calculateAdjacentMines` function and the contents of the returned React component."
                                )
                            )
                        )
                    ),
                ),
            )
            .withOutputProcessor {
                // Common bad output for some reason
                it.copy(content = it.content.replace("partial_complete:", "partially_complete:"))
            }
            .withPromptShortener { record, _ ->
                record.copy(
                    t6 = record.t6.let { filePathsAndContents ->
                        filePathsAndContents.copy(
                            value = filePathsAndContents.value.dropLast(1)
                        )
                    }
                )
            }
            .withMaxTokens(512)
            .build()

    fun fileOpsDetailApplication() =
        aiApplicationConfigFactory
            .builder("commit-file-op-detail")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrTask.TaskQuery)
                    .withKey("main_task")
                    .withDescription("Main Task: The main task being accomplished, which this commit is a part of.")
                    .p(ArbrSubtask.Subtask)
                    .withKey("subtask")
                    .withDescription("Subtask: A subtask of the main task being accomplished, which this commit is a part of.")
                    .p(ArbrCommit.CommitMessage)
                    .withKey("current_commit_message")
                    .withDescription("Current Commit Message: The commit message for this commit.")
                    .p(ArbrCommit.DiffSummary)
                    .withKey("current_commit_summary")
                    .withDescription("Current Commit Summary: A summary of the goal of this commit.")
                    .p(FilePathsAndContents)
                    .withKey("relevant_file_contents")
                    .withDescription("Relevant File Contents: Contents of some files that seem relevant to the task.")
                    .p(FileOperationsAndTargetFilePaths)
                    .withKey("current_file_operations")
                    .withDescription("File Operations: The currently planned file operations for this commit which need descriptions.")
            }
            .withOutputSchema {
                p(FileOperationsAndTargetFilePathsWithDescriptions)
                    .withKey("necessary_operations")
            }
            .withInstructions { inputSchemaDescription, outputSchemaDescription ->
                (
                        "You are an assistant who plans implementations of code changes. Given an input of the following form:\n\n"
                                + inputSchemaDescription
                                + "\n\nYour goal is to add descriptions to the given current file operations in order to inform" +
                                " how the implementation should be carried out in order to accomplish the goal of the" +
                                " current commit. You may also omit entries from the list of current file operations in" +
                                " your response if they are not necessary for the commit. Focus on the goal of the current" +
                                " commit. Your output should be strictly YAML conforming to the following schema: \n\n" + outputSchemaDescription
                        )
            }
            .withExample(
                SourcedStruct7(
                    ArbrProject.FullName.constant("podtown-research/minesweeper"),
                    ArbrTask.TaskQuery.constant("Implement a simple Minesweeper game as a Web app"),
                    ArbrSubtask.Subtask.constant("Display the basic game board layout in the App component."),
                    ArbrCommit.CommitMessage.constant("Create basic game board layout in App component"),
                    ArbrCommit.DiffSummary.constant("This commit adds the basic game board layout to the `App` component in `src/App.jsx`. The layout consists of a grid of cells, each representing a tile on the Minesweeper game board. The cells are rendered using a nested `div` structure, with each cell having a unique identifier based on its row and column position. The styling for the cells is defined in `src/index.css`, which sets the width, height, border, and background color. The commit also updates the `App` component to render the grid of cells and display them on the web page. The cells are currently empty and do not have any functionality attached to them."),
                    FilePathsAndContents.initializeMerged(
                        listOf(
                            SourcedStruct2(
                                ArbrFile.FilePath.constant("src/App.jsx"),
                                ArbrFile.Content.constant(
                                    "import React, { useState, useEffect } from 'react'\nimport viteLogo from '/vite.svg'\nimport './App.css'\n\nfunction App() {\n    const [count, setCount] = useState(0)\n    const [flags, setFlags] = useState([])\n    const [gameOver, setGameOver] = useState(false)\n    const [board, setBoard] = useState([])\n\n    useEffect(() => {\n        placeMines(board, 10)\n    }, [])\n\n    function generateGameBoard(difficulty) {\n        const newBoard = []\n        for (let i = 0; i < difficulty; i++) {\n            const row = []\n            for (let j = 0; j < difficulty; j++) {\n                row.push({\n                    value: 0,\n                    revealed: false,\n                    flagged: false,\n                })\n            }\n            newBoard.push(row)\n        }\n        setBoard(newBoard)\n    }\n\n    function placeMines(board, numMines) {\n        for (let i = 0; i < numMines; i++) {\n            const row = Math.floor(Math.random() * board.length)\n            const col = Math.floor(Math.random() * board.length)\n            board[row][col].value = -1\n        }\n    }\n\n    function calculateAdjacentMines(board) {\n        const newBoard = []\n        for (let i = 0; i < board.length; i++) {\n            const newRow = []\n            for (let j = 0; j < board[i].length; j++) {\n                const cell = board[i][j]\n                let adjacentMines = 0\n                for (let x = -1; x <= 1; x++) {\n                    for (let y = -1; y <= 1; y++) {\n                        if (x === 0 && y === 0) continue\n                        const row = i + x\n                        const col = j + y\n                        if (row >= 0 && row < board.length && col >= 0 && col < board[i].length) {\n                            if (board[row][col].value === -1) {\n                                adjacentMines++\n                            }\n                        }\n                    }\n                }\n                newRow.push({\n                    ...cell,\n                    adjacentMines: adjacentMines\n                })\n            }\n            newBoard.push(newRow)\n        }\n        return newBoard\n    }\n\n    function handleFlagClick(row, col) {\n        if (!board[row][col].revealed) {\n            const newFlags = [...flags]\n            newFlags[row][col] = !newFlags[row][col]\n            setFlags(newFlags)\n        }\n    }\n\n    function handleGameOver() {\n        setGameOver(true)\n    }\n\n    function handleCellClick(row, col) {\n        if (!board[row][col].revealed) {\n            const newBoard = [...board]\n            newBoard[row][col].revealed = true\n            setBoard(newBoard)\n            if (newBoard[row][col].value === -1) {\n                handleGameOver()\n            } else if (newBoard[row][col].value === 0) {\n                revealAdjacentCells(row, col)\n            }\n        }\n    }\n\n    function revealAdjacentCells(row, col) {\n        const newBoard = [...board]\n        const queue = [[row, col]]\n        while (queue.length > 0) {\n            const [currentRow, currentCol] = queue.shift()\n            for (let x = -1; x <= 1; x++) {\n                for (let y = -1; y <= 1; y++) {\n                    const newRow = currentRow + x\n                    const newCol = currentCol + y\n                    if (newRow >= 0 && newRow < newBoard.length && newCol >= 0 && newCol < newBoard[currentRow].length) {\n                        if (!newBoard[newRow][newCol].revealed) {\n                            newBoard[newRow][newCol].revealed = true\n                            if (newBoard[newRow][newCol].value === 0) {\n                                queue.push([newRow, newCol])\n                            }\n                        }\n                    }\n                }\n            }\n        }\n        setBoard(newBoard)\n    }\n\n    return (\n        <>\n            <div>\n                <img src={viteLogo} className=\"logo\" alt=\"Vite logo\" />\n            </div>\n            <h1>Placeholder</h1>\n            {gameOver ? (\n                <div className=\"game-over-message\">Game Over!</div>\n            ) : (\n                <div className=\"card\">\n                    <button onClick={() => setCount((count) => count + 1)}>\n                        count is {count}\n                    </button>\n                    <p>\n                        Edit <code>src/App.jsx</code> and save to test HMR\n                    </p>\n                </div>\n            )}\n        </>\n    )\n}\n\nexport default App\n"
                                ),
                            ),
                            SourcedStruct2(
                                ArbrFile.FilePath.constant("src/index.css"),
                                ArbrFile.Content.constant(
                                    ":root {\n  font-family: Inter, system-ui, Avenir, Helvetica, Arial, sans-serif;\n  line-height: 1.5;\n  font-weight: 400;\n\n  color-scheme: light dark;\n  color: rgba(255, 255, 255, 0.87);\n  background-color: #242424;\n\n  font-synthesis: none;\n  text-rendering: optimizeLegibility;\n  -webkit-font-smoothing: antialiased;\n  -moz-osx-font-smoothing: grayscale;\n  -webkit-text-size-adjust: 100%;\n}\n\na {\n  font-weight: 500;\n  color: #646cff;\n  text-decoration: inherit;\n}\na:hover {\n  color: #535bf2;\n}\n\nbody {\n  margin: 0;\n  display: flex;\n  place-items: center;\n  min-width: 320px;\n  min-height: 100vh;\n}\n\nh1 {\n  font-size: 3.2em;\n  line-height: 1.1;\n}\n\nbutton {\n  border-radius: 8px;\n  border: 1px solid transparent;\n  padding: 0.6em 1.2em;\n  font-size: 1em;\n  font-weight: 500;\n  font-family: inherit;\n  background-color: #1a1a1a;\n  cursor: pointer;\n  transition: border-color 0.25s;\n}\nbutton:hover {\n  border-color: #646cff;\n}\nbutton:focus,\nbutton:focus-visible {\n  outline: 4px auto -webkit-focus-ring-color;\n}\n\n@media (prefers-color-scheme: light) {\n  :root {\n    color: #213547;\n    background-color: #ffffff;\n  }\n  a:hover {\n    color: #747bff;\n  }\n  button {\n    background-color: #f9f9f9;\n  }\n}\n"
                                ),
                            ),
                            SourcedStruct2(
                                ArbrFile.FilePath.constant("index.html"),
                                ArbrFile.Content.constant(
                                    "<!doctype html>\n<html lang=\"en\">\n  <head>\n    <meta charset=\"UTF-8\" />\n    <link rel=\"icon\" type=\"image/svg+xml\" href=\"/vite.svg\" />\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n    <!-- Placeholder title. TODO: Replace with application name -->\n    <title>Application</title>\n  </head>\n  <body>\n    <div id=\"root\"></div>\n    <script type=\"module\" src=\"/src/main.jsx\"></script>\n  </body>\n</html>\n"
                                ),
                            ),
                        )
                    ),
                    FileOperationsAndTargetFilePaths.initializeMerged(
                        listOf(
                            SourcedStruct2(
                                ArbrFileOp.FileOperation.constant("edit_file"),
                                ArbrFile.FilePath.constant("src/App.jsx")
                            ),
                        )
                    )
                ),
                SourcedStruct1(
                    FileOperationsAndTargetFilePathsWithDescriptions.initializeMerged(
                        listOf(
                            SourcedStruct3(
                                ArbrFileOp.FileOperation.constant("edit_file"),
                                ArbrFile.FilePath.constant("src/App.jsx"),
                                ArbrFileOp.Description.constant(
                                    "Modify the `App` component to include a grid of cells representing the Minesweeper game board. Each cell should be a `div` element with a unique identifier based on its row and column position. The cells should be styled using CSS classes defined in `src/index.css`. The `App` component should also be updated to render the grid of cells and display them on the web page. The cells should initially be empty and not have any functionality attached to them. This can be achieved by creating a new function `renderGameBoard` that generates the grid of cells and is called in the `return` statement of the `App` component. The `renderGameBoard` function should iterate over the `board` state variable and create a `div` for each cell, with the `id` attribute set to the row and column position of the cell."
                                )
                            )
                        )
                    ),
                )
            )
            .withPromptShortener { record, _ ->
                record.copy(
                    t6 = record.t6.let { relevantFiles ->
                        relevantFiles.copy(
                            value = relevantFiles.value.dropLast(1)
                        )
                    }
                )
            }
            .withMaxTokens(512)
            // TODO: Consider smaller model or Anthropic - this ends up being very blocking
//        .withModel(OpenAiChatCompletionModel.GPT_4_0125_PREVIEW)
            .build()

    fun fileOpNewToFileSegOps() =
        aiApplicationConfigFactory
            .builder("file-op-new-to-file-seg-ops")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrTask.TaskQuery)
                    .withKey("main_task")
                    .withDescription("Main Task: The main task being accomplished, which this commit is a part of.")
                    .p(ArbrSubtask.Subtask)
                    .withKey("subtask")
                    .withDescription("Subtask: A subtask of the main task being accomplished, which this commit is a part of.")
                    .p(ArbrCommit.CommitMessage)
                    .withKey("current_commit_message")
                    .withDescription("Current Commit Message: The commit message for this commit.")
                    .p(ArbrCommit.DiffSummary)
                    .withKey("current_commit_summary")
                    .withDescription("Current Commit Summary: A summary of the goal of this commit.")
                    .p(ArbrFileOp.FileOperation)
                    .withKey("current_file_operation")
                    .withDescription("Current File Operation: The operation to perform on the file.")
                    .p(ArbrFile.FilePath)
                    .withKey("current_file_operation_target_file_path")
                    .withDescription("Current File Operation Target File Path: Path of the file on which to perform the current file operation.")
                    .p(ArbrFileOp.Description)
                    .withKey("current_file_operation_description")
                    .withDescription("Current File Operation Description: Description of the code changes required as part of the current file operation.")
            }
            .withOutputSchema {
                p(FileSegmentOperations)
            }
            .withModel(OpenAiChatCompletionModel.GPT_4_0125_PREVIEW)
            /*
            NestedObjectListType5(
                "source_element_operations",
                "Source Element Operations: Operations on source code elements, i.e. segments of the file, including the operation (one of [add, edit, delete]), the content type of the element, the kind of source code element, the name of the source code element within the file, and a description of the change.",
                ArbrFileSegmentOp.Operation,
                ArbrFileSegment.ContentType,
                ArbrFileSegment.RuleName,
                ArbrFileSegment.Name,
                ArbrFileSegmentOp.Description,
            )
             */
            .withInstructions { inputSchemaDescription, outputSchemaDescription ->
                (
                        /*
                "NestedStatementContext" -> "block"
                "FunctionDeclarationContext" -> "function"
                "ClassDeclarationContext" -> "class"
                "HtmlElementContext" -> "element"
                "ProgramContext",
                "StylesheetContext",
                "HtmlDocumentContext",
                "WholeFile" -> null
                         */
                        "You are an assistant who plans implementations of code changes. Given an input of the following form:\n\n"
                                + inputSchemaDescription
                                + "\n\nYour goal is to break down the given file operation into operations on source" +
                                " elements, i.e. segments of the file. You should include the operation, the content type (the source code language of the element), the source" +
                                " element kind (the name for the type of element represented by the source code, such as" +
                                " class, function, selector, etc.), the name of the element (the identifier by which the" +
                                " element is referenced in source code in a local scope, such as the name of the function" +
                                " or class), and finally a description of the change to the source element. In this case," +
                                " the file is being newly created via these operations, so every source element operation" +
                                " should be 'add' and the operations should reflect that the file is new. The set of valid" +
                                " rule names is determined by file type according to the following strict enumeration:\n" +
                                " - For JavaScript and JSX, the valid rule names are `class` and `function`.\n" +
                                " - For CSS, the only valid rule name is `block`.\n" +
                                " - For HTML, the only valid rule name is `element`.\n" +
                                " - For other file types, any rule name is valid.\n" +
                                "If a source element operation is required for the code change but doesn't fall neatly" +
                                " into one of these categories, you should include it with a rule name that would be most" +
                                " applicable, even if invalid. For example, an import statement might reasonably use the" +
                                " rule name `import`. But, you should try to stick to valid rule names whenever possible.\n\n" +
                                "Your output should be strictly YAML conforming to" +
                                " the following schema: \n\n" + outputSchemaDescription
                        )
            }
            .build()

    fun fileOpEditToFileSegOps() =
        aiApplicationConfigFactory
            .builder("file-op-edit-to-file-seg-ops")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrTask.TaskQuery)
                    .withKey("main_task")
                    .withDescription("Main Task: The main task being accomplished, which this commit is a part of.")
                    .p(ArbrSubtask.Subtask)
                    .withKey("subtask")
                    .withDescription("Subtask: A subtask of the main task being accomplished, which this commit is a part of.")
                    .p(ArbrCommit.CommitMessage)
                    .withKey("current_commit_message")
                    .withDescription("Current Commit Message: The commit message for this commit.")
                    .p(ArbrFileOp.FileOperation)
                    .withKey("current_file_operation")
                    .withDescription("Current File Operation: The operation to perform on the file.")
                    .p(ArbrFile.FilePath)
                    .withKey("current_file_operation_target_file_path")
                    .withDescription("Current File Operation Target File Path: Path of the file on which to perform the current file operation.")
                    .p(ArbrFileOp.Description)
                    .withKey("current_file_operation_description")
                    .withDescription("Current File Operation Description: Description of the code changes required as part of the current file operation.")
                    .p(FileSegments)
            }
            .withOutputSchema {
                p(FileSegmentOperations)
            }
            .withModel(OpenAiChatCompletionModel.GPT_4_0125_PREVIEW)
            .withInstructions { inputSchemaDescription, outputSchemaDescription ->
                (
                        "You are an assistant who plans implementations of code changes. Given an input of the following form:\n\n"
                                + inputSchemaDescription
                                + "\n\nYour goal is to break down the given file operation into operations on source" +
                                " elements, i.e. segments of the file. You should include the operation (`add` for new" +
                                " segments, `edit` for modifying existing segments, and `delete` for removing existing" +
                                " segments), the content type (the source code language of the element), the source" +
                                " element kind (the name for the type of element represented by the source code, such as" +
                                " class, function, selector, etc.), the name of the element (the identifier by which the" +
                                " element is referenced in source code in a local scope, such as the name of the function" +
                                " or class), and finally a description of the change to the source element.\n" +
                                "Your output should be strictly YAML conforming to" +
                                " the following schema: \n\n" + outputSchemaDescription
                        )
            }
//        .withExample(
//            SourcedStruct8(
//
//            ),
//            SourcedStruct1(
//                FileSegmentOperations.initializeMerged(
//                    listOf(
//                        SourcedStruct6(
//
//                        )
//                    )
//                )
//            )
//        )
            .build()

    fun fileSearchApplication() =
        aiApplicationConfigFactory
            .builder("commit-relevant-file-search-v3")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrCommit.CommitMessage)
                    .p(ArbrCommit.DiffSummary)
                    .p(
                        FileOperationsAndTargetFilePaths
                    )
                    .withKey("commit_file_operations")
                    .withDescription("Operations to be performed on files as part of this commit.")
                    .p(
                        FilePathsAndSummaries
                    )
                    .withKey("project_file_info")
            }
            .withOutputSchema {
                p(FilePaths)
                    .withKey("relevant_file_paths")
                    .withDescription("The paths of up to $maxNumFiles files in the project which are most relevant to the given commit info, with the most relevant file first.")
            }
            .withPromptShortener { record, _ ->
                val numToRemove = max(
                    1, (record.t5.value.size * 1.0 / 5).toInt()
                )

                record.copy(
                    t5 = record.t5.let { filePaths ->
                        filePaths.copy(
                            value = filePaths.value.dropLast(numToRemove)
                        )
                    }
                )
            }
            .build()


    fun subtaskFileSearchApplication() =
        aiApplicationConfigFactory
            .builder("subtask-relevant-file-search-v3")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrSubtask.Subtask)
                    .p(FilePathsAndSummaries)
                    .withKey("project_file_info")
            }
            .withOutputSchema {
                p(FilePaths)
                    .withKey("relevant_file_paths")
                    .withDescription("The paths of up to $maxNumFilesOutputted files in the project which are most relevant to the given subtask, with the most relevant file first.")
            }
            .withPromptShortener { record, _ ->
                val numToRemove = max(
                    1, (record.t3.value.size * 1.0 / 5).toInt()
                )

                record.copy(
                    t3 = record.t3.let { filePaths ->
                        filePaths.copy(
                            value = filePaths.value.dropLast(numToRemove)
                        )
                    }
                )
            }
            .build()

    fun taskFileSearchApplication() =
        aiApplicationConfigFactory
            .builder("task-relevant-file-search")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrTask.TaskQuery)
                    .p(FilePathsAndSummaries)
            }
            .withOutputSchema {
                p(FilePaths)
            }
            .withPromptShortener { record, _ ->
                val numToRemove = max(
                    1, (record.t3.value.size * 1.0 / 5).toInt()
                )

                record.copy(
                    t3 = record.t3.let { filePaths ->
                        filePaths.copy(
                            value = filePaths.value.dropLast(numToRemove)
                        )
                    }
                )
            }
            .build()

    fun taskCompletionApplication() =
        aiApplicationConfigFactory
            .builder("eval-task-completion-2")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrTask.TaskQuery)
                    .p(SubtaskPlans) // TODO: Include subtask evals
            }
            .withOutputSchema {
                p(ArbrTaskEval.PartiallyComplete)
                    .p(ArbrTaskEval.MostlyComplete)
                    .p(ArbrTaskEval.Complete)
                    .p(Subtasks)
                    .withKey("remaining_subtasks")
            }
            .withInstructions { inputSchemaDescription, outputSchemaDescription ->
                (
                        "You are an assistant who, given an input of the following form:\n"
                                + inputSchemaDescription
                                + "\n\nProduces an output of the following form:\n"
                                + outputSchemaDescription
                                + "\n\nYour goal is to decide if the task has been accomplished given" +
                                " the current state of the provided files in the project, according to the plan given as subtasks and associated commits." +
                                " If the code reflects significant, non-trivial progress towards accomplishing the goal, then you " +
                                "should flag the task as at least partially complete. If the code reflects complete " +
                                "accomplishment the task, you should flag the task as complete. If the task is not completely" +
                                " accomplished, you should provide the information for at most 2 remaining subtasks which " +
                                "should be completed in order to accomplish the given task." +
                                "Always give a boolean value \"true\" or \"false\" for each of the flags. If the task is " +
                                "only partially complete, always give at least one remaining subtask. Ensure the output is formatted as YAML." +
                                "\nThe remaining subtasks should be included in the remaining_subtasks field in the output. " +
                                "You may act optimistically about the completion status of the task; if it is nearly complete, mark it as complete."
                        )
            }
            .withOutputProcessor {
                // Common bad output for some reason
                it.copy(content = it.content.replace("partial_complete:", "partially_complete:"))
            }
            .withMaxTokens(512)
            .withModel(OpenAiChatCompletionModel.GPT_4_0125_PREVIEW)
            .build()

    fun subtaskCompletionApplication() =
        aiApplicationConfigFactory
            .builder("eval-subtask-completion-3")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrTask.TaskQuery)
                    .withKey("main_task")
                    .withDescription("Main Task: The main task being implemented.")
                    .p(Subtasks)
                    .withKey("all_subtasks")
                    .withDescription("All Subtasks: All the subtasks planned for implementation to accomplish the task.")
                    .p(ArbrSubtask.Subtask)
                    .withKey("current_subtask")
                    .withDescription("Current Subtask: The statement for the subtask that is currently being evaluated.")
                    .p(CommitDetailsAndFileOps) // TODO: Include commit evals
                    .withKey("current_subtask_commits")
                    .withDescription("Commits completed for the current subtask whose progress is being evaluated.")
            }
            .withOutputSchema {
                p(ArbrSubtaskEval.PartiallyComplete)
                    .p(ArbrSubtaskEval.MostlyComplete)
                    .p(ArbrSubtaskEval.Complete)
                    .p(CommitDetailsAndFileOps)
                    .withKey("remaining_subtask_commits")
                    .withDescription(
                        "Remaining Commit Details: Details for commits that are" +
                                " necessary to accomplish the current subtask."
                    )
            }
            .withInstructions { inputSchemaDescription, outputSchemaDescription ->
                (
                        "You are an assistant who, given an input of the following form:\n"
                                + inputSchemaDescription
                                + "\n\nProduces an output of the following form:\n"
                                + outputSchemaDescription
                                + "\n\nYour goal is to decide if the goal of the subtask has been accomplished given" +
                                " the current state of the provided files in the project." +
                                " If the code reflects significant, non-trivial progress towards accomplishing the goal, then you " +
                                "should flag the subtask as at least partially complete. If the code reflects complete " +
                                "accomplishment the subtask, you should flag the subtask as complete. If the subtask is not completely" +
                                " accomplished, you should provide the information for at most 2 remaining commits which " +
                                "should be completed in order to accomplish the given subtask." +
                                "Always give a boolean value \"true\" or \"false\" for each of the flags. If the subtask is " +
                                "only partially complete, always give at least one remaining commit. Ensure the output is formatted as YAML." +
                                "\nThe remaining commits should be included in the remaining_subtask_commits field in the output. " +
                                "You may act optimistically about the completion status of the subtask; if it is nearly complete, mark it as complete."
                        )
            }
            .withExample(
                SourcedStruct5(
                    ArbrProject.FullName.constant("podtown-research/minesweeper"),
                    ArbrTask.TaskQuery.constant("Implement a simple Minesweeper game as a Web app"),
                    Subtasks.initializeMerged(
                        listOf(
                            SourcedStruct1(ArbrSubtask.Subtask.constant("Create a function to generate the game board with the specified difficulty.")),
                            SourcedStruct1(ArbrSubtask.Subtask.constant("Display the basic game board layout in the App component.")),
                            SourcedStruct1(ArbrSubtask.Subtask.constant("Add logic to randomly place mines on the game board.")),
                            SourcedStruct1(ArbrSubtask.Subtask.constant("Calculate the number of adjacent mines for each cell on the game board.")),
                            SourcedStruct1(ArbrSubtask.Subtask.constant("Implement the logic to reveal cells when clicked by the user.")),
                            SourcedStruct1(ArbrSubtask.Subtask.constant("Implement the logic to flag cells as potential mines.")),
                            SourcedStruct1(ArbrSubtask.Subtask.constant("Add logic to handle game over conditions, such as revealing a mine.")),
                        )
                    ),
                    ArbrSubtask.Subtask.constant("Calculate the number of adjacent mines for each cell on the game board."),
                    CommitDetailsAndFileOps.initializeMerged(
                        listOf(
                            SourcedStruct3(
                                ArbrCommit.CommitMessage.constant("Create function to generate game board"),
                                ArbrCommit.DiffSummary.constant("This commit adds a new function to generate the game board with the specified difficulty. The function is implemented in the `src/App.jsx` file. It takes a difficulty parameter as input and returns a 2D array representing the game board. The function uses nested loops to iterate over the rows and columns of the board and initializes each cell with the appropriate values based on the difficulty level. The function also handles the placement of mines on the board, ensuring that the specified number of mines is distributed randomly across the cells. This commit also includes necessary changes to the `App` component to call the new function and render the game board on the web page."),
                                FileOperationsAndTargetFilePaths.initializeMerged(
                                    listOf(
                                        SourcedStruct2(
                                            ArbrFileOp.FileOperation.constant("edit_file"),
                                            ArbrFile.FilePath.constant("src/App.jsx"),
                                        )
                                    )
                                )
                            )
                        )
                    ),
                ),
                SourcedStruct4(
                    ArbrSubtaskEval.PartiallyComplete.constant(false),
                    ArbrSubtaskEval.MostlyComplete.constant(false),
                    ArbrSubtaskEval.Complete.constant(true),
                    CommitDetailsAndFileOps.initializeMerged(emptyList()),
                ),
            )
            .withOutputProcessor {
                // Common bad output for some reason
                it.copy(content = it.content.replace("partial_complete:", "partially_complete:"))
            }
            .withMaxTokens(512)
            .build()

    fun commitReassignmentToSubtask() = aiApplicationConfigFactory
        .builder("eval-subtask-reassign-commits")
        .withInputSchema {
            p(ArbrProject.FullName)
                .p(ArbrTask.TaskQuery)
                .withKey("main_task")
                .withDescription("Main Task: The main task being implemented.")
                .p(Subtasks)
                .withKey("all_subtasks")
                .withDescription("All Subtasks: All the subtasks planned for implementation to accomplish the task.")
                .p(CommitDetailsAndFileOps)
        }
        .withOutputSchema {
            p(CommitMessageSubtaskPair)
        }
        .withInstructions { inputSchemaDescription, outputSchemaDescription ->
            (
                    "You are an assistant who, given an input of the following form:\n"
                            + inputSchemaDescription
                            + "\n\nProduces an output of the following form:\n"
                            + outputSchemaDescription
                            + "\n\nYour goal is to assign each given commit to the given subtask which it is most naturally a part of."
                    )
        }
        .build()

//    fun commitCompletionApplication() =
//        aiApplicationConfigFactory
//            .builder("db-fn-commit-completion-yaml-3")
//            .withInputSchema {
//                p(ArbrProject.FullName)
//                    .p(
//                        ArbrTask.TaskQuery
//                    )
//                    .p(ArbrCommit.CommitMessage)
//                    .p(FilePathsAndContents) // note changed added path
//                    .p(ArbrCommit.DiffContent)
//            }
//            .withOutputSchema {
//                p(ArbrTaskEval.PartiallyComplete)
//                    .p(ArbrTaskEval.Complete)
//                    .p(RemainingTasks)
//            }
//            .withInstructions { inputSchemaDescription, outputSchemaDescription ->
//                (
//                        "You are an assistant who, given an input of the following form:\n"
//                                + inputSchemaDescription
//                                + "\n\nProduces an output of the following form:\n"
//                                + outputSchemaDescription
//                                + "\n\nYour goal is to decide if the provided code change accomplishes the described task." +
//                                " If it makes significant, non-trivial progress towards accomplishing the task, then you " +
//                                "should flag the task as at least partially complete. If the code change completely " +
//                                "accomplishes the task, you should flag the task as complete. If the task is not completely" +
//                                " accomplished, you should describe at most 2 remaining tasks, each in at most 300 words. " +
//                                "Always give a boolean value \"true\" or \"false\" for each of the flags. If the task is " +
//                                "only partially complete, always give at least one remaining task. Ensure the output is formatted as YAML." +
//                                "\nEach remaining task should be given as an imperative statement similar to a commit message."
//                        )
//            }
//            .withModel(OpenAiChatCompletionModel.GPT_4_0125_PREVIEW)
//            .build()

    fun contextualTaskCompletionApplication() =
        aiApplicationConfigFactory
            .builder("contextual-task-completion")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrTask.TaskQuery)
                    .p(FilePathsAndContents) // relevant other file contents
                    .withKey("relevant_other_file_contents")
                    .p(FilePathsAndContents) // updated file contents
                    .withKey("updated_file_contents")
            }
            .withOutputSchema {
                p(ArbrTaskEval.PartiallyComplete)
                    .p(ArbrTaskEval.Complete)
                    .p(Subtasks) // Remaining tasks
            }
            .withInstructions { inputSchemaDescription, outputSchemaDescription ->
                (
                        "You are an assistant who, given an input of the following form:\n"
                                + inputSchemaDescription
                                + "\n\nProduces an output of the following form:\n"
                                + outputSchemaDescription
                                + "\n\nYour goal is to decide if the task has been accomplished given the current state of the files in the project." +
                                " If the code reflects significant, non-trivial progress towards accomplishing the task, then you " +
                                "should flag the task as at least partially complete. If the code reflects complete " +
                                "accomplishment the task, you should flag the task as complete. If the task is not completely" +
                                " accomplished, you should describe at most 2 remaining tasks, each in at most 300 words. " +
                                "Always give a boolean value \"true\" or \"false\" for each of the flags. If the task is " +
                                "only partially complete, always give at least one remaining subtask. Ensure the output is formatted as YAML." +
                                "\nEach remaining task should be given as an imperative statement similar to a commit message."
                        )
            }
            .withPromptShortener { record, _ ->
                record.copy(
                    t3 = record.t3.copy(value = record.t3.value.dropLast(1))
                )
            }
            .build()

    fun taskCompletionPipelineWithSummaries() =
        aiApplicationConfigFactory
            .builder("evaluate-remaining-tasks-with-summaries-yaml")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrTask.TaskQuery)
                    .p(ArbrCommit.DiffContent)
                    .p(
                        DiffSummaries
                    )
            }
            .withOutputSchema {
                p(ArbrTaskEval.PartiallyComplete)
                    .p(ArbrTaskEval.Complete)
                    .p(Subtasks) // Remaining tasks
            }
            .withInstructions { inputSchemaDescription, outputSchemaDescription ->
                (
                        "You are an assistant who, given an input of the following form:\n"
                                + inputSchemaDescription
                                + "\n\nProduces an output of the following form:\n"
                                + outputSchemaDescription
                                + "\n\nYour goal is to decide if the provided code change accomplishes the described task." +
                                " If it makes significant, non-trivial progress towards accomplishing the task, then you " +
                                "should flag the task as at least partially complete. If the code change completely " +
                                "accomplishes the task, you should flag the task as complete. If the task is not completely" +
                                " accomplished, you should describe at most 2 remaining tasks, each in at most 300 words. " +
                                "Always give a boolean value \"true\" or \"false\" for each of the flags. If the task is " +
                                "only partially complete, always give at least one remaining subtask."
                        )
            }
            .build()

    fun taskCompletionPipelineOnlySummaries() =
        aiApplicationConfigFactory
            .builder("evaluate-remaining-tasks-only-summaries-yaml")
            .withInputSchema {
                p(ArbrProject.FullName)
                    .p(ArbrTask.TaskQuery)
                    .p(
                        DiffSummaries
                    )
            }
            .withOutputSchema {
                p(ArbrTaskEval.PartiallyComplete)
                    .p(ArbrTaskEval.Complete)
                    .p(Subtasks) // Remaining tasks
            }
            .withInstructions { inputSchemaDescription, outputSchemaDescription ->
                (
                        "You are an assistant who, given an input of the following form:\n"
                                + inputSchemaDescription
                                + "\n\nProduces an output of the following form:\n"
                                + outputSchemaDescription
                                + "\n\nYour goal is to decide if the provided code change accomplishes the described task." +
                                " If it makes significant, non-trivial progress towards accomplishing the task, then you " +
                                "should flag the task as at least partially complete. If the code change completely " +
                                "accomplishes the task, you should flag the task as complete. If the task is not completely" +
                                " accomplished, you should describe at most 2 remaining tasks, each in at most 300 words. " +
                                "Always give a boolean value \"true\" or \"false\" for each of the flags. If the task is " +
                                "only partially complete, always give at least one remaining subtask."
                        )
            }
            .build()

    fun commitSummarizationApplication() =
        aiApplicationConfigFactory
            .builder("summarize-commit-yaml")
            .withInputSchema {
                p(ArbrCommit.CommitMessage)
                    .p(ArbrCommit.DiffContent)
            }
            .withOutputSchema {
                p(ArbrCommit.DiffSummary)
            }
            .withInstructions { inputSchemaDescription, outputSchemaDescription ->
                (
                        "You are an assistant who, given an input of the following form:\n"
                                + inputSchemaDescription
                                + "\n\nProduces an output of the following form:\n"
                                + outputSchemaDescription
                                + "\n\nYour goal is to summarize the contents of the commit represented by the given commit" +
                                " message and diff. Your summary should be about 300 words and should best summarize" +
                                " the structural nature of the code change as well as the specific, objective changes in" +
                                " functionality."
                        )
            }
            .build()

    companion object {
        private const val maxNumFiles = 3

        private const val maxNumFilesOutputted = 5
    }
}
