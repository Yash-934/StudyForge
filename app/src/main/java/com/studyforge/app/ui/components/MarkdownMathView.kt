package com.studyforge.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyforge.app.ui.theme.ErrorRed
import com.studyforge.app.ui.theme.PurpleAccent
import com.studyforge.app.ui.theme.SuccessGreen
import com.studyforge.app.ui.theme.WarningYellow

/**
 * High-performance Markdown and Mathematical Notation renderer for StudyForge.
 * Supports LaTeX math, callouts, lists, checklists, code blocks, and formatted text.
 */
@Composable
fun MarkdownMathView(
    markdownText: String,
    modifier: Modifier = Modifier
) {
    if (markdownText.isBlank()) return

    val lines = markdownText.lines()
    var i = 0

    Column(modifier = modifier) {
        while (i < lines.size) {
            val line = lines[i]

            // 1. Math Block: $$ ... $$
            if (line.trim().startsWith("$$")) {
                val mathContent = StringBuilder()
                val inlineBlock = line.trim().removePrefix("$$").removeSuffix("$$")
                if (line.trim() != "$$" && line.trim().endsWith("$$") && line.trim().length > 4) {
                    mathContent.append(inlineBlock)
                    i++
                } else {
                    i++
                    while (i < lines.size && !lines[i].trim().endsWith("$$")) {
                        mathContent.appendLine(lines[i])
                        i++
                    }
                    if (i < lines.size) {
                        mathContent.append(lines[i].replace("$$", ""))
                        i++
                    }
                }
                BlockMathCard(rawLatex = mathContent.toString().trim())
                Spacer(modifier = Modifier.height(10.dp))
                continue
            }

            // 2. Code Block: ``` ... ```
            if (line.trim().startsWith("```")) {
                val codeBuilder = StringBuilder()
                i++
                while (i < lines.size && !lines[i].trim().startsWith("```")) {
                    codeBuilder.appendLine(lines[i])
                    i++
                }
                if (i < lines.size) i++ // skip ending ```
                CodeBlockCard(code = codeBuilder.toString())
                Spacer(modifier = Modifier.height(10.dp))
                continue
            }

            // 3. Special Callouts: [IMPORTANT], [FORMULA], [CONCEPT], [EXAMPLE], [COMMON MISTAKE], [REMEMBER]
            val upperLine = line.trim().uppercase()
            if (upperLine.startsWith("[IMPORTANT]") || upperLine.startsWith("[FORMULA]") ||
                upperLine.startsWith("[CONCEPT]") || upperLine.startsWith("[EXAMPLE]") ||
                upperLine.startsWith("[COMMON MISTAKE]") || upperLine.startsWith("[REMEMBER]")
            ) {
                val tag = when {
                    upperLine.startsWith("[IMPORTANT]") -> "IMPORTANT"
                    upperLine.startsWith("[FORMULA]") -> "FORMULA"
                    upperLine.startsWith("[CONCEPT]") -> "CONCEPT"
                    upperLine.startsWith("[EXAMPLE]") -> "EXAMPLE"
                    upperLine.startsWith("[COMMON MISTAKE]") -> "COMMON MISTAKE"
                    else -> "REMEMBER"
                }
                val inlineFirstLine = line.trim().substringAfter("]", "").trim()
                val calloutLines = mutableListOf<String>()
                if (inlineFirstLine.isNotBlank()) calloutLines.add(inlineFirstLine)
                i++
                while (i < lines.size && lines[i].isNotBlank() && !lines[i].startsWith("[") && !lines[i].startsWith("#")) {
                    calloutLines.add(lines[i])
                    i++
                }
                CalloutCard(tag = tag, content = calloutLines.joinToString("\n"))
                Spacer(modifier = Modifier.height(10.dp))
                continue
            }

            // 4. Headings
            when {
                line.startsWith("# ") -> {
                    HeadingText(text = line.removePrefix("# ").trim(), level = 1)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                line.startsWith("## ") -> {
                    HeadingText(text = line.removePrefix("## ").trim(), level = 2)
                    Spacer(modifier = Modifier.height(6.dp))
                }
                line.startsWith("### ") -> {
                    HeadingText(text = line.removePrefix("### ").trim(), level = 3)
                    Spacer(modifier = Modifier.height(6.dp))
                }
                line.startsWith("#### ") -> {
                    HeadingText(text = line.removePrefix("#### ").trim(), level = 4)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                line.startsWith("- [x] ") || line.startsWith("- [X] ") -> {
                    ChecklistItem(checked = true, text = line.substring(6))
                    Spacer(modifier = Modifier.height(4.dp))
                }
                line.startsWith("- [ ] ") -> {
                    ChecklistItem(checked = false, text = line.substring(6))
                    Spacer(modifier = Modifier.height(4.dp))
                }
                line.startsWith("- ") || line.startsWith("* ") -> {
                    BulletListItem(text = line.substring(2))
                    Spacer(modifier = Modifier.height(4.dp))
                }
                line.matches(Regex("^\\d+\\.\\s+.*")) -> {
                    val number = line.substringBefore(".").trim()
                    val content = line.substringAfter(". ").trim()
                    NumberedListItem(number = number, text = content)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                line.startsWith("> ") -> {
                    BlockquoteCard(text = line.removePrefix("> ").trim())
                    Spacer(modifier = Modifier.height(6.dp))
                }
                line.trim() == "---" || line.trim() == "***" -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                }
                line.isNotBlank() -> {
                    val annotated = parseInlineMarkdownAndLatex(line)
                    Text(
                        text = annotated,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                else -> {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
            i++
        }
    }
}

@Composable
fun BlockMathCard(rawLatex: String) {
    val formatted = formatLatexToReadableMath(rawLatex)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Calculate,
                contentDescription = "Math",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = formatted,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CalloutCard(tag: String, content: String) {
    val (bgColor, borderColor, icon, title) = when (tag) {
        "IMPORTANT" -> CalloutStyle(
            ErrorRed.copy(alpha = 0.12f),
            ErrorRed,
            Icons.Default.ErrorOutline,
            "Important"
        )
        "FORMULA" -> CalloutStyle(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            MaterialTheme.colorScheme.primary,
            Icons.Default.Calculate,
            "Key Formula"
        )
        "CONCEPT" -> CalloutStyle(
            SuccessGreen.copy(alpha = 0.12f),
            SuccessGreen,
            Icons.Default.Lightbulb,
            "Core Concept"
        )
        "EXAMPLE" -> CalloutStyle(
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
            MaterialTheme.colorScheme.secondary,
            Icons.AutoMirrored.Filled.MenuBook,
            "Example Problem"
        )
        "COMMON MISTAKE" -> CalloutStyle(
            WarningYellow.copy(alpha = 0.14f),
            WarningYellow,
            Icons.Default.Warning,
            "Common Mistake"
        )
        else -> CalloutStyle(
            PurpleAccent.copy(alpha = 0.12f),
            PurpleAccent,
            Icons.Default.Bookmark,
            "Remember"
        )
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = borderColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = borderColor
                )
            }
            if (content.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                val annotated = parseInlineMarkdownAndLatex(content)
                Text(
                    text = annotated,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

private data class CalloutStyle(
    val bg: Color,
    val border: Color,
    val icon: ImageVector,
    val title: String
)

@Composable
fun HeadingText(text: String, level: Int) {
    val style = when (level) {
        1 -> MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        2 -> MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
        3 -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        else -> MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
    }
    val color = when (level) {
        1 -> MaterialTheme.colorScheme.primary
        2 -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(if (level == 1) 24.dp else 18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = style, color = color)
    }
}

@Composable
fun BulletListItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 9.dp)
                .size(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = parseInlineMarkdownAndLatex(text),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun NumberedListItem(number: String, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$number.",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(26.dp)
        )
        Text(
            text = parseInlineMarkdownAndLatex(text),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ChecklistItem(checked: Boolean, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (checked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
            contentDescription = if (checked) "Checked" else "Unchecked",
            tint = if (checked) SuccessGreen else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = parseInlineMarkdownAndLatex(text),
            style = MaterialTheme.typography.bodyLarge.copy(
                textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = if (checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun BlockquoteCard(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.secondary)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = parseInlineMarkdownAndLatex(text),
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CodeBlockCard(code: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
    ) {
        Text(
            text = code.trimEnd(),
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(12.dp)
        )
    }
}

/**
 * Parses inline Markdown (bold, italic, code, LaTeX math $...$) into an AnnotatedString.
 */
fun parseInlineMarkdownAndLatex(text: String): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        while (cursor < text.length) {
            // Check for inline math $...$
            if (text[cursor] == '$' && cursor + 1 < text.length && text[cursor + 1] != '$') {
                val endDollar = text.indexOf('$', cursor + 1)
                if (endDollar != -1) {
                    val latex = text.substring(cursor + 1, endDollar)
                    val formattedMath = formatLatexToReadableMath(latex)
                    pushStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6366F1)
                        )
                    )
                    append(formattedMath)
                    pop()
                    cursor = endDollar + 1
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

            // Bold **...**
            if (text.startsWith("**", cursor)) {
                val endBold = text.indexOf("**", cursor + 2)
                if (endBold != -1) {
                    val boldText = text.substring(cursor + 2, endBold)
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(boldText)
                    pop()
                    cursor = endBold + 2
                    continue
                }
            }

            // Italic *...*
            if (text[cursor] == '*' && (cursor + 1 < text.length && text[cursor + 1] != '*')) {
                val endItalic = text.indexOf('*', cursor + 1)
                if (endItalic != -1) {
                    val italicText = text.substring(cursor + 1, endItalic)
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(italicText)
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
                    append(strikeText)
                    pop()
                    cursor = endStrike + 2
                    continue
                }
            }

            append(text[cursor])
            cursor++
        }
    }
}

/**
 * Transforms LaTeX expressions into readable mathematical typography Unicode symbols.
 */
fun formatLatexToReadableMath(latex: String): String {
    var s = latex
        // Fractions: \frac{a}{b} -> (a / b)
        .replace(Regex("""\\frac\{([^}]+)\}\{([^}]+)\}"""), "($1 / $2)")
        // Square roots: \sqrt{a} -> √(a)
        .replace(Regex("""\\sqrt\{([^}]+)\}"""), "√($1)")
        // Subscripts: x_{1} -> x₁, x_1 -> x₁
        .replace(Regex("""_\{?([0-9])\}?""")) { match ->
            when (match.groupValues[1]) {
                "0" -> "₀"; "1" -> "₁"; "2" -> "₂"; "3" -> "₃"; "4" -> "₄"
                "5" -> "₅"; "6" -> "₆"; "7" -> "₇"; "8" -> "₈"; "9" -> "₉"
                else -> match.value
            }
        }
        // Superscripts: x^{2} -> x², x^2 -> x²
        .replace(Regex("""\^\{?([0-9n\+\-])\}?""")) { match ->
            when (match.groupValues[1]) {
                "0" -> "⁰"; "1" -> "¹"; "2" -> "²"; "3" -> "³"; "4" -> "⁴"
                "5" -> "⁵"; "6" -> "⁶"; "7" -> "⁷"; "8" -> "⁸"; "9" -> "⁹"
                "n" -> "ⁿ"; "+" -> "⁺"; "-" -> "⁻"
                else -> match.value
            }
        }
        // Integrals, Summations, Limits
        .replace("\\int_{a}^{b}", "∫ₐᵇ")
        .replace("\\int_0^", "∫₀^")
        .replace("\\int", "∫")
        .replace("\\sum", "∑")
        .replace("\\prod", "∏")
        .replace("\\lim_{x \\to 0}", "lim(x→0)")
        .replace("\\lim_{x \\to \\infty}", "lim(x→∞)")
        .replace("\\lim", "lim")
        // Greek letters
        .replace("\\alpha", "α")
        .replace("\\beta", "β")
        .replace("\\gamma", "γ")
        .replace("\\delta", "δ")
        .replace("\\Delta", "Δ")
        .replace("\\epsilon", "ε")
        .replace("\\theta", "θ")
        .replace("\\pi", "π")
        .replace("\\sigma", "σ")
        .replace("\\lambda", "λ")
        .replace("\\omega", "ω")
        .replace("\\Omega", "Ω")
        .replace("\\infty", "∞")
        // Math operators and symbols
        .replace("\\pm", "±")
        .replace("\\times", "×")
        .replace("\\cdot", "·")
        .replace("\\le", "≤")
        .replace("\\ge", "≥")
        .replace("\\neq", "≠")
        .replace("\\approx", "≈")
        .replace("\\partial", "∂")
        .replace("\\nabla", "∇")
        .replace("\\in", "∈")
        .replace("\\subset", "⊂")
        .replace("\\quad", "   ")
        .replace("\\,", " ")
        .replace("\\left(", "(")
        .replace("\\right)", ")")
        .replace("\\left[", "[")
        .replace("\\right]", "]")
        .replace("\\{", "{")
        .replace("\\}", "}")

    return s
}
