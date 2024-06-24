package com.arbr.backdraft.task.resolver

import com.arbr.backdraft.target.BaseTarget
import org.slf4j.LoggerFactory

class TargetResolver {
    private val loader: ClassLoader = this::class.java.classLoader

    private fun getInstance(clazz: Class<*>): BaseTarget {
        if (!BaseTarget::class.java.isAssignableFrom(clazz)) {
            throw Exception("Specified class '${clazz.name}' does not implement interface ${BaseTarget::class.java.name}")
        }

        val objectInstance = clazz.kotlin.objectInstance
        if (objectInstance != null) {
            return objectInstance as BaseTarget
        }

        val ktConstructors = clazz.kotlin.constructors.filter {
            it.parameters.isEmpty()
        }
        for (constructor in ktConstructors) {
            try {
                return constructor.call() as BaseTarget
            } catch (e: Exception) {
                logger.info("Kotlin constructor ineligible for class '${clazz.name}': ${constructor.name}")
            }
        }

        val javaConstructors = clazz.constructors.filter {
            it.parameters.isEmpty()
        }
        for (constructor in javaConstructors) {
            try {
                return constructor.newInstance() as BaseTarget
            } catch (e: Exception) {
                logger.info("Java constructor ineligible for class '${clazz.name}': ${constructor.name}")
            }
        }

        throw Exception("No viable options to construct class '${clazz.name}'")
    }

    fun resolve(
        targetId: String,
    ): BaseTarget {
        // Try loading by class name directly
        // TODO: Re-add configuration of short IDs
        logger.info("Loading compiler target by class name '$targetId'...")
        val clazz = loader.loadClass(targetId)

        return getInstance(clazz)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TargetResolver::class.java)
    }
}