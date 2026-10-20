package com.studyforge.app.ui.components

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlin.math.max

private const val INLINE_CONTENT_TAG = "androidx.compose.foundation.text.inlineContent"

// =========================================================================
// AST FOR 2D TEXTBOOK MATHEMATICAL RENDERING
// =========================================================================

sealed interface MathBlockAst {
    data class Lines(val lines: List<List<MathInlineNode>>) : MathBlockAst
    data class ArrayGrid(
        val prefix: List<MathInlineNode>,
        val colAlignments: List<Alignment.Horizontal>,
        val rows: List<MathArrayRow>,
        val suffix: List<MathInlineNode>
    ) : MathBlockAst
    data class CasesGrid(
        val prefix: List<MathInlineNode>,
        val rows: List<Pair<List<MathInlineNode>, List<MathInlineNode>>>,
        val suffix: List<MathInlineNode>
    ) : MathBlockAst
}

sealed interface MathArrayRow {
    data object HLine : MathArrayRow
    data class Cells(val cells: List<List<MathInlineNode>>) : MathArrayRow
}

sealed interface MathInlineNode {
    data class StyledText(val text: AnnotatedString) : MathInlineNode
    data class Fraction(
        val numLatex: String,
        val denLatex: String,
        val numerator: List<MathInlineNode>,
        val denominator: List<MathInlineNode>
    ) : MathInlineNode
    data class Delimited(
        val leftDelim: String,
        val rightDelim: String,
        val inner: List<MathInlineNode>
    ) : MathInlineNode
    data class ArrowAbove(
        val labelLatex: String,
        val label: List<MathInlineNode>,
        val isLeft: Boolean = false
    ) : MathInlineNode
    data class Sqrt(
        val degree: List<MathInlineNode>?,
        val radicand: List<MathInlineNode>
    ) : MathInlineNode
    data class Boxed(val inner: List<MathInlineNode>) : MathInlineNode
}

private val BLOCK_AST_CACHE = java.util.Collections.synchronizedMap(
    object : java.util.LinkedHashMap<String, MathBlockAst>(256, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, MathBlockAst>?): Boolean = size > 500
    }
)

private val INLINE_NODES_CACHE = java.util.Collections.synchronizedMap(
    object : java.util.LinkedHashMap<String, List<MathInlineNode>>(256, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<MathInlineNode>>?): Boolean = size > 600
    }
)

private val STYLED_RUN_CACHE = java.util.Collections.synchronizedMap(
    object : java.util.LinkedHashMap<String, AnnotatedString>(512, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, AnnotatedString>?): Boolean = size > 1000
    }
)

private val UPRIGHT_MATH_WORDS = setOf(
    "sin", "cos", "tan", "cot", "sec", "csc",
    "sinh", "cosh", "tanh", "arcsin", "arccos", "arctan",
    "log", "ln", "exp", "lim", "min", "max", "sup", "inf",
    "det", "dim", "ker", "gcd", "lcm", "mod",
    "argmin", "argmax", "NOTA", "Ex", "HINTS", "st", "nd", "rd", "th", "day"
)

private val REGEX_ARRAY_BLOCK = Regex("""\\begin\{array\}\{([^}]*)\}([\s\S]*?)\\end\{array\}""")
private val REGEX_CASES_BLOCK = Regex("""\\begin\{cases\}([\s\S]*?)\\end\{cases\}""")
private val REGEX_ALIGN_BLOCK = Regex("""\\begin\{align\*?\}([\s\S]*?)\\end\{align\*?\}""")
private val REGEX_ROW_SPLIT = Regex("""\\\\""")
private val REGEX_MULTI_SPACE = Regex(""" {2,}""")

// =========================================================================
// COMPOSABLE RENDERERS (DISPLAY & INLINE)
// =========================================================================

/**
 * Renders an `AnnotatedString` that may contain inline 2D mathematical elements
 * (`frac:num|||den` or `xarrow:label|||dir`) produced by `parseInlineMarkdownAndLatex`.
 */
@Composable
fun RichMarkdownText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null
) {
    val resolvedColor = if (color != Color.Unspecified) {
        color
    } else if (style.color != Color.Unspecified) {
        style.color
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val baseFontSizeSp = if (style.fontSize.isSp) style.fontSize.value else 15.5f

    val inlineAnnotations = remember(text) {
        text.getStringAnnotations(INLINE_CONTENT_TAG, 0, text.length)
    }

    if (inlineAnnotations.isEmpty()) {
        Text(
            text = text,
            modifier = modifier,
            style = style,
            color = resolvedColor,
            textAlign = textAlign
        )
        return
    }

    val inlineContentMap = remember(inlineAnnotations, baseFontSizeSp, resolvedColor) {
        val map = HashMap<String, InlineTextContent>(inlineAnnotations.size)
        for (ann in inlineAnnotations) {
            val id = ann.item
            if (map.containsKey(id)) continue

            if (id.startsWith("frac:")) {
                val payload = id.removePrefix("frac:")
                val sepIdx = payload.indexOf("|||")
                if (sepIdx != -1) {
                    val numLatex = payload.substring(0, sepIdx)
                    val denLatex = payload.substring(sepIdx + 3)
                    val numStyled = formatMathRunToAnnotatedString(cleanInlineMathSymbols(numLatex))
                    val denStyled = formatMathRunToAnnotatedString(cleanInlineMathSymbols(denLatex))
                    val maxVisualLen = maxOf(
                        visualMathCharCount(numStyled.text),
                        visualMathCharCount(denStyled.text),
                        1
                    )
                    // Calculate compact textbook width in em
                    val widthEm = when {
                        maxVisualLen <= 1 -> 1.15.em
                        maxVisualLen == 2 -> 1.65.em
                        maxVisualLen == 3 -> 2.15.em
                        else -> (maxVisualLen * 0.56f + 0.55f).em
                    }
                    val heightEm = 2.35.em
                    val fracFontSp = (baseFontSizeSp * 0.78f).coerceAtLeast(10f)

                    map[id] = InlineTextContent(
                        placeholder = Placeholder(
                            width = widthEm,
                            height = heightEm,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 1.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.width(IntrinsicSize.Max)
                            ) {
                                Text(
                                    text = numStyled,
                                    fontSize = fracFontSp.sp,
                                    lineHeight = (fracFontSp * 1.05f).sp,
                                    color = resolvedColor,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 1.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(vertical = 1.dp)
                                        .fillMaxWidth()
                                        .height(1.15.dp)
                                        .background(resolvedColor)
                                )
                                Text(
                                    text = denStyled,
                                    fontSize = fracFontSp.sp,
                                    lineHeight = (fracFontSp * 1.05f).sp,
                                    color = resolvedColor,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 1.dp)
                                )
                            }
                        }
                    }
                }
            } else if (id.startsWith("xarrow:")) {
                val payload = id.removePrefix("xarrow:")
                val sepIdx = payload.indexOf("|||")
                val labelLatex = if (sepIdx != -1) payload.substring(0, sepIdx) else payload
                val isLeft = sepIdx != -1 && payload.substring(sepIdx + 3) == "L"
                val labelStyled = formatMathRunToAnnotatedString(cleanInlineMathSymbols(labelLatex))
                val visualLen = maxOf(visualMathCharCount(labelStyled.text), 2)
                val widthEm = (visualLen * 0.56f + 1.1f).em
                val heightEm = 2.05.em
                val labelFontSp = (baseFontSizeSp * 0.72f).coerceAtLeast(9.5f)

                map[id] = InlineTextContent(
                    placeholder = Placeholder(
                        width = widthEm,
                        height = heightEm,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = labelStyled,
                                fontSize = labelFontSp.sp,
                                lineHeight = (labelFontSp * 1.05f).sp,
                                color = resolvedColor,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                softWrap = false
                            )
                            ArrowLineCanvas(
                                isLeft = isLeft,
                                color = resolvedColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                            )
                        }
                    }
                }
            }
        }
        map
    }

    // Increase lineHeight automatically when inline fractions are present so stacked fractions have vertical room
    val effectiveLineHeight = if (style.lineHeight.isSp) {
        maxOf(style.lineHeight.value, baseFontSizeSp * 2.15f).sp
    } else {
        (baseFontSizeSp * 2.15f).sp
    }

    Text(
        text = text,
        inlineContent = inlineContentMap,
        modifier = modifier.padding(vertical = 1.dp),
        style = style.copy(lineHeight = effectiveLineHeight),
        color = resolvedColor,
        textAlign = textAlign
    )
}

