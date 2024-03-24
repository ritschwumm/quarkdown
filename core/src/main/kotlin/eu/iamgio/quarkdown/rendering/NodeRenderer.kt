package eu.iamgio.quarkdown.rendering

import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A converter of [eu.iamgio.quarkdown.ast.Node]s into tag-based output code,
 * by using a DSL-like approach provided by [TagBuilder].
 */
abstract class NodeRenderer<B : TagBuilder> : NodeVisitor<CharSequence> {
    /**
     * Factory method that creates a new builder.
     * @param name name of the tag to open
     * @param pretty whether the output code should be pretty
     */
    abstract fun createBuilder(
        name: String,
        pretty: Boolean,
    ): B
}