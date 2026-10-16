package com.studyforge.app.ui.screens.revision

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyforge.app.data.local.entities.QuestionEntity
import com.studyforge.app.domain.model.QuestionType
import com.studyforge.app.ui.components.DifficultyBadge
import com.studyforge.app.ui.components.EmptyState
import com.studyforge.app.ui.components.ExplanationBox
import com.studyforge.app.ui.components.MarkdownMathView
import com.studyforge.app.ui.components.OptionMathText
import com.studyforge.app.ui.components.StudyCard
import com.studyforge.app.ui.theme.ErrorRed
import com.studyforge.app.ui.theme.SuccessGreen
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
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.padding(end = 8.dp)) {
                        Text(
                            text = "Chapter MCQ Revision",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (chapter != null) {
                            Text(
                                text = chapter.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
                    // Reset Revision
                    IconButton(
                        onClick = {
                            selectedAnswersMap = emptyMap()
                            userCorrectMap = emptyMap()
                            numericalInputs = emptyMap()
                            currentIndex = 0
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Restart Revision")
                    }
                }
            )
        },
        bottomBar = {
            if (chapterQuestions.isNotEmpty()) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { if (currentIndex > 0) currentIndex-- },
                            enabled = currentIndex > 0,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Previous", maxLines = 1)
                        }

                        Text(
                            text = "Q ${currentIndex + 1} / ${chapterQuestions.size}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        Button(
                            onClick = { if (currentIndex < chapterQuestions.size - 1) currentIndex++ },
                            enabled = currentIndex < chapterQuestions.size - 1,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Next", maxLines = 1)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
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
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuccessGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "✓ $correctCount Correct",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = SuccessGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                maxLines = 1
                            )
                        }

                        if (wrongCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ErrorRed.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "✗ $wrongCount Wrong",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ErrorRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Text(
                        text = "${(chapterQuestions.size - userCorrectMap.size)} remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Sequence Navigation Jump Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    chapterQuestions.forEachIndexed { idx, q ->
                        val ansState = userCorrectMap[q.id]
                        val isCurr = idx == currentIndex

                        val (bg, textColor) = when {
                            isCurr -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
                            ansState == true -> SuccessGreen to Color.White
                            ansState == false -> ErrorRed to Color.White
                            else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .widthIn(min = 36.dp)
                                .height(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bg)
                                .clickable { currentIndex = idx }
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${idx + 1}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = textColor,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Progress Bar
                LinearProgressIndicator(
                    progress = { (currentIndex + 1f) / chapterQuestions.size.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                // Scrollable Question Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Question Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DifficultyBadge(difficulty = currentQ.difficulty)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${currentQ.type.displayName} • +${currentQ.marks} marks",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isAnswered) {
                            val userWasCorrect = userCorrectMap[currentQ.id] == true
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (userWasCorrect) SuccessGreen.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (userWasCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = if (userWasCorrect) SuccessGreen else ErrorRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (userWasCorrect) "CORRECT" else "INCORRECT",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (userWasCorrect) SuccessGreen else ErrorRed
                                    )
                                }
                            }
                        }
                    }

                    // Question Text Card
                    StudyCard(
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Box(modifier = Modifier.padding(18.dp)) {
                            MarkdownMathView(markdownText = currentQ.questionText)
                        }
                    }

                    // Options with instant turn-green / turn-red behavior
                    if (optionsList.isNotEmpty()) {
                        Text(
                            text = "Select an option for instant answer checking:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        optionsList.forEachIndexed { idx, opt ->
                            val letter = ('A' + idx).toChar()
                            val isSelectedByUser = selectedAns == idx.toString() || selectedAns == letter.toString()

                            val isThisCorrect = correctList.any { ans ->
                                ans.equals(idx.toString(), ignoreCase = true) ||
                                ans.equals(letter.toString(), ignoreCase = true) ||
                                ans.trim().equals(opt.trim(), ignoreCase = true)
                            }

                            // Dynamic Styling based on Instant Selection
                            val (cardBg, cardBorder, letterColor, icon) = when {
                                !isAnswered -> {
                                    // Not answered yet
                                    listOf(
                                        MaterialTheme.colorScheme.surface,
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                        MaterialTheme.colorScheme.primary,
                                        null
                                    )
                                }
                                isThisCorrect -> {
                                    // Sahi option turnt GREEN ho jaaye AFTER user answers
                                    listOf(
                                        SuccessGreen.copy(alpha = 0.16f),
                                        SuccessGreen,
                                        SuccessGreen,
                                        Icons.Default.CheckCircle
                                    )
                                }
                                isSelectedByUser && !isThisCorrect -> {
                                    // Galt answer select krne pr selected turnt RED ho jaaye
                                    listOf(
                                        ErrorRed.copy(alpha = 0.16f),
                                        ErrorRed,
                                        ErrorRed,
                                        Icons.Default.Cancel
                                    )
                                }
                                else -> {
                                    // Other options when answered
                                    listOf(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        null
                                    )
                                }
                            }

                            val optionTextColor = when {
                                !isAnswered -> MaterialTheme.colorScheme.onSurface
                                isThisCorrect -> SuccessGreen
                                isSelectedByUser -> ErrorRed
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            }

                            val optionFontWeight = when {
                                !isAnswered -> FontWeight.Normal
                                isThisCorrect || isSelectedByUser -> FontWeight.SemiBold
                                else -> FontWeight.Normal
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = cardBg as Color,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isAnswered && (isThisCorrect || isSelectedByUser)) 2.dp else 1.dp,
                                        color = cardBorder as Color,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clip(RoundedCornerShape(16.dp))
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
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    if (icon != null) {
                                        Icon(
                                            imageVector = icon as androidx.compose.ui.graphics.vector.ImageVector,
                                            contentDescription = null,
                                            tint = if (isThisCorrect) SuccessGreen else ErrorRed,
                                            modifier = Modifier
                                                .padding(top = 2.dp)
                                                .size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

                                    Text(
                                        text = "$letter)",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = letterColor as Color,
                                        modifier = Modifier.padding(top = 1.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        OptionMathText(
                                            text = opt,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = optionFontWeight),
                                            color = optionTextColor,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    } else if (currentQ.type == QuestionType.TRUE_FALSE) {
                        // True/False handling
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            listOf("TRUE", "FALSE").forEach { choice ->
                                val isChoiceCorrect = correctList.any { it.equals(choice, ignoreCase = true) }
                                val isSelected = selectedAns?.equals(choice, ignoreCase = true) == true

                                val (choiceBg, choiceBorder, choiceColor) = when {
                                    !isAnswered -> listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.onSurface)
                                    isChoiceCorrect -> listOf(SuccessGreen.copy(alpha = 0.16f), SuccessGreen, SuccessGreen)
                                    isSelected && !isChoiceCorrect -> listOf(ErrorRed.copy(alpha = 0.16f), ErrorRed, ErrorRed)
                                    else -> listOf(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = choiceBg as Color,
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(2.dp, choiceBorder as Color, RoundedCornerShape(16.dp))
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            if (!isAnswered) {
                                                val correct = isChoiceCorrect
                                                selectedAnswersMap = selectedAnswersMap + (currentQ.id to choice)
                                                userCorrectMap = userCorrectMap + (currentQ.id to correct)
                                                viewModel.recordQuestionAttempt(currentQ.id, correct)
                                            }
                                        }
                                ) {
                                    Box(modifier = Modifier.padding(18.dp), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = choice,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = choiceColor as Color
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
                                    Text("Check Answer")
                                }
                            }
                        }
                    }

                    // Explanation Box: Automatically shown when user answers!
                    if (isAnswered) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val explanationContent = when {
                            currentQ.explanation.isNotBlank() -> currentQ.explanation
                            currentQ.detailedSolution.isNotBlank() -> currentQ.detailedSolution
                            else -> "Correct Answer: ${correctList.joinToString(", ")}"
                        }

                        ExplanationBox(
                            explanation = explanationContent,
                            topic = currentQ.topic.ifBlank { null }
                        )

                        // Try again option
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
