package com.fsilberberg.lisp.builtins

import com.fsilberberg.lisp.*

fun defineFunPair(name: String, func: (List<SExpr>, Environment) -> Value): Pair<Atom, Value> = Pair(Atom(name), BuiltinV(func, name))

val builtIns = listOf(
        funPair, letPair, ifPair, // Basic constructs
        plusPair, subPair, multPair, divPair, // Math
        strFirstPair, strRestPair, // String manipulation
        equalPair, notPair, // Basic boolean
        andPair, orPair, // Conjunction operations
        greaterThanPair, greaterThanEqualPair, lessThanPair, lessThanEqualPair // Number Comparisons
)