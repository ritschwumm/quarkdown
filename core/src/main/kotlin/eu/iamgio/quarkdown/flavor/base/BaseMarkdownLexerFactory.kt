package eu.iamgio.quarkdown.flavor.base

import eu.iamgio.quarkdown.flavor.LexerFactory
import eu.iamgio.quarkdown.lexer.Lexer
import eu.iamgio.quarkdown.lexer.regex.StandardRegexLexer

/**
 * [BaseMarkdownFlavor] lexer factory.
 */
class BaseMarkdownLexerFactory : LexerFactory {
    override fun newBlockLexer(source: CharSequence): Lexer =
        with(BaseMarkdownBlockTokenRegexPatterns()) {
            StandardRegexLexer(
                source,
                listOf(
                    blockQuote,
                    blockCode,
                    linkDefinition,
                    fencesCode,
                    heading,
                    horizontalRule,
                    setextHeading,
                    html,
                    unorderedList,
                    orderedList,
                    newline,
                    paragraph,
                    blockText,
                ),
            )
        }

    override fun newListLexer(source: CharSequence): Lexer =
        with(BaseMarkdownBlockTokenRegexPatterns()) {
            StandardRegexLexer(
                source,
                listOf(listItem, newline),
            )
        }

    override fun newInlineLexer(source: CharSequence): Lexer {
        TODO("Not yet implemented")
    }
}