/**
 * Renders a textbook-grade display equation (`$$ ... $$`) directly on the note page.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TextbookMathBlock(
    rawLatex: String,
    modifier: Modifier = Modifier,
    baseFontSizeSp: Float = 18.5f,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    if (rawLatex.isBlank()) return
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val ast = remember(rawLatex) { parseMathBlockAst(rawLatex) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    clipboardManager.setText(AnnotatedString(rawLatex))
                    Toast.makeText(context, "Formula copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            )
            .padding(vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        when (ast) {
            is MathBlockAst.ArrayGrid -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    if (ast.prefix.isNotEmpty()) {
                        RenderMathInlineNodeList(
                            nodes = ast.prefix,
                            fontSizeSp = baseFontSizeSp,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    MathArrayTableComposable(
                        colAlignments = ast.colAlignments,
                        rows = ast.rows,
                        fontSizeSp = baseFontSizeSp * 0.92f,
                        color = textColor
                    )
                    if (ast.suffix.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        RenderMathInlineNodeList(
                            nodes = ast.suffix,
                            fontSizeSp = baseFontSizeSp,
                            color = textColor
                        )
                    }
                }
            }

            is MathBlockAst.CasesGrid -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    if (ast.prefix.isNotEmpty()) {
                        RenderMathInlineNodeList(
                            nodes = ast.prefix,
                            fontSizeSp = baseFontSizeSp,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Row(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TallDelimiterCanvas(
                            delimiter = "{",
                            color = textColor,
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ast.rows.forEach { (expr, cond) ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RenderMathInlineNodeList(
                                        nodes = expr,
                                        fontSizeSp = baseFontSizeSp * 0.92f,
                                        color = textColor
                                    )
                                    if (cond.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(16.dp))
                                        RenderMathInlineNodeList(
                                            nodes = cond,
                                            fontSizeSp = baseFontSizeSp * 0.92f,
                                            color = textColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (ast.suffix.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        RenderMathInlineNodeList(
                            nodes = ast.suffix,
                            fontSizeSp = baseFontSizeSp,
                            color = textColor
                        )
                    }
                }
            }

            is MathBlockAst.Lines -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    ast.lines.forEach { lineNodes ->
                        RenderMathInlineNodeList(
                            nodes = lineNodes,
                            fontSizeSp = baseFontSizeSp,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * Renders a sequence of 2D `MathInlineNode`s in a horizontally aligned Row.
 */
@Composable
fun RenderMathInlineNodeList(
    nodes: List<MathInlineNode>,
    fontSizeSp: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        nodes.forEach { node ->
            RenderSingleMathInlineNode(
                node = node,
                fontSizeSp = fontSizeSp,
                color = color
            )
        }
    }
}

@Composable
private fun RenderSingleMathInlineNode(
    node: MathInlineNode,
    fontSizeSp: Float,
    color: Color
) {
    when (node) {
        is MathInlineNode.StyledText -> {
            Text(
                text = node.text,
                style = TextStyle(
                    fontSize = fontSizeSp.sp,
                    lineHeight = (fontSizeSp * 1.35f).sp,
                    color = color,
                    fontWeight = FontWeight.Normal
                ),
                maxLines = 1,
                softWrap = false
            )
        }

        is MathInlineNode.Fraction -> {
            val childFontSize = (fontSizeSp * 0.86f).coerceAtLeast(11f)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(horizontal = 3.dp, vertical = 1.dp)
                    .width(IntrinsicSize.Max)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 3.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    RenderMathInlineNodeList(
                        nodes = node.numerator,
                        fontSizeSp = childFontSize,
                        color = color
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.35.dp)
                        .background(color)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 3.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    RenderMathInlineNodeList(
                        nodes = node.denominator,
                        fontSizeSp = childFontSize,
                        color = color
                    )
                }
            }
        }

        is MathInlineNode.Delimited -> {
            val isTall = remember(node.inner) { containsTallMathNode(node.inner) }
            if (isTall) {
                Row(
                    modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .padding(horizontal = 1.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (node.leftDelim.isNotEmpty() && node.leftDelim != ".") {
                        TallDelimiterCanvas(
                            delimiter = node.leftDelim,
                            color = color,
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(9.dp)
                        )
                    }
                    Box(
                        modifier = Modifier.padding(horizontal = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        RenderMathInlineNodeList(
                            nodes = node.inner,
                            fontSizeSp = fontSizeSp,
                            color = color
                        )
                    }
                    if (node.rightDelim.isNotEmpty() && node.rightDelim != ".") {
                        TallDelimiterCanvas(
                            delimiter = node.rightDelim,
                            color = color,
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(9.dp)
                        )
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (node.leftDelim.isNotEmpty() && node.leftDelim != ".") {
                        Text(
                            text = node.leftDelim,
                            fontSize = fontSizeSp.sp,
                            color = color
                        )
                    }
                    RenderMathInlineNodeList(
                        nodes = node.inner,
                        fontSizeSp = fontSizeSp,
                        color = color
                    )
                    if (node.rightDelim.isNotEmpty() && node.rightDelim != ".") {
                        Text(
                            text = node.rightDelim,
                            fontSize = fontSizeSp.sp,
                            color = color
                        )
                    }
                }
            }
        }

        is MathInlineNode.ArrowAbove -> {
            val labelSize = (fontSizeSp * 0.72f).coerceAtLeast(10f)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .width(IntrinsicSize.Max)
                    .widthIn(min = 28.dp)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    RenderMathInlineNodeList(
                        nodes = node.label,
                        fontSizeSp = labelSize,
                        color = color
                    )
                }
                ArrowLineCanvas(
                    isLeft = node.isLeft,
                    color = color,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                )
            }
        }

        is MathInlineNode.Sqrt -> {
            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!node.degree.isNullOrEmpty()) {
                    Box(modifier = Modifier.padding(bottom = 8.dp)) {
                        RenderMathInlineNodeList(
                            nodes = node.degree,
                            fontSizeSp = (fontSizeSp * 0.65f).coerceAtLeast(9f),
                            color = color
                        )
                    }
                }
                Text(
                    text = "√",
                    fontSize = (fontSizeSp * 1.15f).sp,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .drawWithContent {
                            drawContent()
                            val strokePx = 1.25.dp.toPx()
                            drawLine(
                                color = color,
                                start = Offset(0f, strokePx),
                                end = Offset(size.width, strokePx),
                                strokeWidth = strokePx
                            )
                        }
                        .padding(top = 2.dp, start = 2.dp, end = 2.dp)
                ) {
                    RenderMathInlineNodeList(
                        nodes = node.radicand,
                        fontSizeSp = fontSizeSp,
                        color = color
                    )
                }
            }
        }

        is MathInlineNode.Boxed -> {
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .border(1.25.dp, color.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                RenderMathInlineNodeList(
                    nodes = node.inner,
                    fontSizeSp = fontSizeSp,
                    color = color
                )
            }
        }
    }
}

