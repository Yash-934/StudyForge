package com.studyforge.app.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

/**
 * Production Markdown, Table, and Mathematical Notation renderer for StudyForge.
 * Features:
 * - Textbook-quality LaTeX math rendering with Unicode symbols, subscripts, superscripts, piecewise cases, integrals, summations
 * - Markdown Tables with clean column alignments, zebra striping, and horizontal scrolling
 * - Monospace Code blocks with language detection and one-tap Copy button
 * - Zero-permission Image importing from gallery with Coil AsyncImage rendering and tap-to-zoom
 * - Headings, checklists, callouts, and inline styling
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

            // 1. Markdown Table: lines starting and containing '|'
            if (isTableStart(lines, i)) {
                val tableLines = mutableListOf<String>()
                while (i < lines.size && lines[i].trim().startsWith("|") && lines[i].trim().endsWith("|")) {
                    tableLines.add(lines[i].trim())
                    i++
                }
                MarkdownTableCard(tableLines = tableLines)
                Spacer(modifier = Modifier.height(12.dp))
                continue
            }

            // 2. Image Markdown: ![Alt](url_or_uri) or [IMAGE: uri]
            val imageMatch = parseImageSyntax(line)
            if (imageMatch != null) {
                MarkdownImageCard(uriString = imageMatch.first, altText = imageMatch.second)
                Spacer(modifier = Modifier.height(12.dp))
                i++
                continue
            }

            // 3. Math Block: $$ ... $$ or \begin{cases} ... \end{cases} or \begin{align} ... \end{align} or direct LaTeX equations
            if (line.trim().startsWith("$$") || line.trim().startsWith("\\begin{cases}") || line.trim().startsWith("\\begin{align}") ||
                (line.trim().startsWith("\\") && (line.contains("\\frac") || line.contains("\\int") || line.contains("\\sum") || line.contains("\\sqrt") || line.contains("=")))
            ) {
                val mathContent = StringBuilder()
                val isExplicitDollar = line.trim().startsWith("$$")
                if (isExplicitDollar) {
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
                } else if (line.trim().startsWith("\\begin{")) {
                    // \begin{...} block
                    val endTag = if (line.trim().startsWith("\\begin{cases}")) "\\end{cases}" else "\\end{align}"
                    while (i < lines.size) {
                        val curr = lines[i]
                        mathContent.appendLine(curr)
                        i++
                        if (curr.contains(endTag)) break
                    }
                } else {
                    // Single LaTeX formula line
                    mathContent.append(line.trim())
                    i++
                }
                BlockMathCard(rawLatex = mathContent.toString().trim())
                Spacer(modifier = Modifier.height(10.dp))
                continue
            }

            // 4. Code Block: ```lang ... ```
            if (line.trim().startsWith("```")) {
                val lang = line.trim().removePrefix("```").trim()
                val codeBuilder = StringBuilder()
                i++
                while (i < lines.size && !lines[i].trim().startsWith("```")) {
                    codeBuilder.appendLine(lines[i])
                    i++
                }
                if (i < lines.size) i++ // skip ending ```
                CodeBlockCard(code = codeBuilder.toString(), language = if (lang.isNotBlank()) lang else null)
                Spacer(modifier = Modifier.height(10.dp))
                continue
            }

            // 5. Special Callouts: [IMPORTANT], [FORMULA], [CONCEPT], [EXAMPLE], [COMMON MISTAKE], [REMEMBER]
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
                while (i < lines.size && lines[i].isNotBlank() && !lines[i].startsWith("[") && !lines[i].startsWith("#") && !lines[i].startsWith("|")) {
                    calloutLines.add(lines[i])
                    i++
                }
                CalloutCard(tag = tag, content = calloutLines.joinToString("\n"))
                Spacer(modifier = Modifier.height(10.dp))
                continue
            }

            // 6. Headings
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
                line.matches(Regex("""^\d+\.\s+.*""")) -> {
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
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
                }
                line.isNotBlank() -> {
                    val containsLatexOrMath = line.contains("\\") || line.contains("$") ||
                        (line.contains("^") && !line.startsWith(" ")) ||
                        line.contains("∫") || line.contains("∑") || line.contains("√") || line.contains("π")

                    if (containsLatexOrMath) {
                        MathRichTextView(
                            text = line,
                            fontSizeSp = 16,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        val annotated = parseInlineMarkdownAndLatex(line)
                        Text(
                            text = annotated,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp
                        )
                    }
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

/**
 * Checks if current line is the start of a Markdown Table (line 1 headers, line 2 separator).
 */
