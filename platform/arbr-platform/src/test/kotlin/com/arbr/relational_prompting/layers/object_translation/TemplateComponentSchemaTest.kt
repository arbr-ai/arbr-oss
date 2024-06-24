package com.arbr.relational_prompting.layers.object_translation//package com.arbr.relational_prompting.layers.object_translation//package com.arbr.relational_prompting.layers.object_translation
//
//import com.arbr.content_formats.json_schematized.JsonSchema
//import com.arbr.relational_prompting.object_model.common._UserAccountSchema
////import com.arbr.og.object_model.impl.DdlGithubSchema
//import com.arbr.og.object_model.impl.NestedObjectListType1
//import com.arbr.og.object_model.impl.NestedObjectListType2
//import com.arbr.og.object_model.common.values.collections.SourcedStruct1
//import com.arbr.og.object_model.common.values.collections.SourcedStruct2
//import com.arbr.relational_prompting.services.ai_application.model.SourcedValueGeneratorInfo
//import com.arbr.relational_prompting.generics.model.ChatMessage
//import com.arbr.og.object_model.impl.DdlGithubSchema
//import com.arbr.og.object_model.impl.NestedObjectListType6
//import org.junit.jupiter.api.Assertions
//import org.junit.jupiter.api.Test
//
//class TemplateComponentSchemaTest {
//
//    @Test
//    fun `builder builds and renders description`() {
//        val tcs = schema {
//            p(_UserAccountSchema.Username)
//                .p(_UserAccountSchema.UsernameFriend)
//        }
//
//        Assertions.assertEquals(
//            """
//            ```yaml
//            username: # (string) Username: The user's account name.
//            friend_username: # (string) Username: The user's friend's account name.
//            ```
//        """.trimIndent(), tcs.description().description
//        )
//    }
//
//    @Test
//    fun `builder builds and renders description 2`() {
//        val innerType1 = NestedObjectListType2(
//            "usernames",
//            "User and their friend's usernames",
//            _UserAccountSchema.Username,
//            _UserAccountSchema.UsernameFriend,
//        )
//        val nestedObjectListType = NestedObjectListType2(
//            "usernames",
//            "User and their friend's usernames",
//            innerType1,
//            _UserAccountSchema.UsernameFriend,
//        )
//
//        val tcs = schema {
//            p(nestedObjectListType)
//        }
//
//        Assertions.assertEquals(
//            """
//            ```yaml
//            usernames: # User and their friend's usernames
//              - usernames: # User and their friend's usernames
//                  - username: # (string) Username: The user's account name.
//                    friend_username: # (string) Username: The user's friend's account name.
//                  - # ...
//                friend_username: # (string) Username: The user's friend's account name.
//              - # ...
//            ```
//        """.trimIndent(), tcs.description().description
//        )
//        println(tcs.description().description)
//
//        val nestedValue = innerType1.initializeMerged(
//            listOf(
//                SourcedStruct2(
//                    _UserAccountSchema.Username.Value.constant("Bob"),
//                    _UserAccountSchema.UsernameFriend.Value.constant("Steve"),
//                ),
//                SourcedStruct2(
//                    _UserAccountSchema.Username.Value.constant("Frob"),
//                    _UserAccountSchema.UsernameFriend.Value.constant("Lob"),
//                )
//            )
//        )
//
//        val objValue = nestedObjectListType.initializeMerged(
//            listOf(
//                SourcedStruct2(
//                    nestedValue,
//                    _UserAccountSchema.UsernameFriend.Value.constant("Steve"),
//                )
//            )
//        )
//
//        val serialized = tcs.serializedValue(
//            SourcedStruct1(objValue)
//        )
//        println(serialized)
//    }
//
//    @Test
//    fun `builder builds and renders values`() {
//        val tcs = schema {
//            p(_UserAccountSchema.Username)
//                .p(_UserAccountSchema.UsernameFriend)
//        }
//
//        Assertions.assertEquals(
//            """
//            ```yaml
//            username: # (string) Username: The user's account name.
//            friend_username: # (string) Username: The user's friend's account name.
//            ```
//        """.trimIndent(), tcs.description().description
//        )
//    }
//
//    @Test
//    fun `duplicate property throws`() {
//        Assertions.assertThrows(TemplateComponentSchema.DuplicatePropertyKeyException::class.java) {
//            schema {
//                p(_UserAccountSchema.Username)
//                    .p(_UserAccountSchema.Username)
//            }
//        }
//    }
//
//    @Test
//    fun `updates key of last property`() {
//        val tcs = schema {
//            p(_UserAccountSchema.Username)
//                .p(_UserAccountSchema.Username)
//                .configure { js ->
//                    js.copy(
//                        properties = LinkedHashMap<String, JsonSchema>().also {
//                            it["different_key"] = js.properties!!.values.first()
//                        }
//                    )
//                }
//        }
//
//        Assertions.assertEquals(
//            """
//            ```yaml
//            username: # (string) Username: The user's account name.
//            different_key: # (string) Username: The user's account name.
//            ```
//        """.trimIndent(), tcs.description().description
//        )
//    }
//
//    @Test
//    fun `updates key and description of last property with convenience method`() {
//        val tcs = schema {
//            p(_UserAccountSchema.Username)
//                .withDescription("Name: The user's own account name.")
//                .p(_UserAccountSchema.Username)
//                .withKey("another_key")
//        }
//
//        Assertions.assertEquals(
//            """
//            ```yaml
//            username: # (string) Name: The user's own account name.
//            another_key: # (string) Username: The user's account name.
//            ```
//        """.trimIndent(), tcs.description().description
//        )
//    }
//
//    @Test
//    fun `serializes values`() {
//        val tcs = schema {
//            p(_UserAccountSchema.Username)
//                .p(_UserAccountSchema.Username)
//                .configure { js ->
//                    js.copy(
//                        properties = LinkedHashMap<String, JsonSchema>().also {
//                            it["different_key"] = js.properties!!.values.first()
//                        }
//                    )
//                }
//        }
//
//        val serialized = tcs.serializedValue(
//            SourcedStruct2(
//                _UserAccountSchema.Username.Value.constant("Bob"),
//                _UserAccountSchema.Username.Value.constant("Alice")
//            )
//        )
//
//        Assertions.assertEquals(
//            """
//            ```yaml
//            username: "Bob"
//            different_key: "Alice"
//            ```
//        """.trimIndent(), serialized.serializedValue
//        )
//    }
//
//    @Test
//    fun `parses values`() {
//        val outputSchema = schema {
//            p(_UserAccountSchema.Username)
//                .p(_UserAccountSchema.Username)
//                .configure { js ->
//                    js.copy(
//                        properties = LinkedHashMap<String, JsonSchema>().also {
//                            it["other_username"] = js.properties!!.values.first()
//                        }
//                    )
//                }
//        }
//        val content = """
//            ```yaml
//            username: "Bill" # (string) Username: The user's account name.
//            other_username: "Jack" # (string) Username: The other user's account name.
//            ```
//        """.trimIndent()
//        val parsed =
//            outputSchema.parse(ChatMessage(ChatMessage.Role.ASSISTANT, content), SourcedValueGeneratorInfo(emptyList()))
//
//        Assertions.assertNotNull(parsed)
//        Assertions.assertEquals("Bill", parsed!!.t1.value)
//        Assertions.assertEquals("Jack", parsed.t2.value)
//    }
//
//    @Test
//    fun `parses value`() {
//        val outputSchema = schema {
//            p(_UserAccountSchema.Username)
//                .p(_UserAccountSchema.Username)
//                .configure { js ->
//                    js.copy(
//                        properties = LinkedHashMap<String, JsonSchema>().also {
//                            it["different_key"] = js.properties!!.values.first()
//                        }
//                    )
//                }
//        }
//        val content = """
//            ```yaml
//            username: "Bill" # (string) Username: The user's account name.
//            different_key: "Bob" # (string) Username: The user's account name.
//            ```
//        """.trimIndent()
//        val parsed =
//            outputSchema.parse(ChatMessage(ChatMessage.Role.ASSISTANT, content), SourcedValueGeneratorInfo(emptyList()))
//
//        Assertions.assertNotNull(parsed)
//        Assertions.assertEquals("Bill", parsed!!.t1.value)
//        Assertions.assertEquals("Bob", parsed.t2.value)
//    }
//
////    @Test
////    fun `identifies schema violation`() {
////        val commitDetailsAndFileOps: NestedObjectListType3<String, String?, List<CombinedSchema.FileOperationsAndTargetFilePaths.InnerValue>, String, String?, JSONB, DdlGithubSchema.ProjectSubtaskCommit.CommitMessageDescription.Value, DdlGithubSchema.ProjectSubtaskCommit.DiffSummary.Value, CombinedSchema.FileOperationsAndTargetFilePaths.Value> =
////            NestedObjectListType3(
////                "commit_details_and_file_operations",
////                "Array of commit information planned for the task with the commit message and a summary of" +
////                        " the commit, as well as the associated file operations.",
////                DdlGithubSchema.ProjectSubtaskCommit.CommitMessageDescription,
////                DdlGithubSchema.ProjectSubtaskCommit.DiffSummary,
////                CombinedSchema.FileOperationsAndTargetFilePaths,
////            )
////
////        val subtaskPlans: NestedObjectListType2<String, List<NestedObjectListType3.InnerValue<String, String?, List<CombinedSchema.FileOperationsAndTargetFilePaths.InnerValue>>>, String, List<JSONB>, DdlGithubSchema.ProjectSubtask.Subtask.Value, NestedObjectListType3.Value<String, String?, List<CombinedSchema.FileOperationsAndTargetFilePaths.InnerValue>, String, String?, JSONB, DdlGithubSchema.ProjectSubtaskCommit.CommitMessageDescription.Value, DdlGithubSchema.ProjectSubtaskCommit.DiffSummary.Value, CombinedSchema.FileOperationsAndTargetFilePaths.Value>> =
////            NestedObjectListType2(
////                "subtask_plans",
////                "A list of plans for subtasks of the main task.",
////                DdlGithubSchema.ProjectSubtask.Subtask,
////                commitDetailsAndFileOps,
////            )
////
////        val outputSchema = schema {
////            p(subtaskPlans)
////        }
////
////        val content = """
////            ```yaml
////            subtask_plans:
////              - subtask: "Create game board state variable"
////                commit_details_and_file_operations:
////                  - commit_message_description: "Create game board state variable"
////                    diff_summary: "Create a state variable inside the `App` component to represent the game board. This variable will hold the state of each cell in the grid, including whether it is a mine, flagged, or uncovered."
////                    file_operations_and_target_file_paths:
////                      - file_operation: "edit_file"
////                        file_path: "src/App.jsx"
////              - subtask: "Initialize game board state"
////                commit_details_and_file_operations:
////                  - commit_message_description: "Initialize game board state"
////                    diff_summary: "Initialize the game board state with an empty grid of cells. Each cell should have properties like `isMine`, `isFlagged`, and `isUncovered`."
////                    file_operations_and_target_file_paths:
////                      - file_operation: "edit_file"
////                        file_path: "src/App.jsx"
////              - subtask: "Generate mines on game board"
////                commit_details_and_file_operations:
////                  - commit_message_description: "Generate mines on game board"
////                    diff_summary: "Implement a function to generate the mines on the game board. This function should randomly place a specified number of mines on the grid."
////                    file_operations_and_target_file_paths:
////                      - file_operation: "edit_file"
////                        file_path: "src/App.jsx"
////              - subtask: "Handle user clicks on cells"
////                commit_details_and_file_operations:
////                  - commit_message_description: "Handle user clicks on cells"
////                    diff_summary: "Implement a function to handle user clicks on the cells. This function should update the state of the clicked cell based on the game rules. If the clicked cell is a mine, the game should end. If the clicked cell is not a mine, it should be uncovered, and adjacent cells should be recursively uncovered if they are not mines."
////                    file_operations_and_target_file_paths:
////                      - file_operation: "edit_file"
////                        file_path: "src/App.jsx"
////              - subtask: "Add event listeners for cell clicks"
////                commit_details_and_file_operations:
////                  - commit_message_description: "Add event listeners for cell clicks"
////                    diff_summary: "Add event listeners to the cells in the grid to trigger the click handler function when a cell is clicked."
////                    file_operations_and_target_file_paths:
////                      - file_operation: "edit_file"
////                        file_path: "src/App.jsx"
////              - subtask: "Handle right-clicks on cells"
////                commit_details_and_file_operations:
////                  - commit_message_description: "Handle right-clicks on cells"
////                    diff_summary: "Create a function to handle right-clicks on the cells. This function should toggle the flagged state of the clicked cell."
////                    file_operations_and_target_file_paths:
////                      - file_operation: "edit_file"
////                        file_path: "src/App.jsx"
////              - subtask: "Add event listeners for right-clicks"
////                commit_details_and_file_operations:
////                  - commit_message_description: "Add event listeners for right-clicks"
////                    diff_summary: "Add event listeners to the cells in the grid to trigger the right-click handler function when a cell is right-clicked."
////                    file_operations_and_target_file_paths:
////                      - file_operation: "edit_file"
////                        file_path: "src/App.jsx"
////              - subtask: "Check if game has been won"
////                commit_details_and_file_operations:
////                  - commit_message_description: "Check if game has been won"
////                    diff_summary: "Implement a function to check if the game has been won. This function should iterate over all cells and check if all non-mine cells have been uncovered."
////                    file_operations_and_target_file_paths:
////                      - file_operation: "edit_file"
////                        file_path: "src/App.jsx"
////              - subtask: "Display game board on web page"
////                commit_details_and_file_operations:
////                  - commit_message_description: "Display game board on web page"
////                    diff_summary: "Display the game board on the web page. Use HTML and CSS to create a grid layout and style the cells based on their state."
////                    file_operations_and_target_file_paths:
////                      - file_operation: "edit_file"
////                        file_path: "src/App.jsx"
////                      - file_operation: "edit_file"
////                        file_path: "src/App.css"
////                      - file_operation: "edit_file"
////                        file_path: "src/index.css"
////                      - file_operation: "edit_file"
////                        file_path: "src/main.jsx"
////                      - file_operation: "edit_file"
////                        file_path: "index.html"
////              - subtask: "Add game status message"
////                commit_details_and_file_operations:
////                  - commit_message_description: "Add game status message"
////                    diff_summary: "Add a game status message to indicate whether the game is ongoing, won, or lost."
////                    file_operations_and_target_file_paths:
////                      - file_operation: "edit_file"
////                        file_path: "src/App.jsx"
////              - subtask: "Test the game"
////                commit_details_and_file_operations:
////                  - commit_message_description: "Test the game"
////                    diff_summary: "Test the game by playing it in the web browser. Make sure all game rules are correctly implemented and the game behaves as expected."
////              - subtask: "Add additional features"
////                commit_details_and_file_operations:
////                  - commit_message_description: "Add additional features"
////                    diff_summary: "Once the game is working correctly, consider adding additional features like a timer, difficulty levels, or a high score leaderboard."
////              - subtask: "Commit and push changes"
////                commit_details_and_file_operations:
////                  - commit_message_description: "Commit and push changes"
////                    diff_summary: "Commit your changes and push them to the remote repository."
////              - subtask: "Deploy web app"
////                commit_details_and_file_operations:
////                  - commit_message_description: "Deploy web app"
////                    diff_summary: "Deploy the web app to a hosting platform of your choice, such as GitHub Pages or Netlify, so that others can play the game online."
////              - subtask: "Update README.md"
////                commit_details_and_file_operations:
////                  - commit_message_description: "Update README.md"
////                    diff_summary: "Update the `README.md` file with instructions on how to play the game and any additional information about the project."
////        ```
////        """.trimIndent()
////
////        try {
////            val result = outputSchema.parse(ChatMessage(ChatMessage.Role.ASSISTANT, content), SourcedValueGeneratorInfo(emptyList()))
////
////            println(result!!.t1)
////            Assertions.fail<Unit>()
////        } catch (se: OutputSchemaExceptionWithKnownViolations) {
////            Assertions.assertEquals(5, se.violations.size)
////        }
////    }
//
//    @Test
//    fun `parses ad hoc type`() {
//        val subtaskString = """Update the greeting message in the README file to include the name of the user."""
//        val output = """
//            ```yaml
//            partially_complete: true
//            complete: false
//            subtasks:
//              - subtask: $subtaskString
//            ```
//        """.trimIndent()
//
//        val subtasksType: NestedObjectListType1<String, String, DdlGithubSchema.ProjectSubtask.Subtask.Value> =
//            NestedObjectListType1(
//                "subtasks",
//                "Subtasks which should be done in order to accomplish the main task.",
//                DdlGithubSchema.ProjectSubtask.Subtask,
//            )
//
//        val schema = schema {
//            p(DdlGithubSchema.ProjectTaskCompletionStatus.PartiallyComplete)
//                .p(DdlGithubSchema.ProjectTaskCompletionStatus.Complete)
//                .p(subtasksType)
//        }
//
//        val result =
//            schema.parse(ChatMessage(ChatMessage.Role.ASSISTANT, output), SourcedValueGeneratorInfo(emptyList()))
//        Assertions.assertNotNull(result)
//        val (partiallyComplete, complete, subtasks) = result!!
//
//        Assertions.assertTrue(partiallyComplete.value)
//        Assertions.assertFalse(complete.value)
//        val subtasksList = subtasks.value
//        Assertions.assertEquals(1, subtasksList.size)
//        subtasksList.first().t1
//        Assertions.assertEquals(subtaskString, subtasksList.first().t1)
//    }
//
////    @Test
////    fun `parses cache list`() {
////        val jsonString = "[{\"id\": \"a6f540ae-079d-429e-af75-a54bbef7c75a\", \"kind\": \"MATERIALIZED\", \"value\": \"topdown-bill/minesweeper-17\", \"schema\": {\"type\": \"object\", \"required\": [\"full_name\"], \"properties\": {\"full_name\": {\"type\": [\"string\"], \"description\": \"Repo Full Name: The full name of the repository, like `organization/repo`.\"}}}, \"typeName\": \"DdlGithubSchema.Project.FullName\", \"generatorInfo\": {\"generators\": []}}, {\"id\": \"83a2b200-8414-4dcb-9ee0-a9fbf2f9d5c5\", \"kind\": \"CONSTANT\", \"value\": \"Add a greeting message to the README file.\", \"schema\": {\"type\": \"object\", \"required\": [\"task_query\"], \"properties\": {\"task_query\": {\"type\": [\"string\"], \"description\": \"Task Summary: Summary of the coding task to be completed.\"}}}, \"typeName\": \"DdlGithubSchema.ProjectTask.TaskQuery\", \"generatorInfo\": {\"generators\": []}}, {\"id\": \"7e2b472b-90f5-42cd-b97e-8a7b8ff2d5b6\", \"kind\": \"CONSTANT\", \"value\": [{\"diff_summary\": \"Add a greeting and a description of the project to README.md.\", \"commit_message_description\": \"Add message to README.md\", \"file_operations_and_target_file_paths\": [{\"file_path\": \"README.md\", \"file_operation\": \"edit_file\"}]}], \"schema\": {\"type\": \"object\", \"required\": [\"commit_details_and_file_operations\"], \"properties\": {\"commit_details_and_file_operations\": {\"type\": \"array\", \"items\": {\"type\": \"object\", \"required\": [\"commit_message_description\", \"diff_summary\", \"file_operations_and_target_file_paths\"], \"properties\": {\"diff_summary\": {\"type\": [\"string\"], \"description\": \"Diff Summary: A summary of the content of the commit, where the content is given by `git diff`. About 300 words.\"}, \"commit_message_description\": {\"type\": [\"string\"], \"description\": \"Commit Message: Short message for the git commit.\"}, \"file_operations_and_target_file_paths\": {\"type\": \"array\", \"items\": {\"type\": \"object\", \"required\": [\"file_operation\", \"file_path\"], \"properties\": {\"file_path\": {\"type\": [\"string\"], \"description\": \"File Path: The path to the file within the GitHub repository.\"}, \"file_operation\": {\"type\": [\"string\", \"null\"], \"description\": \"File Operation: A file operation to perform. One of [create_file, edit_file, delete_file].\"}}}, \"description\": \"File operation names (create, edit, delete) and their target file paths.\"}}}, \"required\": [\"commit_message_description\", \"diff_summary\", \"file_operations_and_target_file_paths\"], \"description\": \"Array of commit information planned for the task with the commit message and a summary of the commit, as well as the associated file operations.\"}}}, \"typeName\": \"NestedObjectListType3\", \"generatorInfo\": {\"generators\": []}}, {\"id\": \"2c29fea2-de56-46f8-9df6-8e0580da0b9c\", \"kind\": \"COMPUTED\", \"value\": \"Add message to README.md\", \"schema\": {\"type\": \"object\", \"required\": [\"commit_message_description\"], \"properties\": {\"commit_message_description\": {\"type\": [\"string\"], \"description\": \"Commit Message: Short message for the git commit.\"}}}, \"typeName\": \"DdlGithubSchema.ProjectSubtaskCommit.CommitMessageDescription\", \"generatorInfo\": {\"generators\": [{\"operationId\": \"projection\", \"applicationId\": null, \"parentValueIds\": [\"4d6d4024-c740-4b26-b4c7-8d3692002549\"], \"completionCacheKey\": null}]}}, {\"id\": \"c479a9b9-6282-4536-b8d0-9b4040285c09\", \"kind\": \"COMPUTED\", \"value\": \"Add a greeting and a description of the project to README.md.\", \"schema\": {\"type\": \"object\", \"required\": [\"diff_summary\"], \"properties\": {\"diff_summary\": {\"type\": [\"string\"], \"description\": \"Diff Summary: A summary of the content of the commit, where the content is given by `git diff`. About 300 words.\"}}}, \"typeName\": \"DdlGithubSchema.ProjectSubtaskCommit.DiffSummary\", \"generatorInfo\": {\"generators\": [{\"operationId\": \"projection\", \"applicationId\": null, \"parentValueIds\": [\"4d6d4024-c740-4b26-b4c7-8d3692002549\"], \"completionCacheKey\": null}]}}, {\"id\": \"bcbaad28-86e1-476d-817a-175782ab006c\", \"kind\": \"COMPUTED\", \"value\": \"README.md\", \"schema\": {\"type\": \"object\", \"required\": [\"file_path\"], \"properties\": {\"file_path\": {\"type\": [\"string\"], \"description\": \"File Path: The path to the file within the GitHub repository.\"}}}, \"typeName\": \"DdlGithubSchema.File.FilePath\", \"generatorInfo\": {\"generators\": [{\"operationId\": \"projection\", \"applicationId\": null, \"parentValueIds\": [\"74d344ca-8a7d-4858-87ef-02a49df949a0\"], \"completionCacheKey\": null}]}}, {\"id\": \"aebe5c41-69f9-4693-99e6-adb0a0d500c0\", \"kind\": \"GENERATED\", \"value\": \"The README.md file in the topdown-bill/minesweeper-17 repository provides a brief overview of the project setup. It mentions that the project is built using React and Vite, and it includes two official plugins: @vitejs/plugin-react and @vitejs/plugin-react-swc. The README.md file also provides links to the documentation of these plugins and the tools they use, such as Babel and SWC. Overall, the README.md file serves as a guide for setting up React in Vite with HMR and ESLint rules.\", \"schema\": {\"type\": \"object\", \"required\": [\"summary\"], \"properties\": {\"summary\": {\"type\": [\"string\"], \"description\": \"File Summary: A summary of the contents of the file. This summary covers the structure and purpose of the file in the project. It focuses on specific factual aspects of the file. About 300 words.\"}}}, \"typeName\": \"DdlGithubSchema.File.Summary\", \"generatorInfo\": {\"generators\": [{\"operationId\": null, \"applicationId\": \"github-existing-file-summarizer-yaml-2\", \"parentValueIds\": [\"58ac8fac-e9bb-4cf0-8669-7545b2c11419\", \"f3fc98a1-0e86-4e5b-a526-785fd2fd071b\", \"b76eccea-3b14-4493-9e1e-7bfea7b2411a\", \"6b9f028e-880e-44f7-b72e-ab3f20ab9377\", \"3ef8eb1c-ca54-42fe-8978-aa2fb6738fad\", \"665b413e-cf64-4a85-85a4-a7ed2b801c2b\", \"3ef77a3b-3a0a-4828-be49-e73d98180836\", \"602932f5-e826-4054-9bdb-cb10eb532028\"], \"completionCacheKey\": \"d976ea7d2488ea812850086b404a793c0d79d688\"}]}}, {\"id\": \"05fd1570-4872-48a1-aa38-72e7d3d56a44\", \"kind\": \"MATERIALIZED\", \"value\": \"# React + Vite\\n\\nThis template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.\\n\\nCurrently, two official plugins are available:\\n\\n- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react/README.md) uses [Babel](https://babeljs.io/) for Fast Refresh\\n- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react-swc) uses [SWC](https://swc.rs/) for Fast Refresh\\n\", \"schema\": {\"type\": \"object\", \"required\": [\"content\"], \"properties\": {\"content\": {\"type\": [\"string\"], \"description\": \"File Contents: The contents of the file.\"}}}, \"typeName\": \"DdlGithubSchema.File.Content\", \"generatorInfo\": {\"generators\": []}}]"
////
////        val commitDetailsAndFileOps: NestedObjectListType3<String, String?, List<CombinedSchema.FileOperationsAndTargetFilePaths.InnerValue>, String, String?, JSONB, DdlGithubSchema.ProjectSubtaskCommit.CommitMessageDescription.Value, DdlGithubSchema.ProjectSubtaskCommit.DiffSummary.Value, CombinedSchema.FileOperationsAndTargetFilePaths.Value> =
////            NestedObjectListType3(
////                "commit_details_and_file_operations",
////                "Array of commit information planned for the task with the commit message and a summary of" +
////                        " the commit, as well as the associated file operations.",
////                DdlGithubSchema.ProjectSubtaskCommit.CommitMessageDescription,
////                DdlGithubSchema.ProjectSubtaskCommit.DiffSummary,
////                CombinedSchema.FileOperationsAndTargetFilePaths,
////            )
////        val inputSchema = schema {
////            p(DdlGithubSchema.Project.FullName)
////                .p(DdlGithubSchema.ProjectTask.TaskQuery)
////                .p(commitDetailsAndFileOps)
////                .p(DdlGithubSchema.ProjectSubtaskCommit.CommitMessageDescription)
////                .p(DdlGithubSchema.ProjectSubtaskCommit.DiffSummary)
////                .p(DdlGithubSchema.File.FilePath)
////                .p(DdlGithubSchema.File.Summary)
////                .p(DdlGithubSchema.File.Content)
////        }
////
////        val genericSourcedValueList = jacksonTypeRef<List<SourcedValue<*>>>()
////        val mapper = jacksonObjectMapper()
////        val inputProxyObject = mapper.readValue(jsonString, genericSourcedValueList)
////
////        val res = inputSchema.deserializeFromSourcedValues(inputProxyObject)
////        for (sv in res) {
////            println(mapper.writeValueAsString(sv))
////            Assertions.assertNotNull(sv.value)
////        }
////
////    }
//
//    private val FileSegmentOperations =
//        NestedObjectListType6(
//            "source_element_operations",
//            "Source Element Operations: Operations on source code elements, i.e. segments of the file, including the operation (one of [add, edit, delete]), the content type of the element, the kind of source code element, the name of the source code element within the file, and a description of the change.",
//            DdlGithubSchema.FileSegmentOp.Operation,
//            DdlGithubSchema.FileSegment.ContentType,
//            DdlGithubSchema.FileSegment.RuleName,
//            DdlGithubSchema.FileSegment.Name,
//            DdlGithubSchema.FileSegment.ElementIndex,
//            DdlGithubSchema.FileSegmentOp.Description,
//        )
//
//    @Test
//    fun `parses file segment ops output`() {
//        val outputSchema = schema {
//            p(FileSegmentOperations)
//        }
//
//        val result = outputSchema.parse(
//            ChatMessage(ChatMessage.Role.ASSISTANT, "```yaml\nsource_element_operations:\n  - operation: \"edit\"\n    content_type: \"jsx\"\n    rule_name: \"function\"\n    name: \"App\"\n    element_index: 0\n    description: \"Replace the existing `useState` call for `count` with a new `useState` call for an object with properties `currentInput`, `previousInput`, and `operation`, all initialized to null. Update the `setCount` function call to a new `setState` function call, which updates the state object with the new values for `currentInput`, `previousInput`, and `operation` whenever the user interacts with the calculator.\"\n```"),
//            SourcedValueGeneratorInfo(emptyList())
//        )
//        Assertions.assertEquals(0L, result!!.t1.containers.first().t5.value)
//    }
//
//}