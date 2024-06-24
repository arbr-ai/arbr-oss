package com.arbr.engine.app.config

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.context.annotation.Configuration
import java.io.Closeable


@Configuration
class ContextLifterConfiguration: Closeable {
    private val contextReactorKey = ContextLifterConfiguration::class.java.name

    @PostConstruct
    private fun contextOperatorHook() {
//        Hooks.onEachOperator(contextReactorKey) { pub ->
//            val lift = Operators.lift<Any, Any> { _, coreSubscriber ->
//                ContextLifter(
//                    coreSubscriber,
//                )
//            }
//            lift.apply(pub)
//        }
    }

    @PreDestroy
    private fun cleanupHook() {
//        Hooks.resetOnEachOperator(contextReactorKey)
    }

    override fun close() {
        cleanupHook()
    }

    companion object {

        fun newInstance(): ContextLifterConfiguration {
            return ContextLifterConfiguration().also { it.contextOperatorHook() }
        }

    }
}
