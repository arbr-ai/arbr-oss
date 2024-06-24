package com.arbr.types.homotopy.config

import com.arbr.types.homotopy.MutablePathContext

interface HomotopyFilterSpecConfigurer<Tr : Any> {
    fun liftNode(lift: (context: MutablePathContext, valueTypeImplementor: Tr?, innerImplementors: List<Tr?>) -> Tr?)

    private class HomotopyFilterSpecConfigurerImpl<Tr : Any> : HomotopyFilterSpecConfigurer<Tr> {
        private var liftNode: ((context: MutablePathContext, valueTypeImplementor: Tr?, innerImplementors: List<Tr?>) -> Tr?)? = null

        override fun liftNode(lift: (context: MutablePathContext, valueTypeImplementor: Tr?, innerImplementors: List<Tr?>) -> Tr?) {
            check(liftNode == null)
            liftNode = lift
        }

        fun build(): HomotopyFilterSpec<Tr> {
            val liftNodeFn = liftNode ?: { _, _, _ -> null }

            return object : HomotopyFilterSpec<Tr> {

                override fun liftNode(
                    context: MutablePathContext,
                    valueTypeImplementor: Tr?,
                    innerImplementors: List<Tr?>
                ): Tr? {
                    return liftNodeFn(context, valueTypeImplementor, innerImplementors)
                }
            }
        }
    }

    companion object {

        fun <Tr : Any> configure(configure: (HomotopyFilterSpecConfigurer<Tr>).() -> Unit): HomotopyFilterSpec<Tr> {
            val configImpl = HomotopyFilterSpecConfigurerImpl<Tr>()
            configure(configImpl)
            return configImpl.build()
        }

    }
}
