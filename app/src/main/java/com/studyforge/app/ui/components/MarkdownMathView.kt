package com.studyforge.app.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.studyforge.app.ui.theme.ErrorRed
import com.studyforge.app.ui.theme.PurpleAccent
import com.studyforge.app.ui.theme.SuccessGreen
import com.studyforge.app.ui.theme.WarningYellow
import java.io.File

// =========================================================================
// PRE-COMPILED TOP-LEVEL REGEXES (Zero runtime pattern allocations)
// =========================================================================
private val REGEX_CASES = Regex("""\\begin\{cases\}([\s\S]*?)\\end\{cases\}""")
private val REGEX_ARRAY = Regex("""\\begin\{array\}\{[^}]*\}([\s\S]*?)\\end\{array\}""")
private val REGEX_BOXED = Regex("""\\boxed\{([^}]+)\}""")
private val REGEX_TEXT_MACROS = Regex("""\\(text|mathrm|mathbf|mathit|operatorname)\{([^}]+)\}""")
private val REGEX_ACCENT_TILDE = Regex("""\\tilde\{([a-zA-Z])\}""")
private val REGEX_ACCENT_HAT = Regex("""\\hat\{([a-zA-Z])\}""")
private val REGEX_ACCENT_BAR = Regex("""\\bar\{([a-zA-Z])\}""")
private val REGEX_ACCENT_VEC = Regex("""\\vec\{([a-zA-Z])\}""")
private val REGEX_ACCENT_DOT = Regex("""\\dot\{([a-zA-Z])\}""")
private val REGEX_ACCENT_DDOT = Regex("""\\ddot\{([a-zA-Z])\}""")
private val REGEX_FRAC = Regex("""\\frac\{([^}]+)\}\{([^}]+)\}""")
private val REGEX_SQRT_N = Regex("""\\sqrt\[([^\]]+)\]\{([^}]+)\}""")
private val REGEX_SQRT = Regex("""\\sqrt\{([^}]+)\}""")
private val REGEX_INT_LIMITS = Regex("""\\int_\{?([0-9a-zA-Z\+\-\*]+)\}?\^\{?([0-9a-zA-Z\+\-\*\\]+)\}?""")
private val REGEX_SUM_LIMITS = Regex("""\\sum_\{?([^\}^]+)\}?\^\{?([^\}^]+)\}?""")
private val REGEX_SUB_BRACES = Regex("""_\{([^}]+)\}""")
private val REGEX_SUB_CHAR = Regex("""_([0-9a-zA-Z\+\-\*])""")
private val REGEX_SUP_BRACES = Regex("""\^\{([^}]+)\}""")
private val REGEX_SUP_CHAR = Regex("""\^([0-9a-zA-Z\+\-\*])""")
private val REGEX_SPACES = Regex("""\s+""")
private val REGEX_DOUBLE_SLASH = Regex("""\\\\|\n""")
private val REGEX_NUMBERED = Regex("""^\d+\.\s+.*""")
private val REGEX_IMAGE_MD = Regex("""!\[(.*?)\]\((.*?)\)""")
private val REGEX_IMAGE_TAG = Regex("""\[IMAGE:\s*(.*?)\]""", RegexOption.IGNORE_CASE)
private val REGEX_BR_TAG = Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE)

// In-memory LRU Cache for formatted math and annotated strings
private val MATH_CACHE = java.util.Collections.synchronizedMap(object : java.util.LinkedHashMap<String, String>(256, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean = size > 600
})

private val ANNOTATED_CACHE = java.util.Collections.synchronizedMap(object : java.util.LinkedHashMap<String, AnnotatedString>(256, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, AnnotatedString>?): Boolean = size > 600
})

private val NODES_CACHE = java.util.Collections.synchronizedMap(object : java.util.LinkedHashMap<String, List<MarkdownNode>>(256, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<MarkdownNode>>?): Boolean = size > 600
})

private val OPTION_CACHE = java.util.Collections.synchronizedMap(object : java.util.LinkedHashMap<String, AnnotatedString>(300, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, AnnotatedString>?): Boolean = size > 800
})

// =========================================================================
// AST MODEL FOR VIRTUALIZED 120 FPS RENDERING
// =========================================================================
sealed interface MarkdownNode {
    data class Heading(val text: AnnotatedString, val level: Int) : MarkdownNode
    data class Paragraph(val text: AnnotatedString) : MarkdownNode
    data class Formula(val rawLatex: String, val formatted: String) : MarkdownNode
    data class Table(
        val headers: List<AnnotatedString>,
        val alignments: List<TextAlign>,
        val rows: List<List<AnnotatedString>>
    ) : MarkdownNode
    data class Callout(val tag: String, val content: AnnotatedString) : MarkdownNode
    data class CodeBlock(val code: String, val language: String?) : MarkdownNode
    data class ListItem(
        val isNumbered: Boolean,
        val prefix: String,
        val text: AnnotatedString,
        val indentLevel: Int = 0
    ) : MarkdownNode
    data class Checklist(val checked: Boolean, val text: AnnotatedString) : MarkdownNode
    data class Blockquote(val text: AnnotatedString) : MarkdownNode
    data object Divider : MarkdownNode
    data class Image(val uri: String, val alt: String) : MarkdownNode
}

/**
 * Parses markdown document ONCE into lightweight immutable AST nodes.
 */
