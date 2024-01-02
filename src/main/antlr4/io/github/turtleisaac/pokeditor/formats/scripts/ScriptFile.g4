grammar ScriptFile;

/*
 * Parser Rules
 */

script_file : (label_definition | command | action_definition | action_command)* EOF;
label_definition : WHITESPACE*? script_definition? label WHITESPACE*? ':' WHITESPACE*? NEWLINE;
script_definition : 'script(' NUMBER ')' WHITESPACE+ ;
action_definition :  WHITESPACE*? action WHITESPACE*? ':' WHITESPACE*? NEWLINE;
label : LABEL ;
action: ACTION_LABEL ;
command : WHITESPACE*? NAME WHITESPACE*? parameters (NEWLINE);
action_command : WHITESPACE*? 'Action' WHITESPACE*? action_parameters NEWLINE;
parameters : (parameter WHITESPACE*?)* ;
parameter : ((NUMBER | NAME | label | action | OVERWORLD) WHITESPACE*?) ;

action_parameters : (action_parameter WHITESPACE*?) (action_parameter WHITESPACE*?);
action_parameter : ((NUMBER | NAME ) WHITESPACE*?) ;

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
ACTION_LABEL : 'action_' (LETTER | NUMBER | '_')+;

NAME : (LETTER | NUMBER | '_')+ ;

OVERWORLD : 'Overworld.' (NUMBER | NAME) ;

//CURRENT_OFFSET : '.' ;

fragment COMMENT_CHARACTER : ';' ;

COMMENT : WHITESPACE*? COMMENT_CHARACTER WHITESPACE*? .*? NEWLINE -> skip ;
WHATEVER : '\'' -> skip ;