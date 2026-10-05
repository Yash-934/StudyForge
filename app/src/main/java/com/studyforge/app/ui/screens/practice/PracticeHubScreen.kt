package com.studyforge.app.ui.screens.practice

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyforge.app.domain.ai.AiResponse
import com.studyforge.app.domain.model.TestMode
import com.studyforge.app.ui.components.MarkdownMathView
import com.studyforge.app.ui.components.SectionHeader
import com.studyforge.app.ui.components.SquircleIconBadge
import com.studyforge.app.ui.components.StatCard
import com.studyforge.app.ui.components.StudyCard
import com.studyforge.app.ui.theme.ErrorRed
import com.studyforge.app.ui.theme.PurpleAccent
import com.studyforge.app.ui.theme.SuccessGreen
import com.studyforge.app.ui.theme.WarningYellow
import com.studyforge.app.viewmodel.StudyViewModel

@Composable
fun PracticeHubScreen(
    viewModel: StudyViewModel,
    onNavigateToSmartTest: (Long) -> Unit,
    onNavigateToMistakes: (Long) -> Unit,
    onNavigateToRevision: () -> Unit,
    onNavigateToFlashcards: (Long) -> Unit,
    onNavigateToFormulaVault: (Long) -> Unit,
    onNavigateToTests: (Long) -> Unit,
    onNavigateToQuestionBank: (Long) -> Unit,
    onNavigateToResult: (Long) -> Unit,
    onNavigateToTestPlayer: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val tests by viewModel.tests.collectAsState()
    val attempts by viewModel.attempts.collectAsState()
    val unresolvedMistakes by viewModel.unresolvedMistakes.collectAsState()
    val revisionDue by viewModel.revisionDueItems.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val aiState by viewModel.aiResponseState.collectAsState()

    var showDiagnosticDialog by remember { mutableStateOf(false) }
    var isCraftingTest by remember { mutableStateOf(false) }

    val topChapId = chapters.firstOrNull()?.id ?: 1L

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Practice & Mastery",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Active Recall • Spaced Repetition • Adaptive Drills",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Smart Adaptive Drill Hero Banner with Gemini AI integration
        item {
            StudyCard(
                shape = RoundedCornerShape(22.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "⚡",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 42.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 40.dp)
                    )

                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ADAPTIVE ENGINE & GEMINI AI",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.2.sp,
                                    fontSize = 12.sp
                                ),
                                color = PurpleAccent
                            )

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PurpleAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = PurpleAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "AI Smart Diagnostic & Adaptive Crafter",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "AI deeply audits your chapter mastery, accuracy trends, and mistake traps, then crafts a personalized diagnostic test.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
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
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Crafting...")
                                } else {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Craft AI Test")
                                }
                            }

                            FilledTonalButton(
                                onClick = {
                                    showDiagnosticDialog = true
                                    viewModel.askAiForOverallPerformanceAnalysis()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Audit")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { onNavigateToSmartTest(topChapId) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Configure Custom Distribution Test", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // Practice Modules Grid / List
        item {
            SectionHeader(title = "Practice Modes")
        }

        item {
            PracticeModeItem(
                title = "Mistake Book",
                subtitle = "${unresolvedMistakes.size} mistakes awaiting resolution",
                icon = Icons.Default.ErrorOutline,
                accentColor = ErrorRed,
                badge = if (unresolvedMistakes.isNotEmpty()) "${unresolvedMistakes.size}" else "0",
                onClick = { onNavigateToMistakes(-1L) }
            )
        }

        item {
            PracticeModeItem(
                title = "Spaced Revision Queue",
                subtitle = "${revisionDue.size} items due for review today",
                icon = Icons.Default.NotificationsActive,
                accentColor = WarningYellow,
                badge = if (revisionDue.isNotEmpty()) "${revisionDue.size} Due" else "Clear",
                onClick = onNavigateToRevision
            )
        }

        item {
            PracticeModeItem(
                title = "Formula Vault Recall",
                subtitle = "Active memory test with hidden equations",
                icon = Icons.Default.Calculate,
                accentColor = MaterialTheme.colorScheme.primary,
                onClick = { onNavigateToFormulaVault(-1L) }
            )
        }

        item {
            PracticeModeItem(
                title = "Flashcard Decks",
                subtitle = "Flip-card spaced repetition drills",
                icon = Icons.Default.Style,
                accentColor = Color(0xFF0284C7),
                onClick = { onNavigateToFlashcards(-1L) }
            )
        }

        item {
            PracticeModeItem(
                title = "Question Bank Practice",
                subtitle = "Browse and solve by difficulty and topic",
                icon = Icons.Default.Quiz,
                accentColor = SuccessGreen,
                onClick = { onNavigateToQuestionBank(-1L) }
            )
        }

        item {
            PracticeModeItem(
                title = "Mock Exams & Tests",
                subtitle = "${tests.size} tests configured",
                icon = Icons.Default.FitnessCenter,
                accentColor = MaterialTheme.colorScheme.secondary,
                onClick = { onNavigateToTests(-1L) }
            )
        }

        // Recent Test Attempts
        if (attempts.isNotEmpty()) {
            item {
                SectionHeader(title = "Recent Test Attempts")
            }
            items(attempts.take(5)) { att ->
                StudyCard(
                    shape = RoundedCornerShape(18.dp),
                    onClick = { onNavigateToResult(att.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (att.accuracyPercentage >= 70) SuccessGreen.copy(alpha = 0.15f)
                                    else if (att.accuracyPercentage >= 40) WarningYellow.copy(alpha = 0.15f)
                                    else ErrorRed.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${att.accuracyPercentage.toInt()}%",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (att.accuracyPercentage >= 70) SuccessGreen
                                else if (att.accuracyPercentage >= 40) WarningYellow
                                else ErrorRed
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = att.testTitle,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Score: ${att.totalScore}/${att.maxScore} • Time: ${att.timeSpentSeconds / 60}m ${att.timeSpentSeconds % 60}s",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showDiagnosticDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.resetAiState()
                showDiagnosticDialog = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Performance Audit", fontWeight = FontWeight.Bold)
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
                                    Text("Gemini is analyzing your overall telemetry...", style = MaterialTheme.typography.bodySmall)
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
                                        isCraftingTest = true
                                        showDiagnosticDialog = false
                                        viewModel.craftAndSaveAiAdaptiveTest(questionCount = 5) { testId ->
                                            isCraftingTest = false
                                            onNavigateToTestPlayer(testId)
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Craft & Start Test")
                                }

                                OutlinedButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(state.data))
                                        Toast.makeText(context, "Copied audit to clipboard!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
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
                        showDiagnosticDialog = false
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun PracticeModeItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    badge: String? = null,
    onClick: () -> Unit
) {
    StudyCard(
        shape = RoundedCornerShape(20.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SquircleIconBadge(
                icon = icon,
                accentColor = accentColor,
                size = 42.dp,
                iconSize = 22.dp
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accentColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
