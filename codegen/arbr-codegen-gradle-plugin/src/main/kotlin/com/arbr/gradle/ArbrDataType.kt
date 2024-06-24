package com.arbr.gradle

import com.arbr.codegen.base.inputs.ArbrPrimitiveValueType
import com.arbr.gradle.field.ArbrObjectField
import com.arbr.gradle.field.ArbrObjectStringField
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

abstract class ArbrDataType @Inject constructor(
    val name: String,
    objects: ObjectFactory,
) {
    val ordinal: Int = ordinalNameSet.computeIfAbsent(name) {
        ordinalCounter.getAndIncrement()
    }

    @get:Suppress("unused")
    val parent: Property<ArbrDataType> = objects.property(ArbrDataType::class.java)

    private val parentReferenceContainer: NamedDomainObjectContainer<ArbrObjectReference> =
        objects.domainObjectContainer(
            ArbrObjectReference::class.java
        ) { name ->
            objects.newInstance(ArbrObjectReference::class.java, name)
        }

    val parentReference: ArbrObjectReference by lazy {
        parentReferenceContainer
            .create(PARENT_REFERENCE_FIELD_NAME)
            .also { it.targetDataType.set(parent.get()) }
    }

    @get:Suppress("unused")
    val description: Property<String> = objects
        .property(String::class.java)
        .convention(DEFAULT_DESCRIPTION)

    private val polymorphicFields: ExtensiblePolymorphicDomainObjectContainer<ArbrObjectField> =
        objects.polymorphicDomainObjectContainer(
            ArbrObjectField::class.java
        ).also { container ->
            fun <U : ArbrObjectField> registerTypedFactory(fieldTypeClass: Class<U>) {
                val namedDomainObjectFactory = NamedDomainObjectFactory { name ->
                    objects.newInstance(fieldTypeClass, name)
                }
                container.registerFactory(fieldTypeClass, namedDomainObjectFactory)
            }

            ArbrObjectField::class.sealedSubclasses.forEach { kClass ->
                registerTypedFactory(kClass.java)
            }

            val resourceIdField = container.create(RESOURCE_ID_FIELD_NAME, ArbrObjectStringField::class.java)
            resourceIdField.type.set(
                ArbrPrimitiveValueType.STRING
            )
            resourceIdField.required.set(true)
        }

    @get:Suppress("unused")
    val fields: ArbrObjectFieldDomainObjectContainer = ArbrObjectFieldDomainObjectContainer(polymorphicFields)

    @get:Suppress("unused")
    val relations: NamedDomainObjectContainer<ArbrObjectReference> = objects.domainObjectContainer(
        ArbrObjectReference::class.java
    ) { name ->
        objects.newInstance(ArbrObjectReference::class.java, name)
    }

    companion object {
        const val PARENT_REFERENCE_FIELD_NAME = "parent"
        const val RESOURCE_ID_FIELD_NAME = "uuid"
        private const val DEFAULT_DESCRIPTION = ""

        private val ordinalCounter = AtomicInteger()
        private val ordinalNameSet = ConcurrentHashMap<String, Int>()
    }
}
