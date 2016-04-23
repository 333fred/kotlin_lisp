import java.util.*

/**
 * Simple LISP parser that I'm making to play around with Kotlin
 */

fun main(x: Array<String>) = println("${testParse("(Hello world (this \"\\(is a test\\)\" (of the parser)))")}")

fun readExpr(): String {
    val expr = StringBuilder()
    var lastLineNewline = false
    while (true) {
        val curLine = readLine()
        if (curLine == "\n") {
            if (lastLineNewline) {
                break
            } else {
                lastLineNewline = true
            }
        } else {
            lastLineNewline = false
            expr.append(curLine).append("\n")
        }
    }

    return expr.toString()
}

// These regexes match non-escaped dividers. This is to be sure we don't replace
// things in strings
val whitespaceRegex = Regex("\\s")
val nullAtom = Atom("NULL")

fun testParse(expr: String): SExpr {
    val iter = expr.iterator()
    return testParseTokens(iter.next(), iter).first ?: nullAtom
}

fun testParseTokens(curChar: Char, iter: Iterator<Char>): Pair<SExpr?, Boolean> {
    var finalChar = curChar
    while (isWhitespace(finalChar)) {
        if (iter.hasNext()) {
            finalChar = iter.next()
        } else {
            return Pair(nullAtom, false)
        }
    }

    return when (finalChar) {
        '(' -> Pair(testParseList(iter), false)
        ')' -> Pair(null, true)
        '"' -> Pair(parseString(iter), false)
        else -> parseAtom(curChar, iter)
    }
}

fun testParseList(iter: Iterator<Char>): SExpr {
    val subExprs = ArrayList<SExpr>()

    while (iter.hasNext()) {
        val (atom, closeParen) = testParseTokens(iter.next(), iter)

        if (atom != null) {
            subExprs.add(atom)
        }

        if (closeParen) {
            break
        }
    }

    return SubExpr(subExprs)
}

fun parseString(iter: Iterator<Char>): SExpr {
    var escaped = false
    var loop = true
    val sb = StringBuilder()
    while (iter.hasNext() && loop) {
        val nextChar = iter.next()
        when (nextChar) {
            '\\' -> {
                // If we haven't escaped before, this will become true. If the last char was a \, then
                // this will revert back to false, and we append a \.
                escaped = !escaped
                if (!escaped) {
                    sb.append(nextChar)
                }
            }
            '"' -> {
                if (escaped) {
                    sb.append(nextChar)
                    escaped = false
                } else {
                    loop = false
                }
            }
            else -> {
                sb.append(nextChar)
                escaped = false
            }
        }
    }

    return Atom(sb.toString())
}

fun parseAtom(curChar: Char, iter: Iterator<Char>): Pair<SExpr, Boolean> {
    val sb = StringBuilder()
    var encounteredCloseParen = false
    var nextChar = curChar
    var firstLoop = true

    do {
        if (!firstLoop) {
            nextChar = iter.next()
        } else {
            firstLoop = false
        }

        if (isWhitespace(nextChar)) {
            break
        } else if (nextChar == ')') {
            encounteredCloseParen = true
            break
        } else {
            sb.append(nextChar)
        }
    } while (iter.hasNext())

    return Pair(Atom(sb.toString()), encounteredCloseParen)
}

fun isWhitespace(curChar: Char): Boolean {
    return whitespaceRegex.matches(curChar.toString());
}

interface SExpr

data class Atom(val expr: String) : SExpr {
    //    override fun toString(): String {
    //        return expr
    //    }
}

data class SubExpr(val exprs: List<SExpr>) : SExpr {
    //    override fun toString(): String {
    //        return "( ${exprs.joinToString { it.toString() + " " }})"
    //    }
}