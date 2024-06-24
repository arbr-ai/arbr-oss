package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView

interface ResourceEmbeddingHelperFunctionQuerySpecContext {

    /**
     * Identifier for the context of the querying definition. ".querying" will be appended to yield an identifier that is
     * meant to be locally unique
     */
    val contextName: String

    fun <RV : ResourceView<*>> view(resourceViewClass: Class<RV>): BuilderWithView<RV>

    sealed interface BuilderWithView<RV : ResourceView<*>> {
        fun <U : FunctionInputElement> map(
            transform: GraphObjectQueryMapContext.(RV) -> U,
        ): BuilderWithTransform<RV, U>
    }

    sealed interface BuilderWithTransform<RV : ResourceView<*>, U : FunctionInputElement> {
        fun querySpec(): GraphObjectQuerySpec<RV, U>

        fun <V : FunctionInputElement> map(
            transform: GraphObjectQueryMapContext.(RV, U) -> V,
        ): BuilderWithTransform<RV, V>
    }

    companion object {
        fun new(
            graphObjectQueryMapContextFactory: GraphObjectQueryMapContextFactory,
            contextName: String,
        ): ResourceEmbeddingHelperFunctionQuerySpecContext {
            return ResourceEmbeddingHelperFunctionQuerySpecContextImpl(
                graphObjectQueryMapContextFactory,
                contextName
            )
        }

        private class ResourceEmbeddingHelperFunctionQuerySpecContextImpl(
            private val graphObjectQueryMapContextFactory: GraphObjectQueryMapContextFactory,
            override val contextName: String
        ) :
            ResourceEmbeddingHelperFunctionQuerySpecContext {
            override fun <RV : ResourceView<*>> view(resourceViewClass: Class<RV>): BuilderWithView<RV> {
                val queryContextName = "$contextName.querying"
                return BuilderWithResourceView(
                    graphObjectQueryMapContextFactory,
                    queryContextName,
                    QueryView.Resource(resourceViewClass),
                )
            }
        }

        private class BuilderWithResourceView<RV : ResourceView<*>>(
            private val graphObjectQueryMapContextFactory: GraphObjectQueryMapContextFactory,
            private val queryContextName: String,
            private val view: QueryView<RV>,
        ) : BuilderWithView<RV> {
            fun queryPivotSpec(): GraphObjectQueryPivotSpec<RV> {
                return view.queryPivotSpec()
            }

            override fun <U : FunctionInputElement> map(
                transform: GraphObjectQueryMapContext.(RV) -> U
            ): BuilderWithTransform<RV, U> {
                val pivotSpec = queryPivotSpec()
                return BuilderWithTransformImpl(
                    graphObjectQueryMapContextFactory,
                    pivotSpec,
                ) { resourceView ->
                    transform(this, resourceView)
                }
            }
        }

        private class QuerySpecImpl<RV : ResourceView<*>, U : FunctionInputElement>(
            private val graphObjectQueryMapContextFactory: GraphObjectQueryMapContextFactory,
            override val pivotClass: Class<RV>,
            private val outerTransform: GraphObjectQueryMapContext.(RV) -> U,
        ) : GraphObjectQuerySpec<RV, U> {
            override val transform: (RV) -> U = { resourceView ->
                val context = graphObjectQueryMapContextFactory.newContext()
                outerTransform(context, resourceView)
            }
        }

        private class BuilderWithTransformImpl<RV : ResourceView<*>, U : FunctionInputElement>(
            private val graphObjectQueryMapContextFactory: GraphObjectQueryMapContextFactory,
            private val queryPivotSpec: GraphObjectQueryPivotSpec<RV>,
            private val transform: GraphObjectQueryMapContext.(RV) -> U,
        ) : BuilderWithTransform<RV, U> {
            private val querySpec: QuerySpecImpl<RV, U> by lazy {
                QuerySpecImpl(
                    graphObjectQueryMapContextFactory,
                    queryPivotSpec.pivotClass,
                    transform,
                )
            }

            override fun querySpec(): GraphObjectQuerySpec<RV, U> {
                return querySpec
            }

            override fun <V : FunctionInputElement> map(transform: GraphObjectQueryMapContext.(RV, U) -> V): BuilderWithTransform<RV, V> {
                val outerTransform = this.transform
                val outerPivotSpec = queryPivotSpec

                return BuilderWithTransformImpl(
                    graphObjectQueryMapContextFactory,
                    outerPivotSpec,
                ) { resourceView ->
                    val rValueFunction = outerTransform(this, resourceView)
                    transform(this, resourceView, rValueFunction)
                }
            }
        }

        /**
         * The view being queried for candidates.
         * From the lens of a typed object graph, the "pivot" element type dictating entity-hood
         */
        private sealed interface QueryView<RV : ResourceView<*>> {

            fun queryPivotSpec(): GraphObjectQueryPivotSpec<RV>

            /**
             * View corresponding to a whole Resource, akin to a table
             */
            data class Resource<RV : ResourceView<*>>(
                val clazz: Class<RV>
            ) : QueryView<RV> {
                override fun queryPivotSpec(): GraphObjectQueryPivotSpec<RV> {
                    return object : GraphObjectQueryPivotSpec<RV> {
                        override val pivotClass: Class<RV>
                            get() = clazz
                    }
                }
            }

            // Future: Indexes, Non-structural types, etc.
        }
    }
}


