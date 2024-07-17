package eu.iamgio.quarkdown.rendering.html

import eu.iamgio.quarkdown.ast.FunctionCallNode
import eu.iamgio.quarkdown.ast.Math
import eu.iamgio.quarkdown.ast.MathSpan
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.PageBreak
import eu.iamgio.quarkdown.ast.quarkdown.Aligned
import eu.iamgio.quarkdown.ast.quarkdown.Box
import eu.iamgio.quarkdown.ast.quarkdown.Clipped
import eu.iamgio.quarkdown.ast.quarkdown.PageCounterInitializer
import eu.iamgio.quarkdown.ast.quarkdown.PageMarginContentInitializer
import eu.iamgio.quarkdown.ast.quarkdown.SlidesConfigurationInitializer
import eu.iamgio.quarkdown.ast.quarkdown.Stacked
import eu.iamgio.quarkdown.ast.quarkdown.TextTransform
import eu.iamgio.quarkdown.ast.quarkdown.Whitespace
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.document.DocumentType
import eu.iamgio.quarkdown.rendering.tag.buildTag
import eu.iamgio.quarkdown.rendering.tag.tagBuilder
import eu.iamgio.quarkdown.util.toPlainText

private const val BLOCK_MATH_FENCE = "__QD_BLOCK_MATH__"
private const val INLINE_MATH_FENCE = "__QD_INLINE_MATH__"

/**
 * A renderer for Quarkdown ([eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor]) nodes that exports their content into valid HTML code.
 * @param context additional information produced by the earlier stages of the pipeline
 */
class QuarkdownHtmlNodeRenderer(context: Context) : BaseHtmlNodeRenderer(context) {
    /**
     * A `<div class="styleClass">...</div>` tag.
     */
    private fun div(
        styleClass: String,
        init: HtmlTagBuilder.() -> Unit,
    ) = tagBuilder("div", init = init)
        .attribute("class", styleClass)
        .build()

    /**
     * A `<div class="styleClass">children</div>` tag.
     */
    private fun div(
        styleClass: String,
        children: List<Node>,
    ) = div(styleClass) { +children }

    // Quarkdown node rendering

    // The function was already expanded by previous stages: its output nodes are stored in its children.
    override fun visit(node: FunctionCallNode): CharSequence = node.children.joinToString(separator = "") { it.accept(this) }

    // Block

    // An empty div that acts as a page break.
    override fun visit(node: PageBreak) = div("page-break") {}

    // Math is processed by the MathJax library which requires text delimiters instead of tags.
    override fun visit(node: Math) = BLOCK_MATH_FENCE + "$" + node.expression + "$" + BLOCK_MATH_FENCE

    override fun visit(node: Aligned) = div("align align-" + node.alignment.name.lowercase(), node.children)

    override fun visit(node: Stacked): CharSequence {
        return div("stack stack-${node.orientation.asCSS}") {
            +node.children

            style {
                "justify-content" value node.mainAxisAlignment
                "align-items" value node.crossAxisAlignment
                "gap" value node.gap
            }
        }
    }

    override fun visit(node: Clipped) = div("clip-${node.clip.asCSS}", node.children)

    override fun visit(node: Box) =
        div("box") {
            if (node.title != null) {
                tag("header") {
                    tag("h4", node.title)

                    style {
                        "color" value node.foregroundColor // Must be repeated to force override.
                        "padding" value node.padding
                    }
                }
            }

            // Box actual content.
            +div("box-content") {
                +node.children

                style { "padding" value node.padding }
            }

            // Box style. Padding is applied separately to the header and the content.
            style {
                "background-color" value node.backgroundColor
                "color" value node.foregroundColor
            }
        }

    override fun visit(node: Whitespace) =
        buildTag("span") {
            style {
                "width" value node.width
                "height" value node.height
            }
        }

    // Inline

    // Math is processed by the MathJax library which requires text delimiters instead of tags.
    override fun visit(node: MathSpan) = INLINE_MATH_FENCE + "$" + node.expression + "$" + INLINE_MATH_FENCE

    override fun visit(node: TextTransform) =
        buildTag("span") {
            +node.children
            style {
                "font-size" value node.data.size
                "font-weight" value node.data.weight
                "font-style" value node.data.style
                "font-variant" value node.data.variant
                "text-decoration" value node.data.decoration
                "text-transform" value node.data.case
                "color" value node.data.color
            }
        }

    // Invisible nodes

    override fun visit(node: PageMarginContentInitializer) =
        if (context.documentInfo.type == DocumentType.PAGED) {
            // Paged documents support only plain text content.
            buildTag("script") {
                // Inject a CSS property used by the HTML wrapper.
                val property = "--page-margin-${node.position.asCSS}-content"
                val content = node.children.toPlainText(renderer = this@QuarkdownHtmlNodeRenderer)
                +"document.documentElement.style.setProperty('$property', '\"$content\"');"
            }
        } else {
            // HTML content.
            // In slides, these elements are copied to each slide through the slides.js script.
            div("page-margin-content page-margin-${node.position.asCSS}", node.children)
        }

    override fun visit(node: PageCounterInitializer) =
        visit(
            PageMarginContentInitializer(
                children =
                    when (context.documentInfo.type) {
                        DocumentType.PAGED ->
                            // Handled by PagedJS' CSS content property.
                            node.content(
                                "\"counter(page)\"",
                                "\"counter(pages)\"",
                            )

                        DocumentType.SLIDES ->
                            node.content(
                                // Get the data-index attribute from the section this element is in.
                                tagBuilder("span")
                                    .attribute("class", "current-page-number")
                                    .build(),
                                // Get the total amount of slides.
                                tagBuilder("span")
                                    .attribute("class", "total-page-number")
                                    .build(),
                            )

                        else -> node.content("-", "-") // Placeholder for document types that don't support page counters.
                    },
                position = node.position,
            ),
        )

    override fun visit(node: SlidesConfigurationInitializer): CharSequence =
        buildTag("script") {
            // Inject properties that are read by the slides.js script after the document is loaded.
            +buildString {
                node.centerVertically?.let {
                    append("const slides_center = $it;")
                }
                node.showControls?.let {
                    append("const slides_showControls = $it;")
                }
                node.transition?.let {
                    append("const slides_transitionStyle = '${it.style.asCSS}';")
                    append("const slides_transitionSpeed = '${it.speed.asCSS}';")
                }
            }
        }
}
