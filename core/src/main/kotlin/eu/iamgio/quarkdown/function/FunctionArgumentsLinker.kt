package eu.iamgio.quarkdown.function

/**
 * Helper that associates [FunctionCallArgument]s to their corresponding [FunctionParameter].
 * @param call function call to link arguments for
 */
class FunctionArgumentsLinker(private val call: FunctionCall<*>) {
    lateinit var links: Map<FunctionParameter<*>, FunctionCallArgument<*>>

    /**
     * Whether every argument suits its corresponding parameter, in terms of type and count.
     */
    val isCompliant: Boolean
        get() {
            // TODO check arguments amount and types
            return true
        }

    /**
     * Stores the associations between [FunctionCallArgument]s and [FunctionParameter]s.
     */
    fun link() {
        this.links =
            call.arguments
                .withIndex()
                .associate { (index, argument) -> call.function.parameters[index] to argument }
    }

    /**
     * @param name name of the parameter to get the corresponding argument value for
     * @param T type of the value
     * @return the value of the argument by the given name
     * @throws NoSuchElementException if [name] does not match any parameter name
     */
    inline fun <reified T> arg(name: String): T =
        this.links.entries
            .first { it.key.name == name }
            .value // Map.Entry method: returns FunctionCallArgument
            .value // FunctionCallArgument method: returns InputValue<T>
            .value as T // InputValue<T> method: returns T
}
