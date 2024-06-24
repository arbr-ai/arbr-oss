package com.arbr.types.homotopy.functional

import com.arbr.types.homotopy.MutablePathContext

fun interface HomotopyGroundMap<Trait, F : Trait, Trait2, G: Trait2> {

    fun transform(singleton: F, context: MutablePathContext): G
}