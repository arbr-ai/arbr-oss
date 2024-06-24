/** Taken from "The Definitive ANTLR 4 Reference" by Terence Parr */

// Derived from https://json.org

// $antlr-format alignTrailingComments true, columnLimit 150, minEmptyLines 1, maxEmptyLinesToKeep 1, reflowComments false, useTab false
// $antlr-format allowShortRulesOnASingleLine false, allowShortBlocksOnASingleLine true, alignSemicolons hanging, alignColons hanging

parser grammar JsonLenientParser;

options {
    tokenVocab=JsonLenientLexer;
}

json
    : value EOF
    ;

obj
    : OpenBrace (pair Comma?)* CloseBrace
    ;

pair
    : STRING Colon value
    ;

arr
    : OpenBracket value ((Comma | WS) value)* CloseBracket
    | OpenBracket CloseBracket
    ;

value
    : STRING
    | NUMBER
    | obj
    | arr
    | TRUE
    | FALSE
    | NULL
    ;
