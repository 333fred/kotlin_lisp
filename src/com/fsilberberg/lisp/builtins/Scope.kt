package com.fsilberberg.lisp.builtins

import com.fsilberberg.lisp.*
import java.util.*

/**
 * All built-in functions related to scope. This includes things like fun and let
 */

val funName = "fun"
val funPair = Pair(Atom(funName), BuiltinV(::funBuiltIn, funName))

/**
 * Contains the functions built in to the language, such as fun
 */
fun funBuiltIn(els: List<SExpr>, env: Environment): ClosV {
    if (els.size != 2) {
        throw RuntimeException("Incorrect arguments to fun! Syntax is (fun (args) body). Provided $els")
    }

    val sexpr = els.first()
    val argList = when (sexpr) {
        is SubExpr -> sexpr.exprs
        is Atom -> listOf(sexpr)
        else -> throw RuntimeException("Unknown sexpr! $sexpr")
    }

    // We generate a new binding for fun just in case the user evilly redefined fun on us. Users are evil
    var funSym = Atom(UUID.randomUUID().toString())
    while (env.lookup(funSym) != null) {
        funSym = Atom(UUID.randomUUID().toString())
    }
    val funEnv = env.extendEnv(listOf(Pair(funSym, BuiltinV(::funBuiltIn, "fun"))))

    return if (argList.size == 0) {
        ClosV(ArrayList(), els.component2(), env)
    } else {
        val firstArg = interp(argList.first(), env, false)
        ClosV(when (firstArg) {
            is ClosV -> throw RuntimeException("Cannot use a ClosV as a function parameter! Given $firstArg")
            else -> listOf(SymV(firstArg.argString()))
        }, if (argList.size == 1) {
            els.component2()
        } else {
            // Use our fun replacement to ensure it works
            SubExpr(listOf(funSym, SubExpr(argList.drop(1)), els.component2()))
        }, if (argList.size > 1) funEnv else env)
    }
}

val letName = "let"
val letPair = Pair(Atom(letName), BuiltinV(::letBuiltIn, letName))
fun letBuiltIn(els: List<SExpr>, env: Environment): Value {
    if (els.size != 2) {
        throw RuntimeException("Incorrect arguments to let! Syntax is (let ((bind a)) body)")
    }

    val bindingsEl = els.first()
    val bindings = when (bindingsEl) {
        is SubExpr -> bindingsEl.exprs.map {
            when (it) {
                is SubExpr -> it
                else -> throw RuntimeException("Bindings must be a list of lists. Given $it, full bindings are $bindingsEl")
            }
        }
        else -> throw RuntimeException("Bindings must be a list of lists. Full bindings are $bindingsEl")
    }

    // Ensure that all bindings are 1-1, ie two SExprs
    if (!bindings.all { it.exprs.size == 2 }) {
        throw RuntimeException("Bindings must be a symbol bound to a value. Given $bindings.")
    }

    val (symbols, values) = bindings.fold(Pair(ArrayList<SymV>(), ArrayList<SExpr>())) {
        pair, expr ->
        val interpedArg = interp(expr.exprs.component1(), env, false)
        pair.first.add(when (interpedArg) {
            is ClosV -> throw RuntimeException("Cannot convert a ClosV to a let binding arg! Given $interpedArg.")
            else -> SymV(interpedArg.argString())
        })
        pair.second.add(expr.exprs.component2())
        pair
    }

    val closV = ClosV(symbols, els.component2(), env)
    return interpClosure(closV, values, env)
}

val ifPair = defineFunPair("if", ::ifBuiltIn)
fun ifBuiltIn(els: List<SExpr>, env: Environment): Value {
    if (els.size != 3) {
        throw RuntimeException("if must take 3 arguments: condition, true, false! Given $els")
    }

    val condS = els.component1()
    val trueCaseS = els.component2()
    val falseCaseS = els.component3()

    val cond = interp(condS, env)
    val res = if (cond !is BoolV) throw RuntimeException("if must take a boolean expression! Given $condS.\n$env")
    else cond.bool

    return if (res) interp(trueCaseS, env) else interp(falseCaseS, env)
}
