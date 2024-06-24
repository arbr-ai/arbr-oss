package com.arbr.platform.alignable.alignable

fun interface CrossTypeCostModel<T, U> {
    fun leftAlignmentCost(element: T?, otherElement: T?): Double = 0.0

    fun rightAlignmentCost(element: U?, otherElement: U?): Double = 0.0

    fun crossAlignmentCost(element: T?, otherElement: U?): Double

    fun reverseAlignmentCost(element: U?, otherElement: T?): Double = crossAlignmentCost(otherElement, element)
}