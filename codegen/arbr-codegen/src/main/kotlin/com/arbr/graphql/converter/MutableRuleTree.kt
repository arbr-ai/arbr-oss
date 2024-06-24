package com.arbr.graphql.converter

class MutableRuleTree(
    override var value: String,
    override val children: MutableList<RuleTree>,
) : RuleTree