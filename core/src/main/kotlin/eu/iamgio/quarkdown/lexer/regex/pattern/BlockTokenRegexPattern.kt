package eu.iamgio.quarkdown.lexer.regex.pattern

import eu.iamgio.quarkdown.lexer.BlockCodeToken
import eu.iamgio.quarkdown.lexer.BlockQuoteToken
import eu.iamgio.quarkdown.lexer.BlockTextToken
import eu.iamgio.quarkdown.lexer.FencesCodeToken
import eu.iamgio.quarkdown.lexer.HeadingToken
import eu.iamgio.quarkdown.lexer.HorizontalRuleToken
import eu.iamgio.quarkdown.lexer.HtmlToken
import eu.iamgio.quarkdown.lexer.LinkDefinitionToken
import eu.iamgio.quarkdown.lexer.ListItemToken
import eu.iamgio.quarkdown.lexer.NewlineToken
import eu.iamgio.quarkdown.lexer.ParagraphToken
import eu.iamgio.quarkdown.lexer.SetextHeadingToken
import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.TokenData
import eu.iamgio.quarkdown.lexer.regex.RegexBuilder

// Some of the following patterns were taken or inspired by https://github.com/markedjs/marked/blob/master/src/rules.ts

/**
 * Collection of [TokenRegexPattern]s that match macro-blocks.
 */
enum class BlockTokenRegexPattern(
    override val wrap: (TokenData) -> Token,
    override val regex: Regex,
) : TokenRegexPattern {
    BLOCKQUOTE(
        ::BlockQuoteToken,
        RegexBuilder("^( {0,3}> ?(paragraph|[^\\n]*)(?:\\n|$))+")
            .withReference("paragraph", PARAGRAPH_PATTERN.pattern)
            .build(),
    ),
    BLOCKCODE(
        ::BlockCodeToken,
        "^( {4}[^\\n]+(?:\\n(?: *(?:\\n|\$))*)?)+"
            .toRegex(),
    ),
    LINKDEFINITION(
        ::LinkDefinitionToken,
        RegexBuilder("^ {0,3}\\[(label)\\]: *(?:\\n *)?([^<\\s][^\\s]*|<.*?>)(?:(?: +(?:\\n *)?| *\\n *)(title))? *(?:\\n+|$)")
            .withReference("label", BLOCK_LABEL_HELPER)
            .withReference("title", "(?:\"(?:\\\\\"?|[^\"\\\\])*\"|'[^'\\n]*(?:\\n[^'\\n]+)*\\n?'|\\([^()]*\\))")
            .build(),
    ),
    FENCESCODE(
        ::FencesCodeToken,
        "^ {0,3}((`{3,})($|\\s*.+$)((.|\\s)+?)(`{3,}))|((~{3,})($|\\s*.+$)((.|\\s)+?)(~{3,}))"
            .toRegex(),
    ),
    HEADING(
        ::HeadingToken,
        "^ {0,3}(#{1,6})(?=\\s|$)(.*)(?:\\n+|$)"
            .toRegex(),
    ),
    HORIZONTALRULE(
        ::HorizontalRuleToken,
        HORIZONTAL_RULE_HELPER
            .toRegex(),
    ),
    SETEXTHEADING(
        ::SetextHeadingToken,
        RegexBuilder("^(?!bullet )((?:.|\\R(?!\\s*?\\n|bullet ))+?)\\R {0,3}(=+|-+) *(?:\\R+|$)")
            .withReference("bullet", BULLET_HELPER)
            .withReference("bullet", BULLET_HELPER)
            .build(),
    ),
    HTML(
        ::HtmlToken,
        RegexBuilder(HTML_HELPER)
            .withReference("comment", COMMENT_HELPER)
            .withReference("tag", TAG_HELPER)
            .withReference(
                "attribute",
                " +[a-zA-Z:_][\\w.:-]*(?: *= *\"[^\"\\n]*\"| *= *'[^'\\n]*'| *= *[^\\s\"'=<>`]+)?",
            )
            .build(),
    ),
    LIST(
        ::ListItemToken,
        RegexBuilder("^( {0,3}bullet)(?! {0,3}bullet)([ \\t][^\\n]+?)?((.+(\\n(?!(\\s+\\n| {0,3}bullet)))?)*((\\s*\\n)( {2,}))*)*")
            .withReference("bullet", BULLET_HELPER)
            .withReference("bullet", BULLET_HELPER)
            .withReference("bullet", BULLET_HELPER)
            .build(),
    ),
    NEWLINE(
        ::NewlineToken,
        "^(?: *(?:\\n|$))+"
            .toRegex(),
    ),
    PARAGRAPH(
        ::ParagraphToken,
        PARAGRAPH_PATTERN,
    ),
    BLOCKTEXT(
        ::BlockTextToken,
        "^[^\\n]+"
            .toRegex(),
    ),
}

