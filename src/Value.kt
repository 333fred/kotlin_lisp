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
}

/**
 * The Value return types that can be used
 */
interface Value

data class NumV(val num: Int) : Value

data class ClosV(val args: List<SymV>, val body: SExpr, val env: Environment) : Value

data class SymV(val sym: String) : Value

data class BoolV(val bool: Boolean) : Value

/**
 * This class handles built-in functionality that can't be easily defined in terms of the language itself. This is
 * things like math operations, let, and so on
 */
data class BuiltinV(val action: (vals: Array<Value>) -> Value) : Value