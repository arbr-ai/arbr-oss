package com.arbr.codegen.base.generator

import java.net.URL

class TemplatingEngine(
    private val fileUrl: URL
) {
    private val nodeRegex = Regex("^\\{\\%([a-zA-Z0-9_]*)\\%\\}")
    private val nodeEndRegex = Regex("^\\{\\%_([a-zA-Z0-9_]*)\\%\\}")

    private val valueBindingRegex = Regex("(\\h*)?\\{\\%([a-zA-Z0-9_]*)\\.([a-zA-Z0-9_]*)\\%\\}")
    private val valueMapBindingRegex = Regex("(\\h*)?\\{\\%([a-zA-Z0-9_]*)\\.([a-zA-Z0-9_]*)\\:\\#([a-zA-Z0-9_]*)\\%\\}")
    private val refBindingRegex = Regex("(\\h*)?\\{\\%\\#([a-zA-Z0-9_]*)\\%\\}")

    private val model: Model
    private val nodeModels = mutableMapOf<String, Node>()

    init {
        model = buildModel()
    }

    data class Node(
        val name: String,
        val templateText: String,
        val valueBindings: List<Binding>,
        val mapBindings: List<MapBinding>,
        val refBindings: List<RefBinding>,
    )

    data class Binding(
        val leadingWhitespace: String,
        val entity: String,
        val property: String,
    )

    data class MapBinding(
        val leadingWhitespace: String,
        val entity: String,
        val property: String,
        val invocation: String,
    )

    data class RefBinding(
        val leadingWhitespace: String,
        val refNode: String,
    )

    data class Entity(
        val name: String,
        val valueBindings: List<Binding>,
        val mapBindings: List<MapBinding>,
        val refBindings: List<RefBinding>,
    )

    data class Model(
        val entities: Map<String, Entity>
    )

    private fun processNode(
        name: String,
        nodeLines: List<String>
    ): Node {
        val valueBindingProperties = linkedSetOf<Binding>()
        val mapBindingProperties = linkedSetOf<MapBinding>()
        val refBindingProperties = linkedSetOf<RefBinding>()

        for (line in nodeLines) {
            val valueBindings = valueBindingRegex.findAll(line)
            for (m in valueBindings) {
                val binding = Binding(m.groups[1]?.value ?: "", m.groups[2]!!.value, m.groups[3]!!.value)
                valueBindingProperties.add(binding)
            }

            val mapBindings = valueMapBindingRegex.findAll(line)
            for (m in mapBindings) {
                val binding = MapBinding(m.groups[1]?.value ?: "", m.groups[2]!!.value, m.groups[3]!!.value, m.groups[4]!!.value)
                mapBindingProperties.add(binding)
            }

            val refBindings = refBindingRegex.findAll(line)
            for (m in refBindings) {
                val binding = RefBinding(m.groups[1]?.value ?: "", m.groups[2]!!.value)
                refBindingProperties.add(binding)
            }
        }

        return Node(
            name,
            nodeLines.joinToString("\n"),
            valueBindingProperties.toList(),
            mapBindingProperties.toList(),
            refBindingProperties.toList(),
        )
    }

    private fun buildModel(): Model {
        val nodeModels = mutableListOf<Node>()

        var node: String? = null
        var nodeLines = mutableListOf<String>()

        for ((i, line) in fileUrl.readText().lines().withIndex()) {
            val matchesEnd = nodeEndRegex.matchEntire(line)
            if (matchesEnd != null) {
                val nodeName = matchesEnd.groups[1]!!.value
                if (nodeName != node) {
                    throw Exception("Unexpected end tag at line $i: $nodeName")
                }

                nodeModels.add(processNode(nodeName, nodeLines))

                node = null
                nodeLines = mutableListOf()

                continue
            }
            val matches = nodeRegex.matchEntire(line)
            if (matches != null) {
                val nodeName = matches.groups[1]!!.value
                node = nodeName

                continue
            }

            if (node != null) {
                nodeLines.add(line)
            }
        }

        this.nodeModels.putAll(nodeModels.associateBy { it.name })

        val valueBindings = nodeModels
            .flatMap { it.valueBindings }
            .groupBy { it.entity }

        val mapBindings = nodeModels
            .flatMap { it.mapBindings }
            .groupBy { it.entity }

        val refBindings = nodeModels
            .associate { it.name to it.refBindings }

        val entities = mutableMapOf<String, Entity>()
        for (n in nodeModels) {
            val entityName = n.name

            val entity = Entity(
                entityName,
                valueBindings.getOrDefault(entityName, emptyList()),
                mapBindings.getOrDefault(entityName, emptyList()),
                refBindings.getOrDefault(entityName, emptyList()),
            )
            entities[entityName] = entity
        }

        return Model(entities)
    }

    private fun render(nodeName: String, scopeName: String, entityObjects: List<Map<String, Any>>, stack: MutableMap<String, String>): String {
        val node = nodeModels[nodeName] ?: throw Exception("Node not found: $nodeName")

        val stringBuilder = StringBuilder()

        for (entity in entityObjects) {
            val pushedSelectors = mutableSetOf<String>()
            for ((prop, value) in entity) {
                if (value is String) {
                    val selector = "${scopeName}.${prop}"
                    stack[selector] = value
                    pushedSelectors.add(selector)
                }
            }

            val valuedText = node.templateText.replace(valueBindingRegex) { m ->
                val whitespace = m.groups[1]?.value ?: ""
                val ename = m.groups[2]!!.value
                val key = m.groups[3]!!.value
                val selector = "${ename}.${key}"
                whitespace + (stack[selector]
                    ?: throw Exception("Selector $selector not found in stack\n\n${stack}"))
            }

            val reffedText = valuedText.replace(refBindingRegex) { m ->
                val whitespace = m.groups[1]?.value ?: ""
                val refName = m.groups[2]!!.value
                // Ref is unvalued
                render(refName, scopeName, listOf(emptyMap()), stack)
                    .split("\n")
                    .joinToString("\n") { whitespace + it }
            }

            val mappedText = reffedText.replace(valueMapBindingRegex) { m ->
                val binding = MapBinding(m.groups[1]?.value ?: "", m.groups[2]!!.value, m.groups[3]!!.value, m.groups[4]!!.value)

                // Sub-list of objects
                val propertyName = binding.property
                @Suppress("UNCHECKED_CAST")
                val objects = (entity[propertyName]
                    ?: throw Exception("Property $propertyName not found on $entity")
                        ) as List<Map<String, Any>>

                render(binding.invocation, binding.property, objects, stack)
                    .split("\n")
                    .joinToString("\n") { binding.leadingWhitespace + it }
            }
            stringBuilder.append(mappedText)

            for (sel in pushedSelectors) {
                stack.remove(sel)
            }
        }

        return stringBuilder.toString()
    }

    fun render(bindings: Map<String, Any>): String {
        // bindings: root: { schemata -> [{schema}] }
        // nest in one more layer
        // "root", [{ schemata -> [{schema}] }]
        return render("root", "root", listOf(bindings), mutableMapOf())
    }
}