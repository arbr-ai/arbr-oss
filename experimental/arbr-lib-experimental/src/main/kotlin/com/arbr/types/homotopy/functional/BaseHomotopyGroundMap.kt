package com.arbr.types.homotopy.functional

import com.arbr.types.homotopy.PlainType

fun interface BaseHomotopyGroundMap<Trait, F : Trait>: HomotopyGroundMap<PlainType, PlainType, Trait, F>