private fun containsTallMathNode(nodes: List<MathInlineNode>): Boolean {
    for (n in nodes) {
        when (n) {
            is MathInlineNode.Fraction -> return true
            is MathInlineNode.Delimited -> if (containsTallMathNode(n.inner)) return true
            is MathInlineNode.Sqrt -> if (containsTallMathNode(n.radicand)) return true
            is MathInlineNode.Boxed -> if (containsTallMathNode(n.inner)) return true
            else -> {}
        }
    }
    return false
}

/**
 * Custom Canvas drawing tall mathematical delimiters `(`, `)`, `[`, `]`, `{`, `}`, `|`.
 */
@Composable
private fun TallDelimiterCanvas(
    delimiter: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.drawBehind {
            val w = size.width
            val h = size.height
            val strokePx = 1.55.dp.toPx()
            val vInset = 2.dp.toPx()
            val topY = vInset
            val botY = (h - vInset).coerceAtLeast(topY + 4f)
            val midY = (topY + botY) / 2f

            when (delimiter) {
                "[" -> {
                    val leftX = w * 0.25f
                    val rightX = w * 0.85f
                    val path = Path().apply {
                        moveTo(rightX, topY)
                        lineTo(leftX, topY)
                        lineTo(leftX, botY)
                        lineTo(rightX, botY)
                    }
                    drawPath(path, color = color, style = Stroke(width = strokePx, cap = StrokeCap.Square))
                }
                "]" -> {
                    val leftX = w * 0.15f
                    val rightX = w * 0.75f
                    val path = Path().apply {
                        moveTo(leftX, topY)
                        lineTo(rightX, topY)
                        lineTo(rightX, botY)
                        lineTo(leftX, botY)
                    }
                    drawPath(path, color = color, style = Stroke(width = strokePx, cap = StrokeCap.Square))
                }
                "(" -> {
                    val edgeX = w * 0.82f
                    val curveX = w * 0.08f
                    val path = Path().apply {
                        moveTo(edgeX, topY)
                        cubicTo(curveX, topY + (botY - topY) * 0.22f, curveX, topY + (botY - topY) * 0.78f, edgeX, botY)
                    }
                    drawPath(path, color = color, style = Stroke(width = strokePx, cap = StrokeCap.Round))
                }
                ")" -> {
                    val edgeX = w * 0.18f
                    val curveX = w * 0.92f
                    val path = Path().apply {
                        moveTo(edgeX, topY)
                        cubicTo(curveX, topY + (botY - topY) * 0.22f, curveX, topY + (botY - topY) * 0.78f, edgeX, botY)
                    }
                    drawPath(path, color = color, style = Stroke(width = strokePx, cap = StrokeCap.Round))
                }
                "{" -> {
                    val rightX = w * 0.82f
                    val midX = w * 0.45f
                    val tipX = w * 0.12f
                    val r = 4.dp.toPx()
                    val path = Path().apply {
                        moveTo(rightX, topY)
                        quadraticTo(midX, topY, midX, topY + r)
                        lineTo(midX, midY - r)
                        quadraticTo(midX, midY, tipX, midY)
                        quadraticTo(midX, midY, midX, midY + r)
                        lineTo(midX, botY - r)
                        quadraticTo(midX, botY, rightX, botY)
                    }
                    drawPath(path, color = color, style = Stroke(width = strokePx, cap = StrokeCap.Round))
                }
                "}" -> {
                    val leftX = w * 0.18f
                    val midX = w * 0.55f
                    val tipX = w * 0.88f
                    val r = 4.dp.toPx()
                    val path = Path().apply {
                        moveTo(leftX, topY)
                        quadraticTo(midX, topY, midX, topY + r)
                        lineTo(midX, midY - r)
                        quadraticTo(midX, midY, tipX, midY)
                        quadraticTo(midX, midY, midX, midY + r)
                        lineTo(midX, botY - r)
                        quadraticTo(midX, botY, leftX, botY)
                    }
                    drawPath(path, color = color, style = Stroke(width = strokePx, cap = StrokeCap.Round))
                }
                "|" -> {
                    drawLine(
                        color = color,
                        start = Offset(w / 2f, topY),
                        end = Offset(w / 2f, botY),
                        strokeWidth = strokePx,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    )
}

@Composable
private fun ArrowLineCanvas(
    isLeft: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.drawBehind {
            val w = size.width
            val midY = size.height / 2f
            val strokePx = 1.35.dp.toPx()
            val headSize = 4.5.dp.toPx()

            drawLine(
                color = color,
                start = Offset(1.dp.toPx(), midY),
                end = Offset((w - 1.dp.toPx()).coerceAtLeast(2f), midY),
                strokeWidth = strokePx,
                cap = StrokeCap.Round
            )

            val path = Path().apply {
                if (isLeft) {
                    val tipX = 1.dp.toPx()
                    moveTo(tipX + headSize, midY - headSize * 0.75f)
                    lineTo(tipX, midY)
                    lineTo(tipX + headSize, midY + headSize * 0.75f)
                } else {
                    val tipX = (w - 1.dp.toPx()).coerceAtLeast(headSize)
                    moveTo(tipX - headSize, midY - headSize * 0.75f)
                    lineTo(tipX, midY)
                    lineTo(tipX - headSize, midY + headSize * 0.75f)
                }
            }
            drawPath(path, color = color, style = Stroke(width = strokePx, cap = StrokeCap.Round))
        }
    )
}

/**
 * Exact 2D Grid Layout for `\begin{array}{...} ... \end{array}` with `\hline` support.
 */
@Composable
private fun MathArrayTableComposable(
    colAlignments: List<Alignment.Horizontal>,
    rows: List<MathArrayRow>,
    fontSizeSp: Float,
    color: Color
) {
    val dataRows = remember(rows) { rows.filterIsInstance<MathArrayRow.Cells>() }
    val numCols = remember(dataRows, colAlignments) {
        maxOf(colAlignments.size, dataRows.maxOfOrNull { it.cells.size } ?: 0)
    }
    if (numCols == 0 || dataRows.isEmpty()) return

    val hlinePositions = remember(rows) {
        val list = mutableListOf<Int>()
        var currentDataRow = 0
        for (r in rows) {
            when (r) {
                is MathArrayRow.HLine -> list.add(currentDataRow)
                is MathArrayRow.Cells -> currentDataRow++
            }
        }
        list
    }

    Layout(
        content = {
            // 1. Emit all data cells (numDataRows * numCols)
            dataRows.forEach { row ->
                for (cIdx in 0 until numCols) {
                    val cellNodes = row.cells.getOrElse(cIdx) { emptyList() }
                    Box(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (cellNodes.isNotEmpty()) {
                            RenderMathInlineNodeList(
                                nodes = cellNodes,
                                fontSizeSp = fontSizeSp,
                                color = color
                            )
                        }
                    }
                }
            }
            // 2. Emit HLine divider bars
            repeat(hlinePositions.size) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.25.dp)
                        .background(color.copy(alpha = 0.75f))
                )
            }
        }
    ) { measurables, _ ->
        val numDataRows = dataRows.size
        val cellCount = numDataRows * numCols
        val cellPlaceables = measurables.take(cellCount).map { it.measure(Constraints()) }
        val hlineMeasurables = measurables.drop(cellCount)

        val colWidths = IntArray(numCols)
        val rowHeights = IntArray(numDataRows)

        for (r in 0 until numDataRows) {
            for (c in 0 until numCols) {
                val p = cellPlaceables[r * numCols + c]
                if (p.width > colWidths[c]) colWidths[c] = p.width
                if (p.height > rowHeights[r]) rowHeights[r] = p.height
            }
        }

        val hlineGapPx = 7.dp.roundToPx()
        val strokePx = max(1, 1.25.dp.roundToPx())
        val totalWidth = colWidths.sum()

        val hlinePlaceables = hlineMeasurables.map {
            it.measure(Constraints.fixed(totalWidth, strokePx))
        }

        val colOffsets = IntArray(numCols)
        var xAcc = 0
        for (c in 0 until numCols) {
            colOffsets[c] = xAcc
            xAcc += colWidths[c]
        }

        val rowOffsets = IntArray(numDataRows)
        val hlineYOffsets = IntArray(hlinePositions.size)
        var yAcc = 0
        var hIdx = 0

        for (r in 0 until numDataRows) {
            while (hIdx < hlinePositions.size && hlinePositions[hIdx] == r) {
                hlineYOffsets[hIdx] = yAcc + hlineGapPx / 2
                yAcc += hlineGapPx
                hIdx++
            }
            rowOffsets[r] = yAcc
            yAcc += rowHeights[r]
        }
        while (hIdx < hlinePositions.size) {
            hlineYOffsets[hIdx] = yAcc + hlineGapPx / 2
            yAcc += hlineGapPx
            hIdx++
        }

        val totalHeight = max(1, yAcc)

        layout(totalWidth, totalHeight) {
            for (r in 0 until numDataRows) {
                for (c in 0 until numCols) {
                    val p = cellPlaceables[r * numCols + c]
                    val align = colAlignments.getOrElse(c) { Alignment.CenterHorizontally }
                    val x = colOffsets[c] + align.align(p.width, colWidths[c], androidx.compose.ui.unit.LayoutDirection.Ltr)
                    val y = rowOffsets[r] + (rowHeights[r] - p.height) / 2
                    p.placeRelative(x, y)
                }
            }
            for (i in hlinePlaceables.indices) {
                hlinePlaceables[i].placeRelative(0, hlineYOffsets[i])
            }
        }
    }
}

