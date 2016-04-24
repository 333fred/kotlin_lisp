import java.util.*

/**
 * Simple LISP parser that I'm making to play around with Kotlin
 */

fun main(x: Array<String>) = println("${testParse("(Hello world (this \"(is a test)\" (of the parser )))")}")

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
    return parseTokens(iter.next(), iter).first ?: nullAtom
}

/**
 * Parses a token, determining whether it is an atom or subexpression, and returning the
 * full SExpr found
 *
 * @param curChar The character to start parsing on
 * @param iter The remaining characters to parse
 * @return If an SExpr was found, the SExpr, and whether or not the expr was a closing paren. The only time that null
 *         will be returned for the SExpr is when a closing paren is found.
 */
fun parseTokens(curChar: Char, iter: Iterator<Char>): Pair<SExpr?, Boolean> {
    var finalChar = curChar
    while (isWhitespace(finalChar)) {
        if (iter.hasNext()) {
            finalChar = iter.next()
        } else {
            return Pair(nullAtom, false)
        }
    }

    return when (finalChar) {
        '(' -> Pair(parseList(iter), false)
        ')' -> Pair(null, true)
        '"' -> Pair(parseString(iter), false)
        else -> parseAtom(curChar, iter)
    }
}

/**
 * Parses a subexpression from the given characters
 *
 * @param iter The characters to parse from
 * @return The SubExpr containing all parsed subexpressions
 */
fun parseList(iter: Iterator<Char>): SExpr {
    val subExprs = ArrayList<SExpr>()

    while (iter.hasNext()) {
        val (atom, closeParen) = parseTokens(iter.next(), iter)

        if (atom != null) {
            subExprs.add(atom)
        }

        if (closeParen) {
            break
        }
    }

    return SubExpr(subExprs)
}

/**
 * Parses a string from the given character iterator
 *
 * @param iter The character stream to parse a string from
 * @return The atom with the parsed string inside
 */
fun parseString(iter: Iterator<Char>): SExpr {
    var escaped = false
    var loop = true
    val sb = StringBuilder()
    sb.append('"')
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
                sb.append(nextChar)
                if (escaped) {
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

/**
 * Parses a given character and iterator for a single atom, returning the atom and whether that
 * atom ended with whitespace or a parenthesis
 *
 * @param curChar The character that starts the atom
 * @param iter The iterator over all remaining characters
 * @return The parsed atom, and true if the atom ended with a closing parenthesis
 */
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
    override fun toString(): String {
        return expr
    }

    fun convertAtom(): Value? {
        try {
            return NumV(expr.toDouble())
        } catch (e: NumberFormatException) {
            if (expr.equals("true", true) || expr.equals("false", true)) {
                return BoolV(expr.toBoolean())
            } else {
                if (expr.length >= 2 && expr.first() == '"' && expr.last() == '"') {
                    return SymV(expr.substringAfter('"').substringBeforeLast('"'))
                } else {
                    return null
                }
            }
        }
    }
}

data class SubExpr(val exprs: List<SExpr>) : SExpr {
    override fun toString(): String {
        return "( ${exprs.joinToString { it.toString() + " " }})"
    }
}