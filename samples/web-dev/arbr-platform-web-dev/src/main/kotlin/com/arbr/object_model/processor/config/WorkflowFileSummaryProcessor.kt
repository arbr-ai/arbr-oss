package com.arbr.object_model.processor.config

import com.arbr.content_formats.tokens.TokenizationUtils
import com.arbr.util.adapt
import com.arbr.object_model.core.partial.PartialFile
import com.arbr.object_model.core.partial.PartialProject
import com.arbr.object_model.core.resource.ArbrFile
import com.arbr.object_model.core.resource.ArbrProject
import com.arbr.object_model.core.resource.field.ArbrFileContentValue
import com.arbr.object_model.core.resource.field.ArbrFileFilePathValue
import com.arbr.object_model.core.resource.field.ArbrFileIsBinaryValue
import com.arbr.object_model.core.resource.field.ArbrFileSourceLanguageValue
import com.arbr.object_model.core.resource.field.ArbrFileSummaryValue
import com.arbr.object_model.core.resource.field.ArbrProjectDescriptionValue
import com.arbr.object_model.core.resource.field.ArbrProjectFullNameValue
import com.arbr.object_model.core.resource.field.ArbrProjectPlatformValue
import com.arbr.object_model.core.resource.field.ArbrProjectPrimaryLanguageValue
import com.arbr.object_model.core.resource.field.ArbrProjectTechStackDescriptionValue
import com.arbr.object_model.core.resource.field.ArbrProjectTitleValue
import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.og.object_model.common.model.ProposedValueReadStream
import com.arbr.og.object_model.common.values.collections.SourcedStruct3
import com.arbr.platform.object_graph.impl.PartialObjectGraph
import com.arbr.og_engine.artifact.Artifact
import com.arbr.og_engine.core.ArbrResourceFunction
import com.arbr.og_engine.core.ObjectModelParser
import com.arbr.og_engine.core.OperationCompleteException
import com.arbr.og_engine.file_system.VolumeState
import com.arbr.prompt_library.PromptLibrary
import com.arbr.relational_prompting.services.ai_application.application.invoke
import com.arbr.util_common.typing.cls
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono

/**
 * Listens to file updates and adds summaries.
 */