private fun isTableStart(lines: List<String>, index: Int): Boolean {
    if (index >= lines.size - 1) return false
    val first = lines[index].trim()
    val second = lines[index + 1].trim()
    if (!first.startsWith("|") || !first.endsWith("|")) return false
    // Separator line e.g. |---|---| or |:---|:---:|---:|
    return second.startsWith("|") && second.endsWith("|") && second.contains("---")
}

/**
 * Parses markdown image syntax: ![Alt text](uri) or [IMAGE: uri]
 */
private fun parseImageSyntax(line: String): Pair<String, String>? {
    val trimmed = line.trim()
    val mdImageRegex = Regex("""^!\[([^\]]*)\]\(([^)]+)\)$""")
    val match = mdImageRegex.find(trimmed)
    if (match != null) {
        val alt = match.groupValues[1]
        val uri = match.groupValues[2]
        return Pair(uri, alt)
    }
    if (trimmed.startsWith("[IMAGE:") && trimmed.endsWith("]")) {
        val uri = trimmed.removePrefix("[IMAGE:").removeSuffix("]").trim()
        return Pair(uri, "")
    }
    return null
}

/**
 * Beautiful textbook-quality Math Block matching the requested screenshot style.
 * Uses real KaTeX mathematical typesetting engine with proper fraction bars,
 * integral signs, Computer Modern typography, exponents, and limits.
 */
