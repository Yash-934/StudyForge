package com.studyforge.app.ui.screens.ai

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyforge.app.domain.ai.AiResponse
import com.studyforge.app.ui.components.MarkdownMathView
import com.studyforge.app.viewmodel.StudyViewModel

@Composable
fun AiAssistantDialog(
    title: String,
    contextText: String,
    viewModel: StudyViewModel,
    chapterId: Long = 1L,
    subjectId: Long = 1L,
    batchId: Long = 1L,
    onInsertIntoNote: ((String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val aiState by viewModel.aiResponseState.collectAsState()
    val generatedQuestionsState by viewModel.generatedQuestionsState.collectAsState()

    var selectedAction by remember { mutableStateOf("simplify") }
    var promptInput by remember { mutableStateOf("") }
    var questionCount by remember { mutableStateOf(3) }

    AlertDialog(
        onDismissRequest = {
            viewModel.resetAiState()
            onDismiss()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Study Assistant (Gemini)", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Select what you want Gemini to do with this note:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Capability chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedAction == "simplify",
                        onClick = { selectedAction = "simplify" },
                        label = { Text("Simplify") }
                    )
                    FilterChip(
                        selected = selectedAction == "explain",
                        onClick = { selectedAction = "explain" },
                        label = { Text("Explain") }
                    )
                    FilterChip(
                        selected = selectedAction == "questions",
                        onClick = { selectedAction = "questions" },
                        label = { Text("Questions") }
                    )
                }

                if (selectedAction == "explain") {
                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = { promptInput = it },
                        label = { Text("Concept to Explain") },
                        placeholder = { Text("e.g. Z-Score, Cointegration, ADF Test") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // AI Response Area (Explain & Simplify)
                when (val state = aiState) {
                    is AiResponse.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Gemini is analyzing your material...", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    is AiResponse.Success -> {
                        Text(
                            text = "AI Generated Output:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Scrollable response container to prevent text box overflow
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp, max = 300.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                MarkdownMathView(markdownText = state.data)
                            }
                        }

                        // Action buttons to add / save the generated output
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (onInsertIntoNote != null) {
                                Button(
                                    onClick = {
                                        val sectionTitle = if (selectedAction == "simplify") "Summary" else "Explanation"
                                        onInsertIntoNote("\n\n## ⚡ AI $sectionTitle: $title\n\n${state.data}\n\n")
                                        Toast.makeText(context, "Added and saved to note!", Toast.LENGTH_SHORT).show()
                                        viewModel.resetAiState()
                                        onDismiss()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add to Note")
                                }
                            }

                            FilledTonalButton(
                                onClick = {
                                    val sectionTitle = if (selectedAction == "simplify") "Summary" else "Explanation"
                                    viewModel.saveAiGeneratedSummaryAsNote(
                                        chapterId = chapterId,
                                        subjectId = subjectId,
                                        batchId = batchId,
                                        title = "AI $sectionTitle: $title",
                                        content = state.data
                                    )
                                    Toast.makeText(context, "Saved as new note!", Toast.LENGTH_SHORT).show()
                                    viewModel.resetAiState()
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Note")
                            }

                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(state.data))
                                    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    is AiResponse.Error -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Notice: ${state.message}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    AiResponse.Idle -> {}
                }

                // Questions generated area
                when (val qState = generatedQuestionsState) {
                    is AiResponse.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        }
                    }
                    is AiResponse.Success -> {
                        Text(
                            text = "Generated ${qState.data.size} Questions:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Scrollable questions box to prevent dialog overflow
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp, max = 280.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                qState.data.forEachIndexed { idx, q ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "Q${idx + 1}. ${q.question}",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            q.options.forEachIndexed { oIdx, opt ->
                                                val char = ('A' + oIdx)
                                                Text(
                                                    text = "($char) $opt",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Ans: ${q.correctAnswers.firstOrNull() ?: ""} • ${q.explanation}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Action 1: Insert Questions directly into Current Note
                        if (onInsertIntoNote != null) {
                            Button(
                                onClick = {
                                    val formattedQuestions = buildString {
                                        appendLine("\n\n## 📝 Practice Questions ($title)\n")
                                        qState.data.forEachIndexed { idx, q ->
                                            appendLine("### Q${idx + 1}. ${q.question}\n")
                                            q.options.forEachIndexed { optIdx, opt ->
                                                val char = ('A' + optIdx)
                                                appendLine("- [ ] **$char)** $opt")
                                            }
                                            appendLine("\n**Correct Answer:** ${q.correctAnswers.firstOrNull() ?: "Option A"}")
                                            if (q.explanation.isNotBlank()) {
                                                appendLine("**Explanation:** ${q.explanation}\n")
                                            }
                                            appendLine("---")
                                        }
                                    }
                                    onInsertIntoNote(formattedQuestions)
                                    Toast.makeText(context, "Added ${qState.data.size} questions to note!", Toast.LENGTH_SHORT).show()
                                    viewModel.resetAiState()
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Insert Questions in Note")
                            }
                        }

                        // Action 2: Add Questions into Chapter Question Bank
                        FilledTonalButton(
                            onClick = {
                                viewModel.saveAiGeneratedQuestionsToChapter(
                                    chapterId = chapterId,
                                    subjectId = subjectId,
                                    batchId = batchId,
                                    questions = qState.data
                                )
                                Toast.makeText(context, "Saved to Question Bank!", Toast.LENGTH_SHORT).show()
                                viewModel.resetAiState()
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save to Chapter Question Bank")
                        }
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (selectedAction) {
                        "explain" -> {
                            val c = promptInput.ifBlank { title }
                            viewModel.askAiToExplain(c, contextText)
                        }
                        "simplify" -> {
                            viewModel.askAiToSimplify(title, contextText)
                        }
                        "questions" -> {
                            viewModel.askAiToGenerateQuestions(title, contextText, questionCount)
                        }
                    }
                }
            ) {
                Text(if (selectedAction == "questions") "Generate Questions" else "Generate with AI")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    viewModel.resetAiState()
                    onDismiss()
                }
            ) {
                Text("Close")
            }
        }
    )
}
