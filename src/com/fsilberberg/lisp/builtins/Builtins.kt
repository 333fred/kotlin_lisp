package com.fsilberberg.lisp.builtins

import com.fsilberberg.lisp.*

fun defineFunPair(name: String, func: (List<SExpr>, Environment) -> Value): Pair<Atom, Value> = Pair(Atom(name), BuiltinV(func, name))

val builtIns = listOf(funPair, letPair, plusPair, subPair, multPair, divPair)