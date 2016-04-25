# EnvLISP (Environment LISP)
This is a relatively simple LISP interpreter that I'm working on to teach myself how to work in Kotlin. The general idea of this LISP is that _everything_ is defined in the environment. This includes basic constructs such as `let`, `fun`, `+`, `-`, `*`, `/`, and so on. Anything and everything in the language can be redefined by modifying the environment, even `let` itself. For example, this is a valid code snippet:

```lisp
;; Returns "let"
(let ((let "let"))
     let)
```

Of course, I can't recommend anyone actually code like this if you value your sanity, but it's an example of what's possible.

## Language Features
This language has the following features:

* Static Scope
* Pure (no mutation)
* Full modification of the value of every atom through the environment

As part of resolving an individual atom, the language will recursively attempt to find a final definition for a value, until it hits upon a final answer. For example:

```lisp
;; Returns 20
(let ((10 15) (15 20)) 10)
```

Additionally, the parameters to a function or let binding are interp'd in a special interp mode, which does not throw an unbound identifier exception. Instead, when it cannot resolve an identifier in the environment or convert it implicitly, that identifier is turned into a symbol, which is then returned and used as the parameter. This has interesting consequences, such as this:

```lisp
;; Returns 20
(let (((+ a b) 20)) ab)
```

## Usage
Are you crazy? Why would you want to use this lisp? I'm writing to explore writing a program in Kotlin, and to explore the logical extremes of defining everything in the environment. Please don't try to do anything real with this. However, if you want to see some examples of the crazy things you can do in a language like this, see src/com/fsilberberg/lisp/Tests.kt.