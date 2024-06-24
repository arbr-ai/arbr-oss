package com.arbr.gradle

import com.arbr.gradle.field.*
import org.gradle.api.Action
import org.gradle.api.PolymorphicDomainObjectContainer

class ArbrObjectFieldDomainObjectContainer(
    polymorphicDomainObjectContainer: PolymorphicDomainObjectContainer<ArbrObjectField>,
) : PolymorphicDomainObjectContainer<ArbrObjectField> by polymorphicDomainObjectContainer {

    fun createBooleanProperty(
        name: String,
    ): ArbrObjectBooleanField = create(name, ArbrObjectBooleanField::class.java)

    fun createBooleanProperty(
        name: String,
        configuration: Action<ArbrObjectBooleanField>,
    ): ArbrObjectBooleanField = create(name, ArbrObjectBooleanField::class.java, configuration)

    fun createIntegerProperty(
        name: String,
    ): ArbrObjectIntegerField = create(name, ArbrObjectIntegerField::class.java)

    fun createIntegerProperty(
        name: String,
        configuration: Action<ArbrObjectIntegerField>,
    ): ArbrObjectIntegerField = create(name, ArbrObjectIntegerField::class.java, configuration)

    fun createLongProperty(
        name: String,
    ): ArbrObjectLongField = create(name, ArbrObjectLongField::class.java)

    fun createLongProperty(
        name: String,
        configuration: Action<ArbrObjectLongField>,
    ): ArbrObjectLongField = create(name, ArbrObjectLongField::class.java, configuration)

    fun createFloatProperty(
        name: String,
    ): ArbrObjectFloatField = create(name, ArbrObjectFloatField::class.java)

    fun createFloatProperty(
        name: String,
        configuration: Action<ArbrObjectFloatField>,
    ): ArbrObjectFloatField = create(name, ArbrObjectFloatField::class.java, configuration)

    fun createDoubleProperty(
        name: String,
    ): ArbrObjectDoubleField = create(name, ArbrObjectDoubleField::class.java)

    fun createDoubleProperty(
        name: String,
        configuration: Action<ArbrObjectDoubleField>,
    ): ArbrObjectDoubleField = create(name, ArbrObjectDoubleField::class.java, configuration)

    fun createStringProperty(
        name: String,
    ): ArbrObjectStringField = create(name, ArbrObjectStringField::class.java)

    fun createStringProperty(
        name: String,
        configuration: Action<ArbrObjectStringField>,
    ): ArbrObjectStringField = create(name, ArbrObjectStringField::class.java, configuration)

    inline fun <reified T> createProperty(
        name: String,
    ): ArbrObjectField {
        return when (T::class) {
            Integer::class -> createIntegerProperty(name)
            String::class -> createStringProperty(name)
            Boolean::class -> createBooleanProperty(name)
            Float::class -> createFloatProperty(name)
            Double::class -> createDoubleProperty(name)
            Long::class -> createLongProperty(name)
            else -> throw IllegalArgumentException("Unsupported property type: ${T::class}")
        }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> createProperty(
        name: String,
        configuration: Action<ArbrObjectField>,
    ): ArbrObjectField {
        return when (T::class) {
            Integer::class -> createIntegerProperty(name, configuration as Action<ArbrObjectIntegerField>)
            String::class -> createStringProperty(name, configuration as Action<ArbrObjectStringField>)
            Boolean::class -> createBooleanProperty(name, configuration as Action<ArbrObjectBooleanField>)
            Float::class -> createFloatProperty(name, configuration as Action<ArbrObjectFloatField>)
            Double::class -> createDoubleProperty(name, configuration as Action<ArbrObjectDoubleField>)
            Long::class -> createLongProperty(name, configuration as Action<ArbrObjectLongField>)
            else -> throw IllegalArgumentException("Unsupported property type: ${T::class}")
        }
    }

}

@Suppress("nothing_to_inline")
inline operator fun ArbrObjectFieldDomainObjectContainer.invoke(
    configuration: Action<ArbrObjectFieldDomainObjectContainer>
): ArbrObjectFieldDomainObjectContainer = apply {
    configuration.execute(this)
}
