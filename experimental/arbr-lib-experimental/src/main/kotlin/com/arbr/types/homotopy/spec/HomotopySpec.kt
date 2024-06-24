package com.arbr.types.homotopy.spec

/**
 * A complete homotopy specification
 * we might not need to have an umbrella homotopy but instead subsets of them for different cases (JSON, graph
 * contractions, etc.)
 */
interface HomotopySpec<Tr> : NodeHomotopy<Tr>, PrimitiveHomotopy<Tr>, HomotopyRoot<Tr, HomotopySpec<Tr>>
