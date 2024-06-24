package com.arbr.platform.alignable.alignable.diff

import com.arbr.platform.data_structures_common.partial_order.LinearOrderList

/**
 * Document state represented by its construction
 * Even if a patch could be initialized via this class, it is not meant to represent the Edit side,
 * which has no state, just a Partial Order of EditOperations.
 */
typealias DiffableDocumentState = LinearOrderList<AlignableDiffOperation>
