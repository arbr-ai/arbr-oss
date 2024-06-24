package com.arbr.relational_prompting.services.ai_application.config

//import com.arbr.content_formats.mapper.Mappers
//import com.arbr.relational_prompting.generics.completions.DefaultChatCompletionProvider
//import com.arbr.relational_prompting.generics.application_cache.ApplicationCompletionCache
//import com.arbr.relational_prompting.generics.examples.ApplicationExampleProvider
//import com.arbr.relational_prompting.generics.model.ChatMessage
//import com.arbr.relational_prompting.generics.model.OpenAiChatCompletion
//import com.arbr.relational_prompting.generics.model.OpenAiChatCompletionModel
//import com.arbr.relational_prompting.layers.object_translation.*
//import com.arbr.relational_prompting.layers.prompt_composition.PropertyListSerializer
//import com.arbr.og.object_model.impl.DdlGithubSchema
//import com.arbr.og.object_model.impl.NestedObjectListType2
//import com.arbr.og.object_model.impl.NestedObjectListType3
//import com.arbr.relational_prompting.services.ai_application.application.invoke
//import com.arbr.og.object_model.common.values.collections.SourcedStruct
//import com.arbr.og.object_model.common.values.collections.SourcedStruct2
//import com.arbr.relational_prompting.services.ai_application.model.SourcedValueGeneratorInfo
//import com.arbr.relational_prompting.services.ai_application.model.TypedApplicationCompletion
//import com.arbr.relational_prompting.services.embedding.model.VectorResourceKeyValuePair
////import com.arbr.db.public.tables.pojos.EmbeddedResourcePair
//import org.jooq.JSONB
//import org.junit.jupiter.api.Test
//import org.mockito.Mockito
//import org.mockito.kotlin.any
//import org.mockito.kotlin.doReturn
//import org.mockito.kotlin.whenever
//import reactor.core.publisher.Flux
//import reactor.core.publisher.Mono
//
//object LenientCodeParser {
//    private val splitRe = Regex("```(\\S*\\s+)?")
//    private val splitRe2 = Regex("`+")
//
//    /**
//     * Parse code leniently, unwrapping things like backticks.
//     *
//     * TODO: Incorporate real language parsers, allow skipping some number of input lines.
//     */
//    fun parse(sourceCodeOutput: String): String {
//        for (re in listOf(splitRe, splitRe2)) {
//            val segments = splitRe.split(sourceCodeOutput)
//                .withIndex()
//                .filter { it.index % 2 == 1 }
//                .map { it.value.trim() }
//
//            val longestSegment = segments.maxByOrNull { it.length }
//            if (longestSegment != null && longestSegment.length * 3 >= sourceCodeOutput.length) {
//                return longestSegment + "\n"
//            }
//        }
//
//        return sourceCodeOutput.trim() + "\n"
//    }
//}
//
//class YamlParserTest {
//
//    private val chatCompletionProvider = Mockito.mock(DefaultChatCompletionProvider::class.java)
//
//    private val applicationCompletionCache = object : ApplicationCompletionCache {
//        override fun <InputModel : SourcedStruct, OutputModel : SourcedStruct> get(
//            applicationId: String,
//            inputSchema: TemplateComponentSchema<InputModel>,
//            outputSchema: TemplateComponentSchema<OutputModel>,
//            input: InputModel,
//            cacheKey: String
//        ): Mono<TypedApplicationCompletion<InputModel, OutputModel>> {
//            return Mono.empty()
//        }
//
//        override fun <InputModel : SourcedStruct, OutputModel : SourcedStruct> set(
//            applicationId: String,
//            inputSchema: TemplateComponentSchema<InputModel>,
//            outputSchema: TemplateComponentSchema<OutputModel>,
//            typedApplicationCompletion: TypedApplicationCompletion<InputModel, OutputModel>
//        ): Mono<Void> {
//            return Mono.empty()
//        }
//    }
//
//    private val applicationExampleProvider = object : ApplicationExampleProvider {
//        override fun <InputModel : SourcedStruct, OutputModel : SourcedStruct> publishAll(
//            inputSchema: TemplateComponentSchema<InputModel>,
//            outputSchema: TemplateComponentSchema<OutputModel>,
//            pairs: List<Pair<InputModel, OutputModel>>
//        ): Flux<EmbeddedResourcePair> {
//            return Flux.empty()
//        }
//
//        override fun <InputModel : SourcedStruct, OutputModel : SourcedStruct> retrieveNearestNeighbors(
//            inputSchema: TemplateComponentSchema<InputModel>,
//            outputSchema: TemplateComponentSchema<OutputModel>,
//            input: InputModel,
//            numNeighborsToRetrieve: Long
//        ): Flux<VectorResourceKeyValuePair<InputModel, OutputModel>> {
//            return Flux.empty()
//        }
//    }
//
//    private val applicationFactory: AiApplicationFactory = AiApplicationFactory(
//        applicationExampleProvider,
//        applicationCompletionCache,
//        chatCompletionProvider,
//        false,
//        0.2,
//        Mappers.mapper,
//    )
//
//    val yamlMapper = Mappers.yamlMapper
//
//    val FileOperationsAndTargetFilePaths: NestedObjectListType2<String?, String, String?, String, DdlGithubSchema.ProjectSubtaskCommitFileOp.FileOperation.Value, DdlGithubSchema.File.FilePath.Value> =
//        NestedObjectListType2(
//            "file_operations_and_target_file_path",
//            "File operation names (create, edit, delete) and the target file path for the operation.",
//            DdlGithubSchema.ProjectSubtaskCommitFileOp.FileOperation,
//            DdlGithubSchema.File.FilePath,
//        )
//
//    fun makeFileOperationsAndTargetFilePathsInnerValue(
//        fileOperation: String,
//        filePath: String,
//    ): NestedObjectListType2.InnerValue<String?, String> {
//        return NestedObjectListType2.InnerValue(
//            FileOperationsAndTargetFilePaths,
//            fileOperation,
//            filePath,
//        )
//    }
//
//    val CommitDetailsAndFileOps: NestedObjectListType3<String, String?, List<NestedObjectListType2.InnerValue<String?, String>>, String, String?, List<JSONB>, DdlGithubSchema.ProjectSubtaskCommit.CommitMessageDescription.Value, DdlGithubSchema.ProjectSubtaskCommit.DiffSummary.Value, NestedObjectListType2.Value<String?, String, String?, String, DdlGithubSchema.ProjectSubtaskCommitFileOp.FileOperation.Value, DdlGithubSchema.File.FilePath.Value>> =
//        NestedObjectListType3(
//            "commit_details_and_file_operations",
//            "Array of commit information planned for the task with the commit message and a summary of" +
//                    " the commit, as well as the associated file operations.",
//            DdlGithubSchema.ProjectSubtaskCommit.CommitMessageDescription,
//            DdlGithubSchema.ProjectSubtaskCommit.DiffSummary,
//            FileOperationsAndTargetFilePaths,
//        )
//
//    val FilePathsAndContents: NestedObjectListType2<String, String?, String, String?, DdlGithubSchema.File.FilePath.Value, DdlGithubSchema.File.Content.Value> =
//        NestedObjectListType2(
//            "file_paths_and_contents",
//            "File paths and their contents.",
//            DdlGithubSchema.File.FilePath,
//            DdlGithubSchema.File.Content,
//        )
//
//    private val repairPlanCommitsPropertyListSerializer = object : PropertyListSerializer {
//        override fun describeProperties(propertySchemas: List<PropertySchema<*, *>>): TemplateDescriptionElement {
//            val descriptionString = """
//Error Content:
//```
//... error content ...
//```
//
//File Content (file path):
//```
//... file content ...
//```
//
//File Content (file path):
//```
//... file content ...
//```
//
//... More files ...
//            """.trimIndent()
//
//            return TemplateDescriptionElement(descriptionString)
//        }
//
//        override fun serializeValuedProperties(
//            propertySchemas: List<PropertySchema<*, *>>,
//            unwrappedPropertyValues: List<*>
//        ): TemplateValueElement {
//            val errorContent = unwrappedPropertyValues.first().toString()
//
//            @Suppress("UNCHECKED_CAST")
//            val fileValues = unwrappedPropertyValues[1] as List<NestedObjectListType2.InnerValue<String, String?>>
//
//            val serializedValue =
//                "Error Content:\n${CodeSerializer.serializeCode(errorContent)}\n\n" + fileValues.joinToString("\n\n") {
//                    "File Content (${it.t1}):\n${CodeSerializer.serializeCode(it.t2 ?: "")}"
//                }
//
//            return TemplateValueElement(serializedValue)
//        }
//
//    }
//
//    private val app = applicationFactory
//        .builder("task-repair-plan-commits-yaml-3")
//        .withInputSchema {
//            p(DdlGithubSchema.ProjectSubtaskCommitBuildResult.ErrorContent)
//                .p(FilePathsAndContents)
//        }
//        .withInputSerializer(repairPlanCommitsPropertyListSerializer)
//        .withOutputSchema {
//            p(CommitDetailsAndFileOps)
//        }
//        .withInstructions { inputSchemaDescription, outputSchemaDescription ->
//            (
//                    "You are a code project planning assistant. Given an input of the following form:\n\n"
//                            + inputSchemaDescription.trim()
//                            + "\n\n\nPlan the commits needed to resolve the given error messages by outputting " +
//                            "summaries of the planned change associated with each commit. The given files" +
//                            " included in the user's input are existing files in the repository which have been" +
//                            " judged to be relevant to the error.\n" +
//                            "Your output has the format:\n"
//                            + outputSchemaDescription
//                            + "\n\nBe sure to include all information in the Commit Diff Summary necessary to implement the change without any additional context. Your response should be formatted as YAML with no additional text."
//                    )
//        }
//        .withModel(OpenAiChatCompletionModel.GPT_4_0613)
//        .withPromptShortener { record, _ ->
//            record
//        }
//        .build()
//
//    fun `parses yaml`() {
//        val mapper = Mappers.mapper
//
//        val responseJson =
//            "{\"id\":\"chatcmpl-89dE25lL7hTb8C456Gu3k0H0RRhGQ\",\"object\":\"chat.completion\",\"created\":1697306426,\"model\":\"gpt-3.5-turbo-0613\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"```yaml\\ncommit_details_and_file_operations:\\n  - commit_message_description: Fix arrow function syntax error in GameBoard component\\n    diff_summary: |-\\n      --- a/src/GameBoard.jsx\\n      +++ b/src/GameBoard.jsx\\n      @@ -53,7 +53,7 @@\\n         }\\n       }\\n     \\n      -  return (\\n      +  return () => {\\n           // JSX code for rendering the game board\\n      -  );\\n      +  };\\n     }\\n     \\n     export default GameBoard;\\n    file_operations_and_target_file_path:\\n      - file_operation: edit_file\\n        file_path: src/GameBoard.jsx\\n```\"},\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":1215,\"completion_tokens\":128,\"total_tokens\":1343}}"
//        val chatCompletion = mapper.readValue(responseJson, OpenAiChatCompletion::class.java)
//
//        doReturn(
//            Mono.just(
//                chatCompletion
//            )
//        ).whenever(chatCompletionProvider).getChatCompletion(
//            any(),
//            any()
//        )
//
//        val res = app.invoke(
//            DdlGithubSchema.ProjectSubtaskCommitBuildResult.ErrorContent.materialized("error"),
//            FilePathsAndContents.initializeMerged(
//                listOf(
//                    SourcedStruct2(
//                        DdlGithubSchema.File.FilePath.materialized("src/main.jsx"),
//                        DdlGithubSchema.File.Content.materialized("content"),
//                    )
//                )
//            ),
//            Mockito.mock(),
//        )
//            
//            .block()!!
//
//        println(res.t1)
//    }
//    @Test
//
//    @Test
//    fun `parses yaml 2`() {
//        val content = """
//commit_details_and_file_operations:
//  - commit_message_description: Fix arrow function syntax error in GameBoard component
//    diff_summary: |-
//      --- a/src/GameBoard.jsx
//      +++ b/src/GameBoard.jsx
//      @@ -53,7 +53,7 @@
//         }
//       }
//
//      -  return (
//      +  return () => {
//           // JSX code for rendering the game board
//      -  );
//      +  };
//     }
//
//     export default GameBoard;
//    file_operations_and_target_file_path:
//      - file_operation: edit_file
//        file_path: src/GameBoard.jsx
//        """.trimIndent()
//
//        val tcs = schema {
//            p(CommitDetailsAndFileOps)
//        }
//
//        tcs.parse(ChatMessage(ChatMessage.Role.ASSISTANT, content), SourcedValueGeneratorInfo(emptyList()))
//    }
//
//    @Test
//    fun `parses yaml 3`() {
//        val content = """
//commit_details_and_file_operations:
//  - commit_message_description: Fix arrow function syntax error in GameBoard component
//    diff_summary: |-
//      --- a/src/GameBoard.jsx
//      +++ b/src/GameBoard.jsx
//      @@ -53,7 +53,7 @@
//         }
//       }
//
//      -  return (
//      +  return () => {
//           // JSX code for rendering the game board
//      -  );
//      +  };
//      }
//
//      export default GameBoard;
//    file_operations_and_target_file_path:
//      - file_operation: edit_file
//        file_path: src/GameBoard.jsx
//        """.trimIndent()
//
//        yamlMapper.readValue(content, LinkedHashMap::class.java)
//    }
//
//    @Test
//    fun `parses commit completion output`() {
//        val app = applicationFactory
//            .builder("contextual-commit-completion-test")
//            .withInputSchema {
//                p(DdlGithubSchema.Project.FullName)
//            }
//            .withOutputSchema {
//                p(DdlGithubSchema.ProjectSubtaskCommitCompletionStatus.PartiallyComplete)
//                    .p(DdlGithubSchema.ProjectSubtaskCommitCompletionStatus.Complete)
//                    .p(CommitDetailsAndFileOps)
//            }
//            .withInstructions { inputSchemaDescription, outputSchemaDescription ->
//                (
//                        "You are an assistant who, given an input of the following form:\n"
//                                + inputSchemaDescription
//                                + "\n\nProduces an output of the following form:\n"
//                                + outputSchemaDescription
//                                + "\n\nYour goal is to decide if the goal of the commit has been accomplished given" +
//                                " the current state of the provided files in the project." +
//                                " If the code reflects significant, non-trivial progress towards accomplishing the goal, then you " +
//                                "should flag the commit as at least partially complete. If the code reflects complete " +
//                                "accomplishment the commit, you should flag the commit as complete. If the commit is not completely" +
//                                " accomplished, you should describe at most 2 remaining commits, each in at most 300 words. " +
//                                "Always give a boolean value \"true\" or \"false\" for each of the flags. If the commit is " +
//                                "only partially complete, always give at least one remaining commit. Ensure the output is formatted as YAML." +
//                                "\nEach remaining commit should be given as an imperative statement similar to a commit message."
//                        )
//            }
//            .withMaxTokens(512)
//            .withModel(OpenAiChatCompletionModel.GPT_4_0613)
//            .build()
//
//        val messageContent = ("```yaml\n" +
//                "partially_complete: true\n" +
//                "complete: false\n" +
//                "commit_details_and_file_operations:\n" +
//                "  - commit_message_description: \"Implement logic to reveal cells when clicked\"\n" +
//                "    diff_summary: \"This commit implements the logic to reveal cells when clicked by the user in the Minesweeper game. The `handleCellClick` function has been added to the `App` component in `src/App.jsx`. However, the logic to reveal the cells is not yet implemented and is marked as a TODO. The commit also includes some changes to the HTML and CSS files, updating the title of the game and adding a CSS class to flag cells as potential mines.\"\n" +
//                "    file_operations_and_target_file_path:\n" +
//                "      - file_operation: \"edit_file\"\n" +
//                "        file_path: \"src/App.jsx\"\n" +
//                "      - file_operation: \"edit_file\"\n" +
//                "        file_path: \"src/App.css\"\n" +
//                "```\n" +
//                "\n" +
//                "The commit is partially complete because it includes the implementation of the `handleCellClick` function in the `App` component, which is the logic to reveal cells when clicked. However, the logic itself is not yet implemented and is marked as a TODO. The commit also includes some changes to the HTML and CSS files, updating the title of the game and adding a CSS class to flag cells as potential mines. \n" +
//                "\n" +
//                "To complete the commit, the logic to reveal the cells needs to be implemented in the `handleCellClick` function. Additionally, the TODO comments should be removed once the logic is implemented.")
//
//        doReturn(
//            Mono.just(
//                OpenAiChatCompletion(
//                    "", "", 0L, "", listOf(
//                        OpenAiChatCompletion.Choice(0, ChatMessage(ChatMessage.Role.ASSISTANT, messageContent)),
//                    ), OpenAiChatCompletion.Usage(0, 0, 0)
//                )
//            )
//        ).whenever(chatCompletionProvider).getChatCompletion(
//            any(),
//            any()
//        )
//
//        val res = app.invoke(
//            DdlGithubSchema.Project.FullName.constant(""),
//            Mockito.mock(),
//        )
//            
//            .block()!!
//
//        println(res)
//    }
//
//}