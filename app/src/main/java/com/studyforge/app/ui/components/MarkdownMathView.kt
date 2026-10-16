package com.studyforge.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Full formatting component supporting LaTeX math formulas ($...$ and $$...$$),
 * Markdown syntax (bold, italic, headers, bullet/numbered lists, code blocks),
 * and responsive tables.
 */
@Composable
fun MarkdownMathView(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: TextUnit = 15.sp,
    lineHeight: TextUnit = 22.sp
) {
    if (text.isBlank()) return

    val blocks = remember(text) { parseContentBlocks(text) }

    Column(modifier = modifier) {
        blocks.forEach { block ->
            when (block) {
                is ContentBlock.Header -> {
                    val hSize = when (block.level) {
                        1 -> 20.sp
                        2 -> 18.sp
                        else -> 16.sp
                    }
                    Text(
                        text = formatMathAndMarkdown(block.text),
                        fontSize = hSize,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                is ContentBlock.Paragraph -> {
                    Text(
                        text = formatMathAndMarkdown(block.text),
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        color = color,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                is ContentBlock.MathBlock -> {
                    MathBlockCard(latex = block.latex)
                }
                is ContentBlock.BulletItem -> {
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "• ",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = fontSize
                        )
                        Text(
                            text = formatMathAndMarkdown(block.text),
                            fontSize = fontSize,
                            lineHeight = lineHeight,
                            color = color
                        )
                    }
                }
                is ContentBlock.NumberedItem -> {
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${block.number}. ",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = fontSize
                        )
                        Text(
                            text = formatMathAndMarkdown(block.text),
                            fontSize = fontSize,
                            lineHeight = lineHeight,
                            color = color
                        )
                    }
                }
                is ContentBlock.Table -> {
                    MarkdownTable(block)
                }
            }
        }
    }
}

