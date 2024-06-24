package com.arbr.prompt_library.util

import com.arbr.object_model.core.resource.*
import com.arbr.object_model.core.resource.field.*
import com.arbr.og.object_model.common.model.collections.*
import com.arbr.og.object_model.common.values.collections.SourcedStruct1
import com.arbr.og.object_model.common.values.collections.SourcedStruct2
import com.arbr.og.object_model.common.values.collections.SourcedStruct7
import com.arbr.og.object_model.common.values.collections.SourcedStruct8
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.shape.Shape


val Subtasks =
    NestedObjectListType1(
        "subtasks",
        "Subtasks which should be done in order to accomplish the main task.",
        ArbrSubtask.Subtask,
    )
typealias SubtasksValue = NestedObjectListType1.Value<String, Dim.VariableT, Dim.VariableF, ArbrSubtaskSubtaskValue>
typealias SubtasksContainer = SourcedStruct1<ArbrSubtaskSubtaskValue>


val FilePaths = NestedObjectListType1(
    "file_paths",
    "Paths of files in the project.",
    ArbrFile.FilePath,
)
typealias FilePathsValue = NestedObjectListType1.Value<String, Dim.VariableT, Dim.VariableF, ArbrFileFilePathValue>
typealias FilePathsContainer = SourcedStruct1<ArbrFileFilePathValue>


val FilePathsAndSummaries =
    NestedObjectListType2(
        "file_paths_and_summaries",
        "File paths and their summaries.",
        ArbrFile.FilePath,
        ArbrFile.Summary,
    )

fun makeFilePathsAndSummariesInnerValue(
    filePath: String,
    summary: String
): SourcedStruct2<ArbrFileFilePathValue, ArbrFileSummaryValue> {
    return SourcedStruct2(
        ArbrFile.FilePath.constant(filePath),
        ArbrFile.Summary.constant(summary),
    )
}


val FilePathsAndContents =
    NestedObjectListType2(
        "file_paths_and_contents",
        "File paths and their contents.",
        ArbrFile.FilePath,
        ArbrFile.Content,
    )

fun makeFilePathsAndContentsInnerValue(
    filePath: String,
    content: String
): NestedObjectListType2.InnerValue<String, String?> {
    return NestedObjectListType2.InnerValue(FilePathsAndContents, filePath, content)
}


val FileOperationsAndTargetFilePaths =
    NestedObjectListType2(
        "file_operations_and_target_file_path",
        "File operation names (create, edit, delete) and the target file path for the operation.",
        ArbrFileOp.FileOperation,
        ArbrFile.FilePath,
    )

fun makeFileOperationsAndTargetFilePathsInnerValue(
    fileOperation: String,
    filePath: String,
): NestedObjectListType2.InnerValue<String?, String> {
    return NestedObjectListType2.InnerValue(
        FileOperationsAndTargetFilePaths,
        fileOperation,
        filePath,
    )
}


val FileOperationsAndTargetFilePathsWithDescriptions =
    NestedObjectListType3(
        "file_operations_with_descriptions",
        "File operation names (create, edit, delete), the target file path for the operation, and a description of the work needed to accomplish the commit.",
        ArbrFileOp.FileOperation,
        ArbrFile.FilePath,
        ArbrFileOp.Description,
    )


val FileSegmentContents =
    NestedObjectListType1(
        "file_segments",
        "File segments",
        ArbrFileSegmentOp.Content,
    )

val CommitDetailsAndFileOps =
    NestedObjectListType3(
        "commit_details_and_file_operations",
        "Array of commit information planned for the task with the commit message and a summary of" +
                " the commit, as well as the associated file operations.",
        ArbrCommit.CommitMessage,
        ArbrCommit.DiffSummary,
        FileOperationsAndTargetFilePaths,
    )


fun commitDetailsAndFileOpsInnerValue(
    commitMessage: String,
    diffSummary: String,
    fileOperationsAndTargetFilePaths: List<NestedObjectListType2.InnerValue<String?, String>>,
) = NestedObjectListType3.InnerValue(
    CommitDetailsAndFileOps,
    commitMessage,
    diffSummary,
    fileOperationsAndTargetFilePaths,
)


val CommitDetailsAndFileOpsWithDescriptions =
    NestedObjectListType3(
        "commit_details_and_file_operations",
        "Array of commit information planned for the task with the commit message and a summary of" +
                " the commit, as well as the associated file operations.",
        ArbrCommit.CommitMessage,
        ArbrCommit.DiffSummary,
        FileOperationsAndTargetFilePathsWithDescriptions,
    )


val SubtaskPlans =
    NestedObjectListType2(
        "subtask_plans",
        "A list of plans for subtasks of the main task.",
        ArbrSubtask.Subtask,
        CommitDetailsAndFileOps,
    )


val CommitMessageSubtaskPair =
    NestedObjectListType2(
        "subtask_assignments",
        "Array of objects representing assignments of commits to existing subtasks.",
        ArbrCommit.CommitMessage,
        ArbrSubtask.Subtask,
    )

val CommitMessages = NestedObjectListType1(
    "commit_messages",
    "Commit messages of commits in the pull request",
    ArbrCommit.CommitMessage,
)

val DiffSummaries = NestedObjectListType1(
    "diff_summaries",
    "Summaries of commits made during implementation of the task.",
    ArbrCommit.DiffSummary
)

