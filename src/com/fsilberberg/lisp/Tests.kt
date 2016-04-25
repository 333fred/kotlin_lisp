package com.fsilberberg.lisp

import org.junit.Assert
import org.junit.Test

/**
 * Various unit tests for the LISP
 */

fun runCode(code: String, startingEnv: Environment): Value {
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
        Assert.assertEquals(runCode("(+ 5 10)", builtInEnv), NumV(15.0))
    }

    @Test fun simpleStringAddition() {
        Assert.assertEquals(StringV("Hello World!"), runCode("(+ \"Hello\" \" \" \"World!\")", builtInEnv))
    }

    @Test fun simpleBoolAddition() {
        Assert.assertEquals(StringV("false true"), runCode("(+ false \" \" true)", builtInEnv))
    }

    @Test fun simpleOverride() {
        Assert.assertEquals(BoolV(false), runCode("(let ((true false)) true)", builtInEnv))
    }

    @Test fun overrideLet() {
        Assert.assertEquals(StringV("let"), runCode("(let ((let \"let\")) let)", builtInEnv))
    }

    @Test fun simpleSubTest() {
        Assert.assertEquals(NumV(10.0), runCode("(- 15 5)", builtInEnv))
    }

    @Test fun multipleMultiplyTest() {
        Assert.assertEquals(NumV(1000.0), runCode("(* 10 10 10)", builtInEnv))
    }

    @Test fun divideTest() {
        Assert.assertEquals(NumV(10.0), runCode("(/ 100 10)", builtInEnv))
    }

    @Test fun funTest() {
        Assert.assertEquals(NumV(10.0), runCode("((fun () 10))", builtInEnv))
    }


    @Test fun funWithArgTest() {
        Assert.assertEquals(NumV(10.0), runCode("((fun (x) x) 10)", builtInEnv))
    }

    @Test fun letTest() {
        Assert.assertEquals(NumV(10.0), runCode("(let ((x 10)) x)", builtInEnv))
    }

    @Test fun letMultiDefTest() {
        Assert.assertEquals(NumV(20.0), runCode("(let ((10 15) (15 20)) 10)", builtInEnv))
    }

    @Test fun letBindingMadness() {
        Assert.assertEquals(NumV(20.0), runCode("(let (((+ a b) 20)) ab)", builtInEnv))
    }

    @Test fun numBecomesFun() {
        Assert.assertEquals(NumV(20.0), runCode("""
        (let ((15 (fun (x) x)))
             (15 20))
        """, builtInEnv))
    }
}
