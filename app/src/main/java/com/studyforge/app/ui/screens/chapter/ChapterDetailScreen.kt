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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.text.style.TextOverflow
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
    onNavigateToChapterRevision: (Long) -> Unit,
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

    val chapter = remember(chapters, chapterId) { chapters.find { it.id == chapterId } }
    val subject = remember(subjects, chapter?.subjectId) { subjects.find { it.id == chapter?.subjectId } }
    val batch = remember(batches, chapter?.batchId) { batches.find { it.id == chapter?.batchId } }

    val chapterNotes = remember(notes, chapterId) { notes.filter { it.chapterId == chapterId } }
    val chapterFormulas = remember(formulas, chapterId) { formulas.filter { it.chapterId == chapterId } }
    val chapterQuestions = remember(questions, chapterId) { questions.filter { it.chapterId == chapterId } }
    val chapterTests = remember(tests, chapterId) { tests.filter { it.chapterId == chapterId } }
    val chapterMistakes = remember(mistakes, chapterId) { mistakes.filter { it.chapterId == chapterId && !it.isResolved } }
    val chapterFlashcards = remember(flashcards, chapterId) { flashcards.filter { it.chapterId == chapterId } }

    var showAiDialog by remember { mutableStateOf(false) }
    var showNotesSheet by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showEditChapterDialog by remember { mutableStateOf(false) }
    var showDeleteChapterDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.padding(end = 4.dp)) {
                        Text(
                            text = chapter?.name ?: "Chapter",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${subject?.name ?: ""} • ${batch?.name ?: ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                    IconButton(onClick = { showMoreMenu = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Chapter Name") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showMoreMenu = false
                                showEditChapterDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Chapter", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showMoreMenu = false
                                showDeleteChapterDialog = true
                            }
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
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (subject != null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = subject.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
                    description = if (chapterNotes.isEmpty()) "0 notes • Tap to write your first note" else "${chapterNotes.size} notes • Tap to view all notes or create new",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    accentColor = MaterialTheme.colorScheme.primary,
                    badgeText = if (chapterNotes.isNotEmpty()) "${chapterNotes.size}" else null,
                    onClick = {
                        if (chapterNotes.size <= 1) {
                            val singleNote = chapterNotes.firstOrNull()
                            onNavigateToNoteEditor(chapterId, singleNote?.id ?: -1L)
                        } else {
                            showNotesSheet = true
                        }
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

            // Quick MCQ Revision Module Card (Before Flashcards)
            item {
                ModuleHubCard(
                    title = "Chapter MCQ Revision",
                    description = "${chapterQuestions.size} questions • Instant correct/wrong check & solutions",
                    icon = Icons.Default.FactCheck,
                    accentColor = Color(0xFF0D9488),
                    badgeText = if (chapterQuestions.isNotEmpty()) "${chapterQuestions.size} MCQs" else null,
                    onClick = { onNavigateToChapterRevision(chapterId) }
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

            // Dedicated Chapter Notes section
            if (chapterNotes.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Chapter Notes (${chapterNotes.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        TextButton(
                            onClick = { onNavigateToNoteEditor(chapterId, -1L) }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Note", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                items(chapterNotes) { note ->
                    StudyCard(
                        shape = RoundedCornerShape(16.dp),
                        onClick = { onNavigateToNoteEditor(chapterId, note.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SquircleIconBadge(
                                icon = Icons.AutoMirrored.Filled.MenuBook,
                                accentColor = MaterialTheme.colorScheme.primary,
                                size = 40.dp,
                                iconSize = 20.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = note.title.ifBlank { "Untitled Note" },
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (note.isPinned) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.PushPin,
                                            contentDescription = "Pinned",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    if (note.isFavorite) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = "Favorite",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = note.contentMarkdown.take(90).ifBlank { "Tap to view/edit note" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showNotesSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotesSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Notes in this Chapter (${chapterNotes.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    FilledTonalButton(
                        onClick = {
                            showNotesSheet = false
                            onNavigateToNoteEditor(chapterId, -1L)
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Note")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(chapterNotes) { note ->
                        StudyCard(
                            shape = RoundedCornerShape(16.dp),
                            onClick = {
                                showNotesSheet = false
                                onNavigateToNoteEditor(chapterId, note.id)
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SquircleIconBadge(
                                    icon = Icons.AutoMirrored.Filled.MenuBook,
                                    accentColor = MaterialTheme.colorScheme.primary,
                                    size = 40.dp,
                                    iconSize = 20.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = note.title.ifBlank { "Untitled Note" },
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        if (note.isPinned) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.PushPin,
                                                contentDescription = "Pinned",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        if (note.isFavorite) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = "Favorite",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = note.contentMarkdown.take(100).ifBlank { "No content" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAiDialog && chapter != null) {
        val notesContext = chapterNotes.joinToString("\n\n") { "${it.title}:\n${it.contentMarkdown}" }
        AiAssistantDialog(
            title = chapter.name,
            contextText = notesContext.ifBlank { "Chapter: ${chapter.name} in Subject ${subject?.name}" },
            viewModel = viewModel,
            chapterId = chapter.id,
            subjectId = chapter.subjectId,
            batchId = chapter.batchId,
            onDismiss = { showAiDialog = false }
        )
    }

    if (showEditChapterDialog && chapter != null) {
        var editName by remember(chapter.name) { mutableStateOf(chapter.name) }
        AlertDialog(
            onDismissRequest = { showEditChapterDialog = false },
            title = { Text("Edit Chapter Name", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Chapter Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank()) {
                            viewModel.updateChapter(chapter.copy(name = editName.trim()))
                            showEditChapterDialog = false
                        }
                    },
                    enabled = editName.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditChapterDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteChapterDialog && chapter != null) {
        AlertDialog(
            onDismissRequest = { showDeleteChapterDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = { Text("Delete Chapter?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete '${chapter.name}'? All notes, questions, and tests associated with this chapter will be permanently removed.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteChapter(chapter)
                        showDeleteChapterDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteChapterDialog = false }) {
                    Text("Cancel")
                }
            }
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
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
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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
