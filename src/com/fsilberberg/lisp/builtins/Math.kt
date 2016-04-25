package com.fsilberberg.lisp.builtins

import com.fsilberberg.lisp.*

/**
 * All built-in functions related to math. This includes +, -, *, and /
 */

val plusPair = defineFunPair("+", ::plusBuiltIn)

/**
 * Defines the + operation in the language. This tries to do the "right" thing when it comes to arguments of differing
 * types. If the args are all NumV's, they are added as you'd expect. If any are symbols or booleans, they are all
 * converted to symbols and concatenated, in left-right order. You cannot concat closures or built-ins. If there are
 * any strings, everything is converted to a string and concatenated in left-right order. This has precedence over
 * symbol conversion.
 *
 * @param els The list of operands to add
 * @param env The environment to interp the operands with
 * @return Value The number or string created by adding all elements
 */
fun plusBuiltIn(els: List<SExpr>, env: Environment): Value {
    val interpedArgs = els.map { interp(it, env) }
    val finalType = interpedArgs.fold(ValEnum.NumV) { curType, arg ->
        when (arg) {
            is NumV -> curType
            is StringV -> ValEnum.StringV
            is BoolV -> if (curType == ValEnum.StringV) ValEnum.StringV else ValEnum.SymV
            is SymV -> if (curType == ValEnum.StringV) ValEnum.StringV else ValEnum.SymV
            else -> throw RuntimeException("Addition must be between strings, booleans, or numbers! Received $arg")
        }
    }

    if (finalType == ValEnum.NumV) {
        return NumV(interpedArgs.map {
            when (it) {
                is NumV -> it.num
                else -> throw RuntimeException("Interp Error! Cannot reach state!")
            }
        }.fold(0.0) { acc, num -> acc + num })
    } else if (finalType == ValEnum.SymV) {
        return SymV(interpedArgs.map { it.argString() }.fold("") { acc, str -> acc + str })
    } else {
        return StringV(interpedArgs.map {
            when (it) {
                is StringV -> it.str
                else -> it.argString()
            }
        }.fold("") { acc, str -> acc + str })
    }
}

val subPair = defineFunPair("-", generateMathFunc({ acc, num -> acc - num }, "subtract"))
val multPair = defineFunPair("*", generateMathFunc({ acc, num -> acc * num }, "multiply"))
val divPair = defineFunPair("/", generateMathFunc({ acc, num -> acc / num }, "divide"))

fun generateMathFunc(func: (Double, Double) -> Double, opType: String): (List<SExpr>, Environment) -> Value {
    return { els, env ->
        val interpedArgs = els.map { interp(it, env) }
        // Ensure that all args are numbers. Only + can handle other types
        val filteredArgs = interpedArgs.map {
            when (it) {
                is NumV -> it.num
                else -> throw RuntimeException("Attempted to $opType with non-number! Received $it.\n$env")
            }
        }

        NumV(filteredArgs.reduce(func))
    }
}
