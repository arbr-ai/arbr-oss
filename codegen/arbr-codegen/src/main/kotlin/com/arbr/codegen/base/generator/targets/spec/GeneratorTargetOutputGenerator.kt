package com.arbr.codegen.base.generator.targets.spec

import com.arbr.codegen.base.dependencies.MapperConfig
import com.arbr.codegen.base.generator.DisplayRootModel
import com.arbr.codegen.base.generator.TemplatingEngine
import com.arbr.codegen.base.generator.targets.GeneratorTargetConfig
import com.arbr.codegen.util.SingleUrlResolver
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import java.io.StringWriter

internal sealed interface GeneratorTargetOutputGenerator {

    val templateFileName: String?

    fun generate(displayRootModel: DisplayRootModel): String

    class MustacheTemplate(override val templateFileName: String): GeneratorTargetOutputGenerator {
        private val mustache = getMustache(templateFileName)

        override fun generate(displayRootModel: DisplayRootModel): String {
            val stringWriter = StringWriter()
            mustache.execute(
                stringWriter,
                displayRootModel
            )
            return stringWriter.toString()
        }

        companion object {
            private fun getMustache(templateFileName: String): Mustache {
                val url = GeneratorTargetConfig.getTemplateUrl(templateFileName)
                return DefaultMustacheFactory(
                    SingleUrlResolver(
                        url
                    )
                ).compile(templateFileName)
            }
        }
    }

    class ArbrTemplate(
        override val templateFileName: String,
    ): GeneratorTargetOutputGenerator {
        private val mapper = MapperConfig().mapper
        private val engine = TemplatingEngine(GeneratorTargetConfig.getTemplateUrl(templateFileName))

        override fun generate(displayRootModel: DisplayRootModel): String {
            val bindings = mapper.convertValue(displayRootModel, jacksonTypeRef<Map<String, Any>>())
            val output = engine.render(bindings)

            return output
        }
    }

    class Custom(private val generateFn: (DisplayRootModel) -> String): GeneratorTargetOutputGenerator {
        override val templateFileName: String? = null

        override fun generate(displayRootModel: DisplayRootModel): String {
            return generateFn(displayRootModel)
        }
    }
}