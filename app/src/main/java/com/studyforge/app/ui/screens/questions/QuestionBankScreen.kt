package com.studyforge.app.ui.screens.questions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studyforge.app.data.local.entities.QuestionEntity
import com.studyforge.app.domain.model.Difficulty
import com.studyforge.app.domain.model.QuestionType
import com.studyforge.app.ui.components.DifficultyBadge
import com.studyforge.app.ui.components.EmptyState
import com.studyforge.app.ui.components.ExplanationBox
import com.studyforge.app.ui.components.MarkdownMathView
import com.studyforge.app.ui.components.OptionMathText
import com.studyforge.app.ui.components.StudyCard
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.studyforge.app.ui.theme.ErrorRed
import com.studyforge.app.ui.theme.GoldenAmberBar
import com.studyforge.app.ui.theme.McqBadgeIdleBg
import com.studyforge.app.ui.theme.McqBadgeIdleBgDark
import com.studyforge.app.ui.theme.McqBadgeIdleText
import com.studyforge.app.ui.theme.McqBadgeIdleTextDark
import com.studyforge.app.ui.theme.McqCorrectBadgeBg
import com.studyforge.app.ui.theme.McqCorrectCardBg
import com.studyforge.app.ui.theme.McqCorrectCardBgDark
import com.studyforge.app.ui.theme.McqCorrectCardBorder
import com.studyforge.app.ui.theme.McqCorrectText
import com.studyforge.app.ui.theme.McqCorrectTextDark
import com.studyforge.app.ui.theme.MintTagBg
import com.studyforge.app.ui.theme.MintTagBgDark
import com.studyforge.app.ui.theme.MintTagText
import com.studyforge.app.ui.theme.MintTagTextDark
import com.studyforge.app.ui.theme.SuccessGreen
import com.studyforge.app.ui.theme.WarmOrangeBadge
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
    val subjects by viewModel.subjects.collectAsState()
    val chapter = chapters.find { it.id == chapterId }

    val rawList = remember(questions, chapterId, subjectId) {
        when {
            chapterId > 0 -> questions.filter { it.chapterId == chapterId }
            subjectId > 0 -> questions.filter { it.subjectId == subjectId }
            else -> questions
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf<Difficulty?>(null) }
    var selectedType by remember { mutableStateOf<QuestionType?>(null) }
    var filterUnsolvedOnly by remember { mutableStateOf(false) }
    var filterBookmarkedOnly by remember { mutableStateOf(false) }
    var interactivePracticeMode by remember { mutableStateOf(false) }
    var showBulkImportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            debouncedQuery = ""
        } else {
            kotlinx.coroutines.delay(200)
            debouncedQuery = searchQuery
        }
    }

    val filteredQuestions = remember(rawList, debouncedQuery, selectedDifficulty, selectedType, filterUnsolvedOnly, filterBookmarkedOnly) {
        if (debouncedQuery.isBlank() && selectedDifficulty == null && selectedType == null && !filterUnsolvedOnly && !filterBookmarkedOnly) {
            rawList
        } else {
            rawList.filter { q ->
                val matchesSearch = debouncedQuery.isBlank() ||
                    q.questionText.contains(debouncedQuery, ignoreCase = true) ||
                    q.topic.contains(debouncedQuery, ignoreCase = true)
                val matchesDiff = selectedDifficulty == null || q.difficulty == selectedDifficulty
                val matchesType = selectedType == null || q.type == selectedType
                val matchesUnsolved = !filterUnsolvedOnly || q.timesAttempted == 0
                val matchesBookmarked = !filterBookmarkedOnly || q.isBookmarked || q.isFavorite
                matchesSearch && matchesDiff && matchesType && matchesUnsolved && matchesBookmarked
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.padding(end = 4.dp)) {
                        Text(
                            text = "Question Bank",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        if (chapter != null) {
                            Text(
                                text = chapter.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showBulkImportDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Bulk Import",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
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
                    selected = interactivePracticeMode,
                    onClick = { interactivePracticeMode = !interactivePracticeMode },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = {
                        Text(
                            text = if (interactivePracticeMode) "Self-Test: ON" else "Self-Test Mode",
                            fontWeight = if (interactivePracticeMode) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                FilterChip(
                    selected = filterBookmarkedOnly,
                    onClick = { filterBookmarkedOnly = !filterBookmarkedOnly },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                    },
                    label = { Text("Bookmarked") }
                )
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
                        message = "Add questions to your Question Bank or import all chapters questions at once via JSON.",
                        actionLabel = "Add Question",
                        onAction = { onNavigateToCreateQuestion(chapterId) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showBulkImportDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.1f)
                        ) {
                            Icon(imageVector = Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import All Chapters (JSON)")
                        }
                        OutlinedButton(
                            onClick = onNavigateToJsonImport,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.9f)
                        ) {
                            Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Single JSON")
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredQuestions, key = { it.id }) { q ->
                        QuestionBankCard(
                            question = q,
                            interactiveMode = interactivePracticeMode,
                            onToggleBookmark = { viewModel.toggleQuestionBookmark(q) },
                            onAttempted = { wasCorrect ->
                                viewModel.recordInteractiveQuestionAttempt(q.id, wasCorrect)
                            },
                            onEdit = { onNavigateToEditQuestion(q.chapterId, q.id) },
                            onDelete = { viewModel.deleteQuestion(q) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }

    if (showBulkImportDialog) {
        ImportBulkQuestionBankDialog(
            viewModel = viewModel,
            preselectedBatchId = chapter?.batchId ?: subjects.find { it.id == subjectId }?.batchId,
            preselectedSubjectId = if (subjectId > 0L) subjectId else chapter?.subjectId,
            onDismiss = { showBulkImportDialog = false },
            onSuccess = { summary ->
                showBulkImportDialog = false
            }
        )
    }
}

@Composable
fun QuestionBankCard(
    question: QuestionEntity,
    interactiveMode: Boolean = false,
    onToggleBookmark: () -> Unit = {},
    onAttempted: (Boolean) -> Unit = {},
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var selectedOptionIdx by remember(question.id, interactiveMode) { mutableStateOf<Int?>(null) }
    var showExplanation by remember(question.id, interactiveMode) { mutableStateOf(false) }
    val revealAnswers = !interactiveMode || selectedOptionIdx != null
    val isBookmarked = question.isBookmarked || question.isFavorite

    val optionsList = remember(question.optionsJson) {
        try {
            val arr = JSONArray(question.optionsJson)
            (0 until arr.length()).map { arr.optString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    val correctList = remember(question.correctAnswersJson) {
        try {
            val arr = JSONArray(question.correctAnswersJson)
            (0 until arr.length()).map { arr.optString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    val hasExplanation = question.explanation.isNotBlank() || question.detailedSolution.isNotBlank()

    val isDark = isSystemInDarkTheme()

    StudyCard(
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = WarmOrangeBadge
                    ) {
                        Text(
                            text = "Q.${question.id}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDark) MintTagBgDark else MintTagBg
                    ) {
                        Text(
                            text = "${question.difficulty} • +${question.marks} / -${question.negativeMarks}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) MintTagTextDark else MintTagText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        onClick = onToggleBookmark,
                        shape = RoundedCornerShape(8.dp),
                        color = if (isBookmarked) GoldenAmberBar.copy(alpha = 0.22f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark Question",
                                tint = if (isBookmarked) GoldenAmberBar else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Surface(
                        onClick = onEdit,
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("✏️", fontSize = 14.sp)
                        }
                    }
                    Surface(
                        onClick = onDelete,
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🗑️", fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            MarkdownMathView(markdownText = question.questionText)

            if (optionsList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    optionsList.forEachIndexed { idx, opt ->
                        val letter = ('A' + idx).toChar()
                        val isCorrect = correctList.any { ans ->
                            ans.equals(idx.toString(), ignoreCase = true) ||
                                ans.equals(letter.toString(), ignoreCase = true) ||
                                ans.trim().equals(opt.trim(), ignoreCase = true)
                        }
                        val isUserPicked = selectedOptionIdx == idx
                        val showCorrectHighlight = revealAnswers && isCorrect
                        val showWrongHighlight = revealAnswers && isUserPicked && !isCorrect

                        val cardBg = when {
                            showCorrectHighlight -> if (isDark) McqCorrectCardBgDark else McqCorrectCardBg
                            showWrongHighlight -> ErrorRed.copy(alpha = 0.12f)
                            else -> MaterialTheme.colorScheme.surface
                        }
                        val cardBorder = when {
                            showCorrectHighlight -> McqCorrectCardBorder
                            showWrongHighlight -> ErrorRed
                            else -> MaterialTheme.colorScheme.outlineVariant
                        }
                        val badgeBg = when {
                            showCorrectHighlight -> McqCorrectBadgeBg
                            showWrongHighlight -> ErrorRed
                            else -> if (isDark) McqBadgeIdleBgDark else McqBadgeIdleBg
                        }
                        val badgeTextColor = if (showCorrectHighlight || showWrongHighlight) {
                            Color.White
                        } else {
                            if (isDark) McqBadgeIdleTextDark else McqBadgeIdleText
                        }
                        val optionTextColor = if (showCorrectHighlight) {
                            if (isDark) McqCorrectTextDark else McqCorrectText
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = cardBg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (showCorrectHighlight || showWrongHighlight) 1.5.dp else 1.2.dp,
                                    color = cardBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(enabled = interactiveMode && selectedOptionIdx == null) {
                                    selectedOptionIdx = idx
                                    showExplanation = true
                                    onAttempted(isCorrect)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(9.dp),
                                    color = badgeBg,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "$letter",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 13.5.sp
                                            ),
                                            color = badgeTextColor
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    OptionMathText(
                                        text = opt,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (showCorrectHighlight) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 15.sp,
                                            lineHeight = 22.sp
                                        ),
                                        color = optionTextColor,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                if (showCorrectHighlight) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Correct Option",
                                        tint = McqCorrectBadgeBg,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else if (showWrongHighlight) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = "Incorrect Option",
                                        tint = ErrorRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (correctList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SuccessGreen.copy(alpha = 0.12f),
                    modifier = Modifier.border(1.dp, SuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Correct Answer: ${correctList.joinToString(", ")}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = SuccessGreen
                        )
                    }
                }
            }

            if (question.topic.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Topic: ${question.topic}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (hasExplanation) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showExplanation = !showExplanation }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showExplanation) "Hide Explanation" else "Show Fast Revision Explanation",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (showExplanation) {
                    val explanationText = question.explanation.ifBlank { question.detailedSolution }
                    if (explanationText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        ExplanationBox(
                            explanation = explanationText,
                            topic = question.topic.ifBlank { null }
                        )
                    }
                }
            }
        }
    }
}
