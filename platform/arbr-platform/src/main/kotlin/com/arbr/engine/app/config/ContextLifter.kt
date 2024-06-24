package com.arbr.engine.app.config

import org.reactivestreams.Subscription
import org.slf4j.MDC
import reactor.core.CoreSubscriber
import reactor.util.context.Context
import reactor.util.context.ContextView
import java.util.stream.Collectors


/**
 * Helper that copies the state of Reactor [Context] to MDC on the #onNext function.
 * And has other listeners
 */
class ContextLifter<T>(
    private val coreSubscriber: CoreSubscriber<T>,
) : CoreSubscriber<T> {
    override fun onSubscribe(subscription: Subscription) {
        coreSubscriber.onSubscribe(subscription)
    }

    override fun onNext(obj: T) {
        val context = currentContext()
        copyToMdc(context)

        coreSubscriber.onNext(obj)
    }

    override fun onError(t: Throwable) {
        coreSubscriber.onError(t)
    }

    override fun onComplete() {
        coreSubscriber.onComplete()
    }

    override fun currentContext(): Context {
        return coreSubscriber.currentContext()
    }

    companion object {
        /**
         * Extension function for the Reactor [ContextView]. Copies the current context to the MDC, if context is empty clears the MDC.
         * State of the MDC after calling this method should be same as Reactor [ContextView] state.
         * One thread-local access only.
         */
        fun copyToMdc(context: ContextView) {
            if (!context.isEmpty) {
                val map = context.stream()
                    .collect(
                        Collectors.toMap(
                            { it.key.toString() },
                            { it.value.toString() })
                    )
                MDC.setContextMap(map)
            } else {
                MDC.clear()
            }
        }
    }
}
