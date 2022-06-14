grammar Proto3;

@header { package io.github.timortel.kotlin_multiplatform_grpc_plugin.anltr; }

file : syntax_def (WS? (option | message | service | proto_package | proto_import | COMMENT_OR_WS | proto_enum))* WS?;

proto_package : 'package' COMMENT_OR_WS? pkgName=EXPRESSION_NAME COMMENT_OR_WS? ';';

proto_import : 'import' (WS? 'public')? COMMENT_OR_WS? VALUE_STRING COMMENT_OR_WS? ';';

syntax_def : 'syntax' COMMENT_OR_WS? '=' COMMENT_OR_WS? VALUE_STRING COMMENT_OR_WS? ';';

option : 'option' COMMENT_OR_WS? optionName=EXPRESSION_NAME COMMENT_OR_WS? '=' COMMENT_OR_WS? (optionValueString=VALUE_STRING | optionValueExpression=EXPRESSION_NAME) COMMENT_OR_WS? ';';

message : 'message' COMMENT_OR_WS? messageName=EXPRESSION_NAME COMMENT_OR_WS? '{' (COMMENT_OR_WS? (message_attribute | one_of | proto_enum | message | map))* COMMENT_OR_WS? '}';

message_attribute : repeated='repeated'? COMMENT_OR_WS? type=EXPRESSION_NAME COMMENT_OR_WS? name=EXPRESSION_NAME COMMENT_OR_WS? '=' COMMENT_OR_WS? num=NUM COMMENT_OR_WS? ';';

one_of : 'oneof' WS? one_of_name=EXPRESSION_NAME WS? '{' (COMMENT_OR_WS? message_attribute)* COMMENT_OR_WS? '}';

service : 'service' COMMENT_OR_WS? serviceName=EXPRESSION_NAME COMMENT_OR_WS? '{' (COMMENT_OR_WS? rpc)* COMMENT_OR_WS? '}';

rpc : 'rpc' COMMENT_OR_WS? rpcName=EXPRESSION_NAME COMMENT_OR_WS? '(' COMMENT_OR_WS? request=EXPRESSION_NAME COMMENT_OR_WS? ')' COMMENT_OR_WS? 'returns' COMMENT_OR_WS? '(' COMMENT_OR_WS? stream='stream'? COMMENT_OR_WS? response=EXPRESSION_NAME COMMENT_OR_WS? ')' COMMENT_OR_WS? ';';

proto_enum : 'enum' WS? enumName=EXPRESSION_NAME WS? '{' WS? (COMMENT_OR_WS? enum_field)+ COMMENT_OR_WS? '}';

enum_field : name=EXPRESSION_NAME WS? '=' WS? num=NUM WS? ';';

map : 'map' WS? '<' WS? key_type=EXPRESSION_NAME WS? ',' WS? value_type=EXPRESSION_NAME WS? '>' WS? name=EXPRESSION_NAME WS? '=' WS? num=NUM COMMENT_OR_WS? ';';

COMMENT_OR_WS : (COMMENT | LINE_COMMENT | WS) -> skip;

COMMENT : '/*' .*? '*/' -> skip;

LINE_COMMENT: '//' ~[\r\n]* -> skip;

NUM : [0-9]+;
EXPRESSION_NAME : ([a-zA-Z0-9_.])+;

WS_NO_NEWLINE : (' ' | '\t')+ -> skip;
WS : (' ' | '\t' | '\n')+ -> skip;
VALUE_STRING : '"' EXPRESSION_NAME? '"';