// =========================================================================
// BALANCED-BRACE LATEX PARSER -> 2D MATH AST
// =========================================================================

fun parseMathBlockAst(rawLatex: String): MathBlockAst {
    val trimmed = rawLatex.trim()
    if (trimmed.isEmpty()) return MathBlockAst.Lines(emptyList())

    val cached = BLOCK_AST_CACHE[trimmed]
    if (cached != null) return cached

    var work = trimmed
    if (work.startsWith("\\boxed{") && work.endsWith("}")) {
        val extracted = extractBraceGroup(work, 6)
        if (extracted != null && extracted.second == work.length) {
            work = extracted.first.trim()
        }
    }

    // 1. Check for \begin{array}{...} ... \end{array}
    val arrayMatch = REGEX_ARRAY_BLOCK.find(work)
    if (arrayMatch != null) {
        val colSpec = arrayMatch.groupValues[1].trim()
        val body = arrayMatch.groupValues[2].trim()
        val prefixStr = work.substring(0, arrayMatch.range.first).trim()
        val suffixStr = work.substring(arrayMatch.range.last + 1).trim()

        val alignments = colSpec.mapNotNull { ch ->
            when (ch) {
                'l' -> Alignment.Start
                'r' -> Alignment.End
                'c' -> Alignment.CenterHorizontally
                else -> null
            }
        }.ifEmpty { listOf(Alignment.CenterHorizontally) }

        val parsedRows = mutableListOf<MathArrayRow>()
        val rawRows = body.split(REGEX_ROW_SPLIT)
        for (rawRow in rawRows) {
            var rTrim = rawRow.trim()
            while (rTrim.startsWith("\\hline")) {
                parsedRows.add(MathArrayRow.HLine)
                rTrim = rTrim.removePrefix("\\hline").trim()
            }
            // Also check if \hline was separated by a newline inside rawRow
            val subLines = rTrim.lines().map { it.trim() }.filter { it.isNotEmpty() }
            val cellBuffer = StringBuilder()
            for (sub in subLines) {
                if (sub == "\\hline") {
                    if (cellBuffer.isNotEmpty()) {
                        val cells = cellBuffer.toString().split("&").map { parseMathInlineNodes(it.trim()) }
                        parsedRows.add(MathArrayRow.Cells(cells))
                        cellBuffer.clear()
                    }
                    parsedRows.add(MathArrayRow.HLine)
                } else if (sub.startsWith("\\hline")) {
                    if (cellBuffer.isNotEmpty()) {
                        val cells = cellBuffer.toString().split("&").map { parseMathInlineNodes(it.trim()) }
                        parsedRows.add(MathArrayRow.Cells(cells))
                        cellBuffer.clear()
                    }
                    parsedRows.add(MathArrayRow.HLine)
                    val after = sub.removePrefix("\\hline").trim()
                    if (after.isNotEmpty()) {
                        cellBuffer.append(after)
                    }
                } else {
                    if (cellBuffer.isNotEmpty()) cellBuffer.append(" ")
                    cellBuffer.append(sub)
                }
            }
            if (cellBuffer.isNotEmpty()) {
                val cells = cellBuffer.toString().split("&").map { parseMathInlineNodes(it.trim()) }
                parsedRows.add(MathArrayRow.Cells(cells))
            }
        }

        val ast = MathBlockAst.ArrayGrid(
            prefix = if (prefixStr.isNotEmpty()) parseMathInlineNodes(prefixStr) else emptyList(),
            colAlignments = alignments,
            rows = parsedRows,
            suffix = if (suffixStr.isNotEmpty()) parseMathInlineNodes(suffixStr) else emptyList()
        )
        BLOCK_AST_CACHE[trimmed] = ast
        return ast
    }

    // 2. Check for \begin{cases} ... \end{cases}
    val casesMatch = REGEX_CASES_BLOCK.find(work)
    if (casesMatch != null) {
        val body = casesMatch.groupValues[1].trim()
        val prefixStr = work.substring(0, casesMatch.range.first).trim()
        val suffixStr = work.substring(casesMatch.range.last + 1).trim()

        val rows = body.split(REGEX_ROW_SPLIT)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { rLine ->
                val parts = rLine.split("&", limit = 2)
                val expr = parseMathInlineNodes(parts.getOrElse(0) { "" }.trim())
                val cond = if (parts.size > 1) parseMathInlineNodes(parts[1].trim()) else emptyList()
                Pair(expr, cond)
            }

        val ast = MathBlockAst.CasesGrid(
            prefix = if (prefixStr.isNotEmpty()) parseMathInlineNodes(prefixStr) else emptyList(),
            rows = rows,
            suffix = if (suffixStr.isNotEmpty()) parseMathInlineNodes(suffixStr) else emptyList()
        )
        BLOCK_AST_CACHE[trimmed] = ast
        return ast
    }

    // 3. Check for \begin{align} ... \end{align}
    val alignMatch = REGEX_ALIGN_BLOCK.find(work)
    if (alignMatch != null) {
        val body = alignMatch.groupValues[1].trim()
        val lines = body.split(REGEX_ROW_SPLIT)
            .map { it.replace("&", " ").trim() }
            .filter { it.isNotEmpty() }
            .map { parseMathInlineNodes(it) }
        val ast = MathBlockAst.Lines(lines)
        BLOCK_AST_CACHE[trimmed] = ast
        return ast
    }

    // 4. Standard single-line or multi-line (\\) equation
    val lines = work.split(REGEX_ROW_SPLIT)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { parseMathInlineNodes(it) }

    val ast = MathBlockAst.Lines(lines)
    BLOCK_AST_CACHE[trimmed] = ast
    return ast
}

