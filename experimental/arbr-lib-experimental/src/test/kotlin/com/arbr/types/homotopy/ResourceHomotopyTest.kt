package com.arbr.types.homotopy
//
//import com.arbr.platform.object_graph.core.model.ArbrCommitEvalModel
//import com.arbr.platform.object_graph.core.model.ArbrCommitModel
//import com.arbr.platform.object_graph.core.model.ArbrCommitRelevantFileModel
//import com.arbr.platform.object_graph.core.model.ArbrFileModel
//import com.arbr.platform.object_graph.core.model.ArbrFileOpModel
//import com.arbr.platform.object_graph.core.model.ArbrFileSegmentModel
//import com.arbr.platform.object_graph.core.model.ArbrFileSegmentOpDependencyModel
//import com.arbr.platform.object_graph.core.model.ArbrFileSegmentOpModel
//import com.arbr.platform.object_graph.core.model.ArbrProjectModel
//import com.arbr.platform.object_graph.core.model.ArbrRootModel
//import com.arbr.platform.object_graph.core.model.ArbrSubtaskEvalModel
//import com.arbr.platform.object_graph.core.model.ArbrSubtaskModel
//import com.arbr.platform.object_graph.core.model.ArbrSubtaskRelevantFileModel
//import com.arbr.platform.object_graph.core.model.ArbrTaskEvalModel
//import com.arbr.platform.object_graph.core.model.ArbrTaskModel
//import com.arbr.platform.object_graph.core.model.ArbrTaskRelevantFileModel
//import com.arbr.platform.object_graph.core.model.ArbrVectorResourceModel
//import com.arbr.platform.object_graph.types.Arbr
//import com.arbr.platform.object_graph.types.ArbrResource
//import com.arbr.platform.object_graph.types.ArbrResourceKey
//import com.arbr.platform.object_graph.types.ArbrResourceKeyResolver
//import com.arbr.platform.object_graph.types.ResourceStreamProviders
//import com.arbr.platform.object_graph.types.ResourceViewProviders
//import com.arbr.platform.object_graph.types.TypedResourceViewProvider
//import com.arbr.platform.object_graph.types.suites.ResourceAssociatedObjectCollection
//import com.arbr.platform.object_graph.types.suites.ResourceAssociatedObjectCollectionBuilder
//import com.arbr.platform.object_graph.common.model.view.ProposedValueStreamViewProviderFactory
//import com.arbr.platform.object_graph.common.requirements.DefaultRequirementsProvider
//import com.arbr.types.homotopy.Homotopy
//import com.arbr.types.homotopy.PlainType
//import com.arbr.types.homotopy.config.HomotopyFilterSpec
//import com.arbr.types.homotopy.functional.BaseHomotopyGroundMap
//import com.arbr.types.homotopy.getBaseType
//import com.arbr.types.homotopy.spec.ContractionHomotopy
//import org.junit.jupiter.api.Test
//import kotlin.reflect.KType
//import kotlin.reflect.jvm.jvmErasure
//
//class ResourceHomotopyTest {
//    private val resourceViewProviderFactory = ResourceViewProviders()
//    private val resourceStreamProviderFactory = ResourceStreamProviders()
//    private val defaultDependencyTracingProvider = DefaultMapReadWriteDependencyTracingProvider(
//        resourceViewProviderFactory,
//        resourceStreamProviderFactory
//    )
//    private val resourceKeyResolver = ArbrResourceKeyResolver
//    private val resourceAssociatedObjectCollectionBuilder =
//        object : ResourceAssociatedObjectCollectionBuilder<ArbrResourceKey, TypedResourceViewProvider<*, *>> {
//            override fun buildWith(transform: (ArbrResourceKey) -> TypedResourceViewProvider<*, *>): ResourceAssociatedObjectCollection<ArbrResourceKey, TypedResourceViewProvider<*, *>> {
//                @Suppress("EnumValuesSoftDeprecate") val enumValues = ArbrResourceKey.values()
//                return ResourceAssociatedObjectCollection.new(enumValues, transform)
//            }
//        }
//
//    private val proposedValueStreamViewProviderFactory = ProposedValueStreamViewProviderFactory(
//        requirementsProvider = DefaultRequirementsProvider(),
//        resourceViewProviderFactory = resourceViewProviderFactory,
//        readDependencyTracingProvider = defaultDependencyTracingProvider,
//        writeDependencyTracingProvider = defaultDependencyTracingProvider,
//        resourceKeyResolver = resourceKeyResolver,
//        resourceAssociatedObjectCollectionBuilder = resourceAssociatedObjectCollectionBuilder,
//    )
//    private val proposedValueStreamProvider =
//        proposedValueStreamViewProviderFactory.provider(ProposedValueAccessTier.TRACE)
//
//    private fun arbrResource(resourceType: KType): ArbrResource {
//        return when (resourceType.jvmErasure.java) {
//            ArbrCommitModel::class.java -> Arbr.Commit
//            ArbrCommitEvalModel::class.java -> Arbr.CommitEval
//            ArbrCommitRelevantFileModel::class.java -> Arbr.CommitRelevantFile
//            ArbrFileModel::class.java -> Arbr.File
//            ArbrFileOpModel::class.java -> Arbr.FileOp
//            ArbrFileSegmentModel::class.java -> Arbr.FileSegment
//            ArbrFileSegmentOpModel::class.java -> Arbr.FileSegmentOp
//            ArbrFileSegmentOpDependencyModel::class.java -> Arbr.FileSegmentOpDependency
//            ArbrProjectModel::class.java -> Arbr.Project
//            ArbrRootModel::class.java -> Arbr.Root
//            ArbrSubtaskModel::class.java -> Arbr.Subtask
//            ArbrSubtaskEvalModel::class.java -> Arbr.SubtaskEval
//            ArbrSubtaskRelevantFileModel::class.java -> Arbr.SubtaskRelevantFile
//            ArbrTaskModel::class.java -> Arbr.Task
//            ArbrTaskEvalModel::class.java -> Arbr.TaskEval
//            ArbrTaskRelevantFileModel::class.java -> Arbr.TaskRelevantFile
//            ArbrVectorResourceModel::class.java -> Arbr.VectorResource
//            else -> throw Exception("Invalid key $resourceType")
//        }
//    }
//
//    @Test
//    fun `derives traits of models`() {
//        // Want resource view instantiator via provider
//        /**
//         * 1. A nullable ground mapping: BaseObj.Singleton = F -> G?
//         */
//        val nullableGroundMap =
//            BaseHomotopyGroundMap<TypedResourceViewProvider<*, *>?, TypedResourceViewProvider<*, *>?> { singleton, _ ->
//                if (singleton is PlainType.Ref) {
//                    resourceViewProviderFactory.resourceViewProvider(proposedValueStreamProvider, arbrResource(singleton.kType))
//                } else {
//                    null
//                }
//            }
//
//        /**
//         * 2. A nullable homotopy spec (lifted to be a total homotopy on a boxed target type)
//         */
//        val nullableHomotopySpec = HomotopyFilterSpec.configure<TypedResourceViewProvider<*, *>> {
//            liftNode { context, valueTypeImplementor, innerImplementors ->
//                valueTypeImplementor
//            }
//        }
//
//        /**
//         * 3. Conformance to partial non-null homotopy in the target field, specifically for Contractible compositions
//         */
//        val contractionHomotopy = ContractionHomotopy.default<TypedResourceViewProvider<*, *>>()
//
//        val commitHType = Homotopy.getBaseType<ArbrRootModel>()
//
//        val resourceViewProviderHType = Homotopy
//            .getContractedHomotopyType(
//                commitHType,
//                nullableHomotopySpec,
//                nullableGroundMap,
//                contractionHomotopy,
//            )
//
//        resourceViewProviderHType.forEach { node ->
//            println(node.localPathTokens.toString() + "   " + node.nodeHType)
//        }
//
////        val provider =
////            resourceViewProviderHType.getInstance(cls<TypedResourceViewProvider<Arbr.Commit, ArbrCommitView>>())!!
////
////        val resourceStreamProvider = resourceStreamProviderFactory.resourceStreamProvider(Arbr.Commit)
////        val resourceStream = resourceStreamProvider.provideEmptyResource("abc")
////        val resourceView = provider.provideResourceView(resourceStream)
////        println(resourceView)
//
////        ExampleFunction()
//    }
//
//    @Test
//    fun `converts base to nodes`() {
//        // Want resource view instantiator via provider
//        /**
//         * 1. A nullable ground mapping: BaseObj.Singleton = F -> G?
//         */
//        val nullableGroundMap = BaseHomotopyGroundMap<PlainType?, PlainType?> { singleton, _ ->
//            if (singleton is PlainType.Ref) {
//                singleton
//            } else {
//                null
//            }
//        }
//
//        /**
//         * 2. A nullable homotopy spec (lifted to be a total homotopy on a boxed target type)
//         */
//        val nullableHomotopySpec = HomotopyFilterSpec.configure<PlainType> {
//            liftNode { context, valueTypeImplementor, _ ->
//                if (valueTypeImplementor == null) {
//                    println("Excluding interior node at path ${context.pathString}")
//                    null
//                } else {
//                    println("Including interior node at path ${context.pathString} - $valueTypeImplementor")
//                    valueTypeImplementor
//                }
//            }
//        }
//
//        /**
//         * 3. Conformance to partial non-null homotopy in the target field, specifically for Contractible compositions
//         */
//        val contractionHomotopy = ContractionHomotopy.default<PlainType>()
//
//        val rootHType = Homotopy.getBaseType<ArbrRootModel>()
//
//        val contractedHType = Homotopy
//            .getContractedHomotopyType(
//                rootHType,
//                nullableHomotopySpec,
//                nullableGroundMap,
//                contractionHomotopy,
//            )
//
////        val reducer = HTypeIndentingStringReducer<BaseHObj>(
////            { str, _ ->
////                when (str) {
////                    is BaseHObj.HNode -> appendLine("<${str::class.java.simpleName} ${str.value}>")
////                    else -> appendLine("<${str::class.java.simpleName}>")
////                }
////            },
////            { str, _ ->
////                when (str) {
////                    is BaseHObj.HNode -> appendLine("</${str::class.java.simpleName} ${str.value}>")
////                    else -> appendLine("</${str::class.java.simpleName}>")
////                }
////            },
////        )
//
////        rootHType.nodes.forEach { node ->
////            @Suppress("UNCHECKED_CAST") val reducedString = reducer.reduce(node as HType<BaseHObj, BaseHObj>)
////            println(reducedString)
////            println("=".repeat(40))
////        }
////        contractedHType.nodes.forEach { node ->
////            println(node.valueHType.baseTypeRepresentation)
////            println("Refs:")
////            node.refHTypes.forEach {
////                println(it.valueHType.baseTypeRepresentation.toString() + " (${it.childHTypes.size})")
////            }
////            println("Children:")
////            node.childHTypes.forEach {
////                println(it.valueHType.baseTypeRepresentation.toString() + " (${it.childHTypes.size})")
////            }
////            println("=".repeat(40))
////        }
//
////        ExampleFunction()
//    }
//
//}

