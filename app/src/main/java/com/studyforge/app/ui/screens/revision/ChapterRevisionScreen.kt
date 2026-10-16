package com.studyforge.app.ui.screens.revision

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyforge.app.data.local.entities.QuestionEntity
import com.studyforge.app.ui.components.EmptyStateView
import com.studyforge.app.ui.components.ExplanationBox
import com.studyforge.app.ui.components.MarkdownMathView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterRevisionScreen(
    chapterName: String,
    questions: List<QuestionEntity>,
    onToggleBookmark: (QuestionEntity) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    if (questions.isEmpty()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(chapterName, maxLines = 1) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            EmptyStateView(
                icon = Icons.Default.Refresh,
                title = "No Questions in this Chapter",
                description = "Import or add MCQs to this chapter to start revision.",
                modifier = Modifier.padding(padding)
            )
        }
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    val userAnswers = remember { mutableStateMapOf<Int, Int>() } // questionIndex -> chosenOptionIndex
    val scrollState = rememberScrollState()

    val currentQuestion = questions[currentIndex]
    val selectedOption = userAnswers[currentIndex]
    val isAnswered = selectedOption != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = chapterName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Question ${currentIndex + 1} of ${questions.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onToggleBookmark(currentQuestion) }) {
                        Icon(
                            imageVector = if (currentQuestion.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (currentQuestion.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentIndex > 0) currentIndex--
                        },
                        enabled = currentIndex > 0,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Prev")
                    }

                    if (currentIndex < questions.size - 1) {
                        Button(
                            onClick = { currentIndex++ },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = { onBack() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Complete Revision")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Meta chips: Difficulty & Topic
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = currentQuestion.difficulty.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (currentQuestion.topic.isNotBlank()) {
                    Text(
                        text = currentQuestion.topic,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Question Statement with LaTeX / Markdown Math
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(18.dp)) {
                    MarkdownMathView(
                        text = currentQuestion.questionText,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Select Option",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // MCQ Options
            currentQuestion.options.forEachIndexed { optIndex, optionText ->
                val optionLetter = ('A' + optIndex).toString()
                val isSelected = selectedOption == optIndex
                val isCorrect = optIndex == currentQuestion.correctOptionIndex

                // Determine styling based on whether question is answered
                val (borderColor, containerColor, indicatorBg, indicatorColor) = when {
                    !isAnswered -> {
                        Quadruple(
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    isCorrect -> {
                        Quadruple(
                            Color(0xFF2E7D32),
                            Color(0xFFE8F5E9),
                            Color(0xFF2E7D32),
                            Color.White
                        )
                    }
                    isSelected && !isCorrect -> {
                        Quadruple(
                            Color(0xFFC62828),
                            Color(0xFFFFEBEE),
                            Color(0xFFC62828),
                            Color.White
                        )
                    }
                    else -> {
                        Quadruple(
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = !isAnswered) {
                            userAnswers[currentIndex] = optIndex
                        },
                    color = containerColor,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Option letter badge (A, B, C, D)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(indicatorBg),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isAnswered && isCorrect) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Correct",
                                    tint = indicatorColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else if (isAnswered && isSelected && !isCorrect) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Wrong",
                                    tint = indicatorColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = optionLetter,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = indicatorColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Option text with Markdown/Math support
                        MarkdownMathView(
                            text = optionText,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Explanation Section: Displayed immediately after answering
            if (isAnswered) {
                Spacer(modifier = Modifier.height(22.dp))

                // User requested: Clean, simple box without 3D shadows/effects, with full LaTeX/Markdown support!
                ExplanationBox(
                    explanation = currentQuestion.explanation.ifBlank {
                        "Correct option is **${('A' + currentQuestion.correctOptionIndex)}**."
                    },
                    topic = currentQuestion.topic
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
