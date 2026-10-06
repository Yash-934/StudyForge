package com.studyforge.app.ui.screens.subject

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.studyforge.app.ui.components.EmptyState
import com.studyforge.app.ui.components.MarkdownMathView
import com.studyforge.app.ui.components.MasteryProgressBar
import com.studyforge.app.ui.components.SectionHeader
import com.studyforge.app.ui.components.StatCard
import com.studyforge.app.ui.components.StudyCard
import com.studyforge.app.ui.screens.questions.ImportBulkQuestionBankDialog
import com.studyforge.app.ui.theme.SuccessGreen
import com.studyforge.app.ui.theme.WarningYellow
import com.studyforge.app.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    subjectId: Long,
    viewModel: StudyViewModel,
    onBack: () -> Unit,
    onNavigateToChapter: (Long) -> Unit,
    onNavigateToNoteEditor: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val subjects by viewModel.subjects.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val tests by viewModel.tests.collectAsState()

    val subject = subjects.find { it.id == subjectId }
    val subjectChapters = chapters.filter { it.subjectId == subjectId }
    val subjectNotes = notes.filter { it.subjectId == subjectId }
    val subjectQuestions = questions.filter { it.subjectId == subjectId }
    val subjectTests = tests.filter { it.subjectId == subjectId }

    val subjectMastery = if (subjectChapters.isNotEmpty()) {
        subjectChapters.sumOf { it.masteryScore } / subjectChapters.size
    } else 0

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Chapters", "Notes", "Questions", "Tests")
    var showCreateChapterDialog by remember { mutableStateOf(false) }
    var showBulkQuestionBankDialog by remember { mutableStateOf(false) }

    val accent = try {
        Color(android.graphics.Color.parseColor(subject?.colorHex ?: "#4F46E5"))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = subject?.name ?: "Subject Details",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { showBulkQuestionBankDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import Questions", style = MaterialTheme.typography.labelSmall)
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 1 || selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = { showCreateChapterDialog = true },
                    containerColor = accent,
                    contentColor = Color.White
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Chapter")
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Subject Mastery Hero Card
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(accent)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${subject?.name} Mastery",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        MasteryProgressBar(score = subjectMastery)
                    }
                }
            }

            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, maxLines = 1) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                when (selectedTabIndex) {
                    0 -> {
                        // Overview Tab
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatCard(
                                    title = "Chapters",
                                    value = "${subjectChapters.size}",
                                    icon = Icons.Default.Folder,
                                    accentColor = accent,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    title = "Questions",
                                    value = "${subjectQuestions.size}",
                                    icon = Icons.Default.Calculate,
                                    accentColor = SuccessGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    title = "Tests",
                                    value = "${subjectTests.size}",
                                    icon = Icons.Default.Folder,
                                    accentColor = WarningYellow,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            SectionHeader(
                                title = "Chapters",
                                actionText = "+ Add Chapter",
                                onActionClick = { showCreateChapterDialog = true }
                            )
                        }

                        if (subjectChapters.isEmpty()) {
                            item {
                                EmptyState(
                                    title = "No Chapters Yet",
                                    message = "Add chapters to organize your notes, questions, and tests under this subject.",
                                    actionLabel = "Add First Chapter",
                                    onAction = { showCreateChapterDialog = true }
                                )
                            }
                        } else {
                            items(subjectChapters) { chap ->
                                ChapterCardRow(chap = chap) {
                                    onNavigateToChapter(chap.id)
                                }
                            }
                        }
                    }

                    1 -> {
                        // Chapters Tab
                        if (subjectChapters.isEmpty()) {
                            item {
                                EmptyState(
                                    title = "No Chapters",
                                    message = "Chapters are the central learning unit. Add your first chapter now.",
                                    actionLabel = "Add Chapter",
                                    onAction = { showCreateChapterDialog = true }
                                )
                            }
                        } else {
                            items(subjectChapters) { chap ->
                                ChapterCardRow(chap = chap) {
                                    onNavigateToChapter(chap.id)
                                }
                            }
                        }
                    }

                    2 -> {
                        // Notes Tab
                        if (subjectNotes.isEmpty()) {
                            item {
                                EmptyState(
                                    title = "No Notes in this Subject",
                                    message = "Open any chapter to write comprehensive Markdown & LaTeX notes."
                                )
                            }
                        } else {
                            items(subjectNotes) { note ->
                                StudyCard(
                                    shape = RoundedCornerShape(18.dp),
                                    onClick = { onNavigateToNoteEditor(note.chapterId) }
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = note.title,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = note.contentMarkdown.take(120),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // Questions Tab
                        if (subjectQuestions.isEmpty()) {
                            item {
                                EmptyState(
                                    icon = Icons.Default.Quiz,
                                    title = "No Questions in this Subject",
                                    message = "Import questions across all chapters at once via JSON or add them inside chapters.",
                                    actionLabel = "Import All Chapters Question Bank",
                                    onAction = { showBulkQuestionBankDialog = true }
                                )
                            }
                        } else {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${subjectQuestions.size} Questions in Subject",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    OutlinedButton(
                                        onClick = { showBulkQuestionBankDialog = true },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Import More (JSON)", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                            items(subjectQuestions) { q ->
                                StudyCard(
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        MarkdownMathView(markdownText = q.questionText)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "${q.type.displayName} • ${q.marks} marks • ${q.difficulty.displayName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    4 -> {
                        // Tests Tab
                        if (subjectTests.isEmpty()) {
                            item {
                                EmptyState(
                                    title = "No Tests in this Subject",
                                    message = "Create a test or generate a Smart Test to challenge your knowledge."
                                )
                            }
                        } else {
                            items(subjectTests) { test ->
                                StudyCard(
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = test.title,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "${test.durationMinutes} mins • ${test.totalMarks} marks • Mode: ${test.mode.displayName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }

    if (showCreateChapterDialog) {
        CreateChapterDialog(
            onDismiss = { showCreateChapterDialog = false },
            onConfirm = { name ->
                if (subject != null) {
                    viewModel.createChapter(subjectId = subject.id, batchId = subject.batchId, name = name)
                }
                showCreateChapterDialog = false
            }
        )
    }

    if (showBulkQuestionBankDialog) {
        ImportBulkQuestionBankDialog(
            viewModel = viewModel,
            preselectedBatchId = subject?.batchId,
            preselectedSubjectId = subjectId,
            onDismiss = { showBulkQuestionBankDialog = false },
            onSuccess = {
                showBulkQuestionBankDialog = false
            }
        )
    }
}

@Composable
fun ChapterCardRow(
    chap: com.studyforge.app.data.local.entities.ChapterEntity,
    onClick: () -> Unit
) {
    StudyCard(
        shape = RoundedCornerShape(20.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chap.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                MasteryProgressBar(score = chap.masteryScore)
            }
            Spacer(modifier = Modifier.width(14.dp))
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
}

@Composable
fun CreateChapterDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Chapter", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Chapter Name *") },
                placeholder = { Text("e.g. Integration, Thermodynamics, Organic Reactions") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
