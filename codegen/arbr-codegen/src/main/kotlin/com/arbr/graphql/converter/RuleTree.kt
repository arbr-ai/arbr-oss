package com.arbr.graphql.converter

import java.util.*

interface RuleTree {
    val value: String
    val children: List<RuleTree>

    companion object {
        fun serializeRuleTree(
            appendable: Appendable,
            ruleTree: RuleTree,
            bracketCharOpen: String,
            bracketCharClose: String,
            splitChar: String,
        ) {
            appendable.append(ruleTree.value)
            if (ruleTree.children.isNotEmpty()) {
                appendable.append(bracketCharOpen)
                val numChildren = ruleTree.children.size
                ruleTree.children.forEachIndexed { index, ch ->
                    serializeRuleTree(appendable, ch, bracketCharOpen, bracketCharClose, splitChar)

                    if (index < numChildren - 1) {
                        appendable.append(splitChar)
                    }
                }
                appendable.append(bracketCharClose)
            }
        }

        @Suppress("SameParameterValue")
        fun parseRuleTree(
            inputString: String,
            bracketCharOpen: Char,
            bracketCharClose: Char,
            splitChar: Char,
        ): RuleTree {
            val stack = Stack<MutableRuleTree>()
            stack.push(MutableRuleTree("", mutableListOf()))

            var tokenStart = 0
            var bracketCt = 0
            var i = 0

            while (i < inputString.length) {
                bracketCt += when (inputString[i]) {
                    bracketCharOpen -> {
                        val ruleName = inputString.substring(tokenStart, i).trim()
                        stack.peek().value = ruleName

                        tokenStart = i + 1
                        stack.push(MutableRuleTree("", mutableListOf()))

                        1
                    }

                    bracketCharClose -> {
                        if (i > tokenStart) {
                            val ruleName = inputString.substring(tokenStart, i).trim()
                            stack.peek().value = ruleName
                        }

                        val subtree = stack.pop()
                        stack.peek().children.add(subtree)
                        tokenStart = i + 1

                        -1
                    }

                    splitChar -> {
                        val child = stack.pop()

                        if (i > tokenStart) {
                            val ruleName = inputString.substring(tokenStart, i).trim()
                            child.value = ruleName
                        }

                        val parent = stack.peek()
                        parent.children.add(child)
                        val newChild = MutableRuleTree("", mutableListOf())
                        stack.push(newChild)

                        tokenStart = i + 1

                        0
                    }

                    else -> 0
                }
                i++
            }
            if (bracketCt > 0) {
                throw IllegalStateException("Unbalanced brackets in: $inputString")
            }

            return stack.pop().also {
                if (i > tokenStart) {
                    val ruleName = inputString.substring(tokenStart, i).trim()
                    it.value = ruleName
                }
            }
        }
    }
}