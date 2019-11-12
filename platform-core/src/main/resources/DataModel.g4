grammar DataModel;

route
    : link fetch
    | entity fetch
    ;

link
    : entity by bwd_node
    | fwd_node to entity
    | link fetch to entity
    | link by bwd_node
    ;
fetch
    : DOT attr
    ;
to
    : DASH
    ;
by
    : TILDE
    ;
fwd_node
    : entity DOT attr
    ;
bwd_node
    : LP attr RP entity
    ;
entity
    : pkg SC ety
    ;
pkg
    : ID
    | DQM
    ;
ety
    : ID
    ;
attr
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
fragment Letter: [a-zA-Z!@#$%^&*_];
fragment Digit: [0-9];
fragment LetterOrDigit: Letter | Digit;
WS : [ \t\r\n]+ -> skip; // skip spaces, tabs, newlines