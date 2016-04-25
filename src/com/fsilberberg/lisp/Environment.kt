package com.fsilberberg.lisp

import com.fsilberberg.lisp.builtins.builtIns
import java.util.*

/**
 * The environment that holds all defined atom->value mappings for the current scope.
 */
class Environment {
    constructor() {
        env = HashMap()
    }

    protected constructor(startingEnv: Map<Atom, Value>) {
        env = startingEnv
    }

    private val env: Map<Atom, Value>

    /**
     * Looks up the given atom in the environment, returning the value if defined
     * @param atom The atom to look up
     * @return The bound value in the environment
     */
    fun lookup(atom: Atom): Value? = env[atom]

    /**
     * Creates a copy of the current environment, extended with the given bindings, overriding as necessary. The current
     * object is not modified by this operation.
     *
     * @param bindings The new bindings to introduce into the environment
     * @return The extended environment
     */
    fun extendEnv(bindings: Collection<Pair<Atom, Value>>): Environment {
        // Make a new environment with a copy of the existing environment
        val newMap = HashMap<Atom, Value>(env)
        for ((key, value) in bindings) {
            newMap[key] = value
        }

        return Environment(newMap)
    }

    override fun toString(): String {
        return "com.fsilberberg.lisp.Environment: ${env.entries.joinToString { pair ->
            "${pair.key} -> ${pair.value}"
        }}"
    }
}

val emptyEnv = Environment()
val builtInEnv = emptyEnv.extendEnv(builtIns)