// Helper expressions

private const val BULLET_HELPER = "[*+-]|\\d{1,9}[\\.)]"

private const val HORIZONTAL_RULE_HELPER = "^ {0,3}((?:-[\\t ]*){3,}|(?:_[ \\t]*){3,}|(?:\\*[ \\t]*){3,})(?:\\n+|$)"

private const val PARAGRAPH_HELPER =
    "^([^\\n]+(?:\\n(?!hr|heading|lheading|blockquote|fences|list|html|table| +\\n)[^\\n]+)*)"

private const val BLOCK_LABEL_HELPER = "(?!\\s*\\])(?:\\\\.|[^\\[\\]\\\\])+"

private const val TAG_HELPER =
    "address|article|aside|base|basefont|blockquote|body|caption" +
        "|center|col|colgroup|dd|details|dialog|dir|div|dl|dt|fieldset|figcaption" +
        "|figure|footer|form|frame|frameset|h[1-6]|head|header|hr|html|iframe" +
        "|legend|li|link|main|menu|menuitem|meta|nav|noframes|ol|optgroup|option" +
        "|p|param|search|section|summary|table|tbody|td|tfoot|th|thead|title" +
        "|tr|track|ul"

private const val HTML_HELPER =
    "^ {0,3}(?:" +
        "<(script|pre|style|textarea)[\\s>][\\s\\S]*?(?:<\\/\\1>[^\\n]*\\n+)" +
        "|comment[^\\n]*(\\n+)" +
        "|<\\?[\\s\\S]*?(?:\\?>\\n*)" +
        "|<![A-Z][\\s\\S]*?(?:>\\n*)" +
        "|<!\\[CDATA\\[[\\s\\S]*?(?:\\]\\]>\\n*)" +
        "|<\\/?(tag)(?: +|\\n|\\/?>)[\\s\\S]*?(?:(?:\\n *)+\\n)" +
        "|<(?!script|pre|style|textarea)([a-z][\\w-]*)(?:attribute)*? *\\/?>(?=[ \\t]*(?:\\n))[\\s\\S]*?(?:(?:\\n *)+\\n)" +
        "|<\\/(?!script|pre|style|textarea)[a-z][\\w-]*\\s*>(?=[ \\t]*(?:\\n))[\\s\\S]*?(?:(?:\\n *)+\\n)" +
        ")"

private const val COMMENT_HELPER = "<!--(?:-?>|[\\s\\S]*?(?:-->))"

private val PARAGRAPH_PATTERN =
    RegexBuilder(PARAGRAPH_HELPER)
        .withReference("hr", HORIZONTAL_RULE_HELPER)
        .withReference("heading", " {0,3}#{1,6}(?:\\s|$)")
        .withReference("fences", " {0,3}(?:`{3,}(?=[^`\\n]*\\n)|~{3,})[^\\n]*\\n")
        .withReference("list", " {0,3}(?:[*+-]|1[.)]) ")
        .withReference("html", "<\\/?(?:tag)(?: +|\\n|\\/?>)|<(?:script|pre|style|textarea|!--)")
        .withReference("blockquote", " {0,3}>")
        .withReference("tag", TAG_HELPER)
        .build(RegexOption.MULTILINE)
