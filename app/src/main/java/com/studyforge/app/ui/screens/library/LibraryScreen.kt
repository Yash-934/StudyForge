package com.studyforge.app.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
    var batchToDelete by remember { mutableStateOf<BatchEntity?>(null) }

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
                Text(
                    text = "Library",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Batches • Subjects • Chapters Knowledge Base",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                                        onClick = { onNavigateToSubject(sub.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
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
}

@Composable
fun BatchCard(
    batch: BatchEntity,
    subjectsCount: Int,
    chaptersCount: Int,
    avgMastery: Int,
    onAddSubject: () -> Unit,
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
                    Column {
                        Text(
                            text = batch.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (batch.courseOrClass.isNotBlank() || batch.session.isNotBlank()) {
                            Text(
                                text = listOf(batch.courseOrClass, batch.session).filter { it.isNotBlank() }.joinToString(" • "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
fun SubjectRowItem(
    subjectName: String,
    colorHex: String,
    chapterCount: Int,
    onClick: () -> Unit
) {
    val accent = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = subjectName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$chapterCount chapters",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
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
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = course,
                    onValueChange = { course = it },
                    label = { Text("Class / Course") },
                    placeholder = { Text("e.g. Undergrad / IIT-JEE / MCAT") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = session,
                    onValueChange = { session = it },
                    label = { Text("Session / Year") },
                    placeholder = { Text("e.g. 2026-2027") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = goal,
                    onValueChange = { goal = it },
                    label = { Text("Primary Goal") },
                    placeholder = { Text("e.g. Score > 95% in Calculus & Physics") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
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
                    modifier = Modifier.fillMaxWidth()
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
