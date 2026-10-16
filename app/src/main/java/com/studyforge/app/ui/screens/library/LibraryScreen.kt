package com.studyforge.app.ui.screens.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.studyforge.app.data.local.entities.SubjectEntity
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import com.studyforge.app.ui.screens.questions.ImportBulkQuestionBankDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyforge.app.data.local.entities.BatchEntity
import com.studyforge.app.ui.components.ConfirmDeleteDialog
import com.studyforge.app.ui.components.EmptyState
import com.studyforge.app.ui.components.MasteryProgressBar
import com.studyforge.app.ui.components.SquircleIconBadge
import com.studyforge.app.ui.components.StudyCard
import com.studyforge.app.viewmodel.StudyViewModel

@Composable
fun LibraryScreen(
    viewModel: StudyViewModel,
    onNavigateToSubject: (Long) -> Unit,
    onNavigateToChapter: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val batches by viewModel.batches.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val chapters by viewModel.chapters.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showCreateBatchDialog by remember { mutableStateOf(false) }
    var showCreateSubjectDialogForBatch by remember { mutableStateOf<Long?>(null) }
    var showImportCurriculumDialog by remember { mutableStateOf(false) }
    var showImportBulkQBankDialog by remember { mutableStateOf(false) }
    var importTargetBatchId by remember { mutableStateOf<Long?>(null) }
    var batchToDelete by remember { mutableStateOf<BatchEntity?>(null) }
    var batchToRename by remember { mutableStateOf<BatchEntity?>(null) }
    var subjectActionTarget by remember { mutableStateOf<SubjectEntity?>(null) }
    var subjectToEdit by remember { mutableStateOf<SubjectEntity?>(null) }
    var subjectToDelete by remember { mutableStateOf<SubjectEntity?>(null) }

    val filteredBatches = remember(batches, searchQuery) {
        if (searchQuery.isBlank()) batches else batches.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.courseOrClass.contains(searchQuery, ignoreCase = true) ||
                it.goal.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateBatchDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Batch")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                text = "Library",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Batches • Subjects • Chapters",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    importTargetBatchId = null
                                    showImportBulkQBankDialog = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Q-Bank", style = MaterialTheme.typography.labelSmall)
                            }
                            FilledTonalButton(
                                onClick = {
                                    importTargetBatchId = null
                                    showImportCurriculumDialog = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Import", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search batches, subjects, courses...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            if (filteredBatches.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.Folder,
                        title = "No Batches Created Yet",
                        message = "Create your first Batch (e.g. 'Class 12 Prep', 'College Semester 1', 'Competitive Exam') to start organizing your knowledge.",
                        actionLabel = "Create First Batch",
                        onAction = { showCreateBatchDialog = true }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        OutlinedButton(
                            onClick = {
                                importTargetBatchId = null
                                showImportCurriculumDialog = true
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Or Import Batch & Subjects via JSON")
                        }
                    }
                }
            } else {
                items(filteredBatches) { batch ->
                    val batchSubjects = subjects.filter { it.batchId == batch.id }
                    val batchChapters = chapters.filter { it.batchId == batch.id }
                    val avgMastery = if (batchChapters.isNotEmpty()) {
                        batchChapters.sumOf { it.masteryScore } / batchChapters.size
                    } else 0

                    BatchCard(
                        batch = batch,
                        subjectsCount = batchSubjects.size,
                        chaptersCount = batchChapters.size,
                        avgMastery = avgMastery,
                        onAddSubject = { showCreateSubjectDialogForBatch = batch.id },
                        onRenameBatch = { batchToRename = batch },
                        onImportSubjectsJson = {
                            importTargetBatchId = batch.id
                            showImportCurriculumDialog = true
                        },
                        onImportQBankJson = {
                            importTargetBatchId = batch.id
                            showImportBulkQBankDialog = true
                        },
                        onDeleteBatch = { batchToDelete = batch }
                    ) {
                        // Expandable or List of Subjects inside this Batch
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (batchSubjects.isEmpty()) {
                                Text(
                                    text = "No subjects added yet. Tap '+ Add Subject' below.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            } else {
                                batchSubjects.forEach { sub ->
                                    val subChapters = chapters.filter { it.subjectId == sub.id }
                                    SubjectRowItem(
                                        subjectName = sub.name,
                                        colorHex = sub.colorHex,
                                        chapterCount = subChapters.size,
                                        onClick = { onNavigateToSubject(sub.id) },
                                        onLongClick = { subjectActionTarget = sub },
                                        onMoreOptionsClick = { subjectActionTarget = sub }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }

    // Create Batch Dialog
    if (showCreateBatchDialog) {
        CreateBatchDialog(
            onDismiss = { showCreateBatchDialog = false },
            onConfirm = { name, course, session, goal, desc ->
                viewModel.createBatch(name, course, session, goal, desc)
                showCreateBatchDialog = false
            }
        )
    }

    // Create Subject Dialog
    showCreateSubjectDialogForBatch?.let { bId ->
        CreateSubjectDialog(
            onDismiss = { showCreateSubjectDialogForBatch = null },
            onConfirm = { name, color ->
                viewModel.createSubject(batchId = bId, name = name, colorHex = color)
                showCreateSubjectDialogForBatch = null
            }
        )
    }

    // Delete Batch Dialog
    batchToDelete?.let { b ->
        ConfirmDeleteDialog(
            title = "Delete Batch '${b.name}'?",
            message = "This will permanently remove this batch and all its subjects, chapters, notes, and questions.",
            onConfirm = { viewModel.deleteBatch(b) },
            onDismiss = { batchToDelete = null }
        )
    }

    // Rename Batch Dialog
    batchToRename?.let { b ->
        RenameBatchDialog(
            batch = b,
            onDismiss = { batchToRename = null },
            onConfirm = { newName, course, session ->
                viewModel.updateBatch(
                    b.copy(
                        name = newName,
                        courseOrClass = course,
                        session = session
                    )
                )
                batchToRename = null
            }
        )
    }

    // Subject Action Dialog (Choose Edit or Delete on Long Press)
    subjectActionTarget?.let { sub ->
        SubjectActionDialog(
            subject = sub,
            onDismiss = { subjectActionTarget = null },
            onEdit = {
                subjectToEdit = sub
                subjectActionTarget = null
            },
            onDelete = {
                subjectToDelete = sub
                subjectActionTarget = null
            }
        )
    }

    // Edit Subject Dialog
    subjectToEdit?.let { sub ->
        EditSubjectDialog(
            subject = sub,
            onDismiss = { subjectToEdit = null },
            onConfirm = { newName, newColor ->
                viewModel.updateSubject(
                    sub.copy(
                        name = newName,
                        colorHex = newColor
                    )
                )
                subjectToEdit = null
            }
        )
    }

    // Delete Subject Confirmation Dialog
    subjectToDelete?.let { sub ->
        ConfirmDeleteDialog(
            title = "Delete Subject '${sub.name}'?",
            message = "This will permanently remove this subject and all its chapters, notes, questions, and tests.",
            onConfirm = {
                viewModel.deleteSubject(sub)
                subjectToDelete = null
            },
            onDismiss = { subjectToDelete = null }
        )
    }

    // Import Curriculum (Batch & Subjects) JSON Dialog
    if (showImportCurriculumDialog) {
        ImportCurriculumDialog(
            viewModel = viewModel,
            preselectedBatchId = importTargetBatchId,
            onDismiss = {
                showImportCurriculumDialog = false
                importTargetBatchId = null
            },
            onSuccess = { createdBatchId ->
                showImportCurriculumDialog = false
                importTargetBatchId = null
            }
        )
    }

    // Import All Chapters Question Bank JSON Dialog
    if (showImportBulkQBankDialog) {
        ImportBulkQuestionBankDialog(
            viewModel = viewModel,
            preselectedBatchId = importTargetBatchId,
            onDismiss = {
                showImportBulkQBankDialog = false
                importTargetBatchId = null
            },
            onSuccess = { summary ->
                showImportBulkQBankDialog = false
                importTargetBatchId = null
                viewModel.showMessage("Imported ${summary.totalQuestionsImported} questions across ${summary.chaptersAffected} chapters!")
            }
        )
    }
}

@Composable
fun BatchCard(
    batch: BatchEntity,
    subjectsCount: Int,
    chaptersCount: Int,
    avgMastery: Int,
    onAddSubject: () -> Unit,
    onRenameBatch: () -> Unit = {},
    onImportSubjectsJson: () -> Unit = {},
    onImportQBankJson: () -> Unit = {},
    onDeleteBatch: () -> Unit,
    content: @Composable () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    StudyCard(
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    SquircleIconBadge(
                        icon = Icons.Default.Folder,
                        accentColor = MaterialTheme.colorScheme.primary,
                        size = 40.dp,
                        iconSize = 22.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = batch.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (batch.courseOrClass.isNotBlank() || batch.session.isNotBlank()) {
                            Text(
                                text = listOf(batch.courseOrClass, batch.session).filter { it.isNotBlank() }.joinToString(" • "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Rename Batch") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onRenameBatch()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Import All Chapters Q-Bank") },
                            leadingIcon = { Icon(Icons.Default.Quiz, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onImportQBankJson()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Import Subjects (JSON)") },
                            leadingIcon = { Icon(Icons.Default.UploadFile, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onImportSubjectsJson()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Batch", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                onDeleteBatch()
                            }
                        )
                    }
                }
            }

            if (batch.goal.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🎯 Goal: ${batch.goal}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            MasteryProgressBar(score = avgMastery)

            Spacer(modifier = Modifier.height(14.dp))

            // Subjects list inside
            content()

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onAddSubject,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Subject", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubjectRowItem(
    subjectName: String,
    colorHex: String,
    chapterCount: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onMoreOptionsClick: () -> Unit = onLongClick
) {
    val accent = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = subjectName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "$chapterCount chap",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.width(2.dp))
                IconButton(
                    onClick = onMoreOptionsClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Subject Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                        .clickable { onClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Subject",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CreateBatchDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, course: String, session: String, goal: String, desc: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var session by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Batch", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Batch Name *") },
                    placeholder = { Text("e.g. Engineering Prep 2026") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = course,
                    onValueChange = { course = it },
                    label = { Text("Class / Course") },
                    placeholder = { Text("e.g. Undergrad / IIT-JEE / MCAT") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = session,
                    onValueChange = { session = it },
                    label = { Text("Session / Year") },
                    placeholder = { Text("e.g. 2026-2027") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = goal,
                    onValueChange = { goal = it },
                    label = { Text("Primary Goal") },
                    placeholder = { Text("e.g. Score > 95% in Calculus & Physics") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) onConfirm(name, course, session, goal, desc)
                },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CreateSubjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#4F46E5") }

    val colors = listOf("#4F46E5", "#0284C7", "#059669", "#D97706", "#DC2626", "#7C3AED")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Subject", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Name *") },
                    placeholder = { Text("e.g. Mathematics, Physics, Organic Chem") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Theme Color", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { hex ->
                        val col = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(col)
                                .clickable { selectedColor = hex }
                        ) {
                            if (selectedColor == hex) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name, selectedColor) },
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

@Composable
fun RenameBatchDialog(
    batch: BatchEntity,
    onDismiss: () -> Unit,
    onConfirm: (newName: String, course: String, session: String) -> Unit
) {
    var name by remember { mutableStateOf(batch.name) }
    var course by remember { mutableStateOf(batch.courseOrClass) }
    var session by remember { mutableStateOf(batch.session) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Batch", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Batch Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = course,
                    onValueChange = { course = it },
                    label = { Text("Course / Class (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = session,
                    onValueChange = { session = it },
                    label = { Text("Session / Target Year (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) onConfirm(name.trim(), course.trim(), session.trim())
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun SubjectActionDialog(
    subject: SubjectEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val accent = try {
        Color(android.graphics.Color.parseColor(subject.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = subject.name,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Select an option for this subject:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))

                // Edit Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEdit() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Edit Subject",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Rename or change theme color",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Delete Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDelete() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Delete Subject",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Delete subject, chapters & notes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditSubjectDialog(
    subject: SubjectEntity,
    onDismiss: () -> Unit,
    onConfirm: (newName: String, newColorHex: String) -> Unit
) {
    var name by remember { mutableStateOf(subject.name) }
    var selectedColor by remember { mutableStateOf(subject.colorHex) }

    val colors = listOf("#4F46E5", "#0284C7", "#059669", "#D97706", "#DC2626", "#7C3AED", "#EC4899", "#14B8A6")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Subject", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Theme Color", style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    colors.forEach { hex ->
                        val col = try {
                            Color(android.graphics.Color.parseColor(hex))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(col)
                                .clickable { selectedColor = hex }
                        ) {
                            if (selectedColor.equals(hex, ignoreCase = true)) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) onConfirm(name.trim(), selectedColor)
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


