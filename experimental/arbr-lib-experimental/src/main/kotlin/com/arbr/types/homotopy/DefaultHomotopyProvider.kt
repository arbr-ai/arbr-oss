package com.arbr.types.homotopy

import com.arbr.types.homotopy.config.HomotopyConfig
import com.arbr.types.homotopy.config.HomotopyFilterSpec
import com.arbr.types.homotopy.functional.BaseHomotopyGroundMap
import com.arbr.types.homotopy.functional.HTypeConstantSpecFilterMapper
import com.arbr.types.homotopy.functional.HTypeConstantSpecMapper
import com.arbr.types.homotopy.functional.HomotopyGroundMap
import com.arbr.types.homotopy.htype.HType
import com.arbr.types.homotopy.spec.ContractionHomotopy
import com.arbr.types.homotopy.spec.HomotopySpec
import kotlin.reflect.KType

class DefaultHomotopyProvider(
    config: HomotopyConfig,
): HomotopyProvider {

    /**
     * Introspection functionality via reflection
     */
    private val introspection = HomotopyIntrospection(config.introspectionConfig)

    override fun getBaseType(
        objectType: KType,
    ): HType<PlainType, PlainType> {
        return introspection.introspectBaseType(objectType, MutablePathContext.new())
    }

    override fun <Trait, F : Trait> getHomotopyType(
        baseHType: HType<PlainType, PlainType>,
        homotopySpec: HomotopySpec<Trait>,
        baseHomotopyGroundMap: BaseHomotopyGroundMap<Trait, F>,
    ): HType<Trait, F> {
        val baseMapper = HTypeConstantSpecMapper<PlainType, PlainType, Trait, F>(
            homotopySpec,
            baseHomotopyGroundMap::transform,
        )
        return baseMapper.mapWithContext(baseHType, MutablePathContext.new())
    }

    override fun <Trait : Any> getContractedBaseHomotopyType(
        baseHType: HType<PlainType, PlainType>,
        homotopyFilterSpec: HomotopyFilterSpec<Trait>,
        baseHomotopyGroundMap: BaseHomotopyGroundMap<Trait?, Trait?>,
        contractionHomotopy: ContractionHomotopy<Trait>
    ): List<LocalPathQualifiedNode<Trait>> {
        val mapper = HTypeConstantSpecFilterMapper(homotopyFilterSpec, baseHomotopyGroundMap)
        return mapper.filterMap(
            contractionHomotopy,
            baseHType,
            MutablePathContext.new(),
        )
    }

    override fun <BaseTrait, F : BaseTrait, Trait : Any> getContractedHomotopyType(
        hType: HType<BaseTrait, F>,
        homotopyFilterSpec: HomotopyFilterSpec<Trait>,
        homotopyGroundMap: HomotopyGroundMap<BaseTrait, F, Trait?, Trait?>,
        contractionHomotopy: ContractionHomotopy<Trait>
    ): List<LocalPathQualifiedNode<Trait>> {
        val mapper = HTypeConstantSpecFilterMapper(homotopyFilterSpec, homotopyGroundMap)
        return mapper.filterMap(
            contractionHomotopy,
            hType,
            MutablePathContext.new(),
        )
    }

    override fun <Trait, F : Trait> contraction(hType: HType<Trait, F>): HomotopyContractionBuilder<Trait, F> {
        return HomotopyContractionBuilder(hType)
    }
}