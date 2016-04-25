package com.fsilberberg.lisp.builtins

import com.fsilberberg.lisp.*

/**
 * Built-ins that have to do with string manipulation
 */

/**
 * Gets the first character in a string, returning the empty string if the string is empty. If the value given to
 * str-first is a character or boolean, it will be converted to a string first.
 */
val strFirstPair = createStrFun("str-first", { StringV("${it.firstOrNull() ?: ""}") })
val strRestPair = createStrFun("str-rest", { StringV("${if (it == "") "" else it.drop(1)}") })

fun createStrFun(funName: String, action: (String) -> Value): Pair<Atom, Value> {
    return defineFunPair(funName, { els, env ->
        if (els.size != 1) {
            throw RuntimeException("$funName takes 1 argument! Given $els.")
        }

        val interped = interp(els.first(), env)
        val str = when (interped) {
            is StringV -> interped.str
            is ClosV -> throw RuntimeException("$funName cannot take the first letter of a closure! Given $els")
            is BuiltinV -> throw RuntimeException("$funName cannot take the first letter of a closure! Given $els")
            else -> interped.argString()
        }

        action(str)
    })
}
