grammar DataModel;
route
    : link+
    ;

op
    : TILDE
    | DASH
    ;

link
    : node op node
    | link op node
    | node
    ;

node
    : pkg SC entity DOT attr
//    | attr op attr
    ;

attr
    : ID
    ;

pkg
    : ID
    | DQM
    ;

pkg_name
    : ID
    | DQM
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
SC : ':';
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