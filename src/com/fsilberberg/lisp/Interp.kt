package com.fsilberberg.lisp

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
    // return the found value
    return when (firstExpr) {
        is BuiltinV -> firstExpr.action(rawArgs, env)
        is ClosV -> interpClosure(firstExpr, rawArgs, env, errorOnUnbound)
        else -> {
            if (rawArgs.isNotEmpty()) {
                throw RuntimeException("Gave args to $firstExpr. Args were $rawArgs.\n$env")
            }

            firstExpr
        }
    }
}

fun interpClosure(closV: ClosV, rawArgs: List<SExpr>, env: Environment, errorOnUnbound: Boolean = true): Value {
    if (rawArgs.size != closV.args.size)
        throw RuntimeException("Mismatched arg lengths. Given $rawArgs, expected ${closV.args}")
    val args = closV.args.map { Atom(it.str) }.zip(rawArgs.map { interp(it, env, errorOnUnbound) })
    return interp(closV.body, env.extendEnv(args), errorOnUnbound)
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

