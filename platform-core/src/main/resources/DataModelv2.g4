grammar DataModelv2;
prog
    : path+ op pkg entity last_op ID
    ;



last_op
    : DOT
    ;
op
    : TILDE
    | DASH
    ;

path
    : attr op attr
    | path op attr
    ;

attr
    : LP ID RP
    | pkg entity attr
//    | attr op attr
    ;

pkg
    : ID DC
    | DQM DC
    ;

entity
    : ID
    ;


TILDE : '~';
DASH : '-';
DOT : '.';
LP : '(';
RP : ')';
DC : ':'':';
DQM : '"''"';
//LSB : '[';
//RSB : ']';
//MEMBER: LSB ID RSB;

ID  : Letter LetterOrDigit*;
PKG_ID : ID;
fragment Letter: [a-zA-Z_];
fragment Digit: [0-9];
fragment LetterOrDigit: Letter | Digit;
WS : [ \t\r\n]+ -> skip; // skip spaces, tabs, newlines