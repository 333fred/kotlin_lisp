import java.util.*

/**
 * Contains the functions built in to the language, such as fun
 */

fun funBuiltIn(els: List<SExpr>, env: Environment): ClosV {
    if (els.size != 2) {
        throw RuntimeException("Incorrect arguments to fun! Syntax is (fun (args) body). Provided $els")
    }

    val argList = els.first()
    val args = when (argList) {
        is SubExpr -> argList.exprs.map { interp(it, env) }
        is Atom -> listOf(interp(argList, env))
        else -> throw RuntimeException("Unknown SExpr type. $argList")
    }

    return ClosV(args.map {
        when (it) {
            is SymV -> it
            else -> throw  RuntimeException("Arg $it is not a SymV! Cannot use as function argument.\n$env")
        }
    }, els.component2(), env)
}

fun letBuiltIn(els: List<SExpr>, env: Environment): Value {
    if (els.size != 0) {
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

    val (symbols, values) = bindings.fold(Pair(ArrayList<SExpr>(), ArrayList<SExpr>())) {
        pair, expr ->
        pair.first.add(expr.exprs.component1())
        pair.second.add(expr.exprs.component2())
        pair
    }

    val closV = funBuiltIn(listOf(SubExpr(symbols), els.component2()), env)
    return interpClosure(closV, values, env)
}