package com.studyforge.app.ui.screens.questions

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.studyforge.app.data.local.entities.ChapterEntity
import com.studyforge.app.data.local.entities.QuestionEntity
import com.studyforge.app.data.local.entities.SubjectEntity
import com.studyforge.app.ui.components.EmptyStateView
import com.studyforge.app.ui.components.ExplanationBox
import com.studyforge.app.ui.components.MarkdownMathView
import com.studyforge.app.ui.components.StudySearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionBankScreen(
    batches: List<BatchEntity>,
    subjects: List<SubjectEntity>,
    chapters: List<ChapterEntity>,
    questions: List<QuestionEntity>,
    selectedBatchId: Long?,
    selectedSubjectId: Long?,
    selectedChapterId: Long?,
    isImporting: Boolean,
    importProgress: String,
    importError: String?,
    importSuccess: String?,
    onSelectBatch: (Long?) -> Unit,
    onSelectSubject: (Long?) -> Unit,
    onSelectChapter: (Long?) -> Unit,
    onToggleBookmark: (QuestionEntity) -> Unit,
    onStartRevision: (chapterId: Long, chapterName: String) -> Unit,
    onImportRawJson: (targetBatchId: Long?, rawJson: String) -> Unit,
    onImportFileUri: (targetBatchId: Long?, uri: Uri) -> Unit,
    onClearImportStatus: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterOnlyBookmarked by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    // Filter questions
    val filteredQuestions = remember(questions, searchQuery, filterOnlyBookmarked) {
        questions.filter { q ->
            val matchesSearch = searchQuery.isBlank() ||
                q.questionText.contains(searchQuery, ignoreCase = true) ||
                q.topic.contains(searchQuery, ignoreCase = true) ||
                q.options.any { it.contains(searchQuery, ignoreCase = true) }
            val matchesBookmark = !filterOnlyBookmarked || q.isBookmarked
            matchesSearch && matchesBookmark
        }
    }

    if (showImportDialog) {
        ImportBulkQuestionBankDialog(
            batches = batches,
            preselectedBatchId = selectedBatchId,
            isImporting = isImporting,
            progressMessage = importProgress,
            errorMessage = importError,
            successMessage = importSuccess,
            onDismiss = {
                showImportDialog = false
                onClearImportStatus()
            },
            onImportRawJson = onImportRawJson,
            onImportFileUri = onImportFileUri
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Question Bank", fontWeight = FontWeight.Bold)
                },
                actions = {
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(Icons.Default.FileUpload, contentDescription = "Import Questions")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showImportDialog = true },
                icon = { Icon(Icons.Default.FileUpload, contentDescription = null) },
                text = { Text("Import Q-Bank") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                StudySearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search questions, topics, formulas..."
                )
            }

            // Filter Chips (Batches, Subjects, Chapters)
            val chipScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(chipScrollState)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bookmarked toggle chip
                FilterChip(
                    selected = filterOnlyBookmarked,
                    onClick = { filterOnlyBookmarked = !filterOnlyBookmarked },
                    label = { Text("Bookmarked") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (filterOnlyBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                // Batch filter chip
                batches.forEach { batch ->
                    FilterChip(
                        selected = selectedBatchId == batch.id,
                        onClick = {
                            if (selectedBatchId == batch.id) onSelectBatch(null) else onSelectBatch(batch.id)
                        },
                        label = { Text(batch.name) }
                    )
                }
            }

            // If Batch is selected, show Subject Chips
            if (subjects.isNotEmpty()) {
                val subScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(subScrollState)
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subjects.forEach { subject ->
                        FilterChip(
                            selected = selectedSubjectId == subject.id,
                            onClick = {
                                if (selectedSubjectId == subject.id) onSelectSubject(null) else onSelectSubject(subject.id)
                            },
                            label = { Text(subject.name) }
                        )
                    }
                }
            }

            // Practice Chapter Button if a Chapter is selected
            if (selectedChapterId != null) {
                val ch = chapters.find { it.id == selectedChapterId }
                if (ch != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ch.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${filteredQuestions.size} questions available",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Button(
                                onClick = { onStartRevision(ch.id, ch.name) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Revise MCQs")
                            }
                        }
                    }
                }
            }

            // Question Count Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredQuestions.size} Questions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Question List
            if (filteredQuestions.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.QuestionMark,
                    title = "No Questions Found",
                    description = if (searchQuery.isNotBlank()) "No matching questions found for '$searchQuery'" else "Import questions from a JSON file or select a different batch.",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredQuestions, key = { it.id }) { question ->
                        QuestionBankItemCard(
                            question = question,
                            onToggleBookmark = { onToggleBookmark(question) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionBankItemCard(
    question: QuestionEntity,
    onToggleBookmark: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Topic, Difficulty, Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = question.difficulty.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (question.topic.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = question.topic,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(onClick = onToggleBookmark, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (question.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (question.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Question Statement with LaTeX / Markdown
            MarkdownMathView(
                text = question.questionText,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Options Preview
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                question.options.forEachIndexed { idx, optText ->
                    val isCorrect = idx == question.correctOptionIndex
                    val letter = ('A' + idx).toString()

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (expanded && isCorrect) Color(0xFFE8F5E9)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$letter. ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (expanded && isCorrect) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MarkdownMathView(
                            text = optText,
                            fontSize = 13.sp,
                            color = if (expanded && isCorrect) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Expand Explanation Button
            if (question.explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (expanded) "Hide Solution" else "View Solution & Explanation",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (expanded) {
                    Spacer(modifier = Modifier.height(6.dp))
                    ExplanationBox(
                        explanation = question.explanation,
                        topic = question.topic
                    )
                }
            }
        }
    }
}
