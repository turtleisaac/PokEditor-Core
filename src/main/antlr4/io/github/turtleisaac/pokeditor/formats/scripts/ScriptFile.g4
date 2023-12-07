grammar ScriptFile;

/*
 * Parser Rules
 */

script_file : (label_definition | command)* EOF;
label_definition : WHITESPACE*? script_definition? label WHITESPACE*? ':' WHITESPACE*? NEWLINE;
script_definition : 'script(' NUMBER ')' WHITESPACE+ ;
label : LABEL ;
command : WHITESPACE*? NAME WHITESPACE*? parameters (NEWLINE);
parameters : (parameter WHITESPACE*?)* ;
parameter : ((NAME | NUMBER | label) WHITESPACE*?) ;

/*
 * Lexer Rules
 */

fragment DIGIT : [0-9] ;
fragment HEX_DIGIT : (DIGIT | [a-fA-F]) ;
fragment LETTER : [a-zA-Z] ;

NUMBER : (DIGIT+ | '0x' HEX_DIGIT)+ ;

WHITESPACE : (' ' | '\t') ;
NEWLINE : ('\r'? '\n' | '\r')+ ;

LABEL : 'label_' (LETTER | NUMBER | '_')+;

NAME : (LETTER | NUMBER | '_')+ ;

CURRENT_OFFSET : '.' ;

fragment COMMENT_CHARACTER : ';' ;

COMMENT : WHITESPACE*? COMMENT_CHARACTER WHITESPACE*? .*? NEWLINE -> skip ;
WHATEVER : '\'' -> skip ;