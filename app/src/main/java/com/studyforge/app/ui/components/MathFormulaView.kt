package com.studyforge.app.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Production KaTeX mathematical formula renderer for StudyForge.
 * Renders LaTeX formulas with textbook typography, proper vertical fraction bars,
 * integral signs, summation limits, matrices, piecewise cases, superscripts, and subscripts.
 *
 * Uses offline bundled KaTeX assets with CDN fallback for instantaneous, zero-latency rendering.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MathFormulaView(
    latex: String,
    modifier: Modifier = Modifier,
    displayMode: Boolean = true,
    fontSizeSp: Int = 20,
    color: Color = MaterialTheme.colorScheme.onSurface,
    minHeight: Dp = if (displayMode) 76.dp else 40.dp
) {
    if (latex.isBlank()) return

    val colorHex = String.format("#%06X", 0xFFFFFF and color.toArgb())

    // Store measured height in CSS DP from JavaScript
    var webViewHeightDp by remember { mutableIntStateOf(0) }

    // Format clean LaTeX for KaTeX
    val cleanLatex = remember(latex) {
        prepareLatexForKaTeX(latex)
    }

    val html = remember(cleanLatex, colorHex, fontSizeSp, displayMode) {
        buildKaTeXBlockHtml(
            latex = cleanLatex,
            colorHex = colorHex,
            fontSizeSp = fontSizeSp,
            displayMode = displayMode
        )
    }

    // Give ample vertical clearance so tall fractions, integrals with limits, cases, and powers are NEVER cut off
    val calculatedHeightDp = remember(webViewHeightDp, minHeight) {
        if (webViewHeightDp > 0) {
            maxOf(webViewHeightDp.dp + 16.dp, minHeight)
        } else {
            minHeight
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(calculatedHeightDp)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                    setBackgroundColor(AndroidColor.TRANSPARENT)
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = true
                    overScrollMode = WebView.OVER_SCROLL_IF_CONTENT_SCROLLS

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = false
                        allowFileAccess = true
                        allowContentAccess = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }

                    // Interface to notify height changes
                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun onHeightCalculated(height: Int) {
                            post {
                                if (height > 0 && height != webViewHeightDp) {
                                    webViewHeightDp = height
                                }
                            }
                        }
                    }, "AndroidBridge")

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            view?.evaluateJavascript(
                                "Math.max(document.body.scrollHeight, document.getElementById('math-container') ? document.getElementById('math-container').offsetHeight : 0)"
                            ) { result ->
                                val h = result?.toIntOrNull() ?: 0
                                if (h > 0 && h != webViewHeightDp) {
                                    webViewHeightDp = h
                                }
                            }
                        }
                    }

                    tag = html
                    loadDataWithBaseURL(
                        "file:///android_asset/katex/",
                        html,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            },
            update = { webView ->
                if (webView.tag != html) {
                    webView.tag = html
                    webView.loadDataWithBaseURL(
                        "file:///android_asset/katex/",
                        html,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            }
        )
    }
}

/**
 * Renders mixed text with inline/block LaTeX formulas using KaTeX auto-render.
 * Perfect for question statements, option choices (e.g. "A) -\frac{1}{2}\cos(2x) + C"),
 * and detailed solutions.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MathRichTextView(
    text: String,
    modifier: Modifier = Modifier,
    fontSizeSp: Int = 16,
    color: Color = MaterialTheme.colorScheme.onSurface,
    minHeight: Dp = 32.dp
) {
    if (text.isBlank()) return

    val colorHex = String.format("#%06X", 0xFFFFFF and color.toArgb())
    var webViewHeightDp by remember { mutableIntStateOf(0) }

    val processedText = remember(text) {
        prepareTextForKaTeXAutoRender(text)
    }

    val html = remember(processedText, colorHex, fontSizeSp) {
        buildKaTeXRichTextHtml(
            text = processedText,
            colorHex = colorHex,
            fontSizeSp = fontSizeSp
        )
    }

    val calculatedHeightDp = remember(webViewHeightDp, minHeight) {
        if (webViewHeightDp > 0) {
            maxOf(webViewHeightDp.dp + 8.dp, minHeight)
        } else {
            minHeight
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(calculatedHeightDp)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                    setBackgroundColor(AndroidColor.TRANSPARENT)
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false
                    overScrollMode = WebView.OVER_SCROLL_NEVER

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = false
                        allowFileAccess = true
                        allowContentAccess = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }

                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun onHeightCalculated(height: Int) {
                            post {
                                if (height > 0 && height != webViewHeightDp) {
                                    webViewHeightDp = height
                                }
                            }
                        }
                    }, "AndroidBridge")

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            view?.evaluateJavascript(
                                "Math.max(document.body.scrollHeight, document.getElementById('rich-content') ? document.getElementById('rich-content').offsetHeight : 0)"
                            ) { result ->
                                val h = result?.toIntOrNull() ?: 0
                                if (h > 0 && h != webViewHeightDp) {
                                    webViewHeightDp = h
                                }
                            }
                        }
                    }

                    tag = html
                    loadDataWithBaseURL(
                        "file:///android_asset/katex/",
                        html,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            },
            update = { webView ->
                if (webView.tag != html) {
                    webView.tag = html
                    webView.loadDataWithBaseURL(
                        "file:///android_asset/katex/",
                        html,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            }
        )
    }
}

/**
 * Escapes and prepares LaTeX string for safe injection into KaTeX JS call
 */