@Composable
fun BlockMathCard(
    rawLatex: String,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                RoundedCornerShape(18.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SquircleIconBadge(
                    icon = Icons.Default.Calculate,
                    accentColor = MaterialTheme.colorScheme.primary,
                    size = 36.dp,
                    iconSize = 20.dp
                )

                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(rawLatex))
                        Toast.makeText(context, "Formula copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy LaTeX",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // KaTeX High-Fidelity Math Typesetting
            MathFormulaView(
                latex = rawLatex,
                displayMode = true,
                fontSizeSp = 20,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Beautiful Markdown Table component with rounded borders, styled headers, and horizontal scrolling.
 */
@Composable
fun MarkdownTableCard(tableLines: List<String>) {
    if (tableLines.size < 2) return

    val headerLine = tableLines[0]
    val separatorLine = tableLines[1]
    val dataLines = tableLines.drop(2)

    val headers = headerLine.split("|").map { it.trim() }.filterIndexed { idx, s ->
        // ignore first and last empty splits from leading/trailing pipe
        !(idx == 0 && s.isEmpty()) && !(idx == headerLine.split("|").lastIndex && s.isEmpty())
    }

    val separators = separatorLine.split("|").map { it.trim() }.filterIndexed { idx, s ->
        !(idx == 0 && s.isEmpty()) && !(idx == separatorLine.split("|").lastIndex && s.isEmpty())
    }

    val alignments = separators.map { sep ->
        when {
            sep.startsWith(":") && sep.endsWith(":") -> TextAlign.Center
            sep.endsWith(":") -> TextAlign.End
            else -> TextAlign.Start
        }
    }

    val rows = dataLines.map { rowLine ->
        rowLine.split("|").map { it.trim() }.filterIndexed { idx, s ->
            !(idx == 0 && s.isEmpty()) && !(idx == rowLine.split("|").lastIndex && s.isEmpty())
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header Row
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                        .padding(vertical = 10.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    headers.forEachIndexed { idx, header ->
                        val align = alignments.getOrElse(idx) { TextAlign.Start }
                        Box(
                            modifier = Modifier
                                .widthIn(min = 120.dp, max = 260.dp)
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = parseInlineMarkdownAndLatex(header),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = align,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Data Rows
                rows.forEachIndexed { rIdx, rowCells ->
                    val rowBg = if (rIdx % 2 == 0) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(rowBg)
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        headers.indices.forEach { cIdx ->
                            val cellText = rowCells.getOrElse(cIdx) { "" }
                            val align = alignments.getOrElse(cIdx) { TextAlign.Start }
                            Box(
                                modifier = Modifier
                                    .widthIn(min = 120.dp, max = 260.dp)
                                    .padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = parseInlineMarkdownAndLatex(cellText),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = align,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    if (rIdx < rows.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
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
            // Header Bar
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

            // Code Content with Horizontal Scroll
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
 * Callout Card with colored badge and rounded modern shape.
 */
@Composable
fun CalloutCard(tag: String, content: String) {
    val (bgColor, borderColor, icon, title) = when (tag) {
        "IMPORTANT" -> CalloutStyle(
            ErrorRed.copy(alpha = 0.10f),
            ErrorRed,
            Icons.Default.ErrorOutline,
            "Important"
        )
        "FORMULA" -> CalloutStyle(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            MaterialTheme.colorScheme.primary,
            Icons.Default.Calculate,
            "Key Formula"
        )
        "CONCEPT" -> CalloutStyle(
            SuccessGreen.copy(alpha = 0.10f),
            SuccessGreen,
            Icons.Default.Lightbulb,
            "Core Concept"
        )
        "EXAMPLE" -> CalloutStyle(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
            MaterialTheme.colorScheme.secondary,
            Icons.AutoMirrored.Filled.MenuBook,
            "Example Problem"
        )
        "COMMON MISTAKE" -> CalloutStyle(
            WarningYellow.copy(alpha = 0.12f),
            WarningYellow,
            Icons.Default.Warning,
            "Common Mistake"
        )
        else -> CalloutStyle(
            PurpleAccent.copy(alpha = 0.10f),
            PurpleAccent,
            Icons.Default.Bookmark,
            "Remember"
        )
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = bgColor,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SquircleIconBadge(
                    icon = icon,
                    accentColor = borderColor,
                    size = 32.dp,
                    iconSize = 18.dp
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
            if (content.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
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
    val bgColor: Color,
    val borderColor: Color,
    val icon: ImageVector,
    val title: String
)

@Composable
fun HeadingText(text: String, level: Int) {
    val style = when (level) {
        1 -> MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        2 -> MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        3 -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        else -> MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(if (level <= 2) 20.dp else 16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = parseInlineMarkdownAndLatex(text),
            style = style,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun BulletListItem(text: String) {
    Row(modifier = Modifier.padding(start = 6.dp, top = 2.dp, bottom = 2.dp)) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = parseInlineMarkdownAndLatex(text),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun NumberedListItem(number: String, text: String) {
    Row(modifier = Modifier.padding(start = 6.dp, top = 2.dp, bottom = 2.dp)) {
        Text(
            text = "$number.",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 8.dp)
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
        modifier = Modifier.padding(vertical = 2.dp),
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
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = parseInlineMarkdownAndLatex(text),
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
                            color = Color(0xFF4F46E5)
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
 * Advanced LaTeX to textbook-quality Unicode Mathematical Notation formatter.
 * Handles:
 * - Piecewise cases: \begin{cases} ... \end{cases}
 * - Integrals with limits: \int_0^3, \int_a^b, \int
 * - Subscripts & Superscripts: x_1, x_{bid}, x^2, e^{-\beta(t-t_k)}, (1-C_t/C_{max})^+
 * - Greek letters: \Delta, \mu, \alpha, \beta, \sigma, \lambda, \rho, \Pi, etc.
 * - Blackboard bold: \mathbb{E}, \mathbb{R}, \mathbb{C}
 * - Mathematical accents: \tilde{m}, \hat{x}, \bar{x}, \mathbf{1}
 * - Operators: \cdot, \times, \pm, \le, \ge, \neq, \approx, \partial, \nabla, \infty
 * - Functions: \ln, \log, \sin, \cos, \tan, \exp, \lim, \sum, \prod
 */
fun formatLatexToReadableMath(latex: String): String {
    var raw = latex.trim()

    // 1. Piecewise cases: \begin{cases} ... \end{cases}
    val casesRegex = Regex("""\\begin\{cases\}([\s\S]*?)\\end\{cases\}""")
    val casesMatch = casesRegex.find(raw)
    if (casesMatch != null) {
        val body = casesMatch.groupValues[1]
        val prefix = formatMathUnit(raw.substring(0, casesMatch.range.first).trim())
        val suffix = formatMathUnit(raw.substring(casesMatch.range.last + 1).trim())

        val caseLines = body.split(Regex("""\\\\|\n""")).map { it.trim() }.filter { it.isNotBlank() }
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
        return if (suffix.isNotBlank()) "$result\n$suffix" else result
    }

    // Split multiple lines if separated by \\
    if (raw.contains("\\\\")) {
        return raw.split("\\\\").map { formatMathUnit(it.trim()) }.joinToString("\n")
    }

    return formatMathUnit(raw)
}

/**
 * Formats a single LaTeX mathematical statement into clean Unicode mathematical symbols.
 */
private fun formatMathUnit(latex: String): String {
    var s = latex

    // 1. Boxed: \boxed{...} -> ⟦ ... ⟧
    s = s.replace(Regex("""\\boxed\{([^}]+)\}"""), "⟦ $1 ⟧")

    // 2. Text macros: \text{...}, \mathrm{...}, \mathbf{...}, \mathit{...}
    s = s.replace(Regex("""\\text\{([^}]+)\}"""), "$1")
    s = s.replace(Regex("""\\mathrm\{([^}]+)\}"""), "$1")
    s = s.replace(Regex("""\\mathbf\{([^}]+)\}"""), "$1")
    s = s.replace(Regex("""\\mathit\{([^}]+)\}"""), "$1")

    // 3. Blackboard bold symbols: \mathbb{E} -> 𝔼, etc.
    s = s.replace("\\mathbb{E}", "𝔼")
        .replace("\\mathbb{R}", "ℝ")
        .replace("\\mathbb{C}", "ℂ")
        .replace("\\mathbb{Z}", "ℤ")
        .replace("\\mathbb{N}", "ℕ")
        .replace("\\mathbb{P}", "ℙ")
        .replace("\\mathbb{Q}", "ℚ")
        .replace("\\mathbf{1}", "𝟏")

    // 4. Mathematical Accents: \tilde{m} -> m̃, \hat{x} -> x̂, etc.
    s = s.replace(Regex("""\\tilde\{([a-zA-Z])\}"""), "$1̃")
        .replace(Regex("""\\hat\{([a-zA-Z])\}"""), "$1̂")
        .replace(Regex("""\\bar\{([a-zA-Z])\}"""), "$1̄")
        .replace(Regex("""\\vec\{([a-zA-Z])\}"""), "$1⃗")
        .replace(Regex("""\\dot\{([a-zA-Z])\}"""), "$1̇")
        .replace(Regex("""\\ddot\{([a-zA-Z])\}"""), "$1̈")

    // 5. Fractions: \frac{a}{b} -> (a / b)
    s = s.replace(Regex("""\\frac\{([^}]+)\}\{([^}]+)\}"""), "($1 / $2)")

    // 6. Square Roots: \sqrt[n]{x} -> ⁿ√(x), \sqrt{x} -> √(x)
    s = s.replace(Regex("""\\sqrt\[([^\]]+)\]\{([^}]+)\}"""), "$1√($2)")
        .replace(Regex("""\\sqrt\{([^}]+)\}"""), "√($1)")

    // 7. Integrals with limits: \int_0^3 -> ∫₀³, \int_{a}^{b} -> ∫ₐᵇ
    s = s.replace(Regex("""\\int_\{?([0-9a-zA-Z\+\-\*]+)\}?\^\{?([0-9a-zA-Z\+\-\*\\]+)\}?""")) { match ->
        val lower = toSubscript(match.groupValues[1])
        val upper = toSuperscript(match.groupValues[2].replace("\\infty", "∞"))
        "∫$lower$upper "
    }
    s = s.replace("\\int", "∫ ")
        .replace("\\iint", "∬ ")
        .replace("\\iiint", "∭ ")
        .replace("\\oint", "∮ ")

    // 8. Summations and Products with limits
    s = s.replace(Regex("""\\sum_\{?([^\}^]+)\}?\^\{?([^\}^]+)\}?""")) { match ->
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
    s = s.replace("\\pm", "±")
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
        .replace("\\left(", "(")
        .replace("\\right)", ")")
        .replace("\\left[", "[")
        .replace("\\right]", "]")
        .replace("\\left\\{", "{")
        .replace("\\right\\}", "}")
        .replace("\\{", "{")
        .replace("\\}", "}")

    // 12. Subscripts: _\pm, _{bid}, _{ask}, _{max}, _{n}, _{t}, _{t-1}, _{t-k}, _1
    s = s.replace(Regex("""_\{([^}]+)\}""")) { match ->
        toSubscript(match.groupValues[1])
    }
    s = s.replace(Regex("""_([0-9a-zA-Z\+\-\*])""")) { match ->
        toSubscript(match.groupValues[1])
    }

    // 13. Superscripts: ^{2}, ^{3}, ^{b}, ^{a}, ^{+}
    s = s.replace(Regex("""\^\{([^}]+)\}""")) { match ->
        toSuperscript(match.groupValues[1])
    }
    s = s.replace(Regex("""\^([0-9a-zA-Z\+\-\*])""")) { match ->
        toSuperscript(match.groupValues[1])
    }

    // Clean up unnecessary leftover double spaces or backslashes
    s = s.replace(Regex("""\s+"""), " ")

    return s
}

/**
 * Converts alphanumeric string to Unicode subscripts where available.
 */
private fun toSubscript(text: String): String {
    val clean = text.trim()
    return clean.map { ch ->
        when (ch) {
            '0' -> '₀'
            '1' -> '₁'
            '2' -> '₂'
            '3' -> '₃'
            '4' -> '₄'
            '5' -> '₅'
            '6' -> '₆'
            '7' -> '₇'
            '8' -> '₈'
            '9' -> '₉'
            '+' -> '₊'
            '-' -> '₋'
            '=' -> '₌'
            '(' -> '₍'
            ')' -> '₎'
            'a' -> 'ₐ'
            'e' -> 'ₑ'
            'h' -> 'ₕ'
            'i' -> 'ᵢ'
            'j' -> 'ⱼ'
            'k' -> 'ₖ'
            'l' -> 'ₗ'
            'm' -> 'ₘ'
            'n' -> 'ₙ'
            'o' -> 'ₒ'
            'p' -> 'ₚ'
            'r' -> 'ᵣ'
            's' -> 'ₛ'
            't' -> 'ₜ'
            'u' -> 'ᵤ'
            'v' -> 'ᵥ'
            'x' -> 'ₓ'
            'β' -> 'ᵦ'
            'γ' -> 'ᵧ'
            'ρ' -> 'ᵨ'
            'φ' -> 'ᵩ'
            'χ' -> 'ᵪ'
            else -> ch
        }
    }.joinToString("")
}

/**
 * Converts alphanumeric string to Unicode superscripts where available.
 */
private fun toSuperscript(text: String): String {
    val clean = text.trim()
    return clean.map { ch ->
        when (ch) {
            '0' -> '⁰'
            '1' -> '¹'
            '2' -> '²'
            '3' -> '³'
            '4' -> '⁴'
            '5' -> '⁵'
            '6' -> '⁶'
            '7' -> '⁷'
            '8' -> '⁸'
            '9' -> '⁹'
            '+' -> '⁺'
            '-' -> '⁻'
            '=' -> '⁼'
            '(' -> '⁽'
            ')' -> '⁾'
            'a' -> 'ᵃ'
            'b' -> 'ᵇ'
            'c' -> 'ᶜ'
            'd' -> 'ᵈ'
            'e' -> 'ᵉ'
            'f' -> 'ᶠ'
            'g' -> 'ᵍ'
            'h' -> 'ʰ'
            'i' -> 'ⁱ'
            'j' -> 'ʲ'
            'k' -> 'ᵏ'
            'l' -> 'ˡ'
            'm' -> 'ᵐ'
            'n' -> 'ⁿ'
            'o' -> 'ᵒ'
            'p' -> 'ᵖ'
            'r' -> 'ʳ'
            's' -> 'ˢ'
            't' -> 'ᵗ'
            'u' -> 'ᵘ'
            'v' -> 'ᵛ'
            'w' -> 'ʷ'
            'x' -> 'ˣ'
            'y' -> 'ʸ'
            'z' -> 'ᶻ'
            '*' -> '﹡'
            'β' -> 'ᵝ'
            'γ' -> 'ᵞ'
            'δ' -> 'ᵟ'
            'θ' -> 'ᶿ'
            'φ' -> 'ᵠ'
            'χ' -> 'ᵡ'
            else -> ch
        }
    }.joinToString("")
}
