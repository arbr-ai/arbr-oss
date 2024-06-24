package com.arbr.content_formats.code

import org.slf4j.LoggerFactory

object CodeBlockParser {

    private val logger = LoggerFactory.getLogger(CodeBlockParser::class.java)

    private val blockRe = Regex("^```([a-zA-Z0-9\\-_.#]+)?$")

    class CodeBlockNotViableException : Exception()

    private data class MutableBlock(
        val tag: String?,
        val lines: MutableList<String>,
    )

    data class Block(
        val tag: String?,
        val lines: List<String>,
    )

    private class CodeBlockParserHelper {
        private val parsedCodeBlocks: MutableList<MutableBlock> = mutableListOf()
        private val blockStack: MutableList<MutableBlock> = mutableListOf()

        private fun newBlock(tag: String?): MutableBlock {
            return MutableBlock(tag, mutableListOf())
        }

        private fun currentBlock(): MutableBlock? {
            return blockStack.lastOrNull()
        }

        private fun parse(lines: List<String>, fromIdx: Int): Int {
            var i = fromIdx
            while (i < lines.size) {
                val line = lines[i]
                val m = blockRe.matchEntire(line.trim())
                val currentBlock = currentBlock()
                if (m != null) {
                    val tag = m.groupValues[1].takeIf { it.isNotEmpty() }

                    if (currentBlock != null) {
                        if (tag != null) {
                            logger.debug("Warning: nested code block with tag; interpreting as potentially nested")
                            try {
                                val newBlock = newBlock(tag)
                                parsedCodeBlocks.add(newBlock)
                                blockStack.add(newBlock)

                                i = parse(lines, i + 1)
                            } catch (e: CodeBlockNotViableException) {
                                logger.debug("Warning: unable to interpret nested code block with tag; interpreting as plain text")
                                currentBlock.lines.add(line.trimEnd())
                            }
                        } else {
                            val wasNested = blockStack.size > 1
                            blockStack.removeAt(blockStack.size - 1)
                            if (wasNested) {
                                return i
                            }
                        }
                    } else {
                        val newBlock = newBlock(tag)
                        parsedCodeBlocks.add(newBlock)
                        blockStack.add(newBlock)
                    }
                } else {
                    currentBlock?.lines?.add(line.trimEnd())
                }

                i++
            }

            if (blockStack.isNotEmpty()) {
                throw CodeBlockNotViableException()
            }

            return i
        }

        fun parse(text: String): List<MutableBlock> {
            val lines = text.split("\n")
            try {
                parse(lines, 0)
            } catch (e: CodeBlockNotViableException) {
                logger.debug("Warning: unable to interpret top-level code block")
            }
            return parsedCodeBlocks
        }
    }

    fun parse(text: String): List<Block> {
        return CodeBlockParserHelper().parse(text).map { mutableBlock ->
            Block(mutableBlock.tag, mutableBlock.lines)
        }
    }

}
