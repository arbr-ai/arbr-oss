package com.arbr.util

import com.arbr.content_formats.mapper.Mappers
import com.arbr.og_engine.artifact.Artifact
import com.arbr.relational_prompting.services.ai_application.application.ApplicationArtifact
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import reactor.core.Disposable
import reactor.core.publisher.FluxSink
import reactor.util.context.Context
import java.util.function.LongConsumer


fun <T : Any, U : Any> FluxSink<T>.adapt(f: (U) -> T): FluxSink<U> = let { outerFlux ->
    object : FluxSink<U> {
        override fun next(t: U): FluxSink<U> {
            outerFlux.next(f(t))
            return this
        }

        override fun complete() {
            outerFlux.complete()
        }

        override fun error(e: Throwable) {
            outerFlux.error(e)
        }

        @Suppress("DEPRECATION")
        @Deprecated("Deprecated in Java", ReplaceWith("outerFlux.currentContext()"))
        override fun currentContext(): Context {
            return outerFlux.currentContext()
        }

        override fun requestedFromDownstream(): Long {
            return outerFlux.requestedFromDownstream()
        }

        override fun isCancelled(): Boolean {
            return outerFlux.isCancelled
        }

        override fun onRequest(consumer: LongConsumer): FluxSink<U> {
            outerFlux.onRequest(consumer)
            return this
        }

        override fun onCancel(d: Disposable): FluxSink<U> {
            outerFlux.onCancel(d)
            return this
        }

        override fun onDispose(d: Disposable): FluxSink<U> {
            outerFlux.onDispose(d)
            return this
        }

    }
}

/**
 * TODO: Just make the relational prompting ApplicationArtifact an Artifact
 */
fun FluxSink<Artifact>.adapt(): FluxSink<ApplicationArtifact> = adapt { applicationArtifact ->
    com.arbr.og_engine.artifact.ApplicationCompletionArtifact(
        applicationArtifact.applicationId,
        Mappers.mapper.convertValue(applicationArtifact.examples, jacksonTypeRef()),
        applicationArtifact.input,
        applicationArtifact.output,
        applicationArtifact.vectorIds,
    )
}

fun <T : Any> fluxSinkIgnore(): FluxSink<T> = object : FluxSink<T> {
    override fun next(t: T): FluxSink<T> {
        return this
    }

    override fun complete() {
    }

    override fun error(e: Throwable) {
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java", ReplaceWith(""))
    override fun currentContext(): Context {
        return Context.empty()
    }

    override fun requestedFromDownstream(): Long {
        return 0
    }

    override fun isCancelled(): Boolean {
        return false
    }

    override fun onRequest(consumer: LongConsumer): FluxSink<T> {
        return this
    }

    override fun onCancel(d: Disposable): FluxSink<T> {
        return this
    }

    override fun onDispose(d: Disposable): FluxSink<T> {
        return this
    }
}