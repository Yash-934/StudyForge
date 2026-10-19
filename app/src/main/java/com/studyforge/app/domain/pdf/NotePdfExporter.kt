package com.studyforge.app.domain.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.studyforge.app.ui.components.MarkdownNode
import com.studyforge.app.ui.components.formatLatexToReadableMath
import com.studyforge.app.ui.components.parseMarkdownToNodes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object NotePdfExporter {

    private const val PAGE_WIDTH = 595 // A4 width in points (72 dpi)
    private const val PAGE_HEIGHT = 842 // A4 height in points
    private const val MARGIN_HORIZONTAL = 42f
    private const val MARGIN_TOP = 48f
    private const val MARGIN_BOTTOM = 48f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN_HORIZONTAL * 2)

    suspend fun exportNoteToPdfUri(
        context: Context,
        outputUri: Uri,
        noteTitle: String,
        chapterName: String,
        markdownContent: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        try {
            val nodes = parseMarkdownToNodes(markdownContent.ifBlank { "No content." })
            var pageNumber = 1
            var currentPage = pdfDocument.startPage(
                PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            )
            var canvas = currentPage.canvas
            var cursorY = MARGIN_TOP

            fun startNewPage() {
                drawPageFooter(canvas, pageNumber)
                pdfDocument.finishPage(currentPage)
                pageNumber += 1
                currentPage = pdfDocument.startPage(
                    PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                )
                canvas = currentPage.canvas
                cursorY = MARGIN_TOP
            }

            fun ensureSpace(requiredHeight: Float) {
                if (cursorY + requiredHeight > PAGE_HEIGHT - MARGIN_BOTTOM) {
                    startNewPage()
                }
            }

            // 1. Header Banner on Page 1
            val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E6F4EA")
                style = Paint.Style.FILL
            }
            val badgeTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0D652D")
                textSize = 9.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            val headerTag = "STUDYFORGE NOTES • ${chapterName.ifBlank { "CHAPTER REVISION" }.uppercase()}"
            val tagWidth = (badgeTextPaint.measureText(headerTag) + 20f).coerceAtMost(CONTENT_WIDTH)
            canvas.drawRoundRect(
                RectF(MARGIN_HORIZONTAL, cursorY, MARGIN_HORIZONTAL + tagWidth, cursorY + 20f),
                6f,
                6f,
                badgePaint
            )
            canvas.drawText(headerTag, MARGIN_HORIZONTAL + 10f, cursorY + 13.5f, badgeTextPaint)
            cursorY += 28f

            // Note Title
            val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0F172A")
                textSize = 20f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            cursorY = drawMultilineText(
                canvas = canvas,
                text = noteTitle.ifBlank { "Untitled Study Note" },
                paint = titlePaint,
                x = MARGIN_HORIZONTAL,
                y = cursorY,
                width = CONTENT_WIDTH.toInt()
            ) + 10f

            // Divider line
            val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#CBD5E1")
                strokeWidth = 1.2f
            }
            canvas.drawLine(MARGIN_HORIZONTAL, cursorY, PAGE_WIDTH - MARGIN_HORIZONTAL, cursorY, dividerPaint)
            cursorY += 16f

            // Body Paints
            val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1E293B")
                textSize = 11f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }
            val headingPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#162238")
                textSize = 14f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            val mathPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0F172A")
                textSize = 12f
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            }

            for (node in nodes) {
                when (node) {
                    is MarkdownNode.Heading -> {
                        val cleanText = cleanMarkdownFormatting(node.text.text)
                        headingPaint.textSize = when (node.level) {
                            1 -> 15.5f
                            2 -> 13.5f
                            else -> 12f
                        }
                        val layout = buildStaticLayout(cleanText, headingPaint, (CONTENT_WIDTH - 14f).toInt())
                        ensureSpace(layout.height + 16f)
                        val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.parseColor("#DF6900")
                            style = Paint.Style.FILL
                        }
                        canvas.drawRoundRect(
                            RectF(MARGIN_HORIZONTAL, cursorY + 2f, MARGIN_HORIZONTAL + 4f, cursorY + layout.height + 2f),
                            2f,
                            2f,
                            barPaint
                        )
                        drawLayout(canvas, layout, MARGIN_HORIZONTAL + 12f, cursorY)
                        cursorY += layout.height + 10f
                    }

                    is MarkdownNode.Paragraph -> {
                        val cleanText = cleanMarkdownFormatting(node.text.text)
                        val layout = buildStaticLayout(cleanText, bodyPaint, CONTENT_WIDTH.toInt())
                        ensureSpace(layout.height + 10f)
                        drawLayout(canvas, layout, MARGIN_HORIZONTAL, cursorY)
                        cursorY += layout.height + 8f
                    }

                    is MarkdownNode.ListItem -> {
                        val cleanText = "${node.prefix}  ${cleanMarkdownFormatting(node.text.text)}"
                        val layout = buildStaticLayout(cleanText, bodyPaint, (CONTENT_WIDTH - 12f).toInt())
                        ensureSpace(layout.height + 8f)
                        drawLayout(canvas, layout, MARGIN_HORIZONTAL + 8f, cursorY)
                        cursorY += layout.height + 6f
                    }

                    is MarkdownNode.Checklist -> {
                        val prefix = if (node.checked) "☑" else "☐"
                        val cleanText = "$prefix  ${cleanMarkdownFormatting(node.text.text)}"
                        val layout = buildStaticLayout(cleanText, bodyPaint, (CONTENT_WIDTH - 12f).toInt())
                        ensureSpace(layout.height + 8f)
                        drawLayout(canvas, layout, MARGIN_HORIZONTAL + 8f, cursorY)
                        cursorY += layout.height + 6f
                    }

                    is MarkdownNode.Blockquote -> {
                        val cleanText = cleanMarkdownFormatting(node.text.text)
                        val layout = buildStaticLayout(cleanText, bodyPaint, (CONTENT_WIDTH - 18f).toInt())
                        ensureSpace(layout.height + 10f)
                        val quoteBar = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.parseColor("#64748B")
                            style = Paint.Style.FILL
                        }
                        canvas.drawRoundRect(
                            RectF(MARGIN_HORIZONTAL, cursorY, MARGIN_HORIZONTAL + 3f, cursorY + layout.height + 4f),
                            1.5f,
                            1.5f,
                            quoteBar
                        )
                        drawLayout(canvas, layout, MARGIN_HORIZONTAL + 12f, cursorY + 2f)
                        cursorY += layout.height + 8f
                    }

                    is MarkdownNode.Formula -> {
                        val readableMath = node.formatted.ifBlank { formatLatexToReadableMath(node.rawLatex) }
                        val layout = buildStaticLayout(readableMath, mathPaint, (CONTENT_WIDTH - 28f).toInt())
                        val boxHeight = layout.height + 20f
                        ensureSpace(boxHeight + 10f)

                        val boxFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.parseColor("#F1F5F9")
                            style = Paint.Style.FILL
                        }
                        val boxStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.parseColor("#CBD5E1")
                            style = Paint.Style.STROKE
                            strokeWidth = 1f
                        }
                        val rect = RectF(MARGIN_HORIZONTAL, cursorY, PAGE_WIDTH - MARGIN_HORIZONTAL, cursorY + boxHeight)
                        canvas.drawRoundRect(rect, 8f, 8f, boxFill)
                        canvas.drawRoundRect(rect, 8f, 8f, boxStroke)
                        drawLayout(canvas, layout, MARGIN_HORIZONTAL + 14f, cursorY + 10f)
                        cursorY += boxHeight + 10f
                    }

                    is MarkdownNode.CodeBlock -> {
                        val codePaint = TextPaint(bodyPaint).apply {
                            typeface = Typeface.MONOSPACE
                            textSize = 9.5f
                        }
                        val layout = buildStaticLayout(node.code.trimEnd(), codePaint, (CONTENT_WIDTH - 24f).toInt())
                        val boxHeight = layout.height + 18f
                        ensureSpace(boxHeight + 10f)

                        val boxFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.parseColor("#F8FAFC")
                            style = Paint.Style.FILL
                        }
                        val boxStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.parseColor("#E2E8F0")
                            style = Paint.Style.STROKE
                            strokeWidth = 1f
                        }
                        val rect = RectF(MARGIN_HORIZONTAL, cursorY, PAGE_WIDTH - MARGIN_HORIZONTAL, cursorY + boxHeight)
                        canvas.drawRoundRect(rect, 6f, 6f, boxFill)
                        canvas.drawRoundRect(rect, 6f, 6f, boxStroke)
                        drawLayout(canvas, layout, MARGIN_HORIZONTAL + 12f, cursorY + 9f)
                        cursorY += boxHeight + 8f
                    }

                    is MarkdownNode.Callout -> {
                        val titleLayout = buildStaticLayout(
                            "★ ${node.tag.uppercase()}",
                            TextPaint(headingPaint).apply { textSize = 10.5f },
                            (CONTENT_WIDTH - 24f).toInt()
                        )
                        val bodyLayout = buildStaticLayout(
                            cleanMarkdownFormatting(node.content.text),
                            bodyPaint,
                            (CONTENT_WIDTH - 24f).toInt()
                        )
                        val totalBoxHeight = titleLayout.height + bodyLayout.height + 22f
                        ensureSpace(totalBoxHeight + 10f)

                        val calloutBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.parseColor("#FFFBEB")
                            style = Paint.Style.FILL
                        }
                        val calloutBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.parseColor("#F59E0B")
                            style = Paint.Style.STROKE
                            strokeWidth = 1.2f
                        }
                        val rect = RectF(MARGIN_HORIZONTAL, cursorY, PAGE_WIDTH - MARGIN_HORIZONTAL, cursorY + totalBoxHeight)
                        canvas.drawRoundRect(rect, 8f, 8f, calloutBg)
                        canvas.drawRoundRect(rect, 8f, 8f, calloutBorder)
                        drawLayout(canvas, titleLayout, MARGIN_HORIZONTAL + 12f, cursorY + 8f)
                        drawLayout(canvas, bodyLayout, MARGIN_HORIZONTAL + 12f, cursorY + 12f + titleLayout.height)
                        cursorY += totalBoxHeight + 10f
                    }

                    is MarkdownNode.Table -> {
                        val colCount = node.headers.size.coerceAtLeast(1)
                        val colWidth = CONTENT_WIDTH / colCount
                        val cellPadding = 6f
                        val cellTextWidth = (colWidth - cellPadding * 2).toInt().coerceAtLeast(24)

                        val headerPaint = TextPaint(bodyPaint).apply {
                            color = Color.WHITE
                            textSize = 9.5f
                            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        }
                        val cellPaint = TextPaint(bodyPaint).apply {
                            textSize = 9.5f
                        }

                        // Draw Header Row
                        val headerLayouts = node.headers.map { h ->
                            buildStaticLayout(cleanMarkdownFormatting(h.text), headerPaint, cellTextWidth)
                        }
                        val headerHeight = (headerLayouts.maxOfOrNull { it.height } ?: 14) + 12f
                        ensureSpace(headerHeight + 20f)

                        val headerBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.parseColor("#162238")
                            style = Paint.Style.FILL
                        }
                        canvas.drawRect(
                            MARGIN_HORIZONTAL,
                            cursorY,
                            PAGE_WIDTH - MARGIN_HORIZONTAL,
                            cursorY + headerHeight,
                            headerBg
                        )
                        headerLayouts.forEachIndexed { idx, layout ->
                            drawLayout(
                                canvas,
                                layout,
                                MARGIN_HORIZONTAL + (idx * colWidth) + cellPadding,
                                cursorY + 6f
                            )
                        }
                        cursorY += headerHeight

                        // Draw Data Rows
                        node.rows.forEachIndexed { rIdx, row ->
                            val rowLayouts = (0 until colCount).map { cIdx ->
                                val rawCell = row.getOrNull(cIdx)?.text ?: ""
                                buildStaticLayout(cleanMarkdownFormatting(rawCell), cellPaint, cellTextWidth)
                            }
                            val rowHeight = (rowLayouts.maxOfOrNull { it.height } ?: 14) + 12f
                            ensureSpace(rowHeight + 6f)

                            val rowBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                color = if (rIdx % 2 == 0) Color.WHITE else Color.parseColor("#F8FAFC")
                                style = Paint.Style.FILL
                            }
                            val cellBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                color = Color.parseColor("#CBD5E1")
                                style = Paint.Style.STROKE
                                strokeWidth = 0.7f
                            }
                            canvas.drawRect(
                                MARGIN_HORIZONTAL,
                                cursorY,
                                PAGE_WIDTH - MARGIN_HORIZONTAL,
                                cursorY + rowHeight,
                                rowBg
                            )
                            canvas.drawRect(
                                MARGIN_HORIZONTAL,
                                cursorY,
                                PAGE_WIDTH - MARGIN_HORIZONTAL,
                                cursorY + rowHeight,
                                cellBorder
                            )
                            rowLayouts.forEachIndexed { cIdx, layout ->
                                drawLayout(
                                    canvas,
                                    layout,
                                    MARGIN_HORIZONTAL + (cIdx * colWidth) + cellPadding,
                                    cursorY + 6f
                                )
                            }
                            cursorY += rowHeight
                        }
                        cursorY += 10f
                    }

                    is MarkdownNode.Divider -> {
                        ensureSpace(14f)
                        canvas.drawLine(MARGIN_HORIZONTAL, cursorY + 6f, PAGE_WIDTH - MARGIN_HORIZONTAL, cursorY + 6f, dividerPaint)
                        cursorY += 14f
                    }

                    else -> Unit
                }
            }

            drawPageFooter(canvas, pageNumber)
            pdfDocument.finishPage(currentPage)

            context.contentResolver.openOutputStream(outputUri)?.use { out ->
                pdfDocument.writeTo(out)
            } ?: return@withContext Result.failure(IllegalStateException("Could not open PDF destination."))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            pdfDocument.close()
        }
    }

    private fun cleanMarkdownFormatting(raw: String): String {
        val withInlineMath = Regex("""\$\$(.+?)\$\$|\$(.+?)\$""").replace(raw) { m ->
            val expr = m.groupValues[1].ifEmpty { m.groupValues[2] }
            formatLatexToReadableMath(expr)
        }
        return withInlineMath
            .replace("**", "")
            .replace("__", "")
            .replace("`", "")
            .trim()
    }

    private fun buildStaticLayout(text: String, paint: TextPaint, width: Int): StaticLayout {
        val safeWidth = width.coerceAtLeast(40)
        return StaticLayout.Builder
            .obtain(text, 0, text.length, paint, safeWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(2f, 1.05f)
            .setIncludePad(false)
            .build()
    }

    private fun drawLayout(canvas: Canvas, layout: StaticLayout, x: Float, y: Float) {
        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun drawMultilineText(
        canvas: Canvas,
        text: String,
        paint: TextPaint,
        x: Float,
        y: Float,
        width: Int
    ): Float {
        val layout = buildStaticLayout(text, paint, width)
        drawLayout(canvas, layout, x, y)
        return y + layout.height
    }

    private fun drawPageFooter(canvas: Canvas, pageNumber: Int) {
        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        canvas.drawText(
            "Generated by StudyForge • Personal Learning OS",
            MARGIN_HORIZONTAL,
            PAGE_HEIGHT - 22f,
            footerPaint
        )
        val pageLabel = "Page $pageNumber"
        val labelW = footerPaint.measureText(pageLabel)
        canvas.drawText(
            pageLabel,
            PAGE_WIDTH - MARGIN_HORIZONTAL - labelW,
            PAGE_HEIGHT - 22f,
            footerPaint
        )
    }
}
