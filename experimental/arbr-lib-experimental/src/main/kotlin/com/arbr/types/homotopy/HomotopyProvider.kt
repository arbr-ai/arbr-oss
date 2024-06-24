package com.arbr.types.homotopy

import com.arbr.types.homotopy.config.HomotopyFilterSpec
import com.arbr.types.homotopy.functional.BaseHomotopyGroundMap
import com.arbr.types.homotopy.functional.HomotopyGroundMap
import com.arbr.types.homotopy.htype.HType
import com.arbr.types.homotopy.spec.ContractionHomotopy
import com.arbr.types.homotopy.spec.HomotopySpec
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType

interface HomotopyProvider {
    fun getBaseType(
        objectType: KType,
    ): HType<PlainType, PlainType>

    fun <Trait, F : Trait> getHomotopyType(
        baseHType: HType<PlainType, PlainType>,
        homotopySpec: HomotopySpec<Trait>,
        baseHomotopyGroundMap: BaseHomotopyGroundMap<Trait, F>,
    ): HType<Trait, F>

    fun <Trait : Any> getContractedBaseHomotopyType(
        baseHType: HType<PlainType, PlainType>,
        homotopyFilterSpec: HomotopyFilterSpec<Trait>,
        baseHomotopyGroundMap: BaseHomotopyGroundMap<Trait?, Trait?>,
        contractionHomotopy: ContractionHomotopy<Trait>,
    ): List<LocalPathQualifiedNode<Trait>>

    fun <BaseTrait, F : BaseTrait, Trait : Any> getContractedHomotopyType(
        hType: HType<BaseTrait, F>,
        homotopyFilterSpec: HomotopyFilterSpec<Trait>,
        homotopyGroundMap: HomotopyGroundMap<BaseTrait, F, Trait?, Trait?>,
        contractionHomotopy: ContractionHomotopy<Trait>
    ): List<LocalPathQualifiedNode<Trait>>

    fun <Trait, F: Trait> contraction(
        hType: HType<Trait, F>,
    ): HomotopyContractionBuilder<Trait, F>
}

inline fun <reified Cl> HomotopyProvider.getBaseType(): HType<PlainType, PlainType> {
    val objectType = Cl::class.starProjectedType
    return getBaseType(objectType)
}

inline fun <reified Cl, Trait, F : Trait> HomotopyProvider.getHomotopyType(
    homotopySpec: HomotopySpec<Trait>,
    baseHomotopyGroundMap: BaseHomotopyGroundMap<Trait, F>,
): HType<Trait, F> {
    val baseType = getBaseType<Cl>()
    return getHomotopyType(baseType, homotopySpec, baseHomotopyGroundMap)
}

