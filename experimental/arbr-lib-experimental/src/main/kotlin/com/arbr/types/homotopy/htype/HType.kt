package com.arbr.types.homotopy.htype

import com.arbr.types.homotopy.Homotopy
import com.arbr.types.homotopy.HomotopyContractionBuilder
import com.arbr.types.homotopy.keys.CompoundKeyPathPropertyKey

/**
 * Homotopy type over some ground field F with a trait Tr
 */
sealed interface HType<Tr, F : Tr> {
    /**
     * Global, fully-qualified key path
     */
    val keyPath: CompoundKeyPathPropertyKey

    val implementor: Tr

    fun contraction(): HomotopyContractionBuilder<Tr, F> {
        return Homotopy.contraction(this)
    }
}