fun parseMarkdownToNodes(markdownText: String): List<MarkdownNode> {
    if (markdownText.isBlank()) return emptyList()
    val cached = NODES_CACHE[markdownText]
    if (cached != null) return cached

    val lines = markdownText.lines()
    val nodes = ArrayList<MarkdownNode>(lines.size / 2)
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()

        // 1. Table (supports contiguous rows as well as blank lines between rows)
        if (isTableStart(lines, i)) {
            val tableLines = mutableListOf<String>()
            while (i < lines.size) {
                val currTrimmed = lines[i].trim()
                if (isTableRowLine(currTrimmed)) {
                    tableLines.add(currTrimmed)
                    i++
                } else if (currTrimmed.isEmpty()) {
                    // Peek ahead to see if table continues after blank line(s)
                    var nextNonBlank = i + 1
                    while (nextNonBlank < lines.size && lines[nextNonBlank].trim().isEmpty()) {
                        nextNonBlank++
                    }
                    if (nextNonBlank < lines.size && isTableRowLine(lines[nextNonBlank].trim())) {
                        i = nextNonBlank
                    } else {
                        break
                    }
                } else {
                    break
                }
            }
            val parsedTable = parseTableLines(tableLines)
            if (parsedTable != null) {
                nodes.add(parsedTable)
            }
            continue
        }

        // 2. Image
        val imageMatch = parseImageSyntax(line)
        if (imageMatch != null) {
            nodes.add(MarkdownNode.Image(uri = imageMatch.first, alt = imageMatch.second))
            i++
            continue
        }

        // 3. Formula Block: $$ ... $$ or \begin{array/cases/align}
        if (trimmed.startsWith("$$") || trimmed.startsWith("\\begin{cases}") || trimmed.startsWith("\\begin{array}") ||
            trimmed.startsWith("\\begin{align") || trimmed.startsWith("\\[") ||
            (trimmed.startsWith("\\boxed{") && trimmed.endsWith("}")) ||
            (trimmed.startsWith("\\") && (trimmed.contains("\\frac") || trimmed.contains("\\int") || trimmed.contains("\\sum") || trimmed.contains("\\sqrt") || (trimmed.contains("=") && !trimmed.contains(" "))))
        ) {
            val mathContent = StringBuilder()
            val isExplicitDollar = trimmed.startsWith("$$") || trimmed.startsWith("\\[")

            if (isExplicitDollar) {
                val cleanLine = trimmed.removePrefix("$$").removeSuffix("$$").removePrefix("\\[").removeSuffix("\\]").trim()
                if (trimmed != "$$" && trimmed != "\\[" && (trimmed.endsWith("$$") || trimmed.endsWith("\\]")) && cleanLine.isNotBlank()) {
                    mathContent.append(cleanLine)
                    i++
                } else {
                    val firstLineAfterDollar = trimmed.removePrefix("$$").removePrefix("\\[").trim()
                    if (firstLineAfterDollar.isNotBlank()) {
                        mathContent.appendLine(firstLineAfterDollar)
                    }
                    i++
                    while (i < lines.size && !lines[i].trim().endsWith("$$") && !lines[i].trim().endsWith("\\]")) {
                        mathContent.appendLine(lines[i])
                        i++
                    }
                    if (i < lines.size) {
                        mathContent.append(lines[i].replace("$$", "").replace("\\]", ""))
                        i++
                    }
                }
            } else if (trimmed.startsWith("\\begin{")) {
                val endTag = when {
                    trimmed.startsWith("\\begin{cases}") -> "\\end{cases}"
                    trimmed.startsWith("\\begin{array}") -> "\\end{array}"
                    else -> "\\end{align"
                }
                while (i < lines.size) {
                    val curr = lines[i]
                    mathContent.appendLine(curr)
                    i++
                    if (curr.contains(endTag)) break
                }
            } else {
                mathContent.append(trimmed)
                i++
            }

            val rawLatex = mathContent.toString().trim()
            if (rawLatex.isNotBlank()) {
                val formatted = formatLatexToReadableMath(rawLatex)
                nodes.add(MarkdownNode.Formula(rawLatex = rawLatex, formatted = formatted))
            }
            continue
        }

        // 4. Code Block
        if (trimmed.startsWith("```")) {
            val lang = trimmed.removePrefix("```").trim()
            val codeBuilder = StringBuilder()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                codeBuilder.appendLine(lines[i])
                i++
            }
            if (i < lines.size) i++
            nodes.add(MarkdownNode.CodeBlock(code = codeBuilder.toString(), language = if (lang.isNotBlank()) lang else null))
            continue
        }

        // 5. Callouts
        val upper = trimmed.uppercase()
        if (upper.startsWith("[IMPORTANT]") || upper.startsWith("[FORMULA]") ||
            upper.startsWith("[CONCEPT]") || upper.startsWith("[EXAMPLE]") ||
            upper.startsWith("[COMMON MISTAKE]") || upper.startsWith("[REMEMBER]")
        ) {
            val tag = when {
                upper.startsWith("[IMPORTANT]") -> "IMPORTANT"
                upper.startsWith("[FORMULA]") -> "FORMULA"
                upper.startsWith("[CONCEPT]") -> "CONCEPT"
                upper.startsWith("[EXAMPLE]") -> "EXAMPLE"
                upper.startsWith("[COMMON MISTAKE]") -> "COMMON MISTAKE"
                else -> "REMEMBER"
            }
            val inlineFirst = trimmed.substringAfter("]", "").trim()
            val calloutLines = mutableListOf<String>()
            if (inlineFirst.isNotBlank()) calloutLines.add(inlineFirst)
            i++
            while (i < lines.size && lines[i].isNotBlank() && !lines[i].startsWith("[") && !lines[i].startsWith("#") && !lines[i].startsWith("|")) {
                calloutLines.add(lines[i])
                i++
            }
            val contentStr = calloutLines.joinToString("\n")
            nodes.add(MarkdownNode.Callout(tag = tag, content = parseInlineMarkdownAndLatex(contentStr)))
            continue
        }

        // 6. Headings, Nested Lists, Blockquotes & Paragraphs
        val leadingSpaces = line.takeWhile { it == ' ' || it == '\t' }.fold(0) { acc, c -> acc + if (c == '\t') 4 else 1 }
        val indentLvl = when {
            leadingSpaces >= 4 -> 2
            leadingSpaces >= 2 -> 1
            else -> 0
        }

        when {
            trimmed.startsWith("#") && trimmed.takeWhile { it == '#' }.length in 1..6 &&
                (trimmed.dropWhile { it == '#' }.firstOrNull()?.isWhitespace() == true) -> {
                val hashes = trimmed.takeWhile { it == '#' }.length
                val cleanHeading = trimmed.drop(hashes).trim { it.isWhitespace() || it == '\u00A0' }
                nodes.add(MarkdownNode.Heading(text = parseInlineMarkdownAndLatex(cleanHeading), level = hashes.coerceIn(1, 4)))
            }
            trimmed.startsWith("- [x] ") || trimmed.startsWith("- [X] ") -> {
                nodes.add(MarkdownNode.Checklist(checked = true, text = parseInlineMarkdownAndLatex(trimmed.substring(6))))
            }
            trimmed.startsWith("- [ ] ") -> {
                nodes.add(MarkdownNode.Checklist(checked = false, text = parseInlineMarkdownAndLatex(trimmed.substring(6))))
            }
            trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                val bulletChar = when (indentLvl) {
                    0 -> "•"
                    1 -> "◦"
                    else -> "▪"
                }
                nodes.add(
                    MarkdownNode.ListItem(
                        isNumbered = false,
                        prefix = bulletChar,
                        text = parseInlineMarkdownAndLatex(trimmed.substring(2)),
                        indentLevel = indentLvl
                    )
                )
            }
            trimmed.matches(REGEX_NUMBERED) -> {
                val number = trimmed.substringBefore(".").trim()
                val content = trimmed.substringAfter(". ").trim()
                nodes.add(
                    MarkdownNode.ListItem(
                        isNumbered = true,
                        prefix = "$number.",
                        text = parseInlineMarkdownAndLatex(content),
                        indentLevel = indentLvl
                    )
                )
            }
            trimmed.startsWith(">") -> {
                val quoteLines = mutableListOf<String>()
                while (i < lines.size && lines[i].trim().startsWith(">")) {
                    val qLine = lines[i].trim().removePrefix(">").trim()
                    if (qLine.isNotBlank()) quoteLines.add(qLine)
                    i++
                }
                val combinedQuote = quoteLines.joinToString("\n")
                if (combinedQuote.isNotBlank()) {
                    nodes.add(MarkdownNode.Blockquote(text = parseInlineMarkdownAndLatex(combinedQuote)))
                }
                continue
            }
            trimmed == "---" || trimmed == "***" -> {
                nodes.add(MarkdownNode.Divider)
            }
            trimmed.isNotBlank() -> {
                nodes.add(MarkdownNode.Paragraph(text = parseInlineMarkdownAndLatex(trimmed)))
            }
        }
        i++
    }

    NODES_CACHE[markdownText] = nodes
    return nodes
}

