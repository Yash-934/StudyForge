package com.studyforge.app.ui.screens.revision

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyforge.app.domain.model.QuestionType
import com.studyforge.app.ui.components.EmptyState
import com.studyforge.app.ui.components.ExplanationBox
import com.studyforge.app.ui.components.MarkdownMathView
import com.studyforge.app.ui.components.OptionMathText
import com.studyforge.app.ui.components.StudyCard
import com.studyforge.app.ui.theme.ErrorRed
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
import com.studyforge.app.ui.theme.McqWrongBadgeBg
import com.studyforge.app.ui.theme.McqWrongCardBg
import com.studyforge.app.ui.theme.McqWrongCardBgDark
import com.studyforge.app.ui.theme.McqWrongCardBorder
import com.studyforge.app.ui.theme.McqWrongText
import com.studyforge.app.ui.theme.McqWrongTextDark
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
fun ChapterRevisionScreen(
    chapterId: Long,
    viewModel: StudyViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val allQuestions by viewModel.questions.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val chapter = chapters.find { it.id == chapterId }

    val chapterQuestions = remember(allQuestions, chapterId) {
        allQuestions.filter { it.chapterId == chapterId }.sortedBy { it.id }
    }

    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedAnswersMap by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }
    var userCorrectMap by remember { mutableStateOf<Map<Long, Boolean>>(emptyMap()) }
    var numericalInputs by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }

    val correctCount = userCorrectMap.count { it.value }
    val wrongCount = userCorrectMap.count { !it.value }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = chapter?.name ?: "Chapter MCQ Revision",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (chapterQuestions.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = "${currentIndex + 1} / ${chapterQuestions.size}",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                    // Reset Revision
                    Surface(
                        onClick = {
                            selectedAnswersMap = emptyMap()
                            userCorrectMap = emptyMap()
                            numericalInputs = emptyMap()
                            currentIndex = 0
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Restart Revision",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (chapterQuestions.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    shadowElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentIndex > 0) {
                            OutlinedButton(
                                onClick = { currentIndex-- },
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Prev", fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }

                        Button(
                            onClick = { if (currentIndex < chapterQuestions.size - 1) currentIndex++ },
                            enabled = currentIndex < chapterQuestions.size - 1,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 13.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Next Question",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        if (chapterQuestions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icon = Icons.Default.Quiz,
                    title = "No Questions in Chapter",
                    message = "Add questions to this chapter's Question Bank or bulk import them to enable instant MCQ revision.",
                    actionLabel = "Back to Chapter",
                    onAction = onBack
                )
            }
        } else {
            val currentQ = chapterQuestions.getOrElse(currentIndex) { chapterQuestions.first() }
            val selectedAns = selectedAnswersMap[currentQ.id]
            val isAnswered = selectedAns != null

            val correctList = remember(currentQ.correctAnswersJson) {
                try {
                    val arr = JSONArray(currentQ.correctAnswersJson)
                    (0 until arr.length()).map { arr.optString(it) }
                } catch (e: Exception) {
                    emptyList()
                }
            }

            val optionsList = remember(currentQ.optionsJson) {
                try {
                    val arr = JSONArray(currentQ.optionsJson)
                    (0 until arr.length()).map { arr.optString(it) }
                } catch (e: Exception) {
                    emptyList()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Header with Progress & Score Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDark) MintTagBgDark else MintTagBg
                        ) {
                            Text(
                                text = "✅ $correctCount Correct",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isDark) MintTagTextDark else MintTagText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                maxLines = 1
                            )
                        }

                        if (wrongCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDark) McqWrongCardBgDark else McqWrongCardBg
                            ) {
                                Text(
                                    text = "❌ $wrongCount Wrong",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isDark) McqWrongTextDark else McqWrongText,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Text(
                        text = "${(chapterQuestions.size - userCorrectMap.size)} remaining",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Virtualized Sequence Navigation Jump Bar (120 FPS even with 500+ questions)
                val jumpListState = rememberLazyListState()
                LaunchedEffect(currentIndex) {
                    if (currentIndex in chapterQuestions.indices) {
                        jumpListState.animateScrollToItem((currentIndex - 2).coerceAtLeast(0))
                    }
                }
                LazyRow(
                    state = jumpListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(
                        items = chapterQuestions,
                        key = { _, q -> q.id }
                    ) { idx, q ->
                        val ansState = userCorrectMap[q.id]
                        val isCurr = idx == currentIndex

                        val (bg, textColor) = when {
                            isCurr -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
                            ansState == true -> McqCorrectBadgeBg to Color.White
                            ansState == false -> McqWrongBadgeBg to Color.White
                            else -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .widthIn(min = 36.dp)
                                .height(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(bg)
                                .border(
                                    width = 1.dp,
                                    color = if (isCurr || ansState != null) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { currentIndex = idx }
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${idx + 1}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = textColor,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Scrollable Question & Explanation Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Unified White Question + Options Card matching Reference Screenshot
                    StudyCard(
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Top Badge Row: Warm Orange Q.X badge + Soft Mint Tag pill
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
                                    // Warm Orange Q.X Badge
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = WarmOrangeBadge
                                    ) {
                                        Text(
                                            text = "Q.${currentIndex + 1}",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 0.3.sp
                                            ),
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }

                                    // Soft Mint Green Tag Pill
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isDark) MintTagBgDark else MintTagBg
                                    ) {
                                        Text(
                                            text = "${currentQ.difficulty} • +${currentQ.marks}m",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = if (isDark) MintTagTextDark else MintTagText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }

                                if (isAnswered) {
                                    val userWasCorrect = userCorrectMap[currentQ.id] == true
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (userWasCorrect) {
                                            if (isDark) MintTagBgDark else MintTagBg
                                        } else {
                                            if (isDark) McqWrongCardBgDark else McqWrongCardBg
                                        }
                                    ) {
                                        Text(
                                            text = if (userWasCorrect) "✓ Correct" else "✗ Wrong",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                            color = if (userWasCorrect) {
                                                if (isDark) MintTagTextDark else MintTagText
                                            } else {
                                                if (isDark) McqWrongTextDark else McqWrongText
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            // Question Text
                            MarkdownMathView(markdownText = currentQ.questionText)

                            // Options inside the same Question Card matching Screenshot 1, 4, 7
                            if (optionsList.isNotEmpty()) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    optionsList.forEachIndexed { idx, opt ->
                                        val letter = ('A' + idx).toChar()
                                        val isSelectedByUser = selectedAns == idx.toString() || selectedAns == letter.toString()

                                        val isThisCorrect = correctList.any { ans ->
                                            ans.equals(idx.toString(), ignoreCase = true) ||
                                                ans.equals(letter.toString(), ignoreCase = true) ||
                                                ans.trim().equals(opt.trim(), ignoreCase = true)
                                        }

                                        val cardBg = when {
                                            !isAnswered -> MaterialTheme.colorScheme.surface
                                            isThisCorrect -> if (isDark) McqCorrectCardBgDark else McqCorrectCardBg
                                            isSelectedByUser -> if (isDark) McqWrongCardBgDark else McqWrongCardBg
                                            else -> if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else Color(0xFFF8FAFC)
                                        }

                                        val cardBorder = when {
                                            !isAnswered -> MaterialTheme.colorScheme.outlineVariant
                                            isThisCorrect -> McqCorrectCardBorder
                                            isSelectedByUser -> McqWrongCardBorder
                                            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                        }

                                        val badgeBg = when {
                                            !isAnswered -> if (isDark) McqBadgeIdleBgDark else McqBadgeIdleBg
                                            isThisCorrect -> McqCorrectBadgeBg
                                            isSelectedByUser -> McqWrongBadgeBg
                                            else -> if (isDark) McqBadgeIdleBgDark else McqBadgeIdleBg
                                        }

                                        val badgeTextColor = when {
                                            !isAnswered -> if (isDark) McqBadgeIdleTextDark else McqBadgeIdleText
                                            isThisCorrect || isSelectedByUser -> Color.White
                                            else -> if (isDark) McqBadgeIdleTextDark.copy(alpha = 0.65f) else McqBadgeIdleText.copy(alpha = 0.65f)
                                        }

                                        val optionTextColor = when {
                                            !isAnswered -> MaterialTheme.colorScheme.onSurface
                                            isThisCorrect -> if (isDark) McqCorrectTextDark else McqCorrectText
                                            isSelectedByUser -> if (isDark) McqWrongTextDark else McqWrongText
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }

                                        val optionFontWeight = when {
                                            !isAnswered -> FontWeight.Medium
                                            isThisCorrect || isSelectedByUser -> FontWeight.Bold
                                            else -> FontWeight.Normal
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = cardBg,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(
                                                    width = if (isAnswered && (isThisCorrect || isSelectedByUser)) 1.6.dp else 1.2.dp,
                                                    color = cardBorder,
                                                    shape = RoundedCornerShape(14.dp)
                                                )
                                                .clip(RoundedCornerShape(14.dp))
                                                .clickable {
                                                    if (!isAnswered) {
                                                        val correct = isThisCorrect
                                                        selectedAnswersMap = selectedAnswersMap + (currentQ.id to idx.toString())
                                                        userCorrectMap = userCorrectMap + (currentQ.id to correct)
                                                        viewModel.recordQuestionAttempt(currentQ.id, correct)
                                                    }
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Square Rounded Letter Badge (A, B, C, D)
                                                Surface(
                                                    shape = RoundedCornerShape(9.dp),
                                                    color = badgeBg,
                                                    modifier = Modifier.size(34.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = "$letter",
                                                            style = MaterialTheme.typography.labelLarge.copy(
                                                                fontWeight = FontWeight.ExtraBold,
                                                                fontSize = 14.sp
                                                            ),
                                                            color = badgeTextColor
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(12.dp))

                                                Box(modifier = Modifier.weight(1f)) {
                                                    OptionMathText(
                                                        text = opt,
                                                        style = MaterialTheme.typography.bodyLarge.copy(
                                                            fontWeight = optionFontWeight,
                                                            fontSize = 15.5.sp,
                                                            lineHeight = 23.sp
                                                        ),
                                                        color = optionTextColor,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }

                                                if (isAnswered && (isThisCorrect || isSelectedByUser)) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Icon(
                                                        imageVector = if (isThisCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                                        contentDescription = null,
                                                        tint = if (isThisCorrect) McqCorrectBadgeBg else McqWrongBadgeBg,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if (currentQ.type == QuestionType.TRUE_FALSE) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    listOf("TRUE", "FALSE").forEach { choice ->
                                        val isChoiceCorrect = correctList.any { it.equals(choice, ignoreCase = true) }
                                        val isSelected = selectedAns?.equals(choice, ignoreCase = true) == true

                                        val choiceBg = when {
                                            !isAnswered -> MaterialTheme.colorScheme.surface
                                            isChoiceCorrect -> if (isDark) McqCorrectCardBgDark else McqCorrectCardBg
                                            isSelected -> if (isDark) McqWrongCardBgDark else McqWrongCardBg
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                        }
                                        val choiceBorder = when {
                                            !isAnswered -> MaterialTheme.colorScheme.outlineVariant
                                            isChoiceCorrect -> McqCorrectCardBorder
                                            isSelected -> McqWrongCardBorder
                                            else -> MaterialTheme.colorScheme.outlineVariant
                                        }
                                        val choiceColor = when {
                                            !isAnswered -> MaterialTheme.colorScheme.onSurface
                                            isChoiceCorrect -> if (isDark) McqCorrectTextDark else McqCorrectText
                                            isSelected -> if (isDark) McqWrongTextDark else McqWrongText
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = choiceBg,
                                            modifier = Modifier
                                                .weight(1f)
                                                .border(1.5.dp, choiceBorder, RoundedCornerShape(14.dp))
                                                .clip(RoundedCornerShape(14.dp))
                                                .clickable {
                                                    if (!isAnswered) {
                                                        val correct = isChoiceCorrect
                                                        selectedAnswersMap = selectedAnswersMap + (currentQ.id to choice)
                                                        userCorrectMap = userCorrectMap + (currentQ.id to correct)
                                                        viewModel.recordQuestionAttempt(currentQ.id, correct)
                                                    }
                                                }
                                        ) {
                                            Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = choice,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = choiceColor
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Numerical / Fill in the blank
                                var numText by remember(currentQ.id) { mutableStateOf(numericalInputs[currentQ.id] ?: "") }
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = numText,
                                        onValueChange = {
                                            if (!isAnswered) {
                                                numText = it
                                                numericalInputs = numericalInputs + (currentQ.id to it)
                                            }
                                        },
                                        label = { Text("Enter Your Answer") },
                                        enabled = !isAnswered,
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    if (!isAnswered) {
                                        Button(
                                            onClick = {
                                                if (numText.isNotBlank()) {
                                                    val correct = correctList.any { it.trim().equals(numText.trim(), ignoreCase = true) }
                                                    selectedAnswersMap = selectedAnswersMap + (currentQ.id to numText.trim())
                                                    userCorrectMap = userCorrectMap + (currentQ.id to correct)
                                                    viewModel.recordQuestionAttempt(currentQ.id, correct)
                                                }
                                            },
                                            enabled = numText.isNotBlank(),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Check Answer", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Explanation Box: Automatically shown when user answers!
                    if (isAnswered) {
                        val explanationContent = when {
                            currentQ.explanation.isNotBlank() -> currentQ.explanation
                            currentQ.detailedSolution.isNotBlank() -> currentQ.detailedSolution
                            else -> "Correct Answer: ${correctList.joinToString(", ")}"
                        }

                        ExplanationBox(
                            explanation = explanationContent,
                            topic = currentQ.topic.ifBlank { null }
                        )

                        // Retry option
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = {
                                    selectedAnswersMap = selectedAnswersMap - currentQ.id
                                    userCorrectMap = userCorrectMap - currentQ.id
                                    numericalInputs = numericalInputs - currentQ.id
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retry Question", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}
