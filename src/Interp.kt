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
    return NumV(10.0)
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