/**
 * Parses a LaTeX math string into a list of 2D `MathInlineNode`s.
 */
fun parseMathInlineNodes(latex: String): List<MathInlineNode> {
    val trimmed = latex.trim()
    if (trimmed.isEmpty()) return emptyList()

    val cached = INLINE_NODES_CACHE[trimmed]
    if (cached != null) return cached

    val nodes = ArrayList<MathInlineNode>()
    val textBuf = StringBuilder()

    fun flushTextBuffer() {
        if (textBuf.isNotEmpty()) {
            val rawRun = textBuf.toString()
            textBuf.clear()
            val cleaned = cleanInlineMathSymbols(rawRun)
            if (cleaned.isNotEmpty()) {
                nodes.add(MathInlineNode.StyledText(formatMathRunToAnnotatedString(cleaned)))
            }
        }
    }

    var i = 0
    val len = trimmed.length

    while (i < len) {
        // 1. \frac{num}{den}, \dfrac{num}{den}, \tfrac{num}{den}
        val fracCmdLen = when {
            trimmed.startsWith("\\frac", i) && (i + 5 == len || !trimmed[i + 5].isLetter()) -> 5
            (trimmed.startsWith("\\dfrac", i) || trimmed.startsWith("\\tfrac", i)) &&
                (i + 6 == len || !trimmed[i + 6].isLetter()) -> 6
            else -> 0
        }
        if (fracCmdLen > 0) {
            var cursor = skipSpaces(trimmed, i + fracCmdLen)
            val numGroup = extractBraceGroup(trimmed, cursor)
            if (numGroup != null) {
                cursor = skipSpaces(trimmed, numGroup.second)
                val denGroup = extractBraceGroup(trimmed, cursor)
                if (denGroup != null) {
                    flushTextBuffer()
                    val numLatex = numGroup.first.trim()
                    val denLatex = denGroup.first.trim()
                    nodes.add(
                        MathInlineNode.Fraction(
                            numLatex = numLatex,
                            denLatex = denLatex,
                            numerator = parseMathInlineNodes(numLatex),
                            denominator = parseMathInlineNodes(denLatex)
                        )
                    )
                    i = denGroup.second
                    continue
                }
            }
        }

        // 2. \left ... \right
        if (trimmed.startsWith("\\left", i) && (i + 5 < len && !trimmed[i + 5].isLetter())) {
            val matched = extractLeftRightGroup(trimmed, i)
            if (matched != null) {
                flushTextBuffer()
                nodes.add(
                    MathInlineNode.Delimited(
                        leftDelim = matched.leftDelim,
                        rightDelim = matched.rightDelim,
                        inner = parseMathInlineNodes(matched.innerLatex)
                    )
                )
                i = matched.nextIndex
                continue
            }
        }

        // 3. Automatic tall parentheses/brackets when (...) or [...] wraps a \frac
        if (trimmed[i] == '(' || trimmed[i] == '[') {
            val openChar = trimmed[i]
            val closeChar = if (openChar == '(') ')' else ']'
            val parenGroup = extractBalancedParenGroup(trimmed, i, openChar, closeChar)
            if (parenGroup != null && parenGroup.first.contains("\\frac")) {
                flushTextBuffer()
                nodes.add(
                    MathInlineNode.Delimited(
                        leftDelim = openChar.toString(),
                        rightDelim = closeChar.toString(),
                        inner = parseMathInlineNodes(parenGroup.first)
                    )
                )
                i = parenGroup.second
                continue
            }
        }

        // 4. \xrightarrow{...} or \xleftarrow{...}
        val isXRight = trimmed.startsWith("\\xrightarrow", i)
        val isXLeft = !isXRight && trimmed.startsWith("\\xleftarrow", i)
        if (isXRight || isXLeft) {
            val cmdLen = if (isXRight) 12 else 11
            var cursor = skipSpaces(trimmed, i + cmdLen)
            // Optional [sub] before {sup}
            if (cursor < len && trimmed[cursor] == '[') {
                val closeBrk = trimmed.indexOf(']', cursor + 1)
                if (closeBrk != -1) cursor = skipSpaces(trimmed, closeBrk + 1)
            }
            val labelGroup = extractBraceGroup(trimmed, cursor)
            if (labelGroup != null) {
                flushTextBuffer()
                val labelLatex = labelGroup.first.trim()
                nodes.add(
                    MathInlineNode.ArrowAbove(
                        labelLatex = labelLatex,
                        label = parseMathInlineNodes(labelLatex),
                        isLeft = isXLeft
                    )
                )
                i = labelGroup.second
                continue
            }
        }

        // 5. \sqrt[n]{x} or \sqrt{x}
        if (trimmed.startsWith("\\sqrt", i) && (i + 5 == len || !trimmed[i + 5].isLetter())) {
            var cursor = skipSpaces(trimmed, i + 5)
            var degreeNodes: List<MathInlineNode>? = null
            if (cursor < len && trimmed[cursor] == '[') {
                val closeBrk = trimmed.indexOf(']', cursor + 1)
                if (closeBrk != -1) {
                    val degStr = trimmed.substring(cursor + 1, closeBrk).trim()
                    if (degStr.isNotEmpty()) {
                        degreeNodes = parseMathInlineNodes(degStr)
                    }
                    cursor = skipSpaces(trimmed, closeBrk + 1)
                }
            }
            val radGroup = extractBraceGroup(trimmed, cursor)
            if (radGroup != null) {
                flushTextBuffer()
                nodes.add(
                    MathInlineNode.Sqrt(
                        degree = degreeNodes,
                        radicand = parseMathInlineNodes(radGroup.first.trim())
                    )
                )
                i = radGroup.second
                continue
            }
        }

        // 6. \boxed{...}
        if (trimmed.startsWith("\\boxed", i) && (i + 6 == len || !trimmed[i + 6].isLetter())) {
            val cursor = skipSpaces(trimmed, i + 6)
            val boxGroup = extractBraceGroup(trimmed, cursor)
            if (boxGroup != null) {
                flushTextBuffer()
                nodes.add(MathInlineNode.Boxed(inner = parseMathInlineNodes(boxGroup.first.trim())))
                i = boxGroup.second
                continue
            }
        }

        textBuf.append(trimmed[i])
        i++
    }

    flushTextBuffer()
    INLINE_NODES_CACHE[trimmed] = nodes
    return nodes
}

