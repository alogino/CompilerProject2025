lexer grammar DecaLexer;

options {
   language=Java;
   superClass = AbstractDecaLexer;
}

@members {
}

COMMENT : ( '//' .*? '\n'
          | '/*' .*? '*/'
          ) -> skip;


PRINTLN : 'println';
PRINT   : 'print';
CLASS   : 'class';
EXTENDS : 'extends';
IF      : 'if';
ELSE    : 'else';
ASM     : 'asm';
RETURN  : 'return';
WHILE   : 'while';

PROTECTED: 'protected';
INSTANCEOF: 'instanceof';
NEW: 'new';
READINT: 'readInt';
READFLOAT: 'readFloat';
PRINTX: 'printx';
PRINTLNX: 'printlnx';


OBRACE  : '{';
CBRACE  : '}';
OPARENT : '(';
CPARENT : ')';
SEMI    : ';';
COMMA   : ',';
EQUALS  : '=';
DOT     : '.';

AND: '&&';
OR: '||';

LT: '<';
GT: '>';
LEQ: '<=';
GEQ: '>=';
EQEQ: '==';
NEQ: '!=';

TRUE: 'true';
FALSE: 'false';

NULL: 'null';
THIS: 'this';

PLUS: '+';
MINUS: '-';
TIMES:'*';
SLASH:'/';
PERCENT: '%';
EXCLAM: '!';



fragment DIGIT: '0'..'9';
fragment POSITIVE_DIGIT : '1' .. '9';
INT: '0' | POSITIVE_DIGIT DIGIT*;

fragment NUM : DIGIT+;
fragment SIGN: '+' | '-' | ;
fragment EXP : ('E' | 'e') SIGN NUM;
fragment DEC : NUM '.' NUM;
fragment FLOATDEC : (DEC EXP?) ('F' | 'f')?;
fragment DIGITHEX : '0' .. '9' | 'A' .. 'F' | 'a' .. 'f';
fragment NUMHEX : DIGITHEX+;
fragment FLOATHEX: ('0x'|'0X') NUMHEX '.' NUMHEX ('P'|'p') SIGN NUM ('F' | 'f')?;
FLOAT : FLOATDEC | FLOATHEX {
    validateFloatLiteral(getText());
};

fragment LETTER : 'a'  ..  'z' | 'A' .. 'Z';
IDENT: (LETTER | '$' | '_') (LETTER | DIGIT | '$' | '_')*;


fragment STRING_CAR : ~('"'|'\\'|'\n');
STRING : '"' (STRING_CAR | '\\"' | '\\\\')*  '"';
MULTI_LINE_STRING : '"' (STRING_CAR | EOL |'\\"' | '\\\\')* '"';

fragment FILENAME : (LETTER | DIGIT | '.' | '-' | '_')+;
INCLUDE: '#include' (' ')* '"' FILENAME '"'{
   doInclude(getText());
};


// Ignore whitespaces and newlines
EOL : [ \t\r\n] -> skip;
