package com.studyforge.app.ui.screens.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyforge.app.ui.components.HeroLearningCard
import com.studyforge.app.ui.components.MasteryProgressBar
import com.studyforge.app.ui.components.SectionHeader
import com.studyforge.app.ui.components.SquircleIconBadge
import com.studyforge.app.ui.components.StatCard
import com.studyforge.app.ui.components.StudyCard
import com.studyforge.app.ui.components.StudyForgeBrandLogo
import com.studyforge.app.ui.components.StudyHeatmap
import com.studyforge.app.ui.screens.questions.ImportBulkQuestionBankDialog
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
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val analytics by viewModel.analytics.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val revisionDue by viewModel.revisionDueItems.collectAsState()
    val unresolvedMistakes by viewModel.unresolvedMistakes.collectAsState()
    val sessions by viewModel.sessions.collectAsState()

    val topChapter = remember(chapters) { chapters.firstOrNull() }
    val topSubjectName = remember(topChapter, subjects) {
        if (topChapter == null) null else subjects.find { it.id == topChapter.subjectId }?.name ?: "General"
    }
    var showBulkQBankDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "home_header") {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(end = 8.dp)
                ) {
                    StudyForgeBrandLogo(size = 44.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "StudyForge",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.6).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Personal Learning Operating System",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = WarningYellow.copy(alpha = 0.14f),
                    border = BorderStroke(0.75.dp, WarningYellow.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = WarningYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${analytics.currentStreakDays}d",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = WarningYellow
                        )
                    }
                }
            }
        }

        // Fast Search Bar Trigger
        item(key = "home_search_bar") {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigateToSearch() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Global Search",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Search chapters, notes, formulas, MCQs...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Quick Action Chips Row
        item(key = "home_quick_actions") {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item(key = "act_qbank") {
                    ActionChip(label = "Import All Q-Bank", icon = Icons.Default.Quiz) {
                        showBulkQBankDialog = true
                    }
                }
                item(key = "act_smart") {
                    ActionChip(label = "Smart Test", icon = Icons.Default.AutoAwesome) {
                        val chapId = topChapter?.id ?: 1L
                        onNavigateToSmartTest(chapId)
                    }
                }
                item(key = "act_note") {
                    ActionChip(label = "New Note", icon = Icons.Default.Add) {
                        val chapId = topChapter?.id ?: 1L
                        onNavigateToNoteEditor(chapId)
                    }
                }
                item(key = "act_question") {
                    ActionChip(label = "New Question", icon = Icons.Default.Quiz) {
                        val chapId = topChapter?.id ?: 1L
                        onNavigateToQuestionEditor(chapId)
                    }
                }
                item(key = "act_json") {
                    ActionChip(label = "Import JSON", icon = Icons.Default.FileUpload) {
                        onNavigateToJsonImport()
                    }
                }
                item(key = "act_rev") {
                    ActionChip(label = "Revision", icon = Icons.Default.NotificationsActive) {
                        onNavigateToRevision()
                    }
                }
                item(key = "act_mistakes") {
                    ActionChip(label = "Mistake Book", icon = Icons.Default.ErrorOutline) {
                        onNavigateToMistakes(-1L)
                    }
                }
            }
        }

        // Stats Overview Row
        item(key = "home_stats_row") {
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
                    title = "Tests",
                    value = "${analytics.totalTestsCompleted}",
                    icon = Icons.Default.FitnessCenter,
                    accentColor = PurpleAccent,
                    subtext = "${analytics.overallAverageScore.toInt()}% avg",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Continue Learning Banner or Welcome Card
        item(key = "home_hero_card") {
            if (topChapter != null) {
                HeroLearningCard(
                    category = "CONTINUE LEARNING",
                    title = topChapter.name,
                    subtitle = topSubjectName,
                    masteryScore = topChapter.masteryScore,
                    watermarkSymbol = "∫dx",
                    onClick = { onNavigateToChapter(topChapter.id) }
                )
            } else {
                HeroLearningCard(
                    category = "GET STARTED WITH STUDYFORGE",
                    title = "Build Your Knowledge Base",
                    subtitle = "Create batches in Library, write distraction-free notes with LaTeX math, organize formulas, and bulk import JSON question banks.",
                    watermarkIcon = Icons.Default.AutoAwesome,
                    actionIcon = Icons.Default.PlayArrow,
                    onClick = onNavigateToPractice
                )
            }
        }

        // Revision Due Alert
        if (revisionDue.isNotEmpty()) {
            item(key = "home_revision_due") {
                StudyCard(
                    shape = RoundedCornerShape(20.dp),
                    borderColor = WarningYellow.copy(alpha = 0.4f),
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
            item(key = "home_mistakes_alert") {
                StudyCard(
                    shape = RoundedCornerShape(20.dp),
                    borderColor = ErrorRed.copy(alpha = 0.4f),
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
            item(key = "home_weak_header") {
                SectionHeader(title = "Needs Attention")
            }
            items(
                items = analytics.weakChapters.take(2),
                key = { "weak_${it.chapterId}" }
            ) { weakChap ->
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
        item(key = "home_heatmap") {
            SectionHeader(title = "Study Consistency")
            Spacer(modifier = Modifier.height(4.dp))
            StudyHeatmap(sessions = sessions)
        }

        // Quick Navigation to Study Pillars
        item(key = "home_pillars") {
            SectionHeader(title = "Learning Modules & Tools")
            Spacer(modifier = Modifier.height(4.dp))
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
                                Text(
                                    "Practice & Mastery Hub",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Smart tests, spaced revision queue, mistake notebook & flashcards",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                                Text(
                                    "JSON Test & Question Importer",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Bulk import MCQs, True/False, and Fill in the Blanks",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
        item(key = "home_strategy_tip") {
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
                        Text(
                            "Active Recall & Spaced Repetition",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
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

        item(key = "home_bottom_spacer") {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    if (showBulkQBankDialog) {
        ImportBulkQuestionBankDialog(
            viewModel = viewModel,
            onDismiss = { showBulkQBankDialog = false },
            onSuccess = { summary ->
                viewModel.showMessage("Imported ${summary.totalQuestionsImported} questions across ${summary.chaptersAffected} chapters!")
            }
        )
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
        shape = RoundedCornerShape(12.dp),
        label = {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface,
            leadingIconContentColor = MaterialTheme.colorScheme.primary
        )
    )
}
