package com.studyforge.app.ui.screens.practice

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyforge.app.domain.model.TestMode
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
    modifier: Modifier = Modifier
) {
    val tests by viewModel.tests.collectAsState()
    val attempts by viewModel.attempts.collectAsState()
    val unresolvedMistakes by viewModel.unresolvedMistakes.collectAsState()
    val revisionDue by viewModel.revisionDueItems.collectAsState()
    val chapters by viewModel.chapters.collectAsState()

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

        // Smart Adaptive Drill Hero Banner
        item {
            StudyCard(
                shape = RoundedCornerShape(22.dp),
                onClick = { onNavigateToSmartTest(topChapId) }
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
                                text = "ADAPTIVE ENGINE",
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
                            text = "Smart Test Generator",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Formulates a custom drill targeting 40% weak areas, 30% medium, 20% fresh learning, and 10% past mistakes.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { onNavigateToSmartTest(topChapId) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Start Smart Test", fontWeight = FontWeight.Bold)
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = att.testTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Score: ${String.format("%.1f", att.totalScore)} / ${String.format("%.1f", att.maxScore)} • Accuracy: ${String.format("%.1f", att.accuracyPercentage)}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
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
