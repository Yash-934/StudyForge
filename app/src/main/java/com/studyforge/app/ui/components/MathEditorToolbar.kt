package com.studyforge.app.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MathEditorToolbar(
    onInsertText: (String) -> Unit,
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
            ToolbarChip(label = "H1") { onInsertText("\n# ") }
            ToolbarChip(label = "H2") { onInsertText("\n## ") }
            ToolbarChip(label = "H3") { onInsertText("\n### ") }
            ToolbarChip(label = "Bold") { onInsertText("**text**") }
            ToolbarChip(label = "Italic") { onInsertText("*text*") }
            ToolbarChip(label = "Code") { onInsertText("`code`") }
            ToolbarChip(label = "List") { onInsertText("\n- ") }
            ToolbarChip(label = "Task") { onInsertText("\n- [ ] ") }
            ToolbarChip(label = "$$ Math $$") { onInsertText("\n$$\n\\int_a^b f(x) dx\n$$\n") }
            ToolbarChip(label = "\$ Inline \$") { onInsertText(" \$x^2\$ ") }
            ToolbarChip(label = "x²") { onInsertText("^2") }
            ToolbarChip(label = "√x") { onInsertText("\\sqrt{x}") }
            ToolbarChip(label = "a/b") { onInsertText("\\frac{a}{b}") }
            ToolbarChip(label = "∫") { onInsertText("\\int ") }
            ToolbarChip(label = "∑") { onInsertText("\\sum ") }
            ToolbarChip(label = "π") { onInsertText("\\pi") }
            ToolbarChip(label = "θ") { onInsertText("\\theta") }
            ToolbarChip(label = "±") { onInsertText("\\pm ") }
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
