grammar SmogonTeam;

/*
 * Parser Rules
 */

team: (speciesEntry)+ EOF;

speciesEntry : NEWLINE*? species (ability | effortValues | nature)+ move move? move? move? NEWLINE+ ;

species: NAME WHITESPACE+ '@' WHITESPACE+ (NAME | WHITESPACE)+ NEWLINE ;
ability: 'Ability:' WHITESPACE+ NAME NEWLINE ;
effortValues: 'EVs:' (WHITESPACE+ effortValueEntry WHITESPACE+? '/')*? WHITESPACE effortValueEntry NEWLINE ;
effortValueEntry: NUMBER WHITESPACE+ STAT ;
nature: NAME WHITESPACE+ 'Nature' NEWLINE ;
move: '-' WHITESPACE+ (NAME | WHITESPACE)+ NEWLINE? ;


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

NAME : (LETTER | NUMBER | '_')+ ;

fragment HP : 'Hp' ;
fragment ATTACK : 'Atk' ;
fragment DEFENSE : 'Def' ;
fragment SPEED : 'Spe' ;
fragment SPECIAL_ATTACK : 'SpA' ;
fragment SPECIAL_DEFENSE : 'SpD' ;



fragment COMMENT_CHARACTER : ';' ;

COMMENT : WHITESPACE*? COMMENT_CHARACTER WHITESPACE*? .*? NEWLINE -> skip ;
WHATEVER : '\'' -> skip ;