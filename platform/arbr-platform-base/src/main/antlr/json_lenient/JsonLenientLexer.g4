lexer grammar JsonLenientLexer;

channels { COMMENT }

OpenBracket:                    '[';
CloseBracket:                   ']';
OpenParen:                      '(';
CloseParen:                     ')';
OpenBrace:                      '{';
CloseBrace:                     '}';
Comma:                          ',';
Colon:                          ':';
TRUE: 'true';
FALSE: 'false';
NULL: 'null';

STRING
    : '"' (ESC | SAFECODEPOINT)* '"'
    ;

NUMBER
    : '-'? INT ('.' [0-9]+)? EXP?
    ;

MultiLineComment:               '/*' .*? '*/' -> channel(COMMENT);
SingleLineComment:              '//' ~[\r\n\u2028\u2029]* -> channel(COMMENT);

fragment ESC
    : '\\' (["\\/bfnrt] | UNICODE)
    ;

fragment UNICODE
    : 'u' HEX HEX HEX HEX
    ;

fragment HEX
    : [0-9a-fA-F]
    ;

fragment SAFECODEPOINT
    : ~ ["\\\u0000-\u001F]
    ;

fragment INT
    // integer part forbids leading 0s (e.g. `01`)
    : '0'
    | [1-9] [0-9]*
    ;

// no leading zeros

fragment EXP
    // exponent number permits leading 0s (e.g. `1e01`)
    : [Ee] [+\-]? [0-9]+
    ;

// \- since - means "range" inside [...]

WS
    : [ \t\n\r]+ -> channel(HIDDEN)
    ;
