package com.arbr.core_web_dev.util.file_segments.morphism

import com.arbr.platform.alignable.alignable.graph.AlignableHomogenousRootedListDAG
import com.arbr.platform.alignable.alignable.graph.HomogenousRootedListDAG
import com.arbr.content_formats.yaml.YamlParser
import com.arbr.object_model.core.partial.PartialFile
import com.arbr.object_model.core.partial.PartialFileSegment
import com.arbr.object_model.core.resource.ArbrFileSegment
import com.arbr.object_model.core.resource.ArbrFileSegmentOp
import com.arbr.object_model.core.resource.field.ArbrFileSegmentOpElementIndexValue
import com.arbr.object_model.core.resource.field.ArbrFileSegmentOpNameValue
import com.arbr.object_model.core.resource.field.ArbrFileSegmentOpRuleNameValue
import com.arbr.object_model.core.resource.field.ArbrFileSegmentRuleNameValue
import com.arbr.og.object_model.common.values.collections.SourcedStruct1
import com.arbr.og.object_model.common.values.collections.SourcedStruct3
import com.arbr.og_engine.artifact.Artifact
import com.arbr.relational_prompting.services.ai_application.application.AiApplication
import com.arbr.relational_prompting.services.ai_application.application.invoke
import com.arbr.util.adapt
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.slf4j.LoggerFactory
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono

sealed interface FileSegmentTreeMapper {

    fun computeNewTree(
        sourceGraph: AlignableHomogenousRootedListDAG<FileSegmentTreeInfo, FileSegmentTreeInfoOperation>,
    ): Mono<AlignableHomogenousRootedListDAG<FileSegmentTreeInfo, FileSegmentTreeInfoOperation>>

    class PlaceNewSegment(
        private val newSegmentIncorporateApplication: AiApplication<SourcedStruct3<ArbrFileSegmentOpRuleNameValue, ArbrFileSegmentOpRuleNameValue, ArbrFileSegmentOpNameValue>, SourcedStruct1<ArbrFileSegmentRuleNameValue>>,
        private val yamlMapper: ObjectMapper,
        private val artifactSink: FluxSink<Artifact>,
        private val targetFile: PartialFile,
        private val newFileSegmentRuleName: ArbrFileSegmentOpRuleNameValue,
        private val newFileSegmentName: ArbrFileSegmentOpNameValue,
        private val newFileSegmentElementIndex: ArbrFileSegmentOpElementIndexValue,
    ) : FileSegmentTreeMapper {
        private fun serializeFileSegmentInfoTree(
            infoTree: HomogenousRootedListDAG<FileSegmentTreeInfo>
        ): LinkedHashMap<String, Any?> = linkedMapOf(
            "element_rule_name" to infoTree.nodeValue.elementRuleName?.value,
            "element_name" to infoTree.nodeValue.elementName?.value,
            "child_elements" to infoTree.children.values.flatMap { childMap ->
                childMap.map(this::serializeFileSegmentInfoTree)
            },
        )

        private fun fileSegmentTreeInfo(tm: LinkedHashMap<String, Any?>) = FileSegmentTreeInfo(
            tm["element_rule_name"]?.let { ArbrFileSegment.RuleName.materialized(it as String) },
            tm["element_name"]?.let { ArbrFileSegment.Name.materialized(it as String) },
        )

        @Suppress("UNCHECKED_CAST")
        private fun parseFileSegmentInfoTree(
            treeMap: LinkedHashMap<String, Any?>,
        ): HomogenousRootedListDAG<FileSegmentTreeInfo> {
            return HomogenousRootedListDAG.fromGenerator(
                treeMap,
                { tm ->
                    fileSegmentTreeInfo(tm)
                },
                { tm ->
                    val childElementMaps = (tm["child_elements"] as? List<LinkedHashMap<String, Any?>>) ?: emptyList()
                    mapOf(
                        fileSegmentsKey to childElementMaps,
                    )
                }
            )
        }

        override fun computeNewTree(sourceGraph: AlignableHomogenousRootedListDAG<FileSegmentTreeInfo, FileSegmentTreeInfoOperation>): Mono<AlignableHomogenousRootedListDAG<FileSegmentTreeInfo, FileSegmentTreeInfoOperation>> {
            // TODO: Filter padding intervals
            val targetFileContent = targetFile.content ?: return Mono.error(Exception("File is missing content"))
            val targetFilePath = targetFile.filePath ?: return Mono.error(Exception("File is missing path"))
            if (targetFileContent.value.isNullOrBlank()) {
                val segmentInfo = FileSegmentTreeInfo(
                    ArbrFileSegment.RuleName.constant("whole_file"),
                    ArbrFileSegment.Name.constant(targetFilePath.value),
                )

                return Mono.just(
                    HomogenousRootedListDAG.fromGenerator(
                        segmentInfo,
                        { it },
                        { emptyMap() }
                    ).flatMapAlignable { it }
                )
            }

            val treeMap = serializeFileSegmentInfoTree(sourceGraph)
            val treeString = yamlMapper.writeValueAsString(treeMap)

            return newSegmentIncorporateApplication.invoke(
                ArbrFileSegmentOp.RuleName.initialize(
                    newFileSegmentRuleName.kind,
                    treeString,
                    newFileSegmentRuleName.generatorInfo,
                ),
                newFileSegmentRuleName,
                newFileSegmentName,
                artifactSink.adapt(),
            )
                .map { (mappedTree) ->
                    val treeMapParsed: Map<String, Any?> =
                        try {
                            yamlMapper.readValue(mappedTree.value, jacksonTypeRef())
                        } catch (e: Exception) {
                            YamlParser().parseMap(emptyList(), mappedTree.value)
                                .firstNotNullOf {
                                    it.first
                                }
                        }

                    @Suppress("UNCHECKED_CAST")
                    parseFileSegmentInfoTree(treeMapParsed["combined_source_element_tree"]!! as LinkedHashMap<String, Any?>)
                        .flatMapAlignable { it }
                }.map {
                    logger.info("Alignment source: ${jacksonObjectMapper().writeValueAsString(sourceGraph)}")
                    logger.info("Alignment target: ${jacksonObjectMapper().writeValueAsString(it)}")
                    it
                }
        }

        companion object {
            private const val fileSegmentsKey = "DdlGithub.FileSegment.ParentSegment"

            private val logger = LoggerFactory.getLogger(PlaceNewSegment::class.java)
        }

    }

    class EditExistingSegment(
        val targetFileSegment: PartialFileSegment,
    ) : FileSegmentTreeMapper {
        override fun computeNewTree(sourceGraph: AlignableHomogenousRootedListDAG<FileSegmentTreeInfo, FileSegmentTreeInfoOperation>): Mono<AlignableHomogenousRootedListDAG<FileSegmentTreeInfo, FileSegmentTreeInfoOperation>> {
            // No-op
            return Mono.just(sourceGraph)
        }
    }

    class DeleteSegment(
        val targetFileSegment: PartialFileSegment,
    ) : FileSegmentTreeMapper {
        override fun computeNewTree(sourceGraph: AlignableHomogenousRootedListDAG<FileSegmentTreeInfo, FileSegmentTreeInfoOperation>): Mono<AlignableHomogenousRootedListDAG<FileSegmentTreeInfo, FileSegmentTreeInfoOperation>> {
            // No-op
            return Mono.just(sourceGraph)
        }
    }

}