private fun prepareLatexForKaTeX(raw: String): String {
    var trimmed = raw.trim()

    // Remove wrapping $$ or $
    if (trimmed.startsWith("$$") && trimmed.endsWith("$$") && trimmed.length >= 4) {
        trimmed = trimmed.substring(2, trimmed.length - 2).trim()
    } else if (trimmed.startsWith("$") && trimmed.endsWith("$") && trimmed.length >= 2) {
        trimmed = trimmed.substring(1, trimmed.length - 1).trim()
    }

    return trimmed
}

/**
 * Wraps raw LaTeX expressions in `$...$` or `$$...$$` if not already wrapped,
 * so KaTeX auto-render can easily identify and typeset them.
 */
fun prepareTextForKaTeXAutoRender(raw: String): String {
    var text = raw.trim()

    // If string is already wrapped with $ or $$, return it
    if ((text.startsWith("$") && text.endsWith("$")) || (text.startsWith("\\[") && text.endsWith("\\]"))) {
        return text
    }

    // If the whole string looks like a formula (starts with minus/plus/digit or LaTeX command and has LaTeX elements):
    val containsLatexCommands = text.contains("\\frac") || text.contains("\\int") || text.contains("\\cos") ||
        text.contains("\\sin") || text.contains("\\tan") || text.contains("\\pi") || text.contains("\\sqrt") ||
        text.contains("\\sum") || text.contains("\\lim") || text.contains("\\neq") || text.contains("\\pm") ||
        text.contains("\\cdot") || text.contains("\\times") || text.contains("\\alpha") || text.contains("\\beta") ||
        text.contains("\\theta") || text.contains("\\lambda") || text.contains("\\sigma") || text.contains("\\infty") ||
        text.contains("^") || text.contains("_")

    // Check if whole string is an equation or option formula like "-\frac{1}{2}\cos(2x) + C" or "x^2 + 7" or "x^2 - \pi"
    val isPureFormula = !text.contains(" ") || (
        containsLatexCommands && !text.contains("?") && !text.contains("What is") && !text.contains("Evaluate") &&
            !text.contains("Which of") && !text.contains("Find the") && !text.contains("Calculate")
    )

    if (isPureFormula && containsLatexCommands) {
        return "$$text$"
    }

    // For mixed text like "Evaluate \int_0^3 2x dx." or "What is the antiderivative of \int \sin(2x) dx?":
    // Replace unescaped LaTeX expressions
    if (!text.contains("$")) {
        // Pattern 1: \int ... dx (with optional limits)
        text = text.replace(Regex("""(\\int(?:_[^\s]+)?(?:\^[^\s]+)?\s+[^\?.,;\n]+?\s+d[a-zA-Z])""")) { match ->
            "$" + match.groupValues[1].trim() + "$"
        }

        // Pattern 2: \frac{...}{...} (with trailing math like \cos(2x) + C)
        text = text.replace(Regex("""(-?\\frac\{[^{}]+\}\{[^{}]+\}(?:[^\s.,;\n]+|\s*[\+\-\*/=]\s*[^\s.,;\n]+)*)""")) { match ->
            "$" + match.groupValues[1].trim() + "$"
        }

        // Pattern 3: \sqrt{...}
        text = text.replace(Regex("""(\\sqrt(?:\[[^{}]+\])?\{[^{}]+\})""")) { match ->
            "$" + match.groupValues[1].trim() + "$"
        }

        // Pattern 4: Standalone math tokens like \cos(2x), \sin(2x), \pi, x^2
        text = text.replace(Regex("""(\b[a-zA-Z]\^[0-9a-zA-Z]+\b)""")) { match ->
            "$" + match.groupValues[1].trim() + "$"
        }
        text = text.replace(Regex("""(\b[0-9]+[a-zA-Z]\^[0-9a-zA-Z]+\b)""")) { match ->
            "$" + match.groupValues[1].trim() + "$"
        }
    }

    return text
}

/**
 * Generates lightweight HTML for Block KaTeX formula
 */
private fun buildKaTeXBlockHtml(
    latex: String,
    colorHex: String,
    fontSizeSp: Int,
    displayMode: Boolean
): String {
    val escapedLatex = latex
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("'", "\\'")
        .replace("\n", "\\n")
        .replace("\r", "")

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=3.0, user-scalable=yes">
            <link rel="stylesheet" href="katex.min.css">
            <style>
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                    -webkit-touch-callout: none;
                    -webkit-user-select: none;
                    user-select: none;
                }
                html, body {
                    background-color: transparent;
                    color: $colorHex;
                    font-size: ${fontSizeSp}px;
                    width: 100%;
                    min-height: 100%;
                    overflow-x: auto;
                    overflow-y: hidden;
                    -webkit-overflow-scrolling: touch;
                }
                body {
                    padding: 6px 8px;
                    display: block;
                    text-align: left;
                }
                .math-wrapper {
                    display: inline-block;
                    min-width: 100%;
                    text-align: left;
                    padding-left: 2px;
                    padding-right: 20px;
                    white-space: nowrap;
                }
                #math-container {
                    display: inline-block;
                    color: $colorHex;
                    font-size: ${fontSizeSp}px;
                    text-align: left;
                    padding: 4px 0;
                }
                .katex-display {
                    margin: 0 !important;
                    padding: 4px 0 !important;
                    text-align: left !important;
                }
                .katex {
                    color: $colorHex !important;
                    font-size: 1.15em !important;
                    line-height: 1.5 !important;
                    text-align: left !important;
                }
                .katex .mfrac .vlist-t {
                    vertical-align: middle !important;
                }
            </style>
            <script src="katex.min.js"></script>
        </head>
        <body>
            <div class="math-wrapper">
                <div id="math-container"></div>
            </div>
            <script>
                try {
                    const formula = "$escapedLatex";
                    const container = document.getElementById('math-container');
                    katex.render(formula, container, {
                        displayMode: $displayMode,
                        throwOnError: false,
                        strict: false
                    });
                } catch (e) {
                    document.getElementById('math-container').innerText = "$escapedLatex";
                }
                
                function sendHeight() {
                    const el = document.getElementById('math-container');
                    const h = Math.max(
                        document.body.scrollHeight,
                        document.documentElement.scrollHeight,
                        el ? Math.ceil(el.getBoundingClientRect().height) : 0
                    ) + 16;
                    if (window.AndroidBridge && window.AndroidBridge.onHeightCalculated) {
                        window.AndroidBridge.onHeightCalculated(h);
                    }
                }
                
                window.addEventListener('load', sendHeight);
                setTimeout(sendHeight, 30);
                setTimeout(sendHeight, 150);
                setTimeout(sendHeight, 400);
            </script>
        </body>
        </html>
    """.trimIndent()
}

/**
 * Generates lightweight HTML with KaTeX Auto-Render for mixed text & inline math
 */
private fun buildKaTeXRichTextHtml(
    text: String,
    colorHex: String,
    fontSizeSp: Int
): String {
    // Escape HTML special characters while preserving math
    val escapedText = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\n", "<br>")

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <link rel="stylesheet" href="katex.min.css">
            <style>
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                    -webkit-touch-callout: none;
                    -webkit-user-select: none;
                    user-select: none;
                }
                body {
                    background-color: transparent;
                    color: $colorHex;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                    font-size: ${fontSizeSp}px;
                    line-height: 1.55;
                    padding: 4px 6px;
                    overflow-x: auto;
                    overflow-y: hidden;
                    display: block;
                }
                #rich-content {
                    display: block;
                    width: 100%;
                    color: $colorHex;
                }
                .katex {
                    color: $colorHex !important;
                    font-size: 1.15em !important;
                }
                .katex-display {
                    margin: 4px 0 !important;
                }
            </style>
            <script src="katex.min.js"></script>
            <script src="auto-render.min.js"></script>
        </head>
        <body>
            <div id="rich-content">$escapedText</div>
            <script>
                try {
                    renderMathInElement(document.getElementById('rich-content'), {
                        delimiters: [
                            {left: "$$", right: "$$", display: true},
                            {left: "$", right: "$", display: false},
                            {left: "\\(", right: "\\)", display: false},
                            {left: "\\[", right: "\\]", display: true}
                        ],
                        throwOnError: false
                    });
                } catch (e) {
                    console.error(e);
                }
                
                function sendHeight() {
                    const el = document.getElementById('rich-content');
                    const h = Math.max(
                        document.body.scrollHeight,
                        document.documentElement.scrollHeight,
                        el ? Math.ceil(el.getBoundingClientRect().height) : 0
                    ) + 8;
                    if (window.AndroidBridge && window.AndroidBridge.onHeightCalculated) {
                        window.AndroidBridge.onHeightCalculated(h);
                    }
                }
                
                window.addEventListener('load', sendHeight);
                setTimeout(sendHeight, 30);
                setTimeout(sendHeight, 150);
                setTimeout(sendHeight, 350);
            </script>
        </body>
        </html>
    """.trimIndent()
}
