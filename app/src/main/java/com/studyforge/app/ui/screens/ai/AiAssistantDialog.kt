package com.studyforge.app.ui.screens.ai

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studyforge.app.domain.ai.AiResponse
import com.studyforge.app.ui.components.MarkdownMathView
import com.studyforge.app.viewmodel.StudyViewModel

@Composable
fun AiAssistantDialog(
    title: String,
    contextText: String,
    viewModel: StudyViewModel,
    onDismiss: () -> Unit
) {
    val aiState by viewModel.aiResponseState.collectAsState()
    val generatedQuestionsState by viewModel.generatedQuestionsState.collectAsState()

    var selectedAction by remember { mutableStateOf("explain") }
    var promptInput by remember { mutableStateOf("") }

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
                Text("AI Study Assistant", fontWeight = FontWeight.Bold)
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
                    text = "Select an AI capability with your chosen context:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Capability chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedAction == "explain",
                        onClick = { selectedAction = "explain" },
                        label = { Text("Explain") }
                    )
                    FilterChip(
                        selected = selectedAction == "simplify",
                        onClick = { selectedAction = "simplify" },
                        label = { Text("Simplify") }
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
                        placeholder = { Text("e.g. LIATE rule, integration by parts") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // AI Response Area
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
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                MarkdownMathView(markdownText = state.data)
                            }
                        }
                    }
                    is AiResponse.Error -> {
                        Text(
                            text = "Error: ${state.message}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
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
                            text = "Generated ${qState.data.size} questions from note:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        qState.data.forEachIndexed { idx, q ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "${idx + 1}. ${q.question}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = "Answer: ${q.correctAnswers.firstOrNull() ?: ""} • ${q.explanation}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
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
                            viewModel.askAiToGenerateQuestions(title, contextText, 3)
                        }
                    }
                }
            ) {
                Text("Generate")
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
