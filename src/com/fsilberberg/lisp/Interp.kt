package com.fsilberberg.lisp

import java.util.*

/**
 * Interpreter for my simple LISP
 */

fun interp(expr: SExpr, env: Environment, errorOnUnbound: Boolean = true): Value {
    return when (expr) {
        is SubExpr -> interpSubExpr(expr, env, errorOnUnbound)
        is Atom -> interpAtom(expr, env, errorOnUnbound)
        else -> throw RuntimeException("Encountered unknown com.fsilberberg.lisp.SExpr $expr")
    }
}

fun interpSubExpr(expr: SubExpr, env: Environment, errorOnUnbound: Boolean = true): Value {
    // First, we interpret the first element of the expression, and get some result from that
    // TODO: This currently will error if the expression is ()
    val exprs = expr.exprs
    val rawArgs = exprs.drop(1)
    val firstExpr = interp(exprs.first(), env, errorOnUnbound)

    // Next, we attempt to do something based on the return type. For built-ins, we call the function with the
    // current environment and expressions. For closures, we bind the arguments and call the closure
    // For everything else, we make sure that there are no other args. If there are, it's supposed to be a function
    // call, but interp'ing the first arg didn't return a function, so we throw an exception. Otherwise, we just
    // return the found value. In all cases that return a value, if that value is of atom type, it must be converted
    // back to an atom and reinterped, in case that atom has been redefined in the environment
    return reinterpIfNecessary(when (firstExpr) {
        is BuiltinV -> firstExpr.action(rawArgs, env)
        is ClosV -> interpClosure(firstExpr, rawArgs, env, errorOnUnbound)
        else -> {
            if (rawArgs.isNotEmpty()) {
                throw RuntimeException("Gave args to $firstExpr. Args were $rawArgs.\n$env")
            }

            firstExpr
        }
    }, env)
}

fun interpClosure(closV: ClosV, rawArgs: List<SExpr>, env: Environment, errorOnUnbound: Boolean = true): Value {
    // Ensure that the number of args are correct
    if ((closV.arg == null && rawArgs.size != 0) ||
            (closV.body is LazyV && rawArgs.size != 1)) {
        throw RuntimeException("Incorrect number of arguments provided to function! Provided $rawArgs.\n$closV")
    }

    val arg = if (rawArgs.size > 1) {
        listOf(Pair(Atom(closV.arg?.str ?: ""), interp(rawArgs.first(), env, errorOnUnbound)))
    } else {
        ArrayList<Pair<Atom, Value>>()
    }

    return reinterpIfNecessary(
            when (closV.body) {
                is ClosV -> interpClosure(closV.body, rawArgs.drop(1), closV.env.extendEnv(arg), errorOnUnbound)
                is LazyV -> interp(closV.body.body, closV.env.extendEnv(arg), errorOnUnbound)
                else -> throw RuntimeException("Unknown value as the body of a closure! Received ${closV.body}")
            }, env)
}

fun reinterpIfNecessary(value: Value, env: Environment): Value {
    return when (value) {
        is ClosV -> value
        else -> interp(Atom(value.argString()), env)
    }
}

fun interpAtom(expr: Atom, env: Environment, errorOnUnbound: Boolean = true): Value {
    var finalVal: Value? = null
    var previousAtom: Atom
    var curAtom = expr
    var continueLoop = true

    do {
        previousAtom = curAtom
        val curVal = env.lookup(curAtom) ?: curAtom.convertAtom() ?: break
        when (curVal) {
            is ClosV -> continueLoop = false
            else -> curAtom = Atom(curVal.argString())
        }

        finalVal = curVal
    } while (previousAtom != curAtom && continueLoop)

    return finalVal ?: if (errorOnUnbound)
        throw RuntimeException("$expr is unbound in the environment!\n$env")
    else
        SymV(curAtom.expr)
}

