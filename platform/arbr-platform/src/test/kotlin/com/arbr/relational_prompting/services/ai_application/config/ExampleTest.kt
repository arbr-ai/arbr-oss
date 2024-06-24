package com.arbr.relational_prompting.services.ai_application.config//package com.arbr.relational_prompting.services.ai_application.config
//
//import com.arbr.content_formats.mapper.Mappers
//import com.arbr.relational_prompting.generics.completions.DefaultChatCompletionProvider
//import com.arbr.relational_prompting.generics.application_cache.ApplicationCompletionCache
//import com.arbr.relational_prompting.generics.examples.ApplicationExampleProvider
//import com.arbr.relational_prompting.generics.model.ChatMessage
//import com.arbr.relational_prompting.generics.model.OpenAiChatCompletionModel
//import com.arbr.relational_prompting.layers.object_translation.PropertySchema
//import com.arbr.relational_prompting.layers.object_translation.TemplateComponentSchema
//import com.arbr.relational_prompting.layers.object_translation.TemplateDescriptionElement
//import com.arbr.relational_prompting.layers.object_translation.TemplateValueElement
//import com.arbr.relational_prompting.layers.prompt_composition.PropertyListSerializer
//import com.arbr.relational_prompting.layers.prompt_composition.YamlPropertyListSerializer
//import com.arbr.og.object_model.impl.DdlGithubSchema
//import com.arbr.og.object_model.impl.NestedObjectListType2
//import com.arbr.og.object_model.impl.NestedObjectListType3
//import com.arbr.relational_prompting.services.ai_application.application.AiApplication
//import com.arbr.relational_prompting.services.ai_application.application.ApplicationArtifact
//import com.arbr.relational_prompting.services.ai_application.model.*
//import com.arbr.relational_prompting.services.embedding.model.FullyQualifiedInputLiteral
//import com.arbr.relational_prompting.services.embedding.model.VectorResourceKeyValuePair
//import com.arbr.content_formats.code.LenientCodeParser
////import com.arbr.db.public.tables.pojos.EmbeddedResourcePair
//import org.junit.jupiter.api.Test
//import org.mockito.Mockito
//import reactor.core.publisher.Flux
//import reactor.core.publisher.Mono
//import java.util.*
//
//private fun <T, U> List<T>.collateBy(key: (T) -> U): List<Pair<U, List<T>>> {
//    if (isEmpty()) {
//        return emptyList()
//    }
//
//    val collated = mutableListOf<Pair<U, List<T>>>()
//
//    val firstElt = first()
//    var bucket = mutableListOf(firstElt)
//    var inBucket = key(firstElt)
//    for (elt in drop(1)) {
//        val eltInBucket = key(elt)
//        if (eltInBucket == inBucket) {
//            bucket.add(elt)
//        } else {
//            collated.add(inBucket to bucket)
//
//            bucket = mutableListOf(elt)
//            inBucket = eltInBucket
//        }
//    }
//
//    // Last bucket always nonempty
//    collated.add(inBucket to bucket)
//
//    return collated
//}
//
//object CodeSerializer {
//
//    fun serializeCode(body: String, languageIndicator: String = ""): String {
//        // Wrap in backticks if not already quoted
//        val strippedBody = LenientCodeParser.parse(body.trim())
//        return if (strippedBody.startsWith("`") && strippedBody.endsWith("`")) {
//            // Note: does not inject new language indicator
//            strippedBody
//        } else {
//            "```$languageIndicator\n$strippedBody\n```"
//        }
//    }
//}
//
//class ExampleTest {
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
//    @Suppress("UNCHECKED_CAST")
//    private val applicationExampleProvider = object : ApplicationExampleProvider {
//        val examplePairs = mutableListOf<Pair<Any, Any>>()
//
//        override fun <InputModel : SourcedStruct, OutputModel : SourcedStruct> publishAll(
//            inputSchema: TemplateComponentSchema<InputModel>,
//            outputSchema: TemplateComponentSchema<OutputModel>,
//            pairs: List<Pair<InputModel, OutputModel>>
//        ): Flux<EmbeddedResourcePair> {
//            examplePairs.addAll(pairs)
//
//            return Flux.fromIterable(
//                List(pairs.size) { i ->
//                    EmbeddedResourcePair(vectorId = i.toString())
//                }
//            )
//        }
//
//        override fun <InputModel : SourcedStruct, OutputModel : SourcedStruct> retrieveNearestNeighbors(
//            inputSchema: TemplateComponentSchema<InputModel>,
//            outputSchema: TemplateComponentSchema<OutputModel>,
//            input: InputModel,
//            numNeighborsToRetrieve: Long
//        ): Flux<VectorResourceKeyValuePair<InputModel, OutputModel>> {
//            return Flux.fromIterable(
//                examplePairs.filter {
//                    it.first == input
//                }.map { (inputEx, outputEx) ->
//
//                    VectorResourceKeyValuePair(
//                        UUID.randomUUID().toString(),
//                        FullyQualifiedInputLiteral(
//                            inputEx as InputModel,
//                            listOf(
//                                ChatMessage(
//                                    ChatMessage.Role.USER,
//                                    inputSchema.serializedValue(input).serializedValue
//                                )
//                            )
//                        ),
//                        FullyQualifiedInputLiteral(
//                            outputEx as OutputModel,
//                            listOf(
//                                ChatMessage(
//                                    ChatMessage.Role.USER,
//                                    inputSchema.serializedValue(input).serializedValue
//                                )
//                            )
//                        ),
//                    )
//                }
//            )
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
//    private val trailingContentPropertyListSerializer = object : PropertyListSerializer {
//        private val numTrailingProperties = 2
//        private val trailingProperties = listOf("relevant_other_file_contents", "updated_file_contents")
//
//        override fun describeProperties(propertySchemas: List<PropertySchema<*, *>>): TemplateDescriptionElement {
//            val head = propertySchemas.dropLast(numTrailingProperties)
//            val headDescription = if (head.isEmpty()) {
//                ""
//            } else {
//                YamlPropertyListSerializer.describeProperties(head).description
//            }
//
//            val tail = propertySchemas.takeLast(numTrailingProperties)
//            val tailDescription = if (tail.size < numTrailingProperties) {
//                ""
//            } else {
//                "Relevant other file contents:\n\n" +
//                        "Relevant File 1 (file_name):\n" +
//                        "```\n... file contents ...\n```\n\n" +
//                        "Relevant File 2 (file_name):\n" +
//                        "```\n... file contents ...\n```\n\n" +
//                        "Updated file contents:\n\n" +
//                        "Updated File 1 (file_name):\n" +
//                        "```\n... file contents ...\n```\n\n" +
//                        "Updated File 2 (file_name):\n" +
//                        "```\n... file contents ...\n```\n\n..."
//            }
//
//            return TemplateDescriptionElement(
//                headDescription + "\n\n" + tailDescription
//            )
//        }
//
//        @Suppress("UNCHECKED_CAST")
//        override fun serializeValuedProperties(
//            propertySchemas: List<PropertySchema<*, *>>,
//            unwrappedPropertyValues: List<*>
//        ): TemplateValueElement {
//            if (unwrappedPropertyValues.isEmpty()) {
//                return TemplateValueElement("")
//            }
//
//            // Because of embeddings, don't assume all properties are here
//            val collatedBuckets = propertySchemas
//                .zip(unwrappedPropertyValues)
//                .collateBy { (p, _) ->
//                    val propertyKey = p.property().first
//                    if (propertyKey in trailingProperties) propertyKey else "yaml"
//                }
//
//            val body = collatedBuckets.joinToString("\n\n") { (bucketKey: String, propValuePairs) ->
//                val props = propValuePairs.map { it.first }
//                val values = propValuePairs.map { it.second }
//
//                if (bucketKey == "relevant_other_file_contents") {
//                    val relevantOtherFileContents: List<NestedObjectListType2.InnerValue<String, String?>> =
//                        values.flatMap { elt ->
//                            if (elt == null) {
//                                emptyList()
//                            } else {
//                                elt as List<NestedObjectListType2.InnerValue<String, String?>>
//                            }
//                        }
//
//                    if (relevantOtherFileContents.isEmpty()) {
//                        ""
//                    } else {
//                        "Relevant other file contents:\n\n" + relevantOtherFileContents.withIndex()
//                            .joinToString("\n\n") { (i, r) ->
//                                val quotedContent = CodeSerializer.serializeCode(r.t2?.trim() ?: "").trim()
//
//                                "Relevant File ${i + 1} (${r.t1}):\n$quotedContent"
//                            }
//                    }
//                } else if (bucketKey == "updated_file_contents") {
//                    val updatedFileContents: List<NestedObjectListType2.InnerValue<String, String?>> =
//                        values.flatMap { elt ->
//                            if (elt == null) {
//                                emptyList()
//                            } else {
//                                elt as List<NestedObjectListType2.InnerValue<String, String?>>
//                            }
//                        }
//
//                    if (updatedFileContents.isEmpty()) {
//                        ""
//                    } else {
//                        "Updated file contents:\n\n" + updatedFileContents.withIndex().joinToString("\n\n") { (i, r) ->
//                            val quotedContent = CodeSerializer.serializeCode(r.t2?.trim() ?: "").trim()
//
//                            "Updated ${i + 1} (${r.t1}):\n$quotedContent"
//                        }
//                    }
//                } else {
//                    YamlPropertyListSerializer.serializeValuedProperties(props, values).serializedValue
//                }
//            }
//
//            return TemplateValueElement(
//                body
//            )
//        }
//
//    }
//
//    val FilePathsAndContents: NestedObjectListType2<String, String?, String, String?, DdlGithubSchema.File.FilePath.Value, DdlGithubSchema.File.Content.Value> =
//        NestedObjectListType2(
//            "file_paths_and_contents",
//            "File paths and their contents.",
//            DdlGithubSchema.File.FilePath,
//            DdlGithubSchema.File.Content,
//        )
//
//    val FileOperationsAndTargetFilePathsWithDescriptions: NestedObjectListType3<String?, String, String?, String?, String, String?, DdlGithubSchema.ProjectSubtaskCommitFileOp.FileOperation.Value, DdlGithubSchema.File.FilePath.Value, DdlGithubSchema.ProjectSubtaskCommitFileOp.Description.Value> =
//        NestedObjectListType3(
//            "file_operations_with_descriptions",
//            "File operation names (create, edit, delete), the target file path for the operation, and a description of the work needed to accomplish the commit.",
//            DdlGithubSchema.ProjectSubtaskCommitFileOp.FileOperation,
//            DdlGithubSchema.File.FilePath,
//            DdlGithubSchema.ProjectSubtaskCommitFileOp.Description,
//        )
//
//    val inputModel = SourcedStruct7(
//        DdlGithubSchema.Project.FullName.constant("topdown-bill/minesweeper-17"),
//        DdlGithubSchema.ProjectTask.TaskQuery.constant("Implement a simple Minesweeper game as a Web app"),
//        DdlGithubSchema.ProjectSubtask.Subtask.constant("Calculate the number of adjacent mines for each cell on the game board."),
//        DdlGithubSchema.ProjectSubtaskCommit.CommitMessage.constant("Implement mine adjacency calculation"),
//        DdlGithubSchema.ProjectSubtaskCommit.DiffSummary.constant("This commit implements the calculation of the number of adjacent mines for each cell on the game board. The changes are made to the `src/App.jsx` file. The `calculateAdjacentMines` function is added, which takes the current game board as input and returns a new board with the number of adjacent mines calculated for each cell. The function iterates over each cell in the board and checks its neighboring cells to count the number of mines. The result is stored in a new board, which is then returned by the function. The `calculateAdjacentMines` function is called in the `App` component's render method to update the game board with the calculated values. This commit also includes necessary imports and updates to the component's state and rendering logic."),
//        FilePathsAndContents.initializeMerged(
//            listOf(
//                SourcedStruct2(
//                    DdlGithubSchema.File.FilePath.constant("src/main.jsx"),
//                    DdlGithubSchema.File.Content.constant(
//                        "import React from 'react'\n" +
//                                "import ReactDOM from 'react-dom/client'\n" +
//                                "import App from './App.jsx'\n" +
//                                "import './index.css'\n" +
//                                "\n" +
//                                "ReactDOM.createRoot(document.getElementById('root')).render(\n" +
//                                "    <React.StrictMode>\n" +
//                                "        <App/>\n" +
//                                "    </React.StrictMode>,\n" +
//                                ")\n" +
//                                "\n" +
//                                "// Add logic to randomly place mines on the game board\n" +
//                                "function placeMines(numMines) {\n" +
//                                "    // TODO: Implement logic to randomly place mines on the game board\n" +
//                                "}\n" +
//                                "\n" +
//                                "export default placeMines\n"
//                    ),
//                ),
//                SourcedStruct2(
//                    DdlGithubSchema.File.FilePath.constant("src/index.css"),
//                    DdlGithubSchema.File.Content.constant(
//                        ":root {\n" +
//                                "  font-family: Inter, system-ui, Avenir, Helvetica, Arial, sans-serif;\n" +
//                                "  line-height: 1.5;\n" +
//                                "  font-weight: 400;\n" +
//                                "\n" +
//                                "  color-scheme: light dark;\n" +
//                                "  color: rgba(255, 255, 255, 0.87);\n" +
//                                "  background-color: #242424;\n" +
//                                "\n" +
//                                "  font-synthesis: none;\n" +
//                                "  text-rendering: optimizeLegibility;\n" +
//                                "  -webkit-font-smoothing: antialiased;\n" +
//                                "  -moz-osx-font-smoothing: grayscale;\n" +
//                                "  -webkit-text-size-adjust: 100%;\n" +
//                                "}\n" +
//                                "\n" +
//                                "a {\n" +
//                                "  font-weight: 500;\n" +
//                                "  color: #646cff;\n" +
//                                "  text-decoration: inherit;\n" +
//                                "}\n" +
//                                "a:hover {\n" +
//                                "  color: #535bf2;\n" +
//                                "}\n" +
//                                "\n" +
//                                "body {\n" +
//                                "  margin: 0;\n" +
//                                "  display: flex;\n" +
//                                "  place-items: center;\n" +
//                                "  min-width: 320px;\n" +
//                                "  min-height: 100vh;\n" +
//                                "}\n" +
//                                "\n" +
//                                "h1 {\n" +
//                                "  font-size: 3.2em;\n" +
//                                "  line-height: 1.1;\n" +
//                                "}\n" +
//                                "\n" +
//                                "button {\n" +
//                                "  border-radius: 8px;\n" +
//                                "  border: 1px solid transparent;\n" +
//                                "  padding: 0.6em 1.2em;\n" +
//                                "  font-size: 1em;\n" +
//                                "  font-weight: 500;\n" +
//                                "  font-family: inherit;\n" +
//                                "  background-color: #1a1a1a;\n" +
//                                "  cursor: pointer;\n" +
//                                "  transition: border-color 0.25s;\n" +
//                                "}\n" +
//                                "button:hover {\n" +
//                                "  border-color: #646cff;\n" +
//                                "}\n" +
//                                "button:focus,\n" +
//                                "button:focus-visible {\n" +
//                                "  outline: 4px auto -webkit-focus-ring-color;\n" +
//                                "}\n" +
//                                "\n" +
//                                "@media (prefers-color-scheme: light) {\n" +
//                                "  :root {\n" +
//                                "    color: #213547;\n" +
//                                "    background-color: #ffffff;\n" +
//                                "  }\n" +
//                                "  a:hover {\n" +
//                                "    color: #747bff;\n" +
//                                "  }\n" +
//                                "  button {\n" +
//                                "    background-color: #f9f9f9;\n" +
//                                "  }\n" +
//                                "}\n"
//                    ),
//                ),
//            )
//        ),
//        FilePathsAndContents.initializeMerged(
//            listOf(
//                SourcedStruct2(
//                    DdlGithubSchema.File.FilePath.constant("src/App.jsx"),
//                    DdlGithubSchema.File.Content.constant(
//                        "import React, { useState } from 'react'\n" +
//                                "import viteLogo from '/vite.svg'\n" +
//                                "import './App.css'\n" +
//                                "\n" +
//                                "function App() {\n" +
//                                "    const [count, setCount] = useState(0)\n" +
//                                "\n" +
//                                "    function generateGameBoard(difficulty) {\n" +
//                                "        // TODO: Implement game board generation logic\n" +
//                                "    }\n" +
//                                "\n" +
//                                "    function placeMines(numMines) {\n" +
//                                "        for (let i = 0; i < numMines; i++) {\n" +
//                                "            // Generate random coordinates for each mine\n" +
//                                "            const row = Math.floor(Math.random() * 10)\n" +
//                                "            const col = Math.floor(Math.random() * 10)\n" +
//                                "\n" +
//                                "            // Update the game board accordingly\n" +
//                                "            // TODO: Implement logic to place mines on the game board\n" +
//                                "        }\n" +
//                                "    }\n" +
//                                "\n" +
//                                "    function calculateAdjacentMines(board) {\n" +
//                                "        const newBoard = []\n" +
//                                "        for (let row = 0; row < board.length; row++) {\n" +
//                                "            const newRow = []\n" +
//                                "            for (let col = 0; col < board[row].length; col++) {\n" +
//                                "                let adjacentMines = 0\n" +
//                                "                // Check neighboring cells for mines\n" +
//                                "                for (let i = -1; i <= 1; i++) {\n" +
//                                "                    for (let j = -1; j <= 1; j++) {\n" +
//                                "                        if (row + i >= 0 && row + i < board.length && col + j >= 0 && col + j < board[row].length) {\n" +
//                                "                            if (board[row + i][col + j] === 'mine') {\n" +
//                                "                                adjacentMines++\n" +
//                                "                            }\n" +
//                                "                        }\n" +
//                                "                    }\n" +
//                                "                }\n" +
//                                "                newRow.push(adjacentMines)\n" +
//                                "            }\n" +
//                                "            newBoard.push(newRow)\n" +
//                                "        }\n" +
//                                "        return newBoard\n" +
//                                "    }\n" +
//                                "\n" +
//                                "    // Call placeMines when the component is mounted\n" +
//                                "    React.useEffect(() => {\n" +
//                                "        placeMines(10) // Change the number of mines as desired\n" +
//                                "    }, [])\n" +
//                                "\n" +
//                                "    return (\n" +
//                                "        <>\n" +
//                                "            <div className=\"game-board\">\n" +
//                                "                {/* Render the grid of cells */}\n" +
//                                "                {Array.from({ length: 10 }, (_, rowIndex) => (\n" +
//                                "                    <div className=\"row\" key={rowIndex}>\n" +
//                                "                        {Array.from({ length: 10 }, (_, colIndex) => (\n" +
//                                "                            <div className=\"cell\" key={`\${rowIndex}-\${colIndex}`} />\n" +
//                                "                        ))}\n" +
//                                "                    </div>\n" +
//                                "                ))}\n" +
//                                "            </div>\n" +
//                                "            <div>\n" +
//                                "                {/*Placeholder logo. TODO: Replace or remove*/}\n" +
//                                "                <img src={viteLogo} className=\"logo\" alt=\"Vite logo\" />\n" +
//                                "            </div>\n" +
//                                "            <h1>Placeholder</h1>\n" +
//                                "            <div className=\"card\">\n" +
//                                "                {/*Sample functionality incrementing a counter on button click. TODO: remove*/}\n" +
//                                "                <button onClick={() => setCount((count) => count + 1)}>\n" +
//                                "                    count is {count}\n" +
//                                "                </button>\n" +
//                                "                <p>\n" +
//                                "                    Edit <code>src/App.jsx</code> and save to test HMR\n" +
//                                "                </p>\n" +
//                                "            </div>\n" +
//                                "        </>\n" +
//                                "    )\n" +
//                                "}\n" +
//                                "\n" +
//                                "export default App\n" +
//                                "\n"
//                    ),
//                ),
//            ),
//        ),
//    )
//
//    val contextualCommitCompletionApplication: AiApplication<SourcedStruct7<DdlGithubSchema.Project.FullName.Value, DdlGithubSchema.ProjectTask.TaskQuery.Value, DdlGithubSchema.ProjectSubtask.Subtask.Value, DdlGithubSchema.ProjectSubtaskCommit.CommitMessage.Value, DdlGithubSchema.ProjectSubtaskCommit.DiffSummary.Value, NestedObjectListType2.Value<String, String?, String, String?, DdlGithubSchema.File.FilePath.Value, DdlGithubSchema.File.Content.Value>, NestedObjectListType2.Value<String, String?, String, String?, DdlGithubSchema.File.FilePath.Value, DdlGithubSchema.File.Content.Value>>, SourcedStruct4<DdlGithubSchema.ProjectSubtaskCommitCompletionStatus.PartiallyComplete.Value, DdlGithubSchema.ProjectSubtaskCommitCompletionStatus.MostlyComplete.Value, DdlGithubSchema.ProjectSubtaskCommitCompletionStatus.Complete.Value, NestedObjectListType3.Value<String?, String, String?, String?, String, String?, DdlGithubSchema.ProjectSubtaskCommitFileOp.FileOperation.Value, DdlGithubSchema.File.FilePath.Value, DdlGithubSchema.ProjectSubtaskCommitFileOp.Description.Value>>> =
//        applicationFactory
//            .builder("contextual-commit-completion-4")
//            .withInputSchema {
//                p(DdlGithubSchema.Project.FullName)
//                    .p(DdlGithubSchema.ProjectTask.TaskQuery)
//                    .withKey("main_task")
//                    .withDescription("Main Task: The main task being accomplished, which this commit is a part of.")
//                    .p(DdlGithubSchema.ProjectSubtask.Subtask)
//                    .withKey("subtask")
//                    .withDescription("Subtask: A subtask of the main task being accomplished, which this commit is a part of.")
//                    .p(DdlGithubSchema.ProjectSubtaskCommit.CommitMessage)
//                    .withKey("current_commit_message")
//                    .withDescription("Current Commit Message: The commit message for this commit being evaluated.")
//                    .p(DdlGithubSchema.ProjectSubtaskCommit.DiffSummary)
//                    .withKey("current_commit_summary")
//                    .withDescription("Current Commit Summary: A summary of the goal of this commit.")
//                    .p(FilePathsAndContents) // relevant other file contents
//                    .withKey("relevant_other_file_contents")
//                    .p(FilePathsAndContents) // updated file contents
//                    .withKey("updated_file_contents")
//            }
//            .withInputSerializer(trailingContentPropertyListSerializer)
//            .withOutputSchema {
//                p(DdlGithubSchema.ProjectSubtaskCommitCompletionStatus.PartiallyComplete)
//                    .p(DdlGithubSchema.ProjectSubtaskCommitCompletionStatus.MostlyComplete)
//                    .p(DdlGithubSchema.ProjectSubtaskCommitCompletionStatus.Complete)
//                    .p(FileOperationsAndTargetFilePathsWithDescriptions)
//                    .withKey("necessary_remaining_operations")
//            }
//            .withInstructions { inputSchemaDescription, outputSchemaDescription ->
//                (
//                        "You are an assistant who, given an input of the following form:\n"
//                                + inputSchemaDescription
//                                + "\n\nProduces an output of the following form:\n"
//                                + outputSchemaDescription
//                                + "\n\nYour goal is to decide if the goal of the current commit has been accomplished given" +
//                                " the current state of the provided files in the project." +
//                                " If the code reflects significant, non-trivial progress towards accomplishing the goal, then you " +
//                                "should flag the commit as at least partially complete. If the code reflects complete " +
//                                "accomplishment the commit, you should flag the commit as complete. If the commit is not completely" +
//                                " accomplished, you should describe at most 2 remaining commits, each in at most 300 words. " +
//                                "Always give a boolean value \"true\" or \"false\" for each of the flags. If the commit is " +
//                                "only partially complete, always give at least one remaining commit. Ensure the output is formatted as YAML." +
//                                "\nEach remaining commit should be given as an imperative statement similar to a commit message." +
//                                " If there are unrelated changes included, ignore them for the sake of evaluating the completion progress of the commit." +
//                                " You may act optimistically about the completion status of the subtask; if it is nearly complete, mark it as complete."
//                        )
//            }
//            .withExample(
//                inputModel,
//                SourcedStruct4(
//                    DdlGithubSchema.ProjectSubtaskCommitCompletionStatus.PartiallyComplete.constant(true),
//                    DdlGithubSchema.ProjectSubtaskCommitCompletionStatus.MostlyComplete.constant(false),
//                    DdlGithubSchema.ProjectSubtaskCommitCompletionStatus.Complete.constant(false),
//                    FileOperationsAndTargetFilePathsWithDescriptions.initializeMerged(
//                        listOf(
//                            SourcedStruct3(
//                                DdlGithubSchema.ProjectSubtaskCommitFileOp.FileOperation.constant("edit_file"),
//                                DdlGithubSchema.File.FilePath.constant("src/App.jsx"),
//                                DdlGithubSchema.ProjectSubtaskCommitFileOp.Description.constant(
//                                    "Update the `generateGameBoard` function to generate the game board layout based on the difficulty level. Currently, the function is empty and needs to be implemented. Then, implement the logic in the `placeMines` function to place the specified number of mines on the game board. Currently, the function generates random coordinates for each mine, but does not update the game board accordingly."
//                                )
//                            )
//                        )
//                    ),
//                ),
//            )
//            .withOutputProcessor {
//                // Common bad output for some reason
//                it.copy(content = it.content.replace("partial_complete:", "partially_complete:"))
//            }
//            .withPromptShortener { record, _ ->
//                record.copy(
//                    t6 = record.t6.let { fileSummaries ->
//                        fileSummaries.copy(
//                            value = fileSummaries.value.dropLast(1)
//                        )
//                    }
//                )
//            }
//            .withMaxTokens(512)
//            .withModel(OpenAiChatCompletionModel.GPT_4_0613)
//            .build()
//
//    private inline fun <reified T> objectClassProxy() = T::class.java
//
//    @Test
//    fun `includes examples`() {
//        Flux.create<ApplicationArtifact> { sink ->
//            val result = contextualCommitCompletionApplication.invoke(
//                inputModel,
//                artifactSink = sink,
//            )
//                .block()!!
//
//            sink.complete()
//        }.collectList().block()
//    }
//}