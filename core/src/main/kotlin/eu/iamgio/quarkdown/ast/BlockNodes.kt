package eu.iamgio.quarkdown.ast

/**
 * A new line.
 */
class Newline : Node

/**
 * A code block defined via indentation.
 * Example:
 * ```
 *     Code
 * ```
 */
data class BlockCode(
    override val text: String,
) : TextNode

/**
 * A code block defined via fences.
 * Example:
 * ~~~
 * ```lang
 * Code
 * ```
 * ~~~
 */
data class FencesCode(
    override val text: String,
    val lang: String?,
) : TextNode

/**
 * A horizontal line.
 * Example:
 * ```
 * ---
 * ```
 */
class HorizontalRule : Node

/**
 * A heading defined via prefix symbols.
 * Example:
 * ```
 * # Heading
 * ```
 * Alternative:
 * ```
 * Heading
 * ====
 * ```
 */
data class Heading(
    val depth: Int,
    override val text: String,
) : TextNode

/**
 * Creation of a link reference.
 * Example:
 * ```
 * [label]: url "Title"
 * ```
 */
data class LinkDefinition(
    override val text: String,
    val url: String,
    val title: String?,
) : TextNode

/**
 * A list, either ordered or unordered.
 * Examples:
 * ```
 * - A
 * - B
 *
 * 1. First
 * 2. Second
 * ```
 */
data class ListItem(
    override val text: String,
    val ordered: Boolean,
    override val children: List<Node>,
) : NestableNode, TextNode

/**
 * An HTML block.
 * Example:
 * ```
 * <p>
 *     Code
 * </p>
 * ```
 */
data class Html(
    val content: String,
) : Node

/**
 * A text paragraph.
 */
data class Paragraph(
    override val text: String,
    override val children: List<Node>,
) : NestableNode, TextNode

/**
 * A block quote.
 * Example:
 * ```
 * > Quote
 * ```
 */
data class BlockQuote(
    override val text: String,
    override val children: List<Node>,
) : NestableNode, TextNode

/**
 * Anything else (should not happen).
 */
class BlockText : Node