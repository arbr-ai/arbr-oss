package com.arbr.types.homotopy.functional

import com.arbr.types.homotopy.HomotopyContractions
import com.arbr.types.homotopy.LocalPathQualifiedNode
import com.arbr.types.homotopy.MutablePathContext
import com.arbr.types.homotopy.NullableTrait
import com.arbr.types.homotopy.NullableTraitImplementor
import com.arbr.types.homotopy.htype.HType
import com.arbr.types.homotopy.spec.ContractionHomotopy

interface HTypeFilterMapper<Tr, F : Tr, Tr2 : Any> :
    HTypeMapper<Tr, F, NullableTrait<Tr2>, NullableTraitImplementor<Tr2, Tr2>> {

    fun filterMap(
        homotopySpec: ContractionHomotopy<Tr2>,
        hType: HType<Tr, F>,
        context: MutablePathContext
    ): List<LocalPathQualifiedNode<Tr2>> {
        val mappedNullableHType = mapWithContext(hType, context)
        return HomotopyContractions.contract(homotopySpec, mappedNullableHType)
    }
}