/**
 * Appends inline math (`$ ... $`) into an `AnnotatedString.Builder` using `appendInlineContent`
 * for 2D fractions (`\frac`) and labeled arrows (`\xrightarrow`), and textbook math styling for text runs.
 */
fun AnnotatedString.Builder.appendInlineMathLatex(latex: String) {
    val nodes = parseMathInlineNodes(latex)
    appendMathNodesToAnnotatedBuilder(nodes)
}

private fun AnnotatedString.Builder.appendMathNodesToAnnotatedBuilder(nodes: List<MathInlineNode>) {
    for (node in nodes) {
        when (node) {
            is MathInlineNode.StyledText -> {
                append(node.text)
            }
            is MathInlineNode.Fraction -> {
                val flatNum = cleanInlineMathSymbols(node.numLatex)
                val flatDen = cleanInlineMathSymbols(node.denLatex)
                // If numerator or denominator contains nested fractions, flatten inner to readable string for inline placeholder
                val id = "frac:${node.numLatex}|||${node.denLatex}"
                appendInlineContent(id = id, alternateText = "($flatNum/$flatDen)")
            }
            is MathInlineNode.Delimited -> {
                if (node.leftDelim.isNotEmpty() && node.leftDelim != ".") {
                    append(node.leftDelim)
                }
                appendMathNodesToAnnotatedBuilder(node.inner)
                if (node.rightDelim.isNotEmpty() && node.rightDelim != ".") {
                    append(node.rightDelim)
                }
            }
            is MathInlineNode.ArrowAbove -> {
                val dir = if (node.isLeft) "L" else "R"
                val id = "xarrow:${node.labelLatex}|||$dir"
                val flatLabel = cleanInlineMathSymbols(node.labelLatex)
                appendInlineContent(id = id, alternateText = " ─[$flatLabel]→ ")
            }
            is MathInlineNode.Sqrt -> {
                if (!node.degree.isNullOrEmpty()) {
                    pushStyle(SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 0.72.em))
                    appendMathNodesToAnnotatedBuilder(node.degree)
                    pop()
                }
                append("√(")
                appendMathNodesToAnnotatedBuilder(node.radicand)
                append(")")
            }
            is MathInlineNode.Boxed -> {
                append("⟦ ")
                appendMathNodesToAnnotatedBuilder(node.inner)
                append(" ⟧")
            }
        }
    }
}

// =========================================================================
// LATEX SYMBOL & TYPOGRAPHY FORMATTERS
// =========================================================================

private const val U_TEXT_START = '\uE010'
private const val U_TEXT_END = '\uE011'
private const val U_SUP_START = '\uE012'
private const val U_SUP_END = '\uE013'
private const val U_SUB_START = '\uE014'
private const val U_SUB_END = '\uE015'

/**
 * Cleans a raw LaTeX run (without top-level `\frac`) into a string with private-use markers
 * for upright `\text{...}`, superscripts `^{...}`, and subscripts `_{...}`, and replaces all
 * LaTeX commands in safe longest-first order.
 */
fun cleanInlineMathSymbols(rawLatex: String): String {
    if (rawLatex.isEmpty()) return ""
    var s = rawLatex

    // Flatten any nested \frac{a}{b} if cleanInlineMathSymbols is called on a nested fraction
    s = flattenNestedFractions(s)

    // 1. Extract \text{...}, \mathrm{...}, \mathbf{...}, \operatorname{...} with balanced braces
    s = replaceMacroWithBalancedBraces(s, listOf("\\text", "\\mathrm", "\\mathbf", "\\mathit", "\\operatorname")) { inner ->
        "$U_TEXT_START${cleanTextMacroInner(inner)}$U_TEXT_END"
    }

    // 2. Escaped symbols FIRST (before any backslash command replacements!)
    s = s.replace("\\%", "%")
        .replace("\\{", "{")
        .replace("\\}", "}")
        .replace("\\&", "&")
        .replace("\\#", "#")
        .replace("\\$", "$")
        .replace("\\_", "_")

    // 3. Delimiter sizing macros (MUST run before \le or \ge!)
    s = s.replace("\\left(", "(")
        .replace("\\right)", ")")
        .replace("\\left[", "[")
        .replace("\\right]", "]")
        .replace("\\left\\{", "{")
        .replace("\\right\\}", "}")
        .replace("\\left{", "{")
        .replace("\\right}", "}")
        .replace("\\left|", "|")
        .replace("\\right|", "|")
        .replace("\\left.", "")
        .replace("\\right.", "")
        .replace("\\left", "")
        .replace("\\right", "")
        .replace("\\bigl", "")
        .replace("\\bigr", "")
        .replace("\\Bigl", "")
        .replace("\\Bigr", "")

    // 4. Integral & Summation limits
    s = s.replace("\\int", "∫")
        .replace("\\iint", "∬")
        .replace("\\iiint", "∭")
        .replace("\\oint", "∮")
        .replace("\\sum", "∑")
        .replace("\\prod", "∏")

    // 5. Arrows (longest first!)
    s = s.replace("\\Longleftrightarrow", " ⟺ ")
        .replace("\\Leftrightarrow", " ⇔ ")
        .replace("\\longrightarrow", " ⟶ ")
        .replace("\\longleftarrow", " ⟵ ")
        .replace("\\Longrightarrow", " ⟹ ")
        .replace("\\Longleftarrow", " ⟸ ")
        .replace("\\implies", " ⟹ ")
        .replace("\\impliedby", " ⟸ ")
        .replace("\\iff", " ⟺ ")
        .replace("\\rightarrow", " → ")
        .replace("\\leftarrow", " ← ")
        .replace("\\Rightarrow", " ⇒ ")
        .replace("\\Leftarrow", " ⇐ ")
        .replace("\\updownarrow", "↕")
        .replace("\\uparrow", "↑")
        .replace("\\downarrow", "↓")
        .replace("\\searrow", "↘")
        .replace("\\swarrow", "↙")
        .replace("\\nearrow", "↗")
        .replace("\\nwarrow", "↖")
        .replace("\\mapsto", " ↦ ")
        .replace("\\to", " → ")

    // 6. Relations, Logic & Set Theory (longest first so \leq/\left never collide with \le!)
    s = s.replace("\\therefore", "∴ ")
        .replace("\\because", "∵ ")
        .replace("\\subseteq", " ⊆ ")
        .replace("\\supseteq", " ⊇ ")
        .replace("\\subset", " ⊂ ")
        .replace("\\supset", " ⊃ ")
        .replace("\\notin", " ∉ ")
        .replace("\\in", " ∈ ")
        .replace("\\forall", "∀")
        .replace("\\exists", "∃")
        .replace("\\nexists", "∄")
        .replace("\\emptyset", "∅")
        .replace("\\varnothing", "∅")
        .replace("\\cup", " ∪ ")
        .replace("\\cap", " ∩ ")
        .replace("\\leq", " ≤ ")
        .replace("\\geq", " ≥ ")
        .replace("\\neq", " ≠ ")
        .replace("\\ne", " ≠ ")
        .replace("\\le", " ≤ ")
        .replace("\\ge", " ≥ ")
        .replace("\\approx", " ≈ ")
        .replace("\\equiv", " ≡ ")
        .replace("\\cong", " ≅ ")
        .replace("\\sim", " ∼ ")
        .replace("\\propto", " ∝ ")

    // 7. Binary Operators, Dots & Geometry
    s = s.replace("\\times", " × ")
        .replace("\\div", " ÷ ")
        .replace("\\pm", " ± ")
        .replace("\\mp", " ∓ ")
        .replace("\\cdot", " · ")
        .replace("\\cdots", "⋯")
        .replace("\\ldots", "…")
        .replace("\\dots", "…")
        .replace("\\infty", "∞")
        .replace("\\partial", "∂")
        .replace("\\nabla", "∇")
        .replace("\\angle", "∠")
        .replace("\\perp", "⊥")
        .replace("\\parallel", "∥")
        .replace("\\triangle", "△")
        .replace("\\degree", "°")
        .replace("\\circ", "°")

    // 8. Blackboard bold & Greek letters (longest first)
    s = s.replace("\\mathbb{E}", "𝔼")
        .replace("\\mathbb{R}", "ℝ")
        .replace("\\mathbb{C}", "ℂ")
        .replace("\\mathbb{Z}", "ℤ")
        .replace("\\mathbb{N}", "ℕ")
        .replace("\\mathbb{P}", "ℙ")
        .replace("\\mathbb{Q}", "ℚ")
        .replace("\\mathbf{1}", "𝟏")
        .replace("\\varepsilon", "ε")
        .replace("\\varphi", "φ")
        .replace("\\alpha", "α")
        .replace("\\beta", "β")
        .replace("\\gamma", "γ")
        .replace("\\Gamma", "Γ")
        .replace("\\delta", "δ")
        .replace("\\Delta", "Δ")
        .replace("\\epsilon", "ε")
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
        .replace("\\Phi", "Φ")
        .replace("\\chi", "χ")
        .replace("\\psi", "ψ")
        .replace("\\Psi", "Ψ")
        .replace("\\omega", "ω")
        .replace("\\Omega", "Ω")

    // 9. Standard functions & spacing
    s = s.replace("\\arg\\min", "argmin")
        .replace("\\arg\\max", "argmax")
        .replace("\\ln", "ln")
        .replace("\\log", "log")
        .replace("\\sin", "sin")
        .replace("\\cos", "cos")
        .replace("\\tan", "tan")
        .replace("\\sec", "sec")
        .replace("\\csc", "csc")
        .replace("\\cot", "cot")
        .replace("\\exp", "exp")
        .replace("\\lim", "lim")
        .replace("\\qquad", "    ")
        .replace("\\quad", "  ")
        .replace("\\;", " ")
        .replace("\\:", " ")
        .replace("\\,", " ")
        .replace("\\!", "")
        .replace("\\ ", " ")

    // 10. Superscripts ^{...} / ^x and Subscripts _{...} / _x
    s = parseSupAndSubMarkers(s)

    // 11. Convert mathematical hyphen-minus to true minus sign '−' outside \text{...}
    s = normalizeMathMinusAndSpaces(s)

    return s
}

