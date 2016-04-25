package com.fsilberberg.lisp

import org.junit.Assert
import org.junit.Test

/**
 * Various unit tests for the LISP
 */

fun runCode(code: String, startingEnv: Environment = builtInEnv): Value {
    return interp(parse(code), startingEnv)
}

class UnitTests {
    @Test fun return1() {
        Assert.assertEquals(runCode("5", emptyEnv), NumV(5.0))
    }

    @Test fun return1InList() {
        Assert.assertEquals(runCode("(5)", emptyEnv), NumV(5.0))
    }

    @Test fun simpleAddition() {
        Assert.assertEquals(runCode("(+ 5 10)"), NumV(15.0))
    }

    @Test fun simpleStringAddition() {
        Assert.assertEquals(StringV("Hello World!"), runCode("(+ \"Hello\" \" \" \"World!\")"))
    }

    @Test fun simpleBoolAddition() {
        Assert.assertEquals(StringV("false true"), runCode("(+ false \" \" true)"))
    }

    @Test fun simpleOverride() {
        Assert.assertEquals(BoolV(false), runCode("(let ((true false)) true)"))
    }

    @Test fun overrideLet() {
        Assert.assertEquals(StringV("let"), runCode("(let ((let \"let\")) let)"))
    }

    @Test fun simpleSubTest() {
        Assert.assertEquals(NumV(10.0), runCode("(- 15 5)"))
    }

    @Test fun multipleMultiplyTest() {
        Assert.assertEquals(NumV(1000.0), runCode("(* 10 10 10)"))
    }

    @Test fun divideTest() {
        Assert.assertEquals(NumV(10.0), runCode("(/ 100 10)"))
    }

    @Test fun funTest() {
        Assert.assertEquals(NumV(10.0), runCode("((fun () 10))"))
    }


    @Test fun funWithArgTest() {
        Assert.assertEquals(NumV(10.0), runCode("((fun (x) x) 10)"))
    }

    @Test fun letTest() {
        Assert.assertEquals(NumV(10.0), runCode("(let ((x 10)) x)"))
    }

    @Test fun letMultiDefTest() {
        Assert.assertEquals(NumV(20.0), runCode("(let ((10 15) (15 20)) 10)"))
    }

    @Test fun letBindingMadness() {
        Assert.assertEquals(NumV(20.0), runCode("(let (((+ a b) 20)) ab)"))
    }

    @Test fun numBecomesFun() {
        Assert.assertEquals(NumV(20.0), runCode("""
        (let ((15 (fun (x) x)))
             (15 20))
        """))
    }

    @Test fun nestedLet() {
        Assert.assertEquals(NumV(20.0), runCode("""
        (let (((let ((10 15))
                    10)
               20))
             15)
        """))
    }

    @Test fun simpleIf() {
        Assert.assertEquals(NumV(1.0), runCode("(if (true) 1 0)"))
    }

    @Test fun simpleEquals() {
        Assert.assertEquals(BoolV(true), runCode("(= 1 1)"))
    }

    @Test fun simpleNot() {
        Assert.assertEquals(BoolV(false), runCode("(! true)"))
    }

    @Test fun simpleAnd() {
        Assert.assertEquals(BoolV(true), runCode("(& true true)"))
    }

    @Test fun simpleAndFalse() {
        Assert.assertEquals(BoolV(false), runCode("(& false true)"))
    }

    @Test fun simpleOr() {
        Assert.assertEquals(BoolV(true), runCode("(| false true)"))
    }

    @Test fun simpleOrFalse() {
        Assert.assertEquals(BoolV(false), runCode("(| false false)"))
    }

    @Test fun simpleGreater() {
        Assert.assertEquals(BoolV(true), runCode("(> 2 1)"))
    }

    @Test fun simpleGreaterFalse() {
        Assert.assertEquals(BoolV(false), runCode("(> 1 2)"))
    }

    @Test fun simpleGreaterEqual() {
        Assert.assertEquals(BoolV(true), runCode("(& (>= 2 2) (>= 2 1))"))
    }

    @Test fun simpleGreaterEqualFalse() {
        Assert.assertEquals(BoolV(false), runCode("(>= 1 2)"))
    }

    @Test fun simpleLess() {
        Assert.assertEquals(BoolV(true), runCode("(< 1 2)"))
    }

    @Test fun simpleLessFalse() {
        Assert.assertEquals(BoolV(false), runCode("(< 4 2)"))
    }

    @Test fun simpleLessEqual() {
        Assert.assertEquals(BoolV(true), runCode("(& (<= 2 2) (<= 2 3))"))
    }

    @Test fun simpleLessEqualFalse() {
        Assert.assertEquals(BoolV(false), runCode("(<= 3 2)"))
    }

    @Test fun simpleStringFirst() {
        Assert.assertEquals(StringV("H"), runCode("""(str-first "Hello world!")"""))
    }

    @Test fun simpleStringRest() {
        Assert.assertEquals(StringV("ello world!"), runCode("""(str-rest "Hello world!")"""))
    }
}
