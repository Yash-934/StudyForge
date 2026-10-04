package com.studyforge.app.ui.screens.chapter

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyforge.app.ui.components.EmptyState
import com.studyforge.app.ui.components.MasteryProgressBar
import com.studyforge.app.ui.components.SectionHeader
import com.studyforge.app.ui.components.SquircleIconBadge
import com.studyforge.app.ui.components.StatCard
import com.studyforge.app.ui.components.StudyCard
import com.studyforge.app.ui.screens.ai.AiAssistantDialog
import com.studyforge.app.ui.theme.ErrorRed
import com.studyforge.app.ui.theme.PurpleAccent
import com.studyforge.app.ui.theme.SuccessGreen
import com.studyforge.app.ui.theme.WarningYellow
import com.studyforge.app.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterDetailScreen(
    chapterId: Long,
    viewModel: StudyViewModel,
    onBack: () -> Unit,
    onNavigateToNoteEditor: (Long, Long) -> Unit, // chapterId, noteId
    onNavigateToFormulaVault: (Long) -> Unit,
    onNavigateToQuestionBank: (Long) -> Unit,
    onNavigateToQuestionEditor: (Long) -> Unit,
    onNavigateToTests: (Long) -> Unit,
    onNavigateToTestCreate: (Long) -> Unit,
    onNavigateToSmartTest: (Long) -> Unit,
    onNavigateToMistakes: (Long) -> Unit,
    onNavigateToFlashcards: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val chapters by viewModel.chapters.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val batches by viewModel.batches.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val formulas by viewModel.formulas.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val tests by viewModel.tests.collectAsState()
    val mistakes by viewModel.allMistakes.collectAsState()
    val flashcards by viewModel.flashcards.collectAsState()

    val chapter = chapters.find { it.id == chapterId }
    val subject = subjects.find { it.id == chapter?.subjectId }
    val batch = batches.find { it.id == chapter?.batchId }

    val chapterNotes = notes.filter { it.chapterId == chapterId }
    val chapterFormulas = formulas.filter { it.chapterId == chapterId }
    val chapterQuestions = questions.filter { it.chapterId == chapterId }
    val chapterTests = tests.filter { it.chapterId == chapterId }
    val chapterMistakes = mistakes.filter { it.chapterId == chapterId && !it.isResolved }
    val chapterFlashcards = flashcards.filter { it.chapterId == chapterId }

    var showAiDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = chapter?.name ?: "Chapter",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${subject?.name ?: ""} • ${batch?.name ?: ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAiDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Study Assistant",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Chapter Mastery Hero matching uploaded style
                StudyCard(
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Subtle watermark math symbol
                        Text(
                            text = "∫dx",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 48.dp, bottom = 40.dp)
                        )

                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CHAPTER MASTERY",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.2.sp,
                                        fontSize = 12.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = chapter?.name ?: "Chapter",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-0.3).sp,
                                    fontSize = 24.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (subject != null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = subject.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            MasteryProgressBar(score = chapter?.masteryScore ?: 0)

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onNavigateToSmartTest(chapterId) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Smart Drill", style = MaterialTheme.typography.labelMedium)
                                }

                                FilledTonalButton(
                                    onClick = { onNavigateToNoteEditor(chapterId, -1L) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("New Note", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }

            // Quick Stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Notes",
                        value = "${chapterNotes.size}",
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Questions",
                        value = "${chapterQuestions.size}",
                        icon = Icons.Default.Quiz,
                        accentColor = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Mistakes",
                        value = "${chapterMistakes.size}",
                        icon = Icons.Default.ErrorOutline,
                        accentColor = ErrorRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Central Learning Unit Hub Modules
            item {
                SectionHeader(title = "Learning Modules")
            }

            // Notes Module Card
            item {
                ModuleHubCard(
                    title = "Notes & Theory",
                    description = "${chapterNotes.size} notes • Markdown & LaTeX math notation",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    accentColor = MaterialTheme.colorScheme.primary,
                    badgeText = if (chapterNotes.isNotEmpty()) "${chapterNotes.size}" else null,
                    onClick = {
                        val firstNote = chapterNotes.firstOrNull()
                        onNavigateToNoteEditor(chapterId, firstNote?.id ?: -1L)
                    }
                )
            }

            // Formula Sheet Module Card
            item {
                ModuleHubCard(
                    title = "Formula Vault",
                    description = "${chapterFormulas.size} formulas with LaTeX equations & recall mode",
                    icon = Icons.Default.Calculate,
                    accentColor = PurpleAccent,
                    badgeText = "${chapterFormulas.size}",
                    onClick = { onNavigateToFormulaVault(chapterId) }
                )
            }

            // Question Bank Module Card
            item {
                ModuleHubCard(
                    title = "Question Bank",
                    description = "${chapterQuestions.size} questions • MCQ, Numerical, Multi-correct",
                    icon = Icons.Default.Quiz,
                    accentColor = SuccessGreen,
                    badgeText = "${chapterQuestions.size}",
                    onClick = { onNavigateToQuestionBank(chapterId) }
                )
            }

            // Tests Module Card
            item {
                ModuleHubCard(
                    title = "Tests & Mock Exams",
                    description = "${chapterTests.size} tests created • Distraction-free exam player",
                    icon = Icons.Default.FitnessCenter,
                    accentColor = WarningYellow,
                    badgeText = "${chapterTests.size}",
                    onClick = { onNavigateToTests(chapterId) }
                )
            }

            // Mistakes Module Card
            item {
                ModuleHubCard(
                    title = "Mistake Book",
                    description = "${chapterMistakes.size} incorrect questions awaiting correction",
                    icon = Icons.Default.ErrorOutline,
                    accentColor = ErrorRed,
                    badgeText = if (chapterMistakes.isNotEmpty()) "${chapterMistakes.size} Pending" else "Resolved",
                    onClick = { onNavigateToMistakes(chapterId) }
                )
            }

            // Flashcards Module Card
            item {
                ModuleHubCard(
                    title = "Flashcards",
                    description = "${chapterFlashcards.size} cards • Spaced repetition memory deck",
                    icon = Icons.Default.Style,
                    accentColor = Color(0xFF0284C7),
                    badgeText = "${chapterFlashcards.size}",
                    onClick = { onNavigateToFlashcards(chapterId) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showAiDialog && chapter != null) {
        val notesContext = chapterNotes.joinToString("\n\n") { "${it.title}:\n${it.contentMarkdown}" }
        AiAssistantDialog(
            title = "AI Study Assistant: ${chapter.name}",
            contextText = notesContext.ifBlank { "Chapter: ${chapter.name} in Subject ${subject?.name}" },
            viewModel = viewModel,
            onDismiss = { showAiDialog = false }
        )
    }
}

@Composable
fun ModuleHubCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    badgeText: String? = null,
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
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (badgeText != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accentColor.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
