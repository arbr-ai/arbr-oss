package com.arbr.types.homotopy

class NullableTraitImplementor<Trait, G : Trait>(
    override val traitOrNull: G?
) : NullableTrait<Trait>