@Composable
fun MathBlockCard(latex: String) {
    val mathFormatted = remember(latex) { formatLatexToReadableMath(latex) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = mathFormatted,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Serif,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MarkdownTable(table: ContentBlock.Table) {
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
    ) {
        Column(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Header Row
            if (table.headers.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    table.headers.forEachIndexed { index, header ->
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = formatMathAndMarkdown(header),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Data Rows
            table.rows.forEachIndexed { rowIndex, rowCells ->
                val rowBg = if (rowIndex % 2 == 0) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                }
                Row(
                    modifier = Modifier
                        .background(rowBg)
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    rowCells.forEachIndexed { colIndex, cellText ->
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = formatMathAndMarkdown(cellText),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

sealed class ContentBlock {
    data class Header(val level: Int, val text: String) : ContentBlock()
    data class Paragraph(val text: String) : ContentBlock()
    data class MathBlock(val latex: String) : ContentBlock()
    data class BulletItem(val text: String) : ContentBlock()
    data class NumberedItem(val number: String, val text: String) : ContentBlock()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : ContentBlock()
}

private fun parseContentBlocks(raw: String): List<ContentBlock> {
    val blocks = mutableListOf<ContentBlock>()
    val lines = raw.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()

        if (trimmed.isEmpty()) {
            i++
            continue
        }

        // Display Math block: $$ ... $$
        if (trimmed.startsWith("$$")) {
            if (trimmed.endsWith("$$") && trimmed.length > 4) {
                blocks.add(ContentBlock.MathBlock(trimmed.removeSurrounding("$$").trim()))
                i++
                continue
            } else {
                val mathLines = mutableListOf<String>()
                mathLines.add(trimmed.removePrefix("$$"))
                i++
                while (i < lines.size && !lines[i].trim().endsWith("$$")) {
                    mathLines.add(lines[i])
                    i++
                }
                if (i < lines.size) {
                    mathLines.add(lines[i].trim().removeSuffix("$$"))
                    i++
                }
                blocks.add(ContentBlock.MathBlock(mathLines.joinToString("\n").trim()))
                continue
            }
        }

        // Table detection: line starts with | and next line has |---
        if (trimmed.startsWith("|") && trimmed.endsWith("|")) {
            val tableLines = mutableListOf<String>()
            while (i < lines.size && lines[i].trim().startsWith("|") && lines[i].trim().endsWith("|")) {
                tableLines.add(lines[i].trim())
                i++
            }
            if (tableLines.size >= 2) {
                val headers = tableLines[0].split("|")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                val separatorIndex = if (tableLines[1].contains("---")) 1 else -1
                val rowLines = if (separatorIndex != -1) tableLines.drop(2) else tableLines.drop(1)
                val rows = rowLines.map { rowLine ->
                    rowLine.split("|")
                        .map { it.trim() }
                        .filterIndexed { idx, _ -> idx > 0 && idx <= headers.size }
                }
                blocks.add(ContentBlock.Table(headers, rows))
                continue
            } else {
                // Not a valid table, treat as paragraph
                tableLines.forEach { blocks.add(ContentBlock.Paragraph(it)) }
                continue
            }
        }

        // Headers
        if (trimmed.startsWith("### ")) {
            blocks.add(ContentBlock.Header(3, trimmed.removePrefix("### ").trim()))
            i++
            continue
        } else if (trimmed.startsWith("## ")) {
            blocks.add(ContentBlock.Header(2, trimmed.removePrefix("## ").trim()))
            i++
            continue
        } else if (trimmed.startsWith("# ")) {
            blocks.add(ContentBlock.Header(1, trimmed.removePrefix("# ").trim()))
            i++
            continue
        }

        // Bullet lists
        if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
            blocks.add(ContentBlock.BulletItem(trimmed.substring(2).trim()))
            i++
            continue
        }

        // Numbered lists (e.g. "1. ")
        val numMatch = Regex("""^(\d+)\.\s+(.*)$""").find(trimmed)
        if (numMatch != null) {
            val num = numMatch.groupValues[1]
            val rest = numMatch.groupValues[2]
            blocks.add(ContentBlock.NumberedItem(num, rest))
            i++
            continue
        }

        // Regular paragraph
        blocks.add(ContentBlock.Paragraph(trimmed))
        i++
    }

    return blocks
}

/**
 * Formats inline math ($...$) and Markdown bold (**...**) and italic (*...*)
 */
fun formatMathAndMarkdown(input: String): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val length = input.length

        while (cursor < length) {
            // Check for inline math: $...$
            if (input[cursor] == '$' && (cursor == 0 || input[cursor - 1] != '\\')) {
                val nextDollar = input.indexOf('$', cursor + 1)
                if (nextDollar != -1) {
                    val rawLatex = input.substring(cursor + 1, nextDollar)
                    val readableMath = formatLatexToReadableMath(rawLatex)
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF00796B)
                        )
                    ) {
                        append(readableMath)
                    }
                    cursor = nextDollar + 1
                    continue
                }
            }

            // Check for bold: **...**
            if (cursor + 1 < length && input[cursor] == '*' && input[cursor + 1] == '*') {
                val nextStars = input.indexOf("**", cursor + 2)
                if (nextStars != -1) {
                    val boldContent = input.substring(cursor + 2, nextStars)
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(formatInlineMathOnly(boldContent))
                    }
                    cursor = nextStars + 2
                    continue
                }
            }

            // Check for italic: *...*
            if (input[cursor] == '*' && (cursor + 1 >= length || input[cursor + 1] != '*')) {
                val nextStar = input.indexOf('*', cursor + 1)
                if (nextStar != -1) {
                    val italicContent = input.substring(cursor + 1, nextStar)
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(formatInlineMathOnly(italicContent))
                    }
                    cursor = nextStar + 1
                    continue
                }
            }

            // Check for inline code: `...`
            if (input[cursor] == '`') {
                val nextBacktick = input.indexOf('`', cursor + 1)
                if (nextBacktick != -1) {
                    val codeContent = input.substring(cursor + 1, nextBacktick)
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color.LightGray.copy(alpha = 0.3f)
                        )
                    ) {
                        append(" $codeContent ")
                    }
                    cursor = nextBacktick + 1
                    continue
                }
            }

            append(input[cursor])
            cursor++
        }
    }
}

private fun formatInlineMathOnly(text: String): String {
    // If text contains $...$, replace with converted math symbols
    val regex = Regex("""\$([^$]+)\$""")
    return regex.replace(text) { match ->
        formatLatexToReadableMath(match.groupValues[1])
    }
}

/**
 * Converts common LaTeX math and KaTeX expressions to readable Unicode math symbols.
 */
fun formatLatexToReadableMath(latex: String): String {
    var s = latex.trim()

    // Remove \text{...} wrappers
    s = s.replace(Regex("""\\text\{([^}]+)\}""")) { it.groupValues[1] }
    s = s.replace(Regex("""\\mathrm\{([^}]+)\}""")) { it.groupValues[1] }
    s = s.replace(Regex("""\\mathbf\{([^}]+)\}""")) { it.groupValues[1] }

    // Fractions: \frac{a}{b} -> (a / b)
    s = s.replace(Regex("""\\frac\{([^}]+)\}\{([^}]+)\}""")) {
        val num = it.groupValues[1].trim()
        val den = it.groupValues[2].trim()
        if (num.length <= 4 && den.length <= 4) "$num/$den" else "($num)/($den)"
    }

    // Square root: \sqrt{x} -> √(x)
    s = s.replace(Regex("""\\sqrt\{([^}]+)\}""")) { "√(${it.groupValues[1].trim()})" }
    s = s.replace(Regex("""\\sqrt\[([^\]]+)\]\{([^}]+)\}""")) { "${it.groupValues[1]}√(${it.groupValues[2].trim()})" }

    // Limits & Calculus
    s = s.replace(Regex("""\\lim_\{([^}]+)\}""")) { "lim[${it.groupValues[1]}] " }
    s = s.replace(Regex("""\\to\s*"""), "→")
    s = s.replace(Regex("""\\cdot\s*"""), " · ")
    s = s.replace(Regex("""\\times\s*"""), " × ")
    s = s.replace(Regex("""\\pm\s*"""), " ± ")
    s = s.replace(Regex("""\\leq\s*"""), " ≤ ")
    s = s.replace(Regex("""\\geq\s*"""), " ≥ ")
    s = s.replace(Regex("""\\neq\s*"""), " ≠ ")
    s = s.replace(Regex("""\\approx\s*"""), " ≈ ")
    s = s.replace(Regex("""\\infty\s*"""), "∞")
    s = s.replace(Regex("""\\sum\s*"""), "∑")
    s = s.replace(Regex("""\\int\s*"""), "∫")

    // Common Greek symbols
    val greekMap = mapOf(
        "\\theta" to "θ", "\\Theta" to "Θ",
        "\\alpha" to "α", "\\beta" to "β",
        "\\gamma" to "γ", "\\Gamma" to "Γ",
        "\\delta" to "δ", "\\Delta" to "Δ",
        "\\lambda" to "λ", "\\Lambda" to "Λ",
        "\\mu" to "μ", "\\pi" to "π", "\\Pi" to "Π",
        "\\sigma" to "σ", "\\Sigma" to "Σ",
        "\\omega" to "ω", "\\Omega" to "Ω",
        "\\phi" to "φ", "\\Phi" to "Φ",
        "\\rho" to "ρ", "\\tau" to "τ",
        "\\epsilon" to "ε"
    )
    for ((latexCode, unicodeChar) in greekMap) {
        s = s.replace(latexCode, unicodeChar)
    }

    // Common functions
    s = s.replace(Regex("""\\sin\s*"""), "sin ")
    s = s.replace(Regex("""\\cos\s*"""), "cos ")
    s = s.replace(Regex("""\\tan\s*"""), "tan ")
    s = s.replace(Regex("""\\ln\s*"""), "ln ")
    s = s.replace(Regex("""\\log\s*"""), "log ")

    // Superscripts and subscripts
    // Convert common powers: ^2 -> ², ^3 -> ³, ^n -> ⁿ
    s = s.replace("^2", "²")
    s = s.replace("^3", "³")
    s = s.replace("^0", "⁰")
    s = s.replace("^1", "¹")
    s = s.replace("^4", "⁴")
    s = s.replace("^5", "⁵")
    s = s.replace("^n", "ⁿ")
    s = s.replace("^-1", "⁻¹")

    // Convert brackets: \{ and \} -> { and }
    s = s.replace("\\{", "{").replace("\\}", "}")
    s = s.replace("\\,", " ").replace("\\;", " ").replace("\\!", "")

    return s
}
