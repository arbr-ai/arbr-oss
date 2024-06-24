package com.arbr.types.homotopy.config

import com.arbr.types.homotopy.spec.HomotopySpec

interface HomotopyFilterSpec<Tr : Any> : HomotopySpec<Tr?> {

    companion object {
        fun <Tr : Any> configure(configure: (HomotopyFilterSpecConfigurer<Tr>).() -> Unit): HomotopyFilterSpec<Tr> {
            return HomotopyFilterSpecConfigurer.configure(configure)
        }
    }
}
