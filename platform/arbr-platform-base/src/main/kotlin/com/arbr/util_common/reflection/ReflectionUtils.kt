package com.arbr.util_common.reflection

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

object ReflectionUtils {
    val logger: Logger = LoggerFactory.getLogger(ReflectionUtils::class.java)

    fun <T: Any> getInstance(
        superClass: Class<T>,
        subClass: Class<out T>,
    ): T {
        if (!superClass.isAssignableFrom(subClass)) {
            throw IllegalStateException("Specified class '${subClass.name}' is not assignable to ${superClass.name}")
        }

        val objectInstance = subClass.kotlin.objectInstance
        if (objectInstance != null) {
            return objectInstance as T
        }

        val ktConstructors = subClass.kotlin.constructors.filter {
            it.parameters.isEmpty()
        }
        for (constructor in ktConstructors) {
            try {
                return constructor.call() as T
            } catch (e: Exception) {
                logger.info("Kotlin constructor ineligible for class '${subClass.name}': ${constructor.name}")
            }
        }

        val javaConstructors = subClass.constructors.filter {
            it.parameters.isEmpty()
        }
        for (constructor in javaConstructors) {
            try {
                @Suppress("UNCHECKED_CAST")
                return constructor.newInstance() as T
            } catch (e: Exception) {
                logger.info("Java constructor ineligible for class '${subClass.name}': ${constructor.name}")
            }
        }

        throw Exception("No viable options to construct class '${subClass.name}'")
    }

    inline fun <reified T: Any> getInstance(kclazz: KClass<out T>): T {
        return getInstance(T::class.java, kclazz.java)
    }

    fun <T: Any> sealedMemberInstances(parentKClass: KClass<T>): List<T> {
        val superClass = parentKClass.java

        return parentKClass.sealedSubclasses.map { subclass ->
            getInstance(superClass, subclass.java)
        }
    }
}
