package com.arbr.core_web_dev.util

// TODO: Delete or use

//import com.arbr.api.workflow.view_model.*
//import com.arbr.api.workflow.view_model.update.ViewModelValueUpdate
//import com.arbr.api.workflow.view_model.update.ViewModelValueUpdateOperation
//import java.time.Instant
//import kotlin.random.Random
//
//object WorkflowPartialViewModelSampleStreamUtils {
//
//    private val random = Random(123487964312L)
//    private const val SECONDS_BETWEEN_PHASE_CHANGE = 5 // Duration in seconds before changing to the next status phase
//
//    private val statuses = listOf(
//        WorkflowViewModelStatus.booting(),
//        WorkflowViewModelStatus.developing(),
//        WorkflowViewModelStatus.testing(),
//        WorkflowViewModelStatus.deploying()
//    )
//
//    private val activeTasks = listOf(
//        WorkflowViewModelActiveTask.building(),
//        WorkflowViewModelActiveTask.testingInWebBrowser(),
//        WorkflowViewModelActiveTask.deployingToServer(),
//        WorkflowViewModelActiveTask.monitoringSystem()
//    )
//
//    fun getInitialWorkflowViewModel(
//        workflowId: String,
//    ): WorkflowPartialViewModel {
//        val initialFileData = listOf(
//            WorkflowViewModelFileData(
//                fileName = "Component.jsx",
//                filePath = "src/components/Component.jsx",
//                fileContents = """function CustomCodeBlock({ code }) {
//                // ...
//                return (
//                    <div>
//                        {/* JSX code here */}
//                    </div>
//                );
//            }""".trimIndent(),
//                annotations = listOf(
//                    WorkflowViewModelFileLineAnnotation.diffAdded(2),
//                    WorkflowViewModelFileLineAnnotation.diffAdded(5),
//                    WorkflowViewModelFileLineAnnotation.diffDeleted(1),
//                    WorkflowViewModelFileLineAnnotation.diffDeleted(4),
//                )
//            ),
//            WorkflowViewModelFileData(
//                fileName = "styling.css",
//                filePath = "css/styling.css",
//                fileContents = """
//                    .container {
//                        display: flex;
//                        justify-content: center;
//                        align-items: center;
//                        flex-direction: column; /* Stack children vertically */
//                        padding: 20px;
//                        max-width: 800px; /* Limiting max width for better readability */
//                        margin: auto; /* Center the container on the page */
//                    }
//
//                    /* Hover effect for buttons */
//                    .button:hover {
//                        background-color: darkblue; /* Darken button on hover */
//                        transition: background-color 0.3s; /* Smooth transition */
//                    }
//
//                    /* Additional button styles for different actions */
//                    .button-primary {
//                        background-color: #007bff; /* Bootstrap primary color */
//                        color: #ffffff;
//                    }
//
//                    .button-secondary {
//                        background-color: grey;
//                        color: #ffffff;
//                    }
//
//                    /* Typography for headings and paragraphs */
//                    h1, h2, h3 {
//                        color: #333;
//                        font-family: 'Arial', sans-serif;
//                    }
//
//                    p {
//                        color: #666;
//                        font-family: 'Georgia', serif;
//                        line-height: 1.6;
//                    }
//
//                    /* Utility classes for margins and padding */
//                    .mt-10 { margin-top: 10px; }
//                    .mb-10 { margin-bottom: 10px; }
//                    .pt-10 { padding-top: 10px; }
//                    .pb-10 { padding-bottom: 10px; }
//
//                    /* Responsive design: Adjust layout on smaller screens */
//                    @media (max-width: 768px) {
//                        .container {
//                            flex-direction: column; /* Stack elements vertically in small screens */
//                        }
//
//                        .button {
//                            padding: 8px 10px; /* Adjust button padding for smaller devices */
//                        }
//                    }
//            """.trimIndent(),
//                annotations = listOf(
//                    WorkflowViewModelFileLineAnnotation.diffAdded(3),
//                    WorkflowViewModelFileLineAnnotation.diffAdded(7),
//                    WorkflowViewModelFileLineAnnotation.diffAdded(11),
//                    WorkflowViewModelFileLineAnnotation.diffAdded(15),
//                    WorkflowViewModelFileLineAnnotation.diffAdded(19),
//                    WorkflowViewModelFileLineAnnotation.diffDeleted(2),
//                    WorkflowViewModelFileLineAnnotation.diffDeleted(6),
//                    WorkflowViewModelFileLineAnnotation.diffDeleted(10),
//                    WorkflowViewModelFileLineAnnotation.diffDeleted(14),
//                    WorkflowViewModelFileLineAnnotation.diffDeleted(18),
//                )
//            ),
//            // Additional fileData entries can be added here similar to the examples above
//            // This should include entries for MyComponent.tsx, index.html, and AnotherComponent.jsx based on content from the Python example
//        )
//
//        return WorkflowPartialViewModel(
//            workflowId = workflowId,
//            workflowStartTimeMs = ViewModelValueUpdate(ViewModelValueUpdateOperation.SET, value = Instant.now().toEpochMilli()),
//            progress = ViewModelValueUpdate(ViewModelValueUpdateOperation.SET, value = 0.0),
//            status = ViewModelValueUpdate(ViewModelValueUpdateOperation.SET, value = WorkflowViewModelStatus.booting()),
//            activeTasks = ViewModelValueUpdate(ViewModelValueUpdateOperation.SET, value = listOf()),
//            fileData = ViewModelValueUpdate(ViewModelValueUpdateOperation.SET, initialFileData),
//            stats = ViewModelValueUpdate(
//                ViewModelValueUpdateOperation.SET,
//                listOf()
//            ), // Placeholder for initializing statistics
//            pullRequest = ViewModelValueUpdate(ViewModelValueUpdateOperation.SET, value = null),
//            commits = ViewModelValueUpdate(
//                ViewModelValueUpdateOperation.SET,
//                listOf()
//            ) // Placeholder to initialize commits information
//        )
//    }
//
//    fun getSampleWorkflowStream(
//        workflowId: String,
//    ): Iterable<WorkflowPartialViewModel> {
//        return object : Iterable<WorkflowPartialViewModel> {
//            override fun iterator(): Iterator<WorkflowPartialViewModel> {
//                return object : Iterator<WorkflowPartialViewModel> {
//                    var counter = 0
//
//                    override fun hasNext(): Boolean = true
//
//                    override fun next(): WorkflowPartialViewModel {
//                        synchronized(this) {
//                            val viewModel = if (counter == 0) {
//                                getInitialWorkflowViewModel(workflowId)
//                            } else {
//                                val currentStatusIndex = (counter / SECONDS_BETWEEN_PHASE_CHANGE) % statuses.size
//                                val currentStatus = statuses[currentStatusIndex]
//
//                                val tasksCount: Int =
//                                    random.nextInt(1, activeTasks.size + 1)
//                                val currentActiveTasks = activeTasks.shuffled(random).take(tasksCount)
//
//                                WorkflowPartialViewModel(
//                                    workflowId = workflowId,
//                                    workflowStartTimeMs = null,
//                                    progress = ViewModelValueUpdate(
//                                        operation = ViewModelValueUpdateOperation.SET,
//                                        value = counter.toDouble() / 100
//                                    ),
//                                    status = ViewModelValueUpdate(
//                                        operation = ViewModelValueUpdateOperation.SET,
//                                        value = currentStatus
//                                    ),
//                                    activeTasks = ViewModelValueUpdate(
//                                        operation = ViewModelValueUpdateOperation.SET,
//                                        value = currentActiveTasks
//                                    ),
//                                    fileData = null,
//                                    stats = null,
//                                    pullRequest = null,
//                                    commits = null
//                                )
//                            }
//
//                            counter += 1
//
//                            return viewModel
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    fun reduceWorkflowViewModel(
//        current: WorkflowViewModel,
//        update: WorkflowPartialViewModel
//    ): WorkflowViewModel {
//        // Each field is conditionally updated based on whether the update's respective ViewModelValueUpdate is non-null
//        val newProgress = update.progress?.value ?: current.progress
//        val newStatus = update.status?.value ?: current.status
//        val newActiveTasks = update.activeTasks?.value ?: current.activeTasks
//        val newFileData = update.fileData?.value ?: current.fileData
//        val newStats = update.stats?.value ?: current.stats
//        val newPullRequest = update.pullRequest?.value ?: current.pullRequest
//        val newCommits = update.commits?.value ?: current.commits
//
//        return WorkflowViewModel(
//            workflowId = current.workflowId,
//            workflowStartTimeMs = current.workflowStartTimeMs,
//            progress = newProgress,
//            status = newStatus,
//            activeTasks = newActiveTasks,
//            fileData = newFileData,
//            stats = newStats,
//            pullRequest = newPullRequest,
//            commits = newCommits
//        )
//    }
//}
