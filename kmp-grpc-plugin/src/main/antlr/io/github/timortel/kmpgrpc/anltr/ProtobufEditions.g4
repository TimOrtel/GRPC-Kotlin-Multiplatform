// SPDX-License-Identifier: Apache-2.0
/**
 * A Protocol Buffers 3 grammar
 *
 * Original source: https://developers.google.com/protocol-buffers/docs/reference/proto3-spec
 * Original source is published under Apache License 2.0.
 *
 * Changes from the source above:
 * - rewrite to antlr
 * - extract some group to rule.
 *
 * @author anatawa12
 *
 * Direct copy from https://github.com/antlr/grammars-v4/blob/25ad11e4ff672b1eca69d6eeff109ce11bbb663d/protobuf3/Protobuf3.g4
 * Changes from the source above:
 * - Updated to support Protobuf Editions (source: https://protobuf.dev/reference/protobuf/edition-2023-spec/)
 * @author Tim Ortel
 */

grammar ProtobufEditions;

@header { package io.github.timortel.kmpgrpc.anltr; }

proto
    : edition (importStatement | packageStatement | option | topLevelDef | emptyStatement_)* EOF
    ;

// Edition

edition
    : EDITION EQ STR_LIT SEMI
    ;

// Import Statement

importStatement
    : IMPORT (WEAK | PUBLIC | OPTION)? strLit SEMI
    ;

// Package

packageStatement
    : PACKAGE fullIdent SEMI
    ;

// Option

option
    : OPTION optionName EQ constant SEMI
    ;

optionName
    : ident
    | fullIdent
    | LP DOT? fullIdent RP
    ;

// Fields

fieldLabel
    : REPEATED
    ;

field
    : fieldLabel? type_ fieldName EQ fieldNumber (LB fieldOptions RB)? SEMI
    ;

fieldOptions
    : fieldOption (COMMA fieldOption)*
    ;

fieldOption
    : optionName EQ constant
    ;

fieldNumber
    : intLit
    ;

// Oneof and oneof field

oneof
    : ONEOF oneofName LC (option | oneofField | emptyStatement_)* RC
    ;

oneofField
    : type_ fieldName EQ fieldNumber (LB fieldOptions RB)? SEMI
    ;

// Map field

mapField
    : MAP LT keyType COMMA type_ GT mapName EQ fieldNumber (LB fieldOptions RB)? SEMI
    ;

keyType
    : INT32
    | INT64
    | UINT32
    | UINT64
    | SINT32
    | SINT64
    | FIXED32
    | FIXED64
    | SFIXED32
    | SFIXED64
    | BOOL
    | STRING
    ;

// field types

type_
    : DOUBLE
    | FLOAT
    | INT32
    | INT64
    | UINT32
    | UINT64
    | SINT32
    | SINT64
    | FIXED32
    | FIXED64
    | SFIXED32
    | SFIXED64
    | BOOL
    | STRING
    | BYTES
    | messageType
    | enumType
    ;

// Reserved

reserved
    : RESERVED (ranges | reservedFieldNames) SEMI
    ;

ranges
    : range_ (COMMA range_)*
    ;

range_
    : intLit (TO ( intLit | MAX))?
    ;

reservedFieldNames
    : strLit (COMMA strLit)*
    ;

// Extensions

extensions
    : EXTENSIONS ranges SEMI
    ;

// lexical

constant
    : fullIdent
    | (MINUS | PLUS)? intLit
    | ( MINUS | PLUS)? floatLit
    | strLit
    | boolLit
    | blockLit
    ;

// not specified in specification but used in tests
blockLit
    : LC (ident COLON constant)* RC
    ;

emptyStatement_
    : SEMI
    ;

// Top Level definitions

topLevelDef
    : messageDef
    | enumDef
    | extendDef
    | serviceDef
    ;

symbolVisibility
    : EXPORT
    | LOCAL
    ;

// enum

enumDef
    : (symbolVisibility)? ENUM enumName enumBody
    ;

enumBody
    : LC enumElement* RC
    ;

enumElement
    : option
    | enumField
    | reserved
    | reservedFieldNames
    | emptyStatement_
    ;

enumField
    : ident EQ (MINUS)? intLit enumValueOptions? SEMI
    ;

enumValueOptions
    : LB enumValueOption (COMMA enumValueOption)* RB
    ;

enumValueOption
    : optionName EQ constant
    ;

// message

messageDef
    : (symbolVisibility)? MESSAGE messageName messageBody
    ;

messageBody
    : LC messageElement* RC
    ;

messageElement
    : field
    | enumDef
    | messageDef
    | extendDef
    | extensions
    | option
    | oneof
    | mapField
    | reserved
    | emptyStatement_
    ;

// Extend definition

extendDef
    : EXTEND messageType LC (field | emptyStatement_)* RC
    ;

// service

serviceDef
    : SERVICE serviceName LC serviceElement* RC
    ;

serviceElement
    : option
    | rpc
    | emptyStatement_
    ;

rpc
    : RPC rpcName LP clientStream=STREAM? messageType RP RETURNS LP serverStream=STREAM? messageType RP (
        LC ( option | emptyStatement_)* RC
        | SEMI
    )
    ;

// Lexical elements

ident
    : IDENTIFIER
    | keywords
    ;

fullIdent
    : ident (DOT ident)*
    ;

messageName
    : ident
    ;

enumName
    : ident
    ;

fieldName
    : ident
    ;

oneofName
    : ident
    ;

mapName
    : ident
    ;

serviceName
    : ident
    ;

rpcName
    : ident
    ;

messageType
    : (DOT)? (ident DOT)* messageName
    ;

