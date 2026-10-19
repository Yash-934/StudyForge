package com.studyforge.app.ui.screens.bookmarks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyforge.app.data.local.entities.FormulaEntity
import com.studyforge.app.data.local.entities.NoteEntity
import com.studyforge.app.data.local.entities.QuestionEntity
import com.studyforge.app.ui.components.BlockMathCard
import com.studyforge.app.ui.components.EmptyState
import com.studyforge.app.ui.components.ExplanationBox
import com.studyforge.app.ui.components.MarkdownMathView
import com.studyforge.app.ui.components.OptionMathText
import com.studyforge.app.ui.components.StudyCard
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
import com.studyforge.app.ui.theme.WarmOrangeBadge
import com.studyforge.app.viewmodel.StudyViewModel
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksVaultScreen(
    viewModel: StudyViewModel,
    onBack: () -> Unit,
    onNavigateToNoteEditor: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val questions by viewModel.questions.collectAsState()
    val formulas by viewModel.formulas.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    val bookmarkedQuestions = remember(questions) {
        questions.filter { it.isBookmarked || it.isFavorite }
    }
    val starredFormulas = remember(formulas) {
        formulas.filter { it.isFavorite }
    }
    val favoriteNotes = remember(notes) {
        notes.filter { it.isFavorite }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var interactiveMcqMode by remember { mutableStateOf(true) }
    var formulaRecallMode by remember { mutableStateOf(false) }

    val chapterNames = remember(chapters) { chapters.associate { it.id to it.name } }
    val subjectNames = remember(subjects) { subjects.associate { it.id to it.name } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Starred & Bookmarks Vault",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Last-Minute High-Yield Exam Revision Hub",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "MCQs (${bookmarkedQuestions.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Formulas (${starredFormulas.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            text = "Notes (${favoriteNotes.size})",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }

            when (selectedTab) {
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tap any option to test yourself:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FilterChip(
                                selected = interactiveMcqMode,
                                onClick = { interactiveMcqMode = !interactiveMcqMode },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (interactiveMcqMode) "Self-Test Mode: ON" else "Show All Answers",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                },
                                modifier = Modifier.testTag("toggle_self_test_mode_chip")
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        if (bookmarkedQuestions.isEmpty()) {
                            EmptyState(
                                icon = Icons.Default.Bookmark,
                                title = "No Bookmarked Questions Yet",
                                message = "Tap the 🔖 Bookmark icon on any MCQ in the Question Bank to save tricky or high-yield questions here for last-minute revision."
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(bookmarkedQuestions, key = { it.id }) { q ->
                                    BookmarkedQuestionCard(
                                        question = q,
                                        chapterName = chapterNames[q.chapterId] ?: "Chapter",
                                        interactiveMode = interactiveMcqMode,
                                        onUnbookmark = { viewModel.toggleQuestionBookmark(q) },
                                        onAttempted = { correct ->
                                            viewModel.recordInteractiveQuestionAttempt(q.id, correct)
                                        }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(60.dp)) }
                            }
                        }
                    }
                }

                1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "High-yield equations & identities:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FilterChip(
                                selected = formulaRecallMode,
                                onClick = { formulaRecallMode = !formulaRecallMode },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (formulaRecallMode) "Active Recall: ON" else "Active Recall: OFF",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        if (starredFormulas.isEmpty()) {
                            EmptyState(
                                icon = Icons.Default.Calculate,
                                title = "No Starred Formulas Yet",
                                message = "Tap the ⭐ Star button on any formula in Formula Vault to pin critical identities here."
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(starredFormulas, key = { it.id }) { formula ->
                                    StarredFormulaItemCard(
                                        formula = formula,
                                        chapterName = chapterNames[formula.chapterId] ?: "General",
                                        recallMode = formulaRecallMode,
                                        onUnstar = { viewModel.toggleFormulaFavorite(formula) }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(60.dp)) }
                            }
                        }
                    }
                }

                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        if (favoriteNotes.isEmpty()) {
                            EmptyState(
                                icon = Icons.AutoMirrored.Filled.MenuBook,
                                title = "No Favorite Notes Yet",
                                message = "Mark important revision sheets and summary notes as Favorite (❤️) in the Note Editor to access them in one click."
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(favoriteNotes, key = { it.id }) { note ->
                                    FavoriteNoteVaultCard(
                                        note = note,
                                        chapterName = chapterNames[note.chapterId] ?: "Chapter",
                                        subjectName = subjectNames[note.subjectId] ?: "Subject",
                                        onOpenNote = { onNavigateToNoteEditor(note.chapterId, note.id) },
                                        onUnfavorite = { viewModel.toggleNoteFavorite(note) }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(60.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkedQuestionCard(
    question: QuestionEntity,
    chapterName: String,
    interactiveMode: Boolean,
    onUnbookmark: () -> Unit,
    onAttempted: (Boolean) -> Unit
) {
    var selectedOptionIdx by remember(question.id, interactiveMode) { mutableStateOf<Int?>(null) }
    var showExplanation by remember(question.id, interactiveMode) { mutableStateOf(!interactiveMode) }

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

    val isDark = isSystemInDarkTheme()
    val hasAnswered = !interactiveMode || selectedOptionIdx != null

    StudyCard(shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
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
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDark) MintTagBgDark else MintTagBg
                    ) {
                        Text(
                            text = "$chapterName • ${question.difficulty}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) MintTagTextDark else MintTagText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onUnbookmark,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Remove Bookmark",
                        tint = GoldenAmberBar
                    )
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

                        val showCorrectHighlight = hasAnswered && isCorrect
                        val showWrongHighlight = hasAnswered && isUserPicked && !isCorrect

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
                                            fontSize = 15.sp
                                        ),
                                        color = if (showCorrectHighlight) {
                                            if (isDark) McqCorrectTextDark else McqCorrectText
                                        } else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (showCorrectHighlight) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Correct",
                                        tint = McqCorrectBadgeBg,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else if (showWrongHighlight) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = "Wrong",
                                        tint = ErrorRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            val explanationText = question.explanation.ifBlank { question.detailedSolution }
            if (explanationText.isNotBlank() && (showExplanation || hasAnswered)) {
                Spacer(modifier = Modifier.height(10.dp))
                ExplanationBox(
                    explanation = explanationText,
                    topic = question.topic.ifBlank { null }
                )
            }
        }
    }
}

@Composable
private fun StarredFormulaItemCard(
    formula: FormulaEntity,
    chapterName: String,
    recallMode: Boolean,
    onUnstar: () -> Unit
) {
    var revealed by remember(formula.id, recallMode) { mutableStateOf(!recallMode) }

    StudyCard(shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formula.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = chapterName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onUnstar) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Unstar Formula",
                        tint = GoldenAmberBar
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            if (!revealed) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { revealed = true }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tap to Reveal Formula & Verify Recall",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                BlockMathCard(rawLatex = formula.formulaLatex)
                if (formula.explanation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formula.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteNoteVaultCard(
    note: NoteEntity,
    chapterName: String,
    subjectName: String,
    onOpenNote: () -> Unit,
    onUnfavorite: () -> Unit
) {
    var expandedPreview by remember { mutableStateOf(false) }

    StudyCard(
        shape = RoundedCornerShape(20.dp),
        onClick = { expandedPreview = !expandedPreview }
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$subjectName • $chapterName",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        onClick = onOpenNote,
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Open Note",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    IconButton(onClick = onUnfavorite) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Remove Favorite",
                            tint = GoldenAmberBar
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (expandedPreview) {
                MarkdownMathView(markdownText = note.contentMarkdown)
            } else {
                Text(
                    text = "Tap card to expand full formatted notes preview, or tap 'Open Note' to edit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
