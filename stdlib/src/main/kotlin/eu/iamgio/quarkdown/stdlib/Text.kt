package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.Aligned
import eu.iamgio.quarkdown.ast.Clipped
import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.ast.Strong
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.StringValue

/**
 * `Text` stdlib module exporter.
 */
val Text =
    setOf(
        ::test,
        ::greet,
        ::bold,
        ::center,
        ::clip,
    )

fun test(x: Int = 0) = StringValue("Test $x from function!!!")

fun greet(name: String) = StringValue("Hello $name")

fun bold(body: MarkdownContent) =
    NodeValue(
        Strong(body.children),
    )

/**
 * Centers content within its parent.
 * @param body content to center
 * @return the new aligned block
 */
fun center(body: MarkdownContent) =
    NodeValue(
        Aligned(Aligned.Alignment.CENTER, body.children),
    )

/**
 * Applies a clipping path to its content.
 * @param clip clip type to apply
 * @return the new clipped block
 */
fun clip(
    clip: String,
    body: MarkdownContent,
) = // TODO support enum parameters to use Clipped.Clip enum
    NodeValue(
        Clipped(Clipped.Clip.CIRCLE, body.children),
    )
