package com.arbr.types.homotopy

import com.arbr.types.homotopy.config.HomotopyFilterSpec
import com.arbr.types.homotopy.htype.HType
import com.arbr.types.homotopy.spec.ContractionHomotopy

class HomotopyContractionBuilder<Trait, F: Trait>(
    private val hType: HType<Trait, F>,
) {

    fun <TargetTrait : Any> mapPrimitivesNotNull(
        transform: (Trait, MutablePathContext) -> TargetTrait?
    ): WithMap<Trait, F, TargetTrait> {
        return WithMap(hType, transform)
    }

    class WithMap<Trait, F: Trait, TargetTrait : Any>(
        override val hType: HType<Trait, F>,
        override val transform: (Trait, MutablePathContext) -> TargetTrait?,
    ): HomotopyContraction<Trait, F, TargetTrait>() {
        override var filterSpec = HomotopyFilterSpec.configure<TargetTrait> { }
        override var contractionHomotopy = ContractionHomotopy.default<TargetTrait>()

        /**
         * Specify nullable trait lifting to interior nodes
         *
         * TODO: Make argument order consistent in functions
         */
        fun liftNodes(
            lift: (MutablePathContext, TargetTrait?, List<TargetTrait?>) -> TargetTrait?
        ): WithMap<Trait, F, TargetTrait> {
            filterSpec = HomotopyFilterSpec.configure {
                liftNode(lift)
            }
            return this
        }

        /**
         * Specify contraction homotopy
         * Defaults to lifting node implementations exactly from the node value implementation
         */
        fun constructWith(
            lift: (MutablePathContext, TargetTrait, List<TargetTrait>) -> TargetTrait
        ): WithMap<Trait, F, TargetTrait> {
            contractionHomotopy = object : ContractionHomotopy<TargetTrait> {
                override fun liftNode(
                    context: MutablePathContext,
                    valueTypeImplementor: TargetTrait,
                    innerImplementors: List<TargetTrait>
                ): TargetTrait {
                    return lift(context, valueTypeImplementor, innerImplementors)
                }
            }
            return this
        }
    }
}