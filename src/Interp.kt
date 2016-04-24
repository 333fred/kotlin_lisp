/**
 * Interpreter for my simple LISP
 */

fun interp(expr: SExpr, env: Environment): Value {
    return when (expr) {
        is SubExpr -> interpSubExpr(expr, env)
        is Atom -> interpAtom(expr, env)
        else -> throw RuntimeException("Encountered unknown SExpr $expr")
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
    // call, but interp'ing the first arg didn't return a function, so we throw an exception. Otherwise, we just
    // return the found value
    return when (firstExpr) {
        is BuiltinV -> firstExpr.action(rawArgs, env)
        is ClosV -> {
            if (rawArgs.size != firstExpr.args.size)
                throw RuntimeException("Mismatched arg lengths. Given $rawArgs, expected ${firstExpr.args}")
            val args = firstExpr.args.map { Atom(it.sym) }.zip(exprs.drop(1).map { interp(it, env) })
            interp(firstExpr.body, env.extendEnv(args))
        }
        else -> {
            if (rawArgs.isNotEmpty()) {
                throw RuntimeException("Gave args to $firstExpr. Args were $rawArgs.\n$env")
            }

            firstExpr
        }
    }
}

fun interpAtom(expr: Atom, env: Environment): Value {
    var finalVal: Value? = null
    var curAtom = expr
    var continueSearching = true

    do {
        val curVal = env.lookup(curAtom) ?: expr.convertAtom()

        when (curVal) {
            is BoolV -> curAtom = Atom(curVal.bool.toString())
            is NumV -> curAtom = Atom(curVal.num.toString())
            is SymV -> curAtom = Atom(curVal.sym)
            else -> continueSearching = false
        }

        if (curVal != null) finalVal = curVal
    } while (continueSearching)

    return finalVal ?: throw RuntimeException("$expr is unbound in the environment!\n$env")
}