private fun cleanTextMacroInner(inner: String): String {
    return inner
        .replace("\\%", "%")
        .replace("\\{", "{")
        .replace("\\}", "}")
        .replace("\\&", "&")
        .replace("\\ ", " ")
}

private fun flattenNestedFractions(input: String): String {
    if (!input.contains("\\frac") && !input.contains("\\dfrac") && !input.contains("\\tfrac")) return input
    val sb = StringBuilder()
    var i = 0
    val len = input.length
    while (i < len) {
        val cmdLen = when {
            input.startsWith("\\frac", i) && (i + 5 == len || !input[i + 5].isLetter()) -> 5
            (input.startsWith("\\dfrac", i) || input.startsWith("\\tfrac", i)) &&
                (i + 6 == len || !input[i + 6].isLetter()) -> 6
            else -> 0
        }
        if (cmdLen > 0) {
            var c = skipSpaces(input, i + cmdLen)
            val g1 = extractBraceGroup(input, c)
            if (g1 != null) {
                c = skipSpaces(input, g1.second)
                val g2 = extractBraceGroup(input, c)
                if (g2 != null) {
                    val n = cleanInlineMathSymbols(g1.first.trim())
                    val d = cleanInlineMathSymbols(g2.first.trim())
                    sb.append("(").append(n).append(" / ").append(d).append(")")
                    i = g2.second
                    continue
                }
            }
        }
        sb.append(input[i])
        i++
    }
    return sb.toString()
}

private fun parseSupAndSubMarkers(input: String): String {
    if (!input.contains('^') && !input.contains('_')) return input
    val sb = StringBuilder()
    var i = 0
    val len = input.length
    var inText = false

    while (i < len) {
        val ch = input[i]
        if (ch == U_TEXT_START) {
            inText = true
            sb.append(ch)
            i++
            continue
        }
        if (ch == U_TEXT_END) {
            inText = false
            sb.append(ch)
            i++
            continue
        }
        if (!inText && (ch == '^' || ch == '_')) {
            val startMarker = if (ch == '^') U_SUP_START else U_SUB_START
            val endMarker = if (ch == '^') U_SUP_END else U_SUB_END
            val nextIdx = i + 1
            if (nextIdx < len) {
                if (input[nextIdx] == '{') {
                    val grp = extractBraceGroup(input, nextIdx)
                    if (grp != null) {
                        sb.append(startMarker)
                            .append(cleanInlineMathSymbols(grp.first))
                            .append(endMarker)
                        i = grp.second
                        continue
                    }
                } else if (input[nextIdx] == U_TEXT_START) {
                    val endTxt = input.indexOf(U_TEXT_END, nextIdx + 1)
                    if (endTxt != -1) {
                        sb.append(startMarker)
                            .append(input.substring(nextIdx, endTxt + 1))
                            .append(endMarker)
                        i = endTxt + 1
                        continue
                    }
                } else if (!input[nextIdx].isWhitespace()) {
                    sb.append(startMarker)
                        .append(input[nextIdx])
                        .append(endMarker)
                    i = nextIdx + 1
                    continue
                }
            }
        }
        sb.append(ch)
        i++
    }
    return sb.toString()
}

private fun normalizeMathMinusAndSpaces(input: String): String {
    val sb = StringBuilder(input.length + 8)
    var inText = false
    for (i in input.indices) {
        val ch = input[i]
        when (ch) {
            U_TEXT_START -> {
                inText = true
                sb.append(ch)
            }
            U_TEXT_END -> {
                inText = false
                sb.append(ch)
            }
            '-' -> {
                if (inText) {
                    sb.append('-')
                } else {
                    sb.append('−')
                }
            }
            else -> sb.append(ch)
        }
    }
    return REGEX_MULTI_SPACE.replace(sb.toString(), "  ")
}

/**
 * Converts a cleaned math string (with private-use markers for `\text`, `^`, `_`) into
 * a textbook-styled `AnnotatedString`:
 * - `\text{...}` and Devanagari/Hindi characters -> Upright normal font
 * - ASCII variable letters (`x`, `y`, `z`, `a`, `b`, `P`...) -> Mathematical Serif Italic
 * - Standard math function names (`sin`, `cos`, `log`, `NOTA`...) -> Upright
 * - Numbers (`0..9`), `%`, brackets, operators (`+`, `−`, `×`, `=`) -> Upright
 * - Superscripts & subscripts -> `BaselineShift.Superscript` / `BaselineShift.Subscript` at `0.74.em`
 */
