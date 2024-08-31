package com.arbr.platform.object_graph.common.functions.spec.base

import com.arbr.platform.object_graph.types.ResourceView
import reactor.core.publisher.Mono

interface RValueFunction<RV : ResourceView<*>, T> {
    val name: String
    val resourceViewClass: Class<RV>

    fun apply(resourceView: RV): Mono<T>

    companion object {
        fun <RV : ResourceView<*>, T> named(name: String, resourceViewClass: Class<RV>, apply: (RV) -> T) =
            object : RValueFunction<RV, T> {
                override val name: String
                    get() = name
                override val resourceViewClass: Class<RV>
                    get() = resourceViewClass

                override fun apply(resourceView: RV): Mono<T> {
                    return Mono.justOrEmpty(apply(resourceView))
                }
            }

        fun <RV : ResourceView<*>, T> async(name: String, resourceViewClass: Class<RV>, apply: (RV) -> Mono<T>) =
            object : RValueFunction<RV, T> {
                override val name: String
                    get() = name
                override val resourceViewClass: Class<RV>
                    get() = resourceViewClass

                override fun apply(resourceView: RV): Mono<T> {
                    return apply(resourceView)
                }
            }
    }
}
