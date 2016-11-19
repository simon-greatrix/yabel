# YABEL Explained

## Basics

Write Java operation code mnemonics and have them compiled to byte code. A
map of named properties is used to dynamically construct larger blocks of code
from boilerplate.

The input takes the form of whitespace separated operation codes, with
parameters for operation codes separated by colons. For example, the copy
construction `java.lang.String(String)` could be represented like this:

```
aload:0
invokespecial:Object.<init>
aload:0
aload:1
getfield:value
putfield:value
aload:0
aload:1
getfield:hash
putfield:hash
return
```


## Template engine 
A general purpose template engine, such as Apache Velocity or FreeMarker can
be used to prepare the input.

## Constants


## Variables


## Labels