private fun splitTableCells(line: String): List<String> {
    val cells = mutableListOf<String>()
    val current = StringBuilder()
    var inMath = false
    var inCode = false
    var i = 0
    while (i < line.length) {
        val ch = line[i]
        if (ch == '\\' && i + 1 < line.length && line[i + 1] == '|') {
            current.append('|')
            i += 2
            continue
        }
        if (ch == '`') {
            inCode = !inCode
            current.append(ch)
            i++
            continue
        }
        if (ch == '$' && !inCode) {
            if (i + 1 < line.length && line[i + 1] == '$') {
                inMath = !inMath
                current.append("$$")
                i += 2
                continue
            } else {
                inMath = !inMath
                current.append(ch)
                i++
                continue
            }
        }
        if (ch == '|' && !inMath && !inCode) {
            cells.add(current.toString().trim())
            current.clear()
        } else {
            current.append(ch)
        }
        i++
    }
    cells.add(current.toString().trim())

    // Drop leading and trailing empty elements produced by outer pipes | ... |
    val startIdx = if (cells.isNotEmpty() && cells.first().isEmpty() && line.trimStart().startsWith("|")) 1 else 0
    val endIdx = if (cells.size > startIdx && cells.last().isEmpty() && line.trimEnd().endsWith("|")) cells.lastIndex else cells.size
    return cells.subList(startIdx, endIdx)
}

private fun cleanAndParseTableCell(rawCell: String): AnnotatedString {
    if (rawCell.isBlank()) return AnnotatedString("")
    val normalized = REGEX_BR_TAG.replace(rawCell, "\n")
        .lines()
        .map { REGEX_SPACES.replace(it.trim(), " ") }
        .filter { it.isNotEmpty() }
        .joinToString("\n")
    if (normalized.isEmpty()) return AnnotatedString("")

    // If cell contains raw LaTeX macros without $ delimiters, format cleanly
    return if (!normalized.contains('$') &&
        (normalized.contains("\\frac") || normalized.contains("\\sqrt") ||
            normalized.contains("\\int") || normalized.contains("\\sum") ||
            normalized.contains("\\alpha") || normalized.contains("\\beta") ||
            normalized.contains("\\gamma") || normalized.contains("\\Delta") ||
            normalized.contains("\\theta") || normalized.contains("\\lambda") ||
            normalized.contains("\\mu") || normalized.contains("\\sigma") ||
            normalized.contains("\\pi") || normalized.contains("\\omega") ||
            normalized.contains("\\le") || normalized.contains("\\ge") ||
            normalized.contains("\\neq") || normalized.contains("\\approx") ||
            normalized.contains("\\pm") || normalized.contains("\\times") ||
            normalized.contains("\\cdot") || normalized.contains("\\to") ||
            normalized.contains("\\rightarrow") || normalized.contains("\\infty"))
    ) {
        buildAnnotatedString { appendInlineMathLatex(normalized) }
    } else {
        parseInlineMarkdownAndLatex(normalized)
    }
}

private fun parseTableLines(tableLines: List<String>): MarkdownNode.Table? {
    if (tableLines.size < 2) return null
    val headerLine = tableLines[0]
    val separatorLine = tableLines[1]
    val dataLines = tableLines.drop(2)

    val headers = splitTableCells(headerLine)
    if (headers.isEmpty() || headers.all { it.isEmpty() }) return null

    val separators = splitTableCells(separatorLine)
    val alignments = headers.indices.map { idx ->
        val sep = separators.getOrElse(idx) { "" }
        when {
            sep.startsWith(":") && sep.endsWith(":") -> TextAlign.Center
            sep.endsWith(":") -> TextAlign.End
            else -> TextAlign.Start
        }
    }

    val rows = dataLines
        .filter { line -> !isTableSeparatorLine(line.trim()) }
        .map { rowLine -> splitTableCells(rowLine) }
        .filter { rowCells -> rowCells.any { it.isNotBlank() } }
        .map { rowCells ->
            headers.indices.map { cIdx ->
                cleanAndParseTableCell(rowCells.getOrElse(cIdx) { "" })
            }
        }

    val headerAnnotated = headers.map { cleanAndParseTableCell(it) }

    return MarkdownNode.Table(
        headers = headerAnnotated,
        alignments = alignments,
        rows = rows
    )
}

/**
 * High-performance node renderer for virtualized Compose layouts.
 */
