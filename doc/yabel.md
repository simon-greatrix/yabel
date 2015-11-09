# The YABEL language

Input to YABEL consists of whitespace separated tokens. Each token starts with an instruction or marker indicating its type. Parameters to the token are supplied as a colon delimited list. For example, the Java statement `i+=35;` could be represented as `iinc:i:35`.

## Class Data

The `ClassData` class provides a map with String keys and any data type for the value. There are many places where a lookup to the current Class Data can be used. 

## Instructions

Any instruction can be entered directly.

`astore_1 iconst_0 istore_2 iload_2`

## Expansion

A block of code may be stored in Class Data and included in the output. The code block will be recursively expanded.

`{firstBoilerPlateCode}`

## Named Parameters

Any instruction that takes a parameter may use a value from the current Class Data instead of an explicit value.

`invokestatic:myMethod`