val FileSegmentOperations =
    NestedObjectListType6(
        "source_element_operations",
        "Source Element Operations: Operations on source code elements, i.e. segments of the file, including the operation (one of [add, edit, delete]), the content type of the element, the kind of source code element, the name of the source code element within the file, and a description of the change.",
        ArbrFileSegmentOp.Operation,
        ArbrFileSegmentOp.ContentType,
        ArbrFileSegmentOp.RuleName,
        ArbrFileSegmentOp.Name,
        ArbrFileSegmentOp.ElementIndex,
        ArbrFileSegmentOp.Description,
    )

val FileSegmentOperationsInFile = NestedObjectListType7(
    "source_element_operations_in_file",
    "Source Element Operations: Operations on source code elements, i.e. segments of the file, including the operation (one of [add, edit, delete]), the content type of the element, the kind of source code element, and the name of the source code element within the file.",
    ArbrFile.FilePath,
    ArbrFileSegmentOp.Operation,
    ArbrFileSegmentOp.ContentType,
    ArbrFileSegmentOp.RuleName,
    ArbrFileSegmentOp.Name,
    ArbrFileSegmentOp.ElementIndex,
    ArbrFileSegmentOp.Description,
)
typealias FileSegmentOperationsInFileContainer = SourcedStruct7<
        ArbrFileFilePathValue,
        ArbrFileSegmentOpOperationValue,
        ArbrFileSegmentOpContentTypeValue,
        ArbrFileSegmentOpRuleNameValue,
        ArbrFileSegmentOpNameValue,
        ArbrFileSegmentOpElementIndexValue,
        ArbrFileSegmentOpDescriptionValue,
        >

val FileSegmentOperationsInFileWithContent = NestedObjectListType8(
    "source_element_operations_in_file_with_content",
    "Source Element Operations: Operations on source code elements, i.e. segments of the file, including the operation (one of [add, edit, delete]), the content type of the element, the kind of source code element, the name of the source code element within the file, and the contents of the source code element.",
    ArbrFile.FilePath,
    ArbrFileSegmentOp.Operation,
    ArbrFileSegmentOp.ContentType,
    ArbrFileSegmentOp.RuleName,
    ArbrFileSegmentOp.Name,
    ArbrFileSegmentOp.ElementIndex,
    ArbrFileSegmentOp.Content,
    ArbrFileSegmentOp.Description,
)
typealias FileSegmentOperationsInFileWithContentContainer = SourcedStruct8<
        ArbrFileFilePathValue,
        ArbrFileSegmentOpOperationValue,
        ArbrFileSegmentOpContentTypeValue,
        ArbrFileSegmentOpRuleNameValue,
        ArbrFileSegmentOpNameValue,
        ArbrFileSegmentOpElementIndexValue,
        ArbrFileSegmentOpContentValue,
        ArbrFileSegmentOpDescriptionValue,
        >

val FileSegmentOpDependencies =
    NestedObjectListType1(
        "dependency_file_segment_ops",
        "Dependencies: IDs of dependencies of the source element operation. Dependencies are other elements" +
                " which must exist in order for the current element to compile or otherwise be valid, such as references" +
                " made within the source code.",
        ArbrFileSegmentOpDependency.DependencyFileSegmentOp
    )

val FileSegmentOpDependencyEdges = NestedObjectListType2(
    "all_dependencies",
    "All Dependencies: For each source element operation, its ID as well as the list of IDs of operations" +
            " on which it is dependent.",
    ArbrFileSegmentOpDependency.DependencyFileSegmentOp,
    FileSegmentOpDependencies,
)
typealias FileSegmentOpDependencyEdgesContainer = SourcedStruct2<
        ArbrFileSegmentOpDependencyDependencyFileSegmentOpValue,
        NestedObjectListType1.Value<String?, Dim.VariableT, Dim.VariableF, ArbrFileSegmentOpDependencyDependencyFileSegmentOpValue>,
        >

typealias FileSegmentOpDependencyEdgesValueRecord = SourcedStruct1<
        NestedObjectListType2.Value<String?, List<NestedObjectListType1.InnerValue<String?>>, Dim.VariableT,
                Dim.VariableF, Shape.Product<Dim.VariableC, Dim.VariableT>, Shape.Product<Dim.VariableC, Dim.VariableF>,
                ArbrFileSegmentOpDependencyDependencyFileSegmentOpValue,
                NestedObjectListType1.Value<String?, Dim.VariableT, Dim.VariableF, ArbrFileSegmentOpDependencyDependencyFileSegmentOpValue>>
        >


private val FileSegOpIdList =
    NestedObjectListType1(
        "source_element_operation_ids",
        "Source Element Operation IDs: The list of IDs of source element operations that make up the commit.",
        ArbrFileSegmentOp.Name,
    )
val CommitDetailsAndFileSegOps = NestedObjectListType3(
    "commit_details_and_file_operations",
    "Array of commit information planned for the task with the commit message and a summary of" +
            " the commit, as well as the associated operations on source elements.",
    ArbrCommit.CommitMessage,
    ArbrCommit.DiffSummary,
    FileSegOpIdList,
)


val FileSegments =
    NestedObjectListType4(
        "existing_source_elements",
        "Some of the existing source elements within the file (not exhaustive), including their rule name, identifier name, and content.",
        ArbrFileSegmentOp.RuleName,
        ArbrFileSegmentOp.Name,
        ArbrFileSegmentOp.ElementIndex,
        ArbrFileSegmentOp.Content,
    )