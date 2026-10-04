package com.studyforge.app.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MathEditorToolbar(
    onInsertText: (String) -> Unit,
    onPickImage: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            if (onPickImage != null) {
                ToolbarIconChip(label = "Gallery Image", icon = Icons.Default.AddPhotoAlternate, onClick = onPickImage)
            }
            ToolbarIconChip(
                label = "Table",
                icon = Icons.Default.TableChart,
                onClick = {
                    onInsertText("\n| Feature / Metric | Description |\n|---|---|\n| Sample Field | Sample Value |\n| Metric 2 | Value 2 |\n")
                }
            )
            ToolbarIconChip(
                label = "Code Block",
                icon = Icons.Default.Code,
                onClick = {
                    onInsertText("\n```kotlin\n// Code snippet\nval x = 10\n```\n")
                }
            )
            ToolbarChip(label = "H1") { onInsertText("\n# ") }
            ToolbarChip(label = "H2") { onInsertText("\n## ") }
            ToolbarChip(label = "H3") { onInsertText("\n### ") }
            ToolbarChip(label = "Bold") { onInsertText("**text**") }
            ToolbarChip(label = "Italic") { onInsertText("*text*") }
            ToolbarChip(label = "Inline Code") { onInsertText("`code`") }
            ToolbarChip(label = "List") { onInsertText("\n- ") }
            ToolbarChip(label = "Task") { onInsertText("\n- [ ] ") }
            ToolbarChip(label = "$$ Math $$") { onInsertText("\n$$\n\\Delta W_B = \\int_a^b f(x) dx\n$$\n") }
            ToolbarChip(label = "\$ Inline \$") { onInsertText(" \$x^2\$ ") }
            ToolbarChip(label = "x²") { onInsertText("^2") }
            ToolbarChip(label = "√x") { onInsertText("\\sqrt{x}") }
            ToolbarChip(label = "a/b") { onInsertText("\\frac{a}{b}") }
            ToolbarChip(label = "∫") { onInsertText("\\int ") }
            ToolbarChip(label = "∑") { onInsertText("\\sum ") }
            ToolbarChip(label = "Δ") { onInsertText("\\Delta ") }
            ToolbarChip(label = "π") { onInsertText("\\pi") }
            ToolbarChip(label = "θ") { onInsertText("\\theta") }
            ToolbarChip(label = "λ") { onInsertText("\\lambda ") }
            ToolbarChip(label = "μ") { onInsertText("\\mu ") }
            ToolbarChip(label = "σ") { onInsertText("\\sigma ") }
            ToolbarChip(label = "±") { onInsertText("\\pm ") }
            ToolbarChip(label = "≤") { onInsertText("\\le ") }
            ToolbarChip(label = "≥") { onInsertText("\\ge ") }
            ToolbarChip(label = "[FORMULA]") { onInsertText("\n[FORMULA]\n\\int u dv = uv - \\int v du\n") }
            ToolbarChip(label = "[IMPORTANT]") { onInsertText("\n[IMPORTANT]\nKey theorem or condition.\n") }
            ToolbarChip(label = "[CONCEPT]") { onInsertText("\n[CONCEPT]\nCore theoretical intuition.\n") }
            ToolbarChip(label = "[EXAMPLE]") { onInsertText("\n[EXAMPLE]\nStep 1: ...\n") }
            ToolbarChip(label = "[MISTAKE]") { onInsertText("\n[COMMON MISTAKE]\nWatch out for sign errors.\n") }
        }
    }
}

@Composable
private fun ToolbarChip(
    label: String,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.padding(end = 6.dp)
    )
}

@Composable
private fun ToolbarIconChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        },
        label = { Text(label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            labelColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.padding(end = 6.dp)
    )
}
