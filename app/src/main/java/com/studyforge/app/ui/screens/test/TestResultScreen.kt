package com.studyforge.app.ui.screens.test

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyforge.app.data.local.entities.TestAttemptEntity
import com.studyforge.app.domain.ai.AiResponse
import com.studyforge.app.ui.components.MarkdownMathView
import com.studyforge.app.ui.components.StatCard
import com.studyforge.app.ui.components.StudyCard
import com.studyforge.app.ui.theme.ErrorRed
import com.studyforge.app.ui.theme.PurpleAccent
import com.studyforge.app.ui.theme.SuccessGreen
import com.studyforge.app.ui.theme.WarningYellow
import com.studyforge.app.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestResultScreen(
    attemptId: Long,
    viewModel: StudyViewModel,
    onNavigateToReview: (Long) -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val attempts by viewModel.attempts.collectAsState()
    val aiState by viewModel.aiResponseState.collectAsState()
    var attempt by remember { mutableStateOf<TestAttemptEntity?>(null) }
    var showAiEvaluationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(attemptId, attempts) {
        val found = attempts.find { it.id == attemptId }
        if (found != null) {
            attempt = found
        } else {
            attempt = viewModel.repository.getAttemptById(attemptId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scorecard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        val att = attempt
        if (att == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading scorecard...")
            }
            return@Scaffold
        }

        val totalMins = att.timeSpentSeconds / 60
        val totalSecs = att.timeSpentSeconds % 60
        val avgSecPerQ = if (att.totalQuestions > 0) att.timeSpentSeconds / att.totalQuestions else 0

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Score Card
            StudyCard(
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = att.testTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${String.format("%.1f", att.totalScore)} / ${String.format("%.1f", att.maxScore)}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 38.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Score: ${String.format("%.1f", (att.totalScore / att.maxScore.coerceAtLeast(1.0)) * 100)}% • Accuracy: ${String.format("%.1f", att.accuracyPercentage)}%",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Correct / Wrong / Skipped Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Correct",
                    value = "${att.correctCount}",
                    icon = Icons.Default.CheckCircle,
                    accentColor = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Incorrect",
                    value = "${att.wrongCount}",
                    icon = Icons.Default.Close,
                    accentColor = ErrorRed,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Skipped",
                    value = "${att.skippedCount}",
                    icon = Icons.Default.RemoveCircle,
                    accentColor = WarningYellow,
                    modifier = Modifier.weight(1f)
                )
            }

            // Time Breakdown Card
            StudyCard(
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Time & Speed Analytics",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Time Used:", style = MaterialTheme.typography.bodyMedium)
                        Text("${totalMins}m ${totalSecs}s", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Average Pace per Question:", style = MaterialTheme.typography.bodyMedium)
                        Text("${avgSecPerQ} seconds", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Action Buttons
            FilledTonalButton(
                onClick = {
                    showAiEvaluationDialog = true
                    viewModel.askAiToEvaluateTestAttempt(att.id)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Deep Evaluation & Misconception Diagnosis", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { onNavigateToReview(att.id) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Review Every Question & Solution", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onFinish,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Finish & Return to Hub")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAiEvaluationDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.resetAiState()
                showAiEvaluationDialog = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Test Evaluation", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (val state = aiState) {
                        is AiResponse.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Gemini is diagnosing your test responses...", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        is AiResponse.Success -> {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 120.dp, max = 340.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(14.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    MarkdownMathView(markdownText = state.data)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.saveAiGeneratedSummaryAsNote(
                                            chapterId = 1L,
                                            subjectId = 1L,
                                            batchId = 1L,
                                            title = "AI Evaluation: ${attempt?.testTitle ?: "Test"}",
                                            content = state.data
                                        )
                                        showAiEvaluationDialog = false
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Save Evaluation as Note")
                                }
                            }
                        }
                        is AiResponse.Error -> {
                            Text(
                                text = "Notice: ${state.message}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        AiResponse.Idle -> {}
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetAiState()
                        showAiEvaluationDialog = false
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }
}
