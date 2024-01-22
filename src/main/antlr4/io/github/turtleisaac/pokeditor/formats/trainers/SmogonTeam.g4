grammar SmogonTeam;

/*
 * Parser Rules
 */

team: (speciesEntry)+ EOF?;

move: '-' WHITESPACE+ nameWithSpace WHITESPACE*? NEWLINE? ;

speciesEntry : NEWLINE*? species ability? level? shiny? effortValues? nature? individualValues? move move? move? move? NEWLINE*? ;

species: NEWLINE NAME WHITESPACE+? item? WHITESPACE*? NEWLINE ;
item: '@' WHITESPACE+ nameWithSpace ;
nameWithSpace : (NAME | WHITESPACE)*? NAME ;
ability: 'Ability:' WHITESPACE+ nameWithSpace WHITESPACE*? NEWLINE ;
level: 'Level:' WHITESPACE+ NUMBER WHITESPACE*? NEWLINE;
shiny: 'Shiny:' WHITESPACE+ YES_NO WHITESPACE*? NEWLINE;
individualValues: 'IVs:' (WHITESPACE+ effortValueEntry WHITESPACE+? '/')*? WHITESPACE effortValueEntry WHITESPACE*? NEWLINE ;
effortValues: 'EVs:' (WHITESPACE+ effortValueEntry WHITESPACE+? '/')*? WHITESPACE effortValueEntry WHITESPACE*? NEWLINE ;
effortValueEntry: NUMBER WHITESPACE+ STAT ;
nature: NAME WHITESPACE+ 'Nature' WHITESPACE*? NEWLINE ;



/*
 * Lexer Rules
 */

fragment DIGIT : [0-9] ;
fragment HEX_DIGIT : (DIGIT | [a-fA-F]) ;
fragment LETTER : [a-zA-Z] ;

NUMBER : (DIGIT+ | '0x' HEX_DIGIT)+ ;

WHITESPACE : (' ' | '\t') ;
NEWLINE : ('\r'? '\n' | '\r')+ ;

STAT : (HP | ATTACK | DEFENSE | SPEED | SPECIAL_ATTACK | SPECIAL_DEFENSE);

fragment YES : 'Yes' ;
fragment NO : 'No' ;

YES_NO : (YES | NO) ;

NAME : (LETTER | NUMBER | '_')+ ;

//NAME_WITH_SPACE : (LETTER | NUMBER | '_' )+ (LETTER | NUMBER | '_' | WHITESPACE)*? (LETTER | NUMBER | '_')+?  ;

fragment HP : 'HP' ;
fragment ATTACK : 'Atk' ;
fragment DEFENSE : 'Def' ;
fragment SPEED : 'Spe' ;
fragment SPECIAL_ATTACK : 'SpA' ;
fragment SPECIAL_DEFENSE : 'SpD' ;



fragment COMMENT_CHARACTER : ';' ;

COMMENT : WHITESPACE*? COMMENT_CHARACTER WHITESPACE*? .*? NEWLINE -> skip ;
WHATEVER : '\'' -> skip ;