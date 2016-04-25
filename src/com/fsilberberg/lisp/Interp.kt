package com.fsilberberg.lisp

/**
 * Interpreter for my simple LISP
 */

fun interp(expr: SExpr, env: Environment): Value {
    return when (expr) {
        is SubExpr -> interpSubExpr(expr, env)
        is Atom -> interpAtom(expr, env)
        else -> throw RuntimeException("Encountered unknown com.fsilberberg.lisp.SExpr $expr")
    }
}

fun interpSubExpr(expr: SubExpr, env: Environment): Value {
    // First, we interpret the first element of the expression, and get some result from that
    // TODO: This currently will error if the expression is ()
    val exprs = expr.exprs
    val rawArgs = exprs.drop(1)
    val firstExpr = interp(exprs.first(), env)

    // Next, we attempt to do something based on the return type. For built-ins, we call the function with the
    // current environment and expressions. For closures, we bind the arguments and call the closure
    // For everything else, we make sure that there are no other args. If there are, it's supposed to be a function
    // call, but com.fsilberberg.lisp.interp'ing the first arg didn't return a function, so we throw an exception. Otherwise, we just
    // return the found value
    return when (firstExpr) {
        is BuiltinV -> firstExpr.action(rawArgs, env)
        is ClosV -> interpClosure(firstExpr, rawArgs, env)
        else -> {
            if (rawArgs.isNotEmpty()) {
                throw RuntimeException("Gave args to $firstExpr. Args were $rawArgs.\n$env")
            }

            firstExpr
        }
    }
}

fun interpClosure(closV: ClosV, rawArgs: List<SExpr>, env: Environment): Value {
    if (rawArgs.size != closV.args.size)
        throw RuntimeException("Mismatched arg lengths. Given $rawArgs, expected ${closV.args}")
    val args = closV.args.map { Atom(it.sym) }.zip(rawArgs.map { interp(it, env) })
    return interp(closV.body, env.extendEnv(args))
}

fun interpAtom(expr: Atom, env: Environment): Value {
    var finalVal: Value? = env.lookup(expr) ?: expr.convertAtom()
    var curVal = finalVal
    var curAtom = expr
    var continueLoop = true

    while (curVal != null && continueLoop) {
        curVal = env.lookup(curAtom)
        when (curVal) {
        // If curVal = finalVal, we're in a loop, so exit
            finalVal -> continueLoop = false
            is BoolV -> curAtom = Atom(curVal.bool.toString())
            is NumV -> curAtom = Atom(curVal.num.toString())
            is SymV -> curAtom = Atom(curVal.sym)
            else -> continueLoop = false
        }

        if (curVal != null) finalVal = curVal
    }

    return finalVal ?: throw RuntimeException("$expr is unbound in the environment!\n$env")
}

