package com.studyforge.app.ui.screens.home

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.studyforge.app.data.local.entities.ChapterEntity
import com.studyforge.app.ui.components.EmptyState
import com.studyforge.app.ui.components.HeroLearningCard
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
fun HomeScreen(
    viewModel: StudyViewModel,
    onNavigateToChapter: (Long) -> Unit,
    onNavigateToNoteEditor: (Long) -> Unit,
    onNavigateToQuestionEditor: (Long) -> Unit,
    onNavigateToTestCreate: (Long) -> Unit,
    onNavigateToSmartTest: (Long) -> Unit,
    onNavigateToJsonImport: () -> Unit,
    onNavigateToRevision: () -> Unit,
    onNavigateToMistakes: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToPractice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val analytics by viewModel.analytics.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val tests by viewModel.tests.collectAsState()
    val revisionDue by viewModel.revisionDueItems.collectAsState()
    val unresolvedMistakes by viewModel.unresolvedMistakes.collectAsState()
    val sessions by viewModel.sessions.collectAsState()

    val topChapter = chapters.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Top Bar Greeting & Search
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "StudyForge",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Personal Learning Operating System",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Study Streak Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = WarningYellow.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = WarningYellow,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${analytics.currentStreakDays}d streak",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = WarningYellow
                            )
                        }
                    }

                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Quick Action Chips Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    ActionChip(label = "New Note", icon = Icons.Default.Add) {
                        val chapId = topChapter?.id ?: 1L
                        onNavigateToNoteEditor(chapId)
                    }
                }
                item {
                    ActionChip(label = "New Question", icon = Icons.Default.Quiz) {
                        val chapId = topChapter?.id ?: 1L
                        onNavigateToQuestionEditor(chapId)
                    }
                }
                item {
                    ActionChip(label = "Smart Test", icon = Icons.Default.AutoAwesome) {
                        val chapId = topChapter?.id ?: 1L
                        onNavigateToSmartTest(chapId)
                    }
                }
                item {
                    ActionChip(label = "Import JSON", icon = Icons.Default.FileUpload) {
                        onNavigateToJsonImport()
                    }
                }
                item {
                    ActionChip(label = "Revision", icon = Icons.Default.NotificationsActive) {
                        onNavigateToRevision()
                    }
                }
                item {
                    ActionChip(label = "Mistake Book", icon = Icons.Default.ErrorOutline) {
                        onNavigateToMistakes(-1L)
                    }
                }
            }
        }

        // Stats Overview Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Study Time",
                    value = "${analytics.totalStudyTimeMinutes}m",
                    icon = Icons.Default.Timer,
                    accentColor = MaterialTheme.colorScheme.primary,
                    subtext = "Logged sessions",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Solved",
                    value = "${analytics.totalQuestionsSolved}",
                    icon = Icons.Default.CheckCircle,
                    accentColor = SuccessGreen,
                    subtext = "${analytics.overallAccuracy.toInt()}% accuracy",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Tests Taken",
                    value = "${analytics.totalTestsCompleted}",
                    icon = Icons.Default.FitnessCenter,
                    accentColor = PurpleAccent,
                    subtext = "${analytics.overallAverageScore.toInt()}% avg score",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Continue Learning Banner or Welcome Card
        item {
            if (topChapter != null) {
                val subject = subjects.find { it.id == topChapter.subjectId }
                HeroLearningCard(
                    category = "CONTINUE LEARNING",
                    title = topChapter.name,
                    subtitle = subject?.name ?: "Mathematics",
                    masteryScore = topChapter.masteryScore,
                    watermarkSymbol = "∫dx",
                    onClick = { onNavigateToChapter(topChapter.id) }
                )
            } else {
                HeroLearningCard(
                    category = "GET STARTED WITH STUDYFORGE",
                    title = "Build Your Knowledge Base",
                    subtitle = "Create batches in Library, write distraction-free notes with LaTeX math, organize formulas, practice questions with smart tests, and import JSON question banks.",
                    watermarkIcon = Icons.Default.AutoAwesome,
                    actionIcon = Icons.Default.PlayArrow,
                    onClick = onNavigateToPractice
                )
            }
        }

        // Revision Due Alert
        if (revisionDue.isNotEmpty()) {
            item {
                StudyCard(
                    shape = RoundedCornerShape(20.dp),
                    borderColor = WarningYellow.copy(alpha = 0.35f),
                    onClick = { onNavigateToRevision() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SquircleIconBadge(
                            icon = Icons.Default.NotificationsActive,
                            accentColor = WarningYellow,
                            containerColor = WarningYellow.copy(alpha = 0.15f),
                            size = 42.dp,
                            iconSize = 22.dp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${revisionDue.size} Items Due for Spaced Revision",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Keep your knowledge fresh before forgetting sets in.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(WarningYellow.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = WarningYellow,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Unresolved Mistakes Alert
        if (unresolvedMistakes.isNotEmpty()) {
            item {
                StudyCard(
                    shape = RoundedCornerShape(20.dp),
                    borderColor = ErrorRed.copy(alpha = 0.35f),
                    onClick = { onNavigateToMistakes(-1L) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SquircleIconBadge(
                            icon = Icons.Default.ErrorOutline,
                            accentColor = ErrorRed,
                            containerColor = ErrorRed.copy(alpha = 0.15f),
                            size = 42.dp,
                            iconSize = 22.dp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${unresolvedMistakes.size} Unresolved Mistakes to Correct",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Review incorrect questions to strengthen your weak concepts.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ErrorRed.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Weak Areas Section
        if (analytics.weakChapters.isNotEmpty()) {
            item {
                SectionHeader(title = "Needs Attention")
            }
            items(analytics.weakChapters.take(2)) { weakChap ->
                StudyCard(
                    shape = RoundedCornerShape(18.dp),
                    onClick = { onNavigateToChapter(weakChap.chapterId) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = weakChap.chapterName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${weakChap.subjectName} • ${weakChap.mistakeCount} mistakes recorded",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            MasteryProgressBar(score = weakChap.masteryScore)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        AssistChip(
                            onClick = { onNavigateToSmartTest(weakChap.chapterId) },
                            label = { Text("Drill") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
        }

        // Study Heatmap Section
        item {
            SectionHeader(title = "Study Consistency")
            StudyHeatmap(sessions = sessions)
        }

        // Quick Navigation to Study Pillars
        item {
            SectionHeader(title = "Learning Modules & Tools")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StudyCard(
                    shape = RoundedCornerShape(20.dp),
                    onClick = { onNavigateToPractice() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            SquircleIconBadge(
                                icon = Icons.Default.Quiz,
                                accentColor = MaterialTheme.colorScheme.primary,
                                size = 42.dp,
                                iconSize = 22.dp
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text("Practice & Mastery Hub", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("Smart tests, spaced revision queue, mistake notebook & flashcards", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
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

                StudyCard(
                    shape = RoundedCornerShape(20.dp),
                    onClick = { onNavigateToJsonImport() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            SquircleIconBadge(
                                icon = Icons.Default.FileUpload,
                                accentColor = PurpleAccent,
                                size = 42.dp,
                                iconSize = 22.dp
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text("JSON Test & Question Importer", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("Bulk import MCQs, True/False, and Fill in the Blanks", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PurpleAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = PurpleAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Daily Study Strategy Tip
        item {
            StudyCard(
                shape = RoundedCornerShape(20.dp),
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SquircleIconBadge(
                            icon = Icons.Default.AutoAwesome,
                            accentColor = MaterialTheme.colorScheme.primary,
                            size = 32.dp,
                            iconSize = 18.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Active Recall & Spaced Repetition", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Studying with tests and flashcards builds 50% stronger retention than passive re-reading. Check your Spaced Revision queue daily to retain difficult topics.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun ActionChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface,
            leadingIconContentColor = MaterialTheme.colorScheme.primary
        )
    )
}
