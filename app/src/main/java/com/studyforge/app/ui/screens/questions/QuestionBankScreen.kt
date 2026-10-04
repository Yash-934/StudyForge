package com.studyforge.app.ui.screens.questions

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studyforge.app.data.local.entities.QuestionEntity
import com.studyforge.app.domain.model.Difficulty
import com.studyforge.app.domain.model.QuestionType
import com.studyforge.app.ui.components.DifficultyBadge
import com.studyforge.app.ui.components.EmptyState
import com.studyforge.app.ui.components.MarkdownMathView
import com.studyforge.app.viewmodel.StudyViewModel
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionBankScreen(
    chapterId: Long,
    subjectId: Long,
    viewModel: StudyViewModel,
    onBack: () -> Unit,
    onNavigateToCreateQuestion: (Long) -> Unit,
    onNavigateToEditQuestion: (Long, Long) -> Unit,
    onNavigateToJsonImport: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val questions by viewModel.questions.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val chapter = chapters.find { it.id == chapterId }

    val rawList = remember(questions, chapterId, subjectId) {
        when {
            chapterId > 0 -> questions.filter { it.chapterId == chapterId }
            subjectId > 0 -> questions.filter { it.subjectId == subjectId }
            else -> questions
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf<Difficulty?>(null) }
    var selectedType by remember { mutableStateOf<QuestionType?>(null) }
    var filterUnsolvedOnly by remember { mutableStateOf(false) }

    val filteredQuestions = remember(rawList, searchQuery, selectedDifficulty, selectedType, filterUnsolvedOnly) {
        rawList.filter { q ->
            val matchesSearch = searchQuery.isBlank() ||
                q.questionText.contains(searchQuery, ignoreCase = true) ||
                q.topic.contains(searchQuery, ignoreCase = true)
            val matchesDiff = selectedDifficulty == null || q.difficulty == selectedDifficulty
            val matchesType = selectedType == null || q.type == selectedType
            val matchesUnsolved = !filterUnsolvedOnly || q.timesAttempted == 0
            matchesSearch && matchesDiff && matchesType && matchesUnsolved
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Question Bank", fontWeight = FontWeight.Bold)
                        if (chapter != null) {
                            Text(chapter.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToJsonImport) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = "Import JSON Questions",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToCreateQuestion(chapterId) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Question")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search question text, topic, formula...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterUnsolvedOnly,
                    onClick = { filterUnsolvedOnly = !filterUnsolvedOnly },
                    label = { Text("Unsolved") }
                )
                Difficulty.values().forEach { diff ->
                    FilterChip(
                        selected = selectedDifficulty == diff,
                        onClick = { selectedDifficulty = if (selectedDifficulty == diff) null else diff },
                        label = { Text(diff.displayName) }
                    )
                }
                QuestionType.values().take(4).forEach { qType ->
                    FilterChip(
                        selected = selectedType == qType,
                        onClick = { selectedType = if (selectedType == qType) null else qType },
                        label = { Text(qType.displayName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredQuestions.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    EmptyState(
                        icon = Icons.Default.Quiz,
                        title = "No Questions Found",
                        message = "Add questions to your Question Bank or import from JSON files / paste.",
                        actionLabel = "Add Question",
                        onAction = { onNavigateToCreateQuestion(chapterId) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onNavigateToJsonImport,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Import JSON File / Paste")
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredQuestions) { q ->
                        QuestionBankCard(
                            question = q,
                            onEdit = { onNavigateToEditQuestion(q.chapterId, q.id) },
                            onDelete = { viewModel.deleteQuestion(q) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }
}

@Composable
fun QuestionBankCard(
    question: QuestionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val optionsList = remember(question.optionsJson) {
        try {
            val arr = JSONArray(question.optionsJson)
            (0 until arr.length()).map { arr.optString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DifficultyBadge(difficulty = question.difficulty)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${question.type.displayName} • +${question.marks} / -${question.negativeMarks}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            MarkdownMathView(markdownText = question.questionText)

            if (optionsList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                optionsList.forEachIndexed { idx, opt ->
                    val letter = ('A' + idx).toChar()
                    Text(
                        text = "$letter) $opt",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            if (question.topic.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Topic: ${question.topic}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
