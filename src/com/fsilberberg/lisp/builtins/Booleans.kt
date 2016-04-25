package com.fsilberberg.lisp.builtins

import com.fsilberberg.lisp.*

/**
 * Built-in functions that operate on boolean types
 */

val equalPair = createBoolOp("=", { arg1, arg2 -> BoolV(arg1 == arg2) })
val greaterThanPair = createComparisonOp(">", { a, b -> a > b })
val greaterThanEqualPair = createComparisonOp(">=", { a, b -> a >= b })
val lessThanPair = createComparisonOp("<", { a, b -> a < b })
val lessThanEqualPair = createComparisonOp("<=", { a, b -> a <= b })
val andPair = createBoolOp("&", { arg1, arg2 ->
    BoolV(contextCast<BoolV>(arg1, "&").bool && contextCast<BoolV>(arg2, "&").bool)
})
val orPair = createBoolOp("|", { arg1, arg2 ->
    BoolV(contextCast<BoolV>(arg1, "|").bool || contextCast<BoolV>(arg2, "|").bool)
})
val notPair = defineFunPair("!", ::notBuiltIn)
fun notBuiltIn(els: List<SExpr>, env: Environment): Value {
    if (els.size != 1) {
        throw RuntimeException("! expects 1 argument! Given $els.")
    }

    return BoolV(!contextCast<BoolV>(interp(els.first(), env), "!").bool)
}

/**
 * Generates an operation for comparing between two numbers.
 */
fun createComparisonOp(op: String, compare: (Double, Double) -> Boolean): Pair<Atom, Value> {
    return createBoolOp(op, { arg1, arg2 ->
        BoolV(compare(contextCast<NumV>(arg1, op).num, contextCast<NumV>(arg2, op).num))
    })
}

/**
 * Generates BuiltinV closures with a given action, to generalize code.
 */
fun createBoolOp(funName: String, action: (Value, Value) -> Value): Pair<Atom, Value> {
    return Pair(Atom(funName), BuiltinV({ els, env ->
        if (els.size != 2) {
            throw RuntimeException("$funName takes 2 arguments! Given $els.")
        }

        val arg1 = interp(els.component1(), env)
        val arg2 = interp(els.component2(), env)

        action(arg1, arg2)
    }, funName));
}

