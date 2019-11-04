grammar DataModelv3;
prog
    : entity+ last_op ID
    ;

entity
    : entity attr op entity
    | pkg ID
    ;

last_op
    : DOT
    ;
op
    : TILDE
    | DASH
    ;

pkg
    : ID DC
    | DQM DC
    ;

attr
    : LP ID RP
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