@Component
@ConditionalOnProperty(prefix = "arbr.processor.file-summary", name=["enabled"], havingValue="true", matchIfMissing = true)
class WorkflowFileSummaryProcessor(
    objectModelParser: ObjectModelParser,
    private val promptLibrary: PromptLibrary,
) : ArbrResourceFunction<
        ArbrFile, PartialFile,
        ArbrProject, PartialProject,
        ArbrFile, PartialFile,
        >(objectModelParser) {
    private val githubExistingFileSummarizer = promptLibrary.githubFileSummarizer

    override val name: String
        get() = "file-summary"

    override val targetResourceClass: Class<ArbrProject>
        get() = cls()

    override val writeTargetResourceClass: Class<ArbrFile>
        get() = cls()

    private fun summarizeFile(
        projectFullName: ArbrProjectFullNameValue,
        projectTitle: ArbrProjectTitleValue,
        projectPlatform: ArbrProjectPlatformValue,
        projectPrimaryLanguage: ArbrProjectPrimaryLanguageValue,
        projectTechStackDescription: ArbrProjectTechStackDescriptionValue,
        projectDescription: ArbrProjectDescriptionValue,
        filePath: ArbrFileFilePathValue,
        contents: ArbrFileContentValue,
        artifactSink: FluxSink<Artifact>,
    ): Mono<SourcedStruct3<
            ArbrFileSourceLanguageValue,
            ArbrFileIsBinaryValue,
            ArbrFileSummaryValue
            >> {
        logger.info("Intending to summarize ${filePath.value}")

        val fileSummaryMono = githubExistingFileSummarizer.invoke(
            projectFullName,
            projectTitle,
            projectPlatform,
            projectPrimaryLanguage,
            projectTechStackDescription,
            projectDescription,
            filePath,
            contents,
            artifactSink.adapt(),
        )
            .doOnNext {
                logger.info("Got summary for ${filePath.value} with ${it.t3.value?.length ?: 0} bytes")
            }
            .cache()

        return fileSummaryMono
    }

    private fun preProcessContents(
        fileContents: String
    ): String {
        val tokenLength = TokenizationUtils.getTokenCount(fileContents)
        return if (tokenLength > maxFileTokensForSummary) {
            val targetTokens = maxFileTokensForSummary
            val prefixIndex = fileContents.indices.toList().binarySearch { i ->
                val prefix = fileContents.take(i + 1) + "..."
                TokenizationUtils.getTokenCount(prefix) - targetTokens
            }.let {
                if (it >= 0) it else it.inv()
            }

            fileContents.take(prefixIndex) + "..."
        } else {
            fileContents
        }
    }

    override fun prepareUpdate(
        listenResource: ArbrFile,
        readResource: ArbrProject,
        writeResource: ArbrFile,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ): (PartialObjectGraph<ArbrFile, PartialFile, ArbrForeignKey>) -> Mono<Void> {
        if (listenResource.filePath.getLatestValue() != null && listenResource.isBinary.getLatestValue() != null && listenResource.summary.getLatestValue() != null) {
            throw OperationCompleteException()
        }

        val filePath = require(listenResource.filePath)
        val contents = require(listenResource.content)
        val contentsValue = contents.value
            ?: return { partialObjectGraph ->
                val writeFile = partialObjectGraph.root

                // Unclear here whether to write null or wait - should really be able to listen to content updates and
                // refresh summary or invalidate summary entry
                writeFile.apply {
                    summary = ArbrFile.Summary.initialize(
                        contents.kind,
                        null,
                        contents.generatorInfo,
                    )
                    isBinary = ArbrFile.IsBinary.initialize(
                        contents.kind,
                        null,
                        contents.generatorInfo,
                    )
                    sourceLanguage = ArbrFile.SourceLanguage.initialize(
                        contents.kind,
                        null,
                        contents.generatorInfo,
                    )
                }

                Mono.empty()
            }

        val projectFullName = require(readResource.fullName)
        val projectTitle = readResource.title.getLatestAcceptedValue() ?: ArbrProject.Title.initialize(
            projectFullName.kind,
            projectFullName.value,
            projectFullName.generatorInfo,
        )
        val platform = require(readResource.platform)
        val primaryLanguage = require(readResource.primaryLanguage)
        val techStackDescription = require(readResource.techStackDescription)
        val projectDescription = require(readResource.description)

        return { partialObjectGraph ->
            val writeFile = partialObjectGraph.root

            val processedContents = preProcessContents(contentsValue)
            val processedContentsValue = ArbrFile.Content.initialize(
                contents.kind,
                processedContents,
                contents.generatorInfo,
            )

            summarizeFile(
                projectFullName,
                projectTitle,
                platform,
                primaryLanguage,
                techStackDescription,
                projectDescription,
                filePath,
                processedContentsValue,
                artifactSink,
            ).map { (sourceLanguage, isBinary, summary) ->
                writeFile.apply {
                    this.summary = summary
                    this.isBinary = isBinary
                    this.sourceLanguage = sourceLanguage
                }
            }.then()
        }
    }

    override fun acquireWriteTargets(
        listenResource: ArbrFile,
        readResource: ArbrProject,
        writeResource: ArbrFile,
        acquire: (ProposedValueReadStream<*>) -> Unit
    ) {
        acquire(writeResource.summary)
        acquire(writeResource.isBinary)
        acquire(writeResource.sourceLanguage)
    }

    override fun checkPostConditions(
        listenResource: ArbrFile,
        readTargetResource: ArbrProject,
        writeTargetResource: ArbrFile,
        volumeState: VolumeState,
        artifactSink: FluxSink<Artifact>,
    ) {
        requireLatest(listenResource.summary)
        requireLatest(listenResource.isBinary)
        requireLatest(listenResource.sourceLanguage)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkflowFileSummaryProcessor::class.java)

        private const val maxFileTokensForSummary = 2_000
    }
}