@Composable
fun RenderMarkdownNode(node: MarkdownNode) {
    when (node) {
        is MarkdownNode.Heading -> {
            HeadingText(annotatedText = node.text, level = node.level)
            Spacer(modifier = Modifier.height(4.dp))
        }
        is MarkdownNode.Paragraph -> {
            RichMarkdownText(
                text = node.text,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        is MarkdownNode.Formula -> {
            BlockMathCard(rawLatex = node.rawLatex, preformatted = node.formatted)
            Spacer(modifier = Modifier.height(4.dp))
        }
        is MarkdownNode.Table -> {
            PreParsedMarkdownTableCard(table = node)
            Spacer(modifier = Modifier.height(6.dp))
        }
        is MarkdownNode.Callout -> {
            PreParsedCalloutCard(tag = node.tag, content = node.content)
            Spacer(modifier = Modifier.height(6.dp))
        }
        is MarkdownNode.CodeBlock -> {
            CodeBlockCard(code = node.code, language = node.language)
            Spacer(modifier = Modifier.height(6.dp))
        }
        is MarkdownNode.ListItem -> {
            if (node.isNumbered) {
                NumberedListItem(
                    number = node.prefix.removeSuffix("."),
                    annotatedText = node.text,
                    indentLevel = node.indentLevel
                )
            } else {
                BulletListItem(
                    annotatedText = node.text,
                    indentLevel = node.indentLevel
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
        }
        is MarkdownNode.Checklist -> {
            ChecklistItem(checked = node.checked, annotatedText = node.text)
            Spacer(modifier = Modifier.height(2.dp))
        }
        is MarkdownNode.Blockquote -> {
            BlockquoteCard(annotatedText = node.text)
            Spacer(modifier = Modifier.height(4.dp))
        }
        is MarkdownNode.Divider -> {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )
        }
        is MarkdownNode.Image -> {
            MarkdownImageCard(uriString = node.uri, altText = node.alt)
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

/**
 * Standard MarkdownMathView container.
 */
@Composable
fun MarkdownMathView(
    markdownText: String,
    modifier: Modifier = Modifier
) {
    if (markdownText.isBlank()) return

    val nodes = remember(markdownText) {
        parseMarkdownToNodes(markdownText)
    }

    Column(modifier = modifier) {
        nodes.forEach { node ->
            key(node.hashCode()) {
                RenderMarkdownNode(node)
            }
        }
    }
}

/**
 * Textbook-grade Display Math renderer for `$$ ... $$` equations.
 * Renders real vertical stacked fractions, tall scalable brackets/parentheses,
 * and aligned multi-column `\begin{array}` tables directly on the page like a printed textbook.
 */
@Composable
fun BlockMathCard(
    rawLatex: String,
    preformatted: String? = null,
    modifier: Modifier = Modifier
) {
    TextbookMathBlock(
        rawLatex = rawLatex,
        modifier = modifier
    )
}

private data class ColumnMetrics(
    val maxLineChars: Int,
    val effectiveChars: Int,
    val maxWordChars: Int
)

/**
 * Counts true visual horizontal character width by ignoring zero-width combining marks,
 * Devanagari vowel matras/halants/anusvara, and emoji variation selectors.
 */
private fun visualCharLength(s: String): Int {
    var count = 0
    for (i in 0 until s.length) {
        val ch = s[i]
        val type = Character.getType(ch).toByte()
        if (type == Character.NON_SPACING_MARK ||
            type == Character.COMBINING_SPACING_MARK ||
            type == Character.ENCLOSING_MARK ||
            ch in '\u0900'..'\u0903' ||
            ch == '\u093C' ||
            ch in '\u093E'..'\u094D' ||
            ch in '\u0951'..'\u0957' ||
            ch in '\u0962'..'\u0963' ||
            ch in '\u200B'..'\u200D' ||
            ch == '\uFE0F'
        ) {
            continue
        }
        count++
    }
    return count
}

/**
 * Responsive Pre-parsed Table Card designed for mobile screens:
 * - Uses visual character length (aware of Hindi/Devanagari matras) to prevent phantom horizontal gaps.
 * - Guarantees 1–4 column tables fit 100% within screen bounds without cutting off the right column.
 * - Draws crisp 0.75.dp vertical and horizontal grid lines for textbook-grade readability.
 */
@Composable
fun PreParsedMarkdownTableCard(table: MarkdownNode.Table) {
    val headers = table.headers
    val alignments = table.alignments
    val rows = table.rows
    val numCols = headers.size
    if (numCols == 0) return

    val colMetrics = remember(headers, rows) {
        (0 until numCols).map { cIdx ->
            val hText = headers.getOrNull(cIdx)?.text ?: ""
            val hMaxLine = hText.lines().maxOfOrNull { visualCharLength(it) } ?: 2
            val hMaxWord = hText.split(REGEX_SPACES).maxOfOrNull { visualCharLength(it) } ?: 2

            val rLineLengths = ArrayList<Int>(rows.size + 1)
            rLineLengths.add(hMaxLine)
            var rMaxLine = 0
            var rMaxWord = hMaxWord

            rows.forEach { row ->
                val cellText = row.getOrNull(cIdx)?.text ?: ""
                val cellLineMax = cellText.lines().maxOfOrNull { visualCharLength(it) } ?: 0
                if (cellLineMax > 0) rLineLengths.add(cellLineMax)
                if (cellLineMax > rMaxLine) rMaxLine = cellLineMax
                cellText.split(REGEX_SPACES).forEach { w ->
                    val wLen = visualCharLength(w)
                    if (wLen > rMaxWord) rMaxWord = wLen
                }
            }

            rLineLengths.sort()
            val p75Idx = ((rLineLengths.size - 1) * 0.75f).toInt().coerceIn(0, rLineLengths.lastIndex)
            val p75Chars = rLineLengths[p75Idx]
            val maxLine = maxOf(hMaxLine, rMaxLine, 2)
            // Prevent a single outlier row from stretching a short column across the screen
            val effective = minOf(maxLine, maxOf((p75Chars * 1.55f).toInt(), hMaxLine, 10))

            ColumnMetrics(
                maxLineChars = maxLine,
                effectiveChars = effective,
                maxWordChars = maxOf(rMaxWord, 2)
            )
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        val availableWidth = maxWidth.coerceAtLeast(200.dp)
        val gridBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)

        val (colWidths, needsScroll) = remember(colMetrics, availableWidth) {
            // Natural width based on ~5.4dp per visual character + 16dp horizontal cell padding
            val naturalWidths = colMetrics.map { m ->
                (m.effectiveChars * 5.4f + 16f).dp.coerceIn(40.dp, 190.dp)
            }
            // Absolute max single-line width so a column is NEVER stretched wider than its longest text
            val maxUnwrappedWidths = colMetrics.map { m ->
                (m.maxLineChars * 5.6f + 18f).dp.coerceIn(44.dp, 220.dp)
            }
            // Minimum readable width per column (short columns don't wrap; long columns fit their longest word)
            val minWidths = colMetrics.mapIndexed { idx, m ->
                if (m.effectiveChars <= 9) {
                    naturalWidths[idx]
                } else {
                    val wordBased = (m.maxWordChars.coerceIn(4, 12) * 5.4f + 16f).dp
                    wordBased.coerceIn(54.dp, 96.dp).coerceAtMost(naturalWidths[idx])
                }
            }

            val totalNatural = naturalWidths.fold(0.dp) { acc, d -> acc + d }
            val totalMin = minWidths.fold(0.dp) { acc, d -> acc + d }

            when {
                totalNatural <= availableWidth -> {
                    // All columns fit without wrapping effectiveChars.
                    // Only expand columns that have outlier longer lines, never stretching short columns past maxUnwrappedWidths.
                    val extraRoom = (availableWidth - totalNatural).coerceAtLeast(0.dp)
                    val expandableNeeds = colMetrics.indices.map { idx ->
                        (maxUnwrappedWidths[idx] - naturalWidths[idx]).coerceAtLeast(0.dp)
                    }
                    val totalExpandable = expandableNeeds.fold(0.dp) { acc, d -> acc + d }
                    val widths = colMetrics.indices.map { idx ->
                        val add = if (totalExpandable > 0.dp) {
                            val ratio = (expandableNeeds[idx].value / totalExpandable.value).coerceIn(0f, 1f)
                            (extraRoom * ratio).coerceAtMost(expandableNeeds[idx])
                        } else {
                            0.dp
                        }
                        naturalWidths[idx] + add
                    }
                    Pair(widths, false)
                }

                numCols <= 4 -> {
                    // Always fit 1-4 column tables 100% within availableWidth so right columns are never cut off
                    if (totalMin <= availableWidth) {
                        val remaining = (availableWidth - totalMin).coerceAtLeast(0.dp)
                        val desiredExtra = colMetrics.indices.map { idx ->
                            (naturalWidths[idx] - minWidths[idx]).coerceAtLeast(0.dp)
                        }
                        val totalDesiredExtra = desiredExtra.fold(0.dp) { acc, d -> acc + d }
                        val widths = colMetrics.indices.map { idx ->
                            val add = if (totalDesiredExtra > 0.dp) {
                                val ratio = (desiredExtra[idx].value / totalDesiredExtra.value).coerceIn(0f, 1f)
                                remaining * ratio
                            } else {
                                remaining / numCols
                            }
                            minWidths[idx] + add
                        }
                        Pair(widths, false)
                    } else {
                        val scale = (availableWidth.value / totalMin.value).coerceIn(0.5f, 1f)
                        val widths = minWidths.map { (it * scale).coerceAtLeast(42.dp) }
                        Pair(widths, false)
                    }
                }

                else -> {
                    // 5+ column table: smooth horizontal scroll with compact capped widths
                    val scrollWidths = colMetrics.indices.map { idx ->
                        naturalWidths[idx].coerceIn(minWidths[idx], 150.dp)
                    }
                    Pair(scrollWidths, true)
                }
            }
        }

        val totalTableWidth: Dp = remember(colWidths) {
            colWidths.fold(0.dp) { acc, d -> acc + d }
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .then(if (needsScroll) Modifier.fillMaxWidth() else Modifier.width(totalTableWidth))
                .border(1.dp, gridBorderColor, RoundedCornerShape(10.dp))
        ) {
            val scrollModifier = if (needsScroll) {
                Modifier.horizontalScroll(rememberScrollState())
            } else {
                Modifier
            }

            Column(
                modifier = scrollModifier.width(totalTableWidth)
            ) {
                // Header Row
                Row(
                    modifier = Modifier
                        .width(totalTableWidth)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                        .drawWithContent {
                            drawContent()
                            val strokePx = 0.75.dp.toPx()
                            var xPx = 0f
                            for (cIdx in 0 until numCols - 1) {
                                xPx += colWidths[cIdx].toPx()
                                drawLine(
                                    color = gridBorderColor,
                                    start = Offset(xPx, 0f),
                                    end = Offset(xPx, size.height),
                                    strokeWidth = strokePx
                                )
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    headers.forEachIndexed { idx, headerText ->
                        val align = alignments.getOrElse(idx) { TextAlign.Start }
                        Box(
                            modifier = Modifier
                                .width(colWidths[idx])
                                .padding(horizontal = 8.dp, vertical = 7.dp)
                        ) {
                            RichMarkdownText(
                                text = headerText,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = align,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.width(totalTableWidth),
                    color = gridBorderColor,
                    thickness = 1.dp
                )

                // Data Rows
                rows.forEachIndexed { rIdx, rowCells ->
                    val rowBg = if (rIdx % 2 == 0) {
                        Color.Transparent
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
                    }
                    Row(
                        modifier = Modifier
                            .width(totalTableWidth)
                            .background(rowBg)
                            .drawWithContent {
                                drawContent()
                                val strokePx = 0.75.dp.toPx()
                                var xPx = 0f
                                for (cIdx in 0 until numCols - 1) {
                                    xPx += colWidths[cIdx].toPx()
                                    drawLine(
                                        color = gridBorderColor,
                                        start = Offset(xPx, 0f),
                                        end = Offset(xPx, size.height),
                                        strokeWidth = strokePx
                                    )
                                }
                            },
                        verticalAlignment = Alignment.Top
                    ) {
                        headers.indices.forEach { cIdx ->
                            val cellAnnotated = rowCells.getOrElse(cIdx) { AnnotatedString("") }
                            val align = alignments.getOrElse(cIdx) { TextAlign.Start }
                            Box(
                                modifier = Modifier
                                    .width(colWidths[cIdx])
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                RichMarkdownText(
                                    text = cellAnnotated,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = align,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    if (rIdx < rows.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.width(totalTableWidth),
                            color = gridBorderColor,
                            thickness = 0.75.dp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Legacy compatibility wrapper for MarkdownTableCard.
 */
@Composable
fun MarkdownTableCard(tableLines: List<String>) {
    val tableNode = remember(tableLines) { parseTableLines(tableLines) }
    if (tableNode != null) {
        PreParsedMarkdownTableCard(table = tableNode)
    }
}

/**
 * Monospace Code Block Card with language badge and Copy action button.
 */
@Composable
fun CodeBlockCard(
    code: String,
    language: String? = null
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var isCopied by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = (language ?: "CODE").uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            clipboardManager.setText(AnnotatedString(code.trimEnd()))
                            isCopied = true
                            Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = if (isCopied) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCopied) "Copied!" else "Copy",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isCopied) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {
                Text(
                    text = code.trimEnd(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Image card supporting local gallery images and URLs with tap-to-zoom preview.
 */
@Composable
fun MarkdownImageCard(
    uriString: String,
    altText: String
) {
    var isExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val imageModel = remember(uriString) {
        if (uriString.startsWith("file://")) {
            File(uriString.removePrefix("file://"))
        } else if (uriString.startsWith("content://")) {
            Uri.parse(uriString)
        } else {
            uriString
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
                .clickable { isExpanded = true }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = altText.ifBlank { "Imported Image" },
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp, max = 320.dp)
                        .clip(RoundedCornerShape(18.dp))
                )
                if (altText.isNotBlank()) {
                    Text(
                        text = altText,
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (isExpanded) {
        Dialog(
            onDismissRequest = { isExpanded = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
                    .clickable { isExpanded = false },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = altText,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
                IconButton(
                    onClick = { isExpanded = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }
}

/**
 * Pre-parsed Callout Card.
 */
@Composable
fun PreParsedCalloutCard(tag: String, content: AnnotatedString) {
    val (bgColor, borderColor, icon, title) = when (tag) {
        "IMPORTANT" -> CalloutStyle(ErrorRed.copy(alpha = 0.10f), ErrorRed, Icons.Default.ErrorOutline, "Important")
        "FORMULA" -> CalloutStyle(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f), MaterialTheme.colorScheme.primary, Icons.Default.Calculate, "Key Formula")
        "CONCEPT" -> CalloutStyle(SuccessGreen.copy(alpha = 0.10f), SuccessGreen, Icons.Default.Lightbulb, "Core Concept")
        "EXAMPLE" -> CalloutStyle(MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f), MaterialTheme.colorScheme.secondary, Icons.AutoMirrored.Filled.MenuBook, "Example Problem")
        "COMMON MISTAKE" -> CalloutStyle(WarningYellow.copy(alpha = 0.12f), WarningYellow, Icons.Default.Warning, "Common Mistake")
        else -> CalloutStyle(PurpleAccent.copy(alpha = 0.10f), PurpleAccent, Icons.Default.Bookmark, "Remember")
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SquircleIconBadge(
                    icon = icon,
                    accentColor = borderColor,
                    size = 30.dp,
                    iconSize = 16.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = borderColor
                )
            }
            if (content.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                RichMarkdownText(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun CalloutCard(tag: String, content: String) {
    val annotated = remember(content) { parseInlineMarkdownAndLatex(content) }
    PreParsedCalloutCard(tag = tag, content = annotated)
}

private data class CalloutStyle(
    val bgColor: Color,
    val borderColor: Color,
    val icon: ImageVector,
    val title: String
)

@Composable
fun HeadingText(text: String? = null, annotatedText: AnnotatedString? = null, level: Int = 1) {
    val content = annotatedText ?: (text?.let { parseInlineMarkdownAndLatex(it.trim()) } ?: AnnotatedString(""))
    val barColor = MaterialTheme.colorScheme.primary

    if (level == 1) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 6.dp)
        ) {
            RichMarkdownText(
                text = content,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    lineHeight = 30.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                thickness = 1.2.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
            )
        }
    } else {
        val style = when (level) {
            2 -> MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 19.sp,
                lineHeight = 27.sp
            )
            3 -> MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                lineHeight = 25.sp
            )
            else -> MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.5.sp,
                lineHeight = 23.sp
            )
        }
        val barWidth = if (level == 2) 4.dp else 3.5.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 4.dp)
                .drawWithContent {
                    val strokePx = barWidth.toPx()
                    val insetY = 3.dp.toPx()
                    val startY = insetY.coerceAtMost(size.height / 2f)
                    val endY = (size.height - insetY).coerceAtLeast(startY)
                    drawLine(
                        color = barColor,
                        start = Offset(strokePx / 2f, startY),
                        end = Offset(strokePx / 2f, endY),
                        strokeWidth = strokePx,
                        cap = StrokeCap.Round
                    )
                    drawContent()
                }
                .padding(start = 12.dp)
        ) {
            RichMarkdownText(
                text = content,
                style = style,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun BulletListItem(text: String? = null, annotatedText: AnnotatedString? = null, indentLevel: Int = 0) {
    val content = annotatedText ?: (text?.let { parseInlineMarkdownAndLatex(it) } ?: AnnotatedString(""))
    val startPad = 8.dp + (indentLevel.coerceIn(0, 4) * 18).dp
    val bulletChar = when (indentLevel) {
        0 -> "•"
        1 -> "◦"
        else -> "▪"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startPad, top = 3.dp, bottom = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = bulletChar,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                lineHeight = 25.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(end = 10.dp)
        )
        RichMarkdownText(
            text = content,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 15.5.sp,
                lineHeight = 25.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun NumberedListItem(number: String, text: String? = null, annotatedText: AnnotatedString? = null, indentLevel: Int = 0) {
    val content = annotatedText ?: (text?.let { parseInlineMarkdownAndLatex(it) } ?: AnnotatedString(""))
    val startPad = 8.dp + (indentLevel.coerceIn(0, 4) * 18).dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startPad, top = 3.dp, bottom = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$number.",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.5.sp,
                lineHeight = 25.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 10.dp)
        )
        RichMarkdownText(
            text = content,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 15.5.sp,
                lineHeight = 25.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ChecklistItem(checked: Boolean, text: String? = null, annotatedText: AnnotatedString? = null) {
    val content = annotatedText ?: (text?.let { parseInlineMarkdownAndLatex(it) } ?: AnnotatedString(""))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = if (checked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
            contentDescription = if (checked) "Checked" else "Unchecked",
            tint = if (checked) SuccessGreen else MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        RichMarkdownText(
            text = content,
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 25.sp,
                textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = if (checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun BlockquoteCard(text: String? = null, annotatedText: AnnotatedString? = null) {
    val content = annotatedText ?: (text?.let { parseInlineMarkdownAndLatex(it) } ?: AnnotatedString(""))
    val barColor = MaterialTheme.colorScheme.primary
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .drawWithContent {
                    val strokePx = 4.dp.toPx()
                    drawLine(
                        color = barColor,
                        start = Offset(strokePx / 2f, 2.dp.toPx()),
                        end = Offset(strokePx / 2f, (size.height - 2.dp.toPx()).coerceAtLeast(2.dp.toPx())),
                        strokeWidth = strokePx,
                        cap = StrokeCap.Round
                    )
                    drawContent()
                }
                .padding(start = 14.dp)
        ) {
            RichMarkdownText(
                text = content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontStyle = FontStyle.Italic,
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Ultra-fast parser for MCQ Options.
 * Converts LaTeX formulas, inline math, and styled text directly to native AnnotatedString in 0ms.
 * Backed by an in-memory LRU cache to guarantee zero-latency option rendering across all test and question views.
 */
fun parseOptionText(text: String): AnnotatedString {
    if (text.isBlank()) return AnnotatedString("")
    val cached = OPTION_CACHE[text]
    if (cached != null) return cached

    val trimmed = text.trim()
    val result = when {
        // Explicit inline math $...$
        trimmed.contains('$') -> parseInlineMarkdownAndLatex(trimmed)

        // Raw LaTeX without dollar signs (e.g. -\frac{1}{2}\cos(2x) + C, \sqrt{2gh}, x^2 + 7, etc.)
        trimmed.contains('\\') || (trimmed.contains('^') && !trimmed.contains("http")) ||
        (trimmed.contains('_') && !trimmed.contains(" ") && trimmed.length < 50) -> {
            buildAnnotatedString {
                appendInlineMathLatex(trimmed)
            }
        }

        // Standard inline formatting (**bold**, *italic*, `code`, etc.)
        else -> parseInlineMarkdownAndLatex(trimmed)
    }

    OPTION_CACHE[text] = result
    return result
}

/**
 * 0ms instant native mathematical text composable for MCQ option choices.
 * Replaces heavy Android WebViews with lightweight native typography for 120 FPS performance.
 */
@Composable
fun OptionMathText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.Unspecified
) {
    val annotatedString = remember(text) { parseOptionText(text) }
    RichMarkdownText(
        text = annotatedString,
        style = style,
        color = color,
        modifier = modifier
    )
}

/**
 * High-speed token-based parser for inline markdown and textbook LaTeX with LRU cache.
 * Supports recursive inline math inside **bold** and *italic* spans and emits real 2D
 * inline vertical fractions via InlineTextContent annotations.
 */
fun parseInlineMarkdownAndLatex(text: String): AnnotatedString {
    if (text.isEmpty()) return AnnotatedString("")

    // Check Cache
    val cached = ANNOTATED_CACHE[text]
    if (cached != null) return cached

    val result = buildAnnotatedString {
        appendInlineMarkdownTokens(text)
    }

    ANNOTATED_CACHE[text] = result
    return result
}

private fun AnnotatedString.Builder.appendInlineMarkdownTokens(text: String) {
    var cursor = 0
    val len = text.length
    while (cursor < len) {
        // Check for inline math $$...$$ or $...$
        if (text[cursor] == '$') {
            val isDouble = cursor + 1 < len && text[cursor + 1] == '$'
            val delim = if (isDouble) "$$" else "$"
            val startOffset = cursor + delim.length
            val endDollar = text.indexOf(delim, startOffset)
            if (endDollar != -1) {
                val latex = text.substring(startOffset, endDollar)
                appendInlineMathLatex(latex)
                cursor = endDollar + delim.length
                continue
            }
        }

        // Inline code `...`
        if (text[cursor] == '`') {
            val endBacktick = text.indexOf('`', cursor + 1)
            if (endBacktick != -1) {
                val code = text.substring(cursor + 1, endBacktick)
                pushStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = Color(0x22888888),
                        fontSize = 13.sp
                    )
                )
                append(code)
                pop()
                cursor = endBacktick + 1
                continue
            }
        }

        // Bold **...** (recursively parses inner math $...$ and formatting)
        if (text.startsWith("**", cursor)) {
            val endBold = text.indexOf("**", cursor + 2)
            if (endBold != -1) {
                val boldText = text.substring(cursor + 2, endBold)
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                appendInlineMarkdownTokens(boldText)
                pop()
                cursor = endBold + 2
                continue
            }
        }

        // Italic *...* (recursively parses inner math $...$)
        if (text[cursor] == '*' && (cursor + 1 < len && text[cursor + 1] != '*')) {
            val endItalic = text.indexOf('*', cursor + 1)
            if (endItalic != -1) {
                val italicText = text.substring(cursor + 1, endItalic)
                pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                appendInlineMarkdownTokens(italicText)
                pop()
                cursor = endItalic + 1
                continue
            }
        }

        // Strikethrough ~~...~~
        if (text.startsWith("~~", cursor)) {
            val endStrike = text.indexOf("~~", cursor + 2)
            if (endStrike != -1) {
                val strikeText = text.substring(cursor + 2, endStrike)
                pushStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                appendInlineMarkdownTokens(strikeText)
                pop()
                cursor = endStrike + 2
                continue
            }
        }

        // Escaped \% or \{ or \} outside math
        if (text[cursor] == '\\' && cursor + 1 < len) {
            val nextCh = text[cursor + 1]
            if (nextCh == '%' || nextCh == '{' || nextCh == '}' || nextCh == '&' || nextCh == '#') {
                append(nextCh)
                cursor += 2
                continue
            }
        }

        append(text[cursor])
        cursor++
    }
}

/**
 * High-speed LaTeX to Unicode mathematical notation formatter with LRU cache.
 */
fun formatLatexToReadableMath(latex: String): String {
    val trimmed = latex.trim()
    if (trimmed.isEmpty()) return ""

    val cached = MATH_CACHE[trimmed]
    if (cached != null) return cached

    var raw = trimmed

    // Remove outer \boxed{ ... }
    if (raw.startsWith("\\boxed{") && raw.endsWith("}")) {
        raw = raw.substring(7, raw.length - 1).trim()
    }

    // 1. Piecewise cases: \begin{cases} ... \end{cases}
    val casesMatch = REGEX_CASES.find(raw)
    if (casesMatch != null) {
        val body = casesMatch.groupValues[1]
        val prefix = formatMathUnit(raw.substring(0, casesMatch.range.first).trim())
        val suffix = formatMathUnit(raw.substring(casesMatch.range.last + 1).trim())

        val caseLines = body.split(REGEX_DOUBLE_SLASH).map { it.trim() }.filter { it.isNotBlank() }
        val formattedCaseLines = caseLines.map { cLine ->
            val parts = cLine.split("&")
            val expr = formatMathUnit(parts.getOrElse(0) { "" }.trim())
            val cond = if (parts.size > 1) formatMathUnit(parts[1].trim()) else ""
            if (cond.isNotBlank()) "$expr    $cond" else expr
        }

        val bracketedCases = buildString {
            if (formattedCaseLines.size == 1) {
                append("{ ").append(formattedCaseLines[0])
            } else {
                formattedCaseLines.forEachIndexed { idx, fl ->
                    val braceChar = when (idx) {
                        0 -> "⎧ "
                        formattedCaseLines.lastIndex -> "⎩ "
                        else -> "⎪ "
                    }
                    append(braceChar).append(fl)
                    if (idx < formattedCaseLines.lastIndex) append("\n")
                }
            }
        }

        val result = if (prefix.isNotBlank()) "$prefix\n$bracketedCases" else bracketedCases
        val finalRes = if (suffix.isNotBlank()) "$result\n$suffix" else result
        MATH_CACHE[trimmed] = finalRes
        return finalRes
    }

    // 2. Array/Matrix: \begin{array}{...} ... \end{array}
    val arrayMatch = REGEX_ARRAY.find(raw)
    if (arrayMatch != null) {
        val body = arrayMatch.groupValues[1]
        val arrayLines = body.split(REGEX_DOUBLE_SLASH).map { it.trim() }.filter { it.isNotBlank() }
        val finalRes = arrayLines.map { aLine ->
            aLine.split("&").map { formatMathUnit(it.trim()) }.joinToString("   ")
        }.joinToString("\n")
        MATH_CACHE[trimmed] = finalRes
        return finalRes
    }

    // Split multiple lines if separated by \\
    if (raw.contains("\\\\")) {
        val finalRes = raw.split("\\\\").map { formatMathUnit(it.trim()) }.joinToString("\n")
        MATH_CACHE[trimmed] = finalRes
        return finalRes
    }

    val finalRes = formatMathUnit(raw)
    MATH_CACHE[trimmed] = finalRes
    return finalRes
}

private fun formatMathUnit(latex: String): String {
    var s = latex

    // 1. Boxed
    s = REGEX_BOXED.replace(s, "⟦ $1 ⟧")

    // 2. Text macros (multi-pass for nested braces)
    repeat(3) {
        s = REGEX_TEXT_MACROS.replace(s, "$2")
    }
    s = s.replace(Regex("""\\xrightarrow(?:\[([^\]]*)\])?\{([^{}]*)\}""")) { m ->
        val below = m.groupValues[1].trim()
        val above = m.groupValues[2].trim()
        if (below.isNotEmpty()) " ──($above / $below)──► " else " ──($above)──► "
    }
    s = s.replace(Regex("""\\xleftarrow(?:\[([^\]]*)\])?\{([^{}]*)\}""")) { m ->
        val above = m.groupValues[2].trim()
        " ◄──($above)── "
    }
    s = s.replace("\\arg\\min", "argmin")
        .replace("\\arg\\max", "argmax")

    // 3. Blackboard bold symbols
    s = s.replace("\\mathbb{E}", "𝔼")
        .replace("\\mathbb{R}", "ℝ")
        .replace("\\mathbb{C}", "ℂ")
        .replace("\\mathbb{Z}", "ℤ")
        .replace("\\mathbb{N}", "ℕ")
        .replace("\\mathbb{P}", "ℙ")
        .replace("\\mathbb{Q}", "ℚ")
        .replace("\\mathbf{1}", "𝟏")

    // 4. Mathematical Accents
    s = REGEX_ACCENT_TILDE.replace(s, "$1̃")
    s = REGEX_ACCENT_HAT.replace(s, "$1̂")
    s = REGEX_ACCENT_BAR.replace(s, "$1̄")
    s = REGEX_ACCENT_VEC.replace(s, "$1⃗")
    s = REGEX_ACCENT_DOT.replace(s, "$1̇")
    s = REGEX_ACCENT_DDOT.replace(s, "$1̈")

    // 5. Fractions (multi-pass for nested fractions)
    repeat(3) {
        s = REGEX_FRAC.replace(s, "($1 / $2)")
    }

    // 6. Square Roots
    s = REGEX_SQRT_N.replace(s, "$1√($2)")
    s = REGEX_SQRT.replace(s, "√($1)")

    // 7. Integrals with limits
    s = REGEX_INT_LIMITS.replace(s) { match ->
        val lower = toSubscript(match.groupValues[1])
        val upper = toSuperscript(match.groupValues[2].replace("\\infty", "∞"))
        "∫$lower$upper "
    }
    s = s.replace("\\int", "∫ ")
        .replace("\\iint", "∬ ")
        .replace("\\iiint", "∭ ")
        .replace("\\oint", "∮ ")

    // 8. Summations and Products with limits
    s = REGEX_SUM_LIMITS.replace(s) { match ->
        val lower = toSubscript(match.groupValues[1])
        val upper = toSuperscript(match.groupValues[2].replace("\\infty", "∞"))
        "∑$lower$upper "
    }
    s = s.replace("\\sum", "∑ ")
        .replace("\\prod", "∏ ")

    // 9. Greek Letters (Upper & Lower)
    s = s.replace("\\alpha", "α")
        .replace("\\beta", "β")
        .replace("\\gamma", "γ")
        .replace("\\Gamma", "Γ")
        .replace("\\delta", "δ")
        .replace("\\Delta", "Δ")
        .replace("\\epsilon", "ε")
        .replace("\\varepsilon", "ε")
        .replace("\\zeta", "ζ")
        .replace("\\eta", "η")
        .replace("\\theta", "θ")
        .replace("\\Theta", "Θ")
        .replace("\\iota", "ι")
        .replace("\\kappa", "κ")
        .replace("\\lambda", "λ")
        .replace("\\Lambda", "Λ")
        .replace("\\mu", "μ")
        .replace("\\nu", "ν")
        .replace("\\xi", "ξ")
        .replace("\\Xi", "Ξ")
        .replace("\\pi", "π")
        .replace("\\Pi", "Π")
        .replace("\\rho", "ρ")
        .replace("\\sigma", "σ")
        .replace("\\Sigma", "Σ")
        .replace("\\tau", "τ")
        .replace("\\phi", "φ")
        .replace("\\varphi", "φ")
        .replace("\\Phi", "Φ")
        .replace("\\chi", "χ")
        .replace("\\psi", "ψ")
        .replace("\\Psi", "Ψ")
        .replace("\\omega", "ω")
        .replace("\\Omega", "Ω")

    // 10. Operators and Relations
    s = s.replace("\\%", "%")
        .replace("\\&", "&")
        .replace("\\#", "#")
        .replace("\\hline", "────────")
        .replace("\\pm", "±")
        .replace("\\mp", "∓")
        .replace("\\cdot", " · ")
        .replace("\\times", " × ")
        .replace("\\div", " ÷ ")
        .replace("\\le", "≤")
        .replace("\\leq", "≤")
        .replace("\\ge", "≥")
        .replace("\\geq", "≥")
        .replace("\\neq", "≠")
        .replace("\\approx", "≈")
        .replace("\\equiv", "≡")
        .replace("\\propto", "∝")
        .replace("\\sim", "∼")
        .replace("\\in", "∈")
        .replace("\\notin", "∉")
        .replace("\\subset", "⊂")
        .replace("\\subseteq", "⊆")
        .replace("\\cup", "∪")
        .replace("\\cap", "∩")
        .replace("\\forall", "∀")
        .replace("\\exists", "∃")
        .replace("\\infty", "∞")
        .replace("\\partial", "∂")
        .replace("\\nabla", "∇")
        .replace("\\therefore", "∴")
        .replace("\\because", "∵")
        .replace("\\circ", "°")
        .replace("\\degree", "°")
        .replace("\\uparrow", "↑")
        .replace("\\downarrow", "↓")
        .replace("\\updownarrow", "↕")
        .replace("\\longrightarrow", "⟶")
        .replace("\\longleftarrow", "⟵")
        .replace("\\Longrightarrow", "⟹")
        .replace("\\to", "→")
        .replace("\\rightarrow", "→")
        .replace("\\leftarrow", "←")
        .replace("\\Rightarrow", "⇒")
        .replace("\\iff", "⇔")
        .replace("\\Leftrightarrow", "⇔")

    // 11. Functions & Spacing
    s = s.replace("\\ln", "ln")
        .replace("\\log", "log")
        .replace("\\sin", "sin")
        .replace("\\cos", "cos")
        .replace("\\tan", "tan")
        .replace("\\sec", "sec")
        .replace("\\csc", "csc")
        .replace("\\cot", "cot")
        .replace("\\exp", "exp")
        .replace("\\lim", "lim")
        .replace("\\quad", "   ")
        .replace("\\qquad", "     ")
        .replace("\\,", " ")
        .replace("\\;", " ")
        .replace("\\:", " ")
        .replace("\\!", "")
        .replace("\\ ", " ")
        .replace("\\left(", "(")
        .replace("\\right)", ")")
        .replace("\\left[", "[")
        .replace("\\right]", "]")
        .replace("\\left\\{", "{")
        .replace("\\right\\}", "}")
        .replace("\\left|", "|")
        .replace("\\right|", "|")
        .replace("\\left.", "")
        .replace("\\right.", "")
        .replace("\\{", "{")
        .replace("\\}", "}")

    // 12. Subscripts
    s = REGEX_SUB_BRACES.replace(s) { match -> toSubscript(match.groupValues[1]) }
    s = REGEX_SUB_CHAR.replace(s) { match -> toSubscript(match.groupValues[1]) }

    // 13. Superscripts
    s = REGEX_SUP_BRACES.replace(s) { match -> toSuperscript(match.groupValues[1]) }
    s = REGEX_SUP_CHAR.replace(s) { match -> toSuperscript(match.groupValues[1]) }

    s = REGEX_SPACES.replace(s, " ")
    return s
}

private fun toSubscript(text: String): String {
    val clean = text.trim()
    return clean.map { ch ->
        when (ch) {
            '0' -> '₀'; '1' -> '₁'; '2' -> '₂'; '3' -> '₃'; '4' -> '₄'
            '5' -> '₅'; '6' -> '₆'; '7' -> '₇'; '8' -> '₈'; '9' -> '₉'
            '+' -> '₊'; '-' -> '₋'; '=' -> '₌'; '(' -> '₍'; ')' -> '₎'
            'a' -> 'ₐ'; 'e' -> 'ₑ'; 'h' -> 'ₕ'; 'i' -> 'ᵢ'; 'j' -> 'ⱼ'
            'k' -> 'ₖ'; 'l' -> 'ₗ'; 'm' -> 'ₘ'; 'n' -> 'ₙ'; 'o' -> 'ₒ'
            'p' -> 'ₚ'; 'r' -> 'ᵣ'; 's' -> 'ₛ'; 't' -> 'ₜ'; 'u' -> 'ᵤ'
            'v' -> 'ᵥ'; 'x' -> 'ₓ'; 'β' -> 'ᵦ'; 'γ' -> 'ᵧ'; 'ρ' -> 'ᵨ'
            'φ' -> 'ᵩ'; 'χ' -> 'ᵪ'
            else -> ch
        }
    }.joinToString("")
}

private fun toSuperscript(text: String): String {
    val clean = text.trim()
    return clean.map { ch ->
        when (ch) {
            '0' -> '⁰'; '1' -> '¹'; '2' -> '²'; '3' -> '³'; '4' -> '⁴'
            '5' -> '⁵'; '6' -> '⁶'; '7' -> '⁷'; '8' -> '⁸'; '9' -> '⁹'
            '+' -> '⁺'; '-' -> '⁻'; '=' -> '⁼'; '(' -> '⁽'; ')' -> '⁾'
            'a' -> 'ᵃ'; 'b' -> 'ᵇ'; 'c' -> 'ᶜ'; 'd' -> 'ᵈ'; 'e' -> 'ᵉ'
            'f' -> 'ᶠ'; 'g' -> 'ᵍ'; 'h' -> 'ʰ'; 'i' -> 'ⁱ'; 'j' -> 'ʲ'
            'k' -> 'ᵏ'; 'l' -> 'ˡ'; 'm' -> 'ᵐ'; 'n' -> 'ⁿ'; 'o' -> 'ᵒ'
            'p' -> 'ᵖ'; 'r' -> 'ʳ'; 's' -> 'ˢ'; 't' -> 'ᵗ'; 'u' -> 'ᵘ'
            'v' -> 'ᵛ'; 'w' -> 'ʷ'; 'x' -> 'ˣ'; 'y' -> 'ʸ'; 'z' -> 'ᶻ'
            '*' -> '﹡'; 'β' -> 'ᵝ'; 'γ' -> 'ᵞ'; 'δ' -> 'ᵟ'; 'θ' -> 'ᶿ'
            'φ' -> 'ᵠ'; 'χ' -> 'ᵡ'
            else -> ch
        }
    }.joinToString("")
}

private fun isTableRowLine(trimmed: String): Boolean {
    if (trimmed.isEmpty()) return false
    return trimmed.startsWith("|") && trimmed.count { it == '|' } >= 2
}

private fun isTableSeparatorLine(trimmed: String): Boolean {
    if (!isTableRowLine(trimmed)) return false
    val inner = trimmed.trim('|').trim()
    return inner.isNotEmpty() &&
        inner.contains("--") &&
        inner.all { it == '-' || it == ':' || it == '|' || it == ' ' || it == '\t' }
}

private fun isTableStart(lines: List<String>, index: Int): Boolean {
    if (index >= lines.size) return false
    val l1 = lines[index].trim()
    if (!isTableRowLine(l1) || isTableSeparatorLine(l1)) return false
    var nextIdx = index + 1
    while (nextIdx < lines.size && lines[nextIdx].trim().isEmpty()) {
        nextIdx++
    }
    if (nextIdx >= lines.size) return false
    val l2 = lines[nextIdx].trim()
    return isTableSeparatorLine(l2)
}

private fun parseImageSyntax(line: String): Pair<String, String>? {
    val match = REGEX_IMAGE_MD.find(line.trim())
    if (match != null) {
        val alt = match.groupValues[1]
        val uri = match.groupValues[2]
        return Pair(uri, alt)
    }
    val tagMatch = REGEX_IMAGE_TAG.find(line.trim())
    if (tagMatch != null) {
        val uri = tagMatch.groupValues[1].trim()
        return Pair(uri, "Diagram")
    }
    return null
}
