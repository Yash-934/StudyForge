package com.studyforge.app.ui.screens.test

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyforge.app.data.local.entities.QuestionEntity
import com.studyforge.app.data.local.entities.TestAnswerEntity
import com.studyforge.app.domain.model.QuestionType
import com.studyforge.app.ui.components.DifficultyBadge
import com.studyforge.app.ui.components.MarkdownMathView
import com.studyforge.app.ui.theme.ErrorRed
import com.studyforge.app.ui.theme.SuccessGreen
import com.studyforge.app.ui.theme.WarningYellow
import com.studyforge.app.viewmodel.StudyViewModel
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestReviewScreen(
    attemptId: Long,
    viewModel: StudyViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var answers by remember { mutableStateOf<List<TestAnswerEntity>>(emptyList()) }
    var questions by remember { mutableStateOf<List<QuestionEntity>>(emptyList()) }
    var filterMode by remember { mutableStateOf("ALL") } // ALL, INCORRECT, CORRECT, SKIPPED

    LaunchedEffect(attemptId) {
        val ansList = viewModel.repository.getAnswersForAttempt(attemptId)
        answers = ansList
        val qIds = ansList.map { it.questionId }
        questions = viewModel.repository.getQuestionsByIds(qIds)
    }

    val displayPairs = remember(answers, questions, filterMode) {
        val pairs = answers.mapNotNull { ans ->
            val q = questions.find { it.id == ans.questionId }
            if (q != null) q to ans else null
        }
        when (filterMode) {
            "INCORRECT" -> pairs.filter { !it.second.isCorrect && !it.second.isSkipped }
            "CORRECT" -> pairs.filter { it.second.isCorrect }
            "SKIPPED" -> pairs.filter { it.second.isSkipped }
            else -> pairs
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Answer & Solution Review", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(horizontal = 16.dp)
        ) {
            // Filter row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterMode == "ALL",
                    onClick = { filterMode = "ALL" },
                    label = { Text("All (${answers.size})") }
                )
                FilterChip(
                    selected = filterMode == "INCORRECT",
                    onClick = { filterMode = "INCORRECT" },
                    label = { Text("Incorrect (${answers.count { !it.isCorrect && !it.isSkipped }})") }
                )
                FilterChip(
                    selected = filterMode == "CORRECT",
                    onClick = { filterMode = "CORRECT" },
                    label = { Text("Correct (${answers.count { it.isCorrect }})") }
                )
                FilterChip(
                    selected = filterMode == "SKIPPED",
                    onClick = { filterMode = "SKIPPED" },
                    label = { Text("Skipped (${answers.count { it.isSkipped }})") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(displayPairs) { idx, (q, ans) ->
                    QuestionReviewCard(index = idx + 1, question = q, answer = ans)
                }

                item { Spacer(modifier = Modifier.height(48.dp)) }
            }
        }
    }
}

@Composable
fun QuestionReviewCard(
    index: Int,
    question: QuestionEntity,
    answer: TestAnswerEntity
) {
    val (statusBg, statusBorder, statusText, statusIcon) = when {
        answer.isCorrect -> Quad(SuccessGreen.copy(alpha = 0.12f), SuccessGreen, "Correct (+${answer.marksAwarded})", Icons.Default.Check)
        answer.isSkipped -> Quad(WarningYellow.copy(alpha = 0.12f), WarningYellow, "Skipped (0)", Icons.Default.Remove)
        else -> Quad(ErrorRed.copy(alpha = 0.12f), ErrorRed, "Incorrect (${answer.marksAwarded})", Icons.Default.Close)
    }

    val userSelectedText = remember(answer.userSelectedJson, question) {
        formatAnswerDisplay(answer.userSelectedJson, question)
    }

    val correctAnswerText = remember(question.correctAnswersJson, question) {
        formatAnswerDisplay(question.correctAnswersJson, question)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Q$index",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    DifficultyBadge(difficulty = question.difficulty)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = statusIcon, contentDescription = null, tint = statusBorder, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = statusBorder
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Question Text with LaTeX
            MarkdownMathView(markdownText = question.questionText)

            Spacer(modifier = Modifier.height(12.dp))

            // User Answer vs Correct Answer
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row {
                        Text("Your Answer: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = userSelectedText.ifBlank { "(Skipped / Blank)" },
                            color = if (answer.isCorrect) SuccessGreen else ErrorRed,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text("Correct Answer: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = correctAnswerText,
                            color = SuccessGreen,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }

            // Explanation & Detailed Solution
            if (question.explanation.isNotBlank() || question.detailedSolution.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "EXPLANATION & DETAILED SOLUTION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                val explanationText = question.explanation.ifBlank { question.detailedSolution }
                MarkdownMathView(markdownText = explanationText)
            }
        }
    }
}

private fun formatAnswerDisplay(jsonStr: String, question: QuestionEntity): String {
    return try {
        val arr = JSONArray(jsonStr)
        val list = (0 until arr.length()).map { arr.optString(it) }
        if (question.type == QuestionType.MCQ || question.type == QuestionType.MULTIPLE_CORRECT) {
            val optArr = JSONArray(question.optionsJson)
            list.mapNotNull { idxStr ->
                val idx = idxStr.toIntOrNull()
                if (idx != null && idx in 0 until optArr.length()) {
                    "${('A' + idx)}) ${optArr.optString(idx)}"
                } else idxStr
            }.joinToString(", ")
        } else {
            list.joinToString(", ")
        }
    } catch (e: Exception) {
        jsonStr
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
