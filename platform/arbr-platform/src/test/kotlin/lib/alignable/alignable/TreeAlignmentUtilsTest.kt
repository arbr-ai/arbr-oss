package com.arbr.alignable.alignable

//class TreeAlignmentUtilsTest {
//
//    @Test
//    fun alignTrees() {
//        val tree0 = Tree(
//            "hello",
//            listOf(
//                Tree(
//                    "cat",
//                    emptyList(),
//                ),
//                Tree(
//                    "dog",
//                    emptyList(),
//                ),
//            )
//        )
//
//        val tree1 = Tree(
//            "hello",
//            listOf(
//                Tree(
//                    "frog",
//                    listOf(
//                        Tree("log", emptyList())
//                    ),
//                ),
//            )
//        )
//
//        val treeAlignment = TreeAlignmentUtils.alignTrees(tree0, tree1)
//        println(treeAlignment)
//        println(jacksonObjectMapper().writeValueAsString(treeAlignment))
//
//        val listener = object : TreeAlignmentListener<String>() {
//            private val stack = Stack<String>()
//
//            override fun didEnter(tree: Tree<String>, atIndex: Int) {
//                stack.push(tree.element)
//                println("Did enter tree ${tree.element} at $atIndex")
//                println(stack.toArray().toList())
//                println()
//            }
//
//            override fun didAddSubtree(subtree: Tree<String>, atIndex: Int) {
//                println("Did add subtree ${subtree.element} at $atIndex")
//            }
//
//            override fun didRemoveSubtree(subtree: Tree<String>, atIndex: Int) {
//                println("Did remove subtree ${subtree.element} at $atIndex")
//            }
//
//            override fun didExit(tree: Tree<String>, atIndex: Int) {
//                stack.pop()
//                println("Did exit tree ${tree.element} at $atIndex")
//                println(stack.toArray().toList())
//                println()
//            }
//        }
//
//        val alignedTree = TreeAlignmentUtils.applyAlignment(tree0, treeAlignment, listener)
//        Assertions.assertEquals(1, alignedTree.size)
//        Assertions.assertEquals(tree1, alignedTree[0])
//    }
//}
