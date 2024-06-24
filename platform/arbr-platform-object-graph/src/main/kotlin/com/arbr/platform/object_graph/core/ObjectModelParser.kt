package com.arbr.platform.object_graph.core

import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.platform.object_graph.common.ObjectModelResourceParser
import com.arbr.platform.object_graph.alignable.PartialNodeAlignableValue
import com.arbr.platform.object_graph.impl.ObjectModelResource

// Note component removed
class ObjectModelParser(
    resourceParsers: List<ObjectModelResourceParser<*, *, *>>,
) {
    private val resourceParserMap = resourceParsers
        .associateBy { it.key }

    fun <ForeignKey: NamedForeignKey> parseNodeValue(
        nodeValue: PartialNodeAlignableValue,
    ): ObjectModelResource<*, *, ForeignKey> {
        val elementKey = nodeValue.typeName.element
        val parser = resourceParserMap[elementKey]?.let {
            @Suppress("UNCHECKED_CAST")
            it as? ObjectModelResourceParser<*, *, ForeignKey>
        }
            ?: throw IllegalStateException("No object model resource parser for $elementKey")

        return parser.parseNodeValue(nodeValue)
    }

}