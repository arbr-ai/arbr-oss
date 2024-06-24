package com.arbr.codegen.target.compiler.om

import com.arbr.codegen.base.dependencies.MapperConfig
import com.arbr.codegen.base.generator.ObjectModelGenerator
import com.arbr.codegen.base.inputs.ArbrObjectModelGenerationInputRenderer
import com.arbr.codegen.base.inputs.ArbrPlainDataType
import com.arbr.codegen.target.compiler.dsl.ArbrObjectModelNodeProcessor
import com.arbr.codegen.util.PathUtils
import com.arbr.graphql.lang.common.GraphQlNodeProcessor
import com.arbr.graphql.lang.node.GraphQlLanguageNodeDocument
import com.arbr.graphql.lang.node.GraphQlLanguageNodeTraversalContextPhase
import com.arbr.graphql.lang.node.GraphQlLanguageNodeTraversalState
import org.slf4j.LoggerFactory
import java.io.Writer
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.isDirectory

class GraphQlSchemaToObjectModelNodeProcessor(
    private val outputDirPath: Path,
) : GraphQlNodeProcessor {
    override val writer: Writer = Writer.nullWriter()

    private val mapper = MapperConfig().mapper
    private val innerProcessor = ArbrObjectModelNodeProcessor()
    private val inputDataTypeRenderer = ArbrObjectModelGenerationInputRenderer(
        mapper
    )
    private val objectModelGenerator = ObjectModelGenerator(this::didWriteFilesIntoSubpaths)

    private fun didWriteFilesIntoSubpaths(subpaths: List<String>) {
        val paths = subpaths.map { Paths.get(outputDirPath.toString(), it) }
        val commonRoot = PathUtils.commonRoot(paths, outputDirPath)?.let {
            if (it.isDirectory()) it else it.parent
        }
        if (commonRoot == null) {
            logger.info("Wrote ${paths.size} into unknown output dir")
        } else {
            logger.info("Wrote ${paths.size} source outputs beneath $commonRoot")
        }
    }

    private fun renderDataTypes(
        plainDataTypes: List<ArbrPlainDataType>
    ) {
        val renderedInputs = inputDataTypeRenderer.render(DEFAULT_DOMAIN, plainDataTypes)
        logger.info("Rendered ${renderedInputs.schemata.sumOf { it.tables.size }} inputs for code generation...")

        val projectGroup = validatePackageReverseDomain(DEFAULT_PACKAGE_GROUP)
        val packageQualifier = validatePackageReverseDomain(DEFAULT_PACKAGE_QUALIFIER)

        val packageDomain = "$projectGroup.$packageQualifier"

        logger.info("Generating code into $outputDirPath ...")

        objectModelGenerator
            .generate(
                packageDomain = packageDomain,
                databaseModel = renderedInputs,
                subprojectDir = outputDirPath,
            )
    }

    override fun processNode(traversalState: GraphQlLanguageNodeTraversalState) {
        innerProcessor.processNode(traversalState)

        if (
            traversalState.context.phase == GraphQlLanguageNodeTraversalContextPhase.LEAVE
            && traversalState.node is GraphQlLanguageNodeDocument
        ) {
            renderDataTypes(innerProcessor.getDataTypes())
        }
    }

    override fun close() {
        super.close()
    }

    companion object {
        private const val DEFAULT_DOMAIN = "arbr"
        private const val DEFAULT_PACKAGE_GROUP = "com.arbr"
        private const val DEFAULT_PACKAGE_QUALIFIER = "object_model"

        private val logger = LoggerFactory.getLogger(GraphQlSchemaToObjectModelCompiler::class.java)

        private val packageDomainRegex = Regex("[a-z0-9_\\.]+")

        private fun validatePackageReverseDomain(
            inputString: String,
        ): String {
            if (!packageDomainRegex.matches(inputString)) {
                throw IllegalArgumentException("Invalid package specifier: $inputString")
            }

            return inputString
        }
    }
}