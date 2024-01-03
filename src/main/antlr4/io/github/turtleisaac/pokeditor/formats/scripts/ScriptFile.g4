grammar ScriptFile;

/*
 * Parser Rules
 */

script_file : (label_definition | command | action_definition | action_command | valid_table )* EOF;
label_definition : WHITESPACE*? script_definition? label WHITESPACE*? ':' WHITESPACE*? NEWLINE;
script_definition : 'script(' NUMBER ')' WHITESPACE+ ;
action_definition :  WHITESPACE*? action WHITESPACE*? ':' WHITESPACE*? NEWLINE;
table_definition : WHITESPACE*? table WHITESPACE*? ':' WHITESPACE*? NEWLINE;
label : LABEL ;
action: ACTION_LABEL ;
table: TABLE_LABEL ;
end_table : END_TABLE;
command : WHITESPACE*? NAME WHITESPACE*? parameters (NEWLINE);
action_command : WHITESPACE*? 'Action' WHITESPACE*? action_parameters NEWLINE;
table_entry : WHITESPACE*? (NAME | NUMBER) NEWLINE;
parameters : (parameter WHITESPACE*?)* WHITESPACE*? NEWLINE;
parameter : ((NUMBER | NAME | label | action | table | OVERWORLD) WHITESPACE*?) ;

valid_table : table_definition table_entry* end_table NEWLINE ;

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
TABLE_LABEL : 'table_' (LETTER | NUMBER | '_')+;

END_TABLE : 'endTable' ;

NAME : (LETTER | NUMBER | '_')+ ;

OVERWORLD : 'Overworld.' (NUMBER | NAME) ;

fragment COMMENT_CHARACTER : ';' ;

COMMENT : WHITESPACE*? COMMENT_CHARACTER WHITESPACE*? .*? NEWLINE -> skip ;
WHATEVER : '\'' -> skip ;