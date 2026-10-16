package com.studyforge.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Clean, flat, modern Explanation Box with rounded corners and subtle tint
 * matching the user's requested screenshot style (no 3D shadows or heavy bevels).
 * Fully supports LaTeX math formulas, KaTeX, Markdown, and tables via MarkdownMathView.
 */
@Composable
fun ExplanationBox(
    explanation: String,
    topic: String? = null,
    modifier: Modifier = Modifier
) {
    if (explanation.isBlank() && topic.isNullOrBlank()) return

    val isDark = isSystemInDarkTheme()

    // Clean subtle tint colors
    val containerBg = if (isDark) Color(0xFF132B25) else Color(0xFFE8F5E9)
    val borderColor = if (isDark) Color(0xFF1F4E44) else Color(0xFFA5D6A7)
    val titleColor = if (isDark) Color(0xFF80CBC4) else Color(0xFF2E7D32)
    val textColor = if (isDark) Color(0xFFE0E0E0) else Color(0xFF1C2B24)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(containerBg)
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Clean "Explanation" with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "Explanation",
                    tint = titleColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Explanation",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = titleColor
                )
            }

            // Explanation Content with full LaTeX math, KaTeX, and Markdown support
            if (explanation.isNotBlank()) {
                MarkdownMathView(
                    text = explanation,
                    color = textColor,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Optional Topic Tag at bottom
            if (!topic.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = (if (isDark) Color(0xFF1E3A32) else Color(0xFFC8E6C9)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Topic: $topic",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = titleColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
