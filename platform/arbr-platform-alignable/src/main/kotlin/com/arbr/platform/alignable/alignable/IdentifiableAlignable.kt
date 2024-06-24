package com.arbr.platform.alignable.alignable

interface IdentifiableAlignable<E : Alignable<E, AlignmentOperation>, AlignmentOperation>:
    Alignable<E, AlignmentOperation> {
    val uuid: AtomicAlignable<String>
}