fun formatMathRunToAnnotatedString(cleaned: String): AnnotatedString {
    if (cleaned.isEmpty()) return AnnotatedString("")
    val cached = STYLED_RUN_CACHE[cleaned]
    if (cached != null) return cached

    val result = buildAnnotatedString {
        var inTextMacro = false
        var supDepth = 0
        var subDepth = 0
        var i = 0
        val len = cleaned.length

        while (i < len) {
            when (val ch = cleaned[i]) {
                U_TEXT_START -> {
                    inTextMacro = true
                    i++
                }
                U_TEXT_END -> {
                    inTextMacro = false
                    i++
                }
                U_SUP_START -> {
                    supDepth++
                    pushStyle(
                        SpanStyle(
                            baselineShift = BaselineShift.Superscript,
                            fontSize = 0.74.em
                        )
                    )
                    i++
                }
                U_SUP_END -> {
                    if (supDepth > 0) {
                        pop()
                        supDepth--
                    }
                    i++
                }
                U_SUB_START -> {
                    subDepth++
                    pushStyle(
                        SpanStyle(
                            baselineShift = BaselineShift.Subscript,
                            fontSize = 0.74.em
                        )
                    )
                    i++
                }
                U_SUB_END -> {
                    if (subDepth > 0) {
                        pop()
                        subDepth--
                    }
                    i++
                }
                else -> {
                    if (inTextMacro) {
                        append(ch)
                        i++
                    } else if (ch in 'a'..'z' || ch in 'A'..'Z') {
                        // Collect contiguous ASCII letters to check if it's a known upright function/word
                        val start = i
                        while (i < len && (cleaned[i] in 'a'..'z' || cleaned[i] in 'A'..'Z')) {
                            i++
                        }
                        val word = cleaned.substring(start, i)
                        if (word in UPRIGHT_MATH_WORDS) {
                            append(word)
                        } else {
                            pushStyle(
                                SpanStyle(
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = FontStyle.Italic
                                )
                            )
                            append(word)
                            pop()
                        }
                    } else {
                        append(ch)
                        i++
                    }
                }
            }
        }
        while (supDepth > 0) {
            pop()
            supDepth--
        }
        while (subDepth > 0) {
            pop()
            subDepth--
        }
    }

    STYLED_RUN_CACHE[cleaned] = result
    return result
}

// =========================================================================
// BALANCED BRACE & DELIMITER HELPERS
// =========================================================================

private fun skipSpaces(s: String, startIndex: Int): Int {
    var idx = startIndex
    while (idx < s.length && s[idx].isWhitespace()) idx++
    return idx
}

/**
 * Given `s` and `openBraceIdx` pointing at `{`, returns `Pair(innerContent, indexAfterCloseBrace)`.
 */
fun extractBraceGroup(s: String, openBraceIdx: Int): Pair<String, Int>? {
    if (openBraceIdx >= s.length || s[openBraceIdx] != '{') return null
    var depth = 0
    var i = openBraceIdx
    val len = s.length
    while (i < len) {
        val ch = s[i]
        if (ch == '\\' && i + 1 < len) {
            i += 2
            continue
        }
        if (ch == '{') {
            depth++
        } else if (ch == '}') {
            depth--
            if (depth == 0) {
                return Pair(s.substring(openBraceIdx + 1, i), i + 1)
            }
        }
        i++
    }
    // Unclosed brace fallback: take rest of string
    return Pair(s.substring(openBraceIdx + 1), len)
}

private fun extractBalancedParenGroup(
    s: String,
    openIdx: Int,
    openChar: Char,
    closeChar: Char
): Pair<String, Int>? {
    if (openIdx >= s.length || s[openIdx] != openChar) return null
    var depth = 0
    var i = openIdx
    val len = s.length
    while (i < len) {
        val ch = s[i]
        if (ch == '\\' && i + 1 < len) {
            i += 2
            continue
        }
        if (ch == openChar) {
            depth++
        } else if (ch == closeChar) {
            depth--
            if (depth == 0) {
                return Pair(s.substring(openIdx + 1, i), i + 1)
            }
        }
        i++
    }
    return null
}

private data class LeftRightMatch(
    val leftDelim: String,
    val rightDelim: String,
    val innerLatex: String,
    val nextIndex: Int
)

private fun extractLeftRightGroup(s: String, leftCmdIdx: Int): LeftRightMatch? {
    val afterLeft = skipSpaces(s, leftCmdIdx + 5)
    val leftDelimPair = readDelimiterToken(s, afterLeft) ?: return null
    val contentStart = leftDelimPair.second

    var depth = 1
    var i = contentStart
    val len = s.length

    while (i < len) {
        if (s.startsWith("\\left", i) && (i + 5 < len && !s[i + 5].isLetter())) {
            val d = readDelimiterToken(s, skipSpaces(s, i + 5))
            if (d != null) {
                depth++
                i = d.second
                continue
            }
        } else if (s.startsWith("\\right", i) && (i + 6 <= len && (i + 6 == len || !s[i + 6].isLetter()))) {
            val d = readDelimiterToken(s, skipSpaces(s, i + 6))
            if (d != null) {
                depth--
                if (depth == 0) {
                    val inner = s.substring(contentStart, i).trim()
                    return LeftRightMatch(
                        leftDelim = leftDelimPair.first,
                        rightDelim = d.first,
                        innerLatex = inner,
                        nextIndex = d.second
                    )
                }
                i = d.second
                continue
            }
        }
        i++
    }
    return null
}

private fun readDelimiterToken(s: String, idx: Int): Pair<String, Int>? {
    if (idx >= s.length) return null
    return when {
        s.startsWith("\\{", idx) -> Pair("{", idx + 2)
        s.startsWith("\\}", idx) -> Pair("}", idx + 2)
        s.startsWith("\\|", idx) -> Pair("‖", idx + 2)
        s[idx] in charArrayOf('(', ')', '[', ']', '|', '.', '{', '}') -> Pair(s[idx].toString(), idx + 1)
        else -> null
    }
}

private fun replaceMacroWithBalancedBraces(
    input: String,
    macros: List<String>,
    transform: (String) -> String
): String {
    if (macros.none { input.contains(it) }) return input
    val sb = StringBuilder(input.length)
    var i = 0
    val len = input.length
    while (i < len) {
        var matchedMacro: String? = null
        for (m in macros) {
            if (input.startsWith(m, i) && (i + m.length == len || !input[i + m.length].isLetter())) {
                matchedMacro = m
                break
            }
        }
        if (matchedMacro != null) {
            val braceStart = skipSpaces(input, i + matchedMacro.length)
            val grp = extractBraceGroup(input, braceStart)
            if (grp != null) {
                sb.append(transform(grp.first))
                i = grp.second
                continue
            }
        }
        sb.append(input[i])
        i++
    }
    return sb.toString()
}

/**
 * Counts true visual characters (ignoring Devanagari combining vowel matras & private markers).
 */
fun visualMathCharCount(s: String): Int {
    var count = 0
    for (ch in s) {
        if (ch in U_TEXT_START..U_SUB_END) continue
        val type = Character.getType(ch).toByte()
        if (type == Character.NON_SPACING_MARK ||
            type == Character.COMBINING_SPACING_MARK ||
            type == Character.ENCLOSING_MARK ||
            ch in '\u0900'..'\u0903' ||
            ch == '\u093C' ||
            ch in '\u093E'..'\u094D' ||
            ch in '\u0951'..'\u0957' ||
            ch in '\u0962'..'\u0963' ||
            ch in '\u200B'..'\u200D'
        ) {
            continue
        }
        count++
    }
    return count
}