enumType
    : (DOT)? (ident DOT)* enumName
    ;

intLit
    : INT_LIT
    ;

strLit
    : STR_LIT
    ;

boolLit
    : BOOL_LIT
    ;

floatLit
    : FLOAT_LIT
    ;

// keywords
EDITION
    : 'edition'
    ;

IMPORT
    : 'import'
    ;

WEAK
    : 'weak'
    ;

EXPORT
    : 'export'
    ;

LOCAL
    : 'local'
    ;

PUBLIC
    : 'public'
    ;

PACKAGE
    : 'package'
    ;

OPTION
    : 'option'
    ;

OPTIONAL
    : 'optional'
    ;

REPEATED
    : 'repeated'
    ;

ONEOF
    : 'oneof'
    ;

MAP
    : 'map'
    ;

INT32
    : 'int32'
    ;

INT64
    : 'int64'
    ;

UINT32
    : 'uint32'
    ;

UINT64
    : 'uint64'
    ;

SINT32
    : 'sint32'
    ;

SINT64
    : 'sint64'
    ;

FIXED32
    : 'fixed32'
    ;

FIXED64
    : 'fixed64'
    ;

SFIXED32
    : 'sfixed32'
    ;

SFIXED64
    : 'sfixed64'
    ;

BOOL
    : 'bool'
    ;

STRING
    : 'string'
    ;

DOUBLE
    : 'double'
    ;

FLOAT
    : 'float'
    ;

BYTES
    : 'bytes'
    ;

RESERVED
    : 'reserved'
    ;

EXTENSIONS
    : 'extensions'
    ;

TO
    : 'to'
    ;

MAX
    : 'max'
    ;

ENUM
    : 'enum'
    ;

MESSAGE
    : 'message'
    ;

SERVICE
    : 'service'
    ;

EXTEND
    : 'extend'
    ;

RPC
    : 'rpc'
    ;

STREAM
    : 'stream'
    ;

RETURNS
    : 'returns'
    ;

GROUP
    : 'group'
    ;

// symbols

SEMI
    : ';'
    ;

EQ
    : '='
    ;

LP
    : '('
    ;

RP
    : ')'
    ;

LB
    : '['
    ;

RB
    : ']'
    ;

LC
    : '{'
    ;

RC
    : '}'
    ;

LT
    : '<'
    ;

GT
    : '>'
    ;

DOT
    : '.'
    ;

COMMA
    : ','
    ;

COLON
    : ':'
    ;

PLUS
    : '+'
    ;

MINUS
    : '-'
    ;

STR_LIT
    : ('\'' ( CHAR_VALUE)*? '\'')
    | ( '"' ( CHAR_VALUE)*? '"')
    ;

fragment CHAR_VALUE
    : HEX_ESCAPE
    | OCT_ESCAPE
    | CHAR_ESCAPE
    | ~[\u0000\n\\]
    ;

fragment HEX_ESCAPE
    : '\\' ('x' | 'X') HEX_DIGIT HEX_DIGIT
    ;

fragment OCT_ESCAPE
    : '\\' OCTAL_DIGIT OCTAL_DIGIT OCTAL_DIGIT
    ;

fragment CHAR_ESCAPE
    : '\\' ('a' | 'b' | 'f' | 'n' | 'r' | 't' | 'v' | '\\' | '\'' | '"')
    ;

BOOL_LIT
    : 'true'
    | 'false'
    ;

FLOAT_LIT
    : (DECIMALS DOT DECIMALS? EXPONENT? | DECIMALS EXPONENT | DOT DECIMALS EXPONENT?)
    | 'inf'
    | 'nan'
    ;

fragment EXPONENT
    : ('e' | 'E') (PLUS | MINUS)? DECIMALS
    ;

fragment DECIMALS
    : DECIMAL_DIGIT+
    ;

INT_LIT
    : DECIMAL_LIT
    | OCTAL_LIT
    | HEX_LIT
    ;

DECIMAL_LIT
    : ([1-9]) DECIMAL_DIGIT*
    ;

fragment OCTAL_LIT
    : '0' OCTAL_DIGIT*
    ;

fragment HEX_LIT
    : '0' ('x' | 'X') HEX_DIGIT+
    ;

IDENTIFIER
    : LETTER (LETTER | DECIMAL_DIGIT)*
    ;

LETTER
    : [A-Za-z_]
    ;

CAPITAL_LETTER
    : [A-Z]
    ;

DECIMAL_DIGIT
    : [0-9]
    ;

fragment OCTAL_DIGIT
    : [0-7]
    ;

fragment HEX_DIGIT
    : [0-9A-Fa-f]
    ;

// comments
WS
    : [ \t\r\n\u000C]+ -> skip
    ;

LINE_COMMENT
    : '//' ~[\r\n]* -> channel(HIDDEN)
    ;

COMMENT
    : '/*' .*? '*/' -> channel(HIDDEN)
    ;

keywords
    : EDITION
    | IMPORT
    | WEAK
    | EXPORT
    | LOCAL
    | PUBLIC
    | PACKAGE
    | OPTION
    | OPTIONAL
    | REPEATED
    | ONEOF
    | MAP
    | INT32
    | INT64
    | UINT32
    | UINT64
    | SINT32
    | SINT64
    | FIXED32
    | FIXED64
    | SFIXED32
    | SFIXED64
    | BOOL
    | STRING
    | DOUBLE
    | FLOAT
    | BYTES
    | RESERVED
    | EXTENSIONS
    | GROUP
    | TO
    | MAX
    | ENUM
    | MESSAGE
    | SERVICE
    | EXTEND
    | RPC
    | STREAM
    | RETURNS
    | BOOL_LIT
    ;