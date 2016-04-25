package com.fsilberberg.lisp

enum class ValEnum {
    NumV, ClosV, StringV, BoolV, SymV, BuiltInV
}

inline fun <reified T : Value> contextCast(value: Value, context: String): T {
    return when (value) {
        is T -> value
        else -> throw RuntimeException("$context expected ${T::class}, got $value")
    }
}

/**
 * The com.fsilberberg.lisp.Value return types that can be used
 */
interface Value {
    fun argString(): String
    fun getEnum(): ValEnum
}

data class NumV(val num: Double) : Value {
    override fun argString(): String = num.toString()
    override fun getEnum(): ValEnum = ValEnum.NumV
}

data class ClosV(val args: List<SymV>, val body: SExpr, val env: Environment) : Value {
    override fun argString(): String = toString()
    override fun getEnum(): ValEnum = ValEnum.ClosV
}

data class StringV(val str: String) : Value {
    override fun argString(): String = "\"$str\""
    override fun getEnum(): ValEnum = ValEnum.StringV
}

data class SymV(val str: String) : Value {
    override fun argString(): String = str
    override fun getEnum(): ValEnum = ValEnum.SymV
}

data class BoolV(val bool: Boolean) : Value {
    override fun argString(): String = bool.toString()
    override fun getEnum(): ValEnum = ValEnum.BoolV
}

/**
 * This class handles built-in functionality that can't be easily defined in terms of the language itself. This is
 * things like math operations, let, and so on
 */
data class BuiltinV(val action: (vals: List<SExpr>, env: Environment) -> Value, val funName: String) : Value {
    override fun argString(): String = funName
    override fun getEnum(): ValEnum = ValEnum.BuiltInV
}