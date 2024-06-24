package com.arbr.content_formats.format.tokenizer

fun interface TokenFormatter<TokenType> {

    fun formatToken(index: Int, token: TokenType): TokenType

    companion object {
        fun <T> plain() = TokenFormatter<T> { _, t -> t }
    }
}