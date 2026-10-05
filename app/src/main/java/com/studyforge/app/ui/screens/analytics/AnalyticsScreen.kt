package com.studyforge.app.ui.screens.analytics

import android.widget.Toast
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyforge.app.domain.ai.AiResponse
import com.studyforge.app.ui.components.MarkdownMathView
import com.studyforge.app.ui.components.MasteryProgressBar
import com.studyforge.app.ui.components.SectionHeader
import com.studyforge.app.ui.components.SquircleIconBadge
import com.studyforge.app.ui.components.StatCard
import com.studyforge.app.ui.components.StudyCard
import com.studyforge.app.ui.components.StudyHeatmap
import com.studyforge.app.ui.theme.ErrorRed
import com.studyforge.app.ui.theme.PurpleAccent
import com.studyforge.app.ui.theme.SuccessGreen
import com.studyforge.app.ui.theme.WarningYellow
import com.studyforge.app.viewmodel.StudyViewModel

@Composable
fun AnalyticsScreen(
    viewModel: StudyViewModel,
    onNavigateToTestPlayer: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val analytics by viewModel.analytics.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val aiState by viewModel.aiResponseState.collectAsState()

    var showDiagnosticDialog by remember { mutableStateOf(false) }
    var isCraftingTest by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Performance Analytics",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Deterministic Mastery & AI Learning Telemetry",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // AI Smart Performance Diagnostic & Adaptive Test Crafter Card
        item {
            StudyCard(
                shape = RoundedCornerShape(22.dp),
                borderColor = PurpleAccent.copy(alpha = 0.45f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SquircleIconBadge(
                                icon = Icons.Default.Psychology,
                                accentColor = PurpleAccent,
                                size = 42.dp,
                                iconSize = 24.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "AI Learning Diagnostic",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Gemini Deep Telemetry & Adaptive Crafter",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PurpleAccent
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "AI analyzes your test scores, chapter accuracy, time management, and mistake book traps to evaluate blind spots and craft a personalized diagnostic test.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                showDiagnosticDialog = true
                                viewModel.askAiForOverallPerformanceAnalysis()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Full Diagnosis")
                        }

                        FilledTonalButton(
                            onClick = {
                                isCraftingTest = true
                                viewModel.craftAndSaveAiAdaptiveTest(questionCount = 5) { createdTestId ->
                                    isCraftingTest = false
                                    onNavigateToTestPlayer(createdTestId)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isCraftingTest,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isCraftingTest) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Craft Adaptive Test")
                            }
                        }
                    }
                }
            }
        }

        // Top 4 Stat Metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Study Time",
                    value = "${analytics.totalStudyTimeMinutes}m",
                    icon = Icons.Default.Timer,
                    accentColor = MaterialTheme.colorScheme.primary,
                    subtext = "${analytics.currentStreakDays} days streak",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Questions Solved",
                    value = "${analytics.totalQuestionsSolved}",
                    icon = Icons.Default.CheckCircle,
                    accentColor = SuccessGreen,
                    subtext = "${String.format("%.1f", analytics.overallAccuracy)}% accuracy",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Tests Completed",
                    value = "${analytics.totalTestsCompleted}",
                    icon = Icons.Default.FitnessCenter,
                    accentColor = PurpleAccent,
                    subtext = "${String.format("%.1f", analytics.overallAverageScore)}% avg score",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Mistakes Resolved",
                    value = "${analytics.resolvedMistakesCount}/${analytics.totalMistakesRecorded}",
                    icon = Icons.Default.QueryStats,
                    accentColor = WarningYellow,
                    subtext = "Blind spots cleared",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Study Heatmap
        item {
            SectionHeader(title = "Study Heatmap & Consistency")
            StudyHeatmap(sessions = sessions)
        }

        // Subject Mastery Breakdown
        item {
            SectionHeader(title = "Subject Mastery Breakdown")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (analytics.subjectMasteries.isEmpty()) {
                    Text(
                        text = "No subjects registered yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    analytics.subjectMasteries.forEach { sub ->
                        StudyCard(
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sub.subjectName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${sub.masteryScore}%",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${sub.chapterCount} chapters • ${sub.testsCompleted} tests completed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                MasteryProgressBar(score = sub.masteryScore, showLabel = false)
                            }
                        }
                    }
                }
            }
        }

        // Strong vs Weak Chapters
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Weak chapters
                StudyCard(
                    shape = RoundedCornerShape(18.dp),
                    borderColor = ErrorRed.copy(alpha = 0.35f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ThumbDown, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Weak Areas (<60%)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = ErrorRed)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (analytics.weakChapters.isEmpty()) {
                            Text("None! Great shape.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            analytics.weakChapters.take(3).forEach { chap ->
                                Text("• ${chap.chapterName} (${chap.masteryScore}%)", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // Strong chapters
                StudyCard(
                    shape = RoundedCornerShape(18.dp),
                    borderColor = SuccessGreen.copy(alpha = 0.35f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ThumbUp, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Strong Areas (≥60%)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = SuccessGreen)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (analytics.strongChapters.isEmpty()) {
                            Text("Solve more tests to build strength.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            analytics.strongChapters.take(3).forEach { chap ->
                                Text("• ${chap.chapterName} (${chap.masteryScore}%)", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    // AI Performance Diagnostic Dialog
    if (showDiagnosticDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.resetAiState()
                showDiagnosticDialog = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Performance Diagnostic", fontWeight = FontWeight.Bold)
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
                                    Text("Gemini is analyzing your full learning telemetry...", style = MaterialTheme.typography.bodySmall)
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
                                        showDiagnosticDialog = false
                                        isCraftingTest = true
                                        viewModel.craftAndSaveAiAdaptiveTest(questionCount = 5) { testId ->
                                            isCraftingTest = false
                                            onNavigateToTestPlayer(testId)
                                        }
                                    },
                                    modifier = Modifier.weight(1.3f)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Craft & Start Test")
                                }

                                FilledTonalButton(
                                    onClick = {
                                        viewModel.saveAiGeneratedSummaryAsNote(
                                            chapterId = 1L,
                                            subjectId = 1L,
                                            batchId = 1L,
                                            title = "AI Performance Diagnostic Report",
                                            content = state.data
                                        )
                                        Toast.makeText(context, "Saved diagnostic as Note!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Save Note")
                                }

                                OutlinedButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(state.data))
                                        Toast.makeText(context, "Copied report to clipboard!", Toast.LENGTH_SHORT).show()
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
                                    text = "Error: ${state.message}\n\nPlease check your Gemini API key in Settings.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                        AiResponse.Idle -> {}
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetAiState()
                        showDiagnosticDialog = false
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }
}
