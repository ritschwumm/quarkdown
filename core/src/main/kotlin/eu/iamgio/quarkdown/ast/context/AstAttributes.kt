package eu.iamgio.quarkdown.ast.context

import eu.iamgio.quarkdown.ast.FunctionCallNode
import eu.iamgio.quarkdown.ast.LinkDefinition

/**
 * Additional information about the node tree, produced by the parsing stage and stored in a [Context].
 * @see Context
 */
interface AstAttributes {
    /**
     * The defined links, which can be referenced by other nodes.
     */
    val linkDefinitions: List<LinkDefinition>

    /**
     * The function calls to be later executed.
     */
    val functionCalls: List<FunctionCallNode>

    /**
     * Whether there is at least one math block or inline.
     */
    val hasMath: Boolean
}

/**
 * Writeable attributes that are modified during the parsing process,
 * and carry useful information for the next stages of the pipeline.
 * Storing these attributes while parsing prevents a further visit of the final tree.
 * @param linkDefinitions the defined links, which can be referenced by other nodes
 * @param functionCalls the function calls to be later executed
 * @param hasMath whether there is at least one math block or inline.
 * @see MutableContext
 */
data class MutableAstAttributes(
    override val linkDefinitions: MutableList<LinkDefinition> = mutableListOf(),
    override val functionCalls: MutableList<FunctionCallNode> = mutableListOf(),
    override var hasMath: Boolean = false,
) : AstAttributes
