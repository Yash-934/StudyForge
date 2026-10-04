package com.studyforge.app.ui.screens.test

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyforge.app.data.local.entities.QuestionEntity
import com.studyforge.app.domain.model.QuestionType
import com.studyforge.app.ui.components.DifficultyBadge
import com.studyforge.app.ui.components.MarkdownMathView
import com.studyforge.app.ui.theme.ErrorRed
import com.studyforge.app.ui.theme.PurpleAccent
import com.studyforge.app.ui.theme.SuccessGreen
import com.studyforge.app.ui.theme.WarningYellow
import com.studyforge.app.viewmodel.StudyViewModel
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestPlayerScreen(
    testId: Long,
    attemptId: Long,
    viewModel: StudyViewModel,
    onTestSubmitted: (Long) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTest by viewModel.activeTest.collectAsState()
    val activeQuestions by viewModel.activeQuestions.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val userAnswers by viewModel.userAnswers.collectAsState()
    val markedForReview by viewModel.markedForReview.collectAsState()
    val timeRemaining by viewModel.timeRemainingSeconds.collectAsState()

    var showPaletteSheet by remember { mutableStateOf(false) }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(testId) {
        if (activeTest == null || activeTest?.id != testId) {
            viewModel.startTest(testId) {}
        }
    }

    BackHandler {
        showExitConfirmDialog = true
    }

    if (activeQuestions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading test environment...")
        }
        return
    }

    val currentQ = activeQuestions.getOrNull(currentIndex) ?: return
    val totalCount = activeQuestions.size
    val currentAnswers = userAnswers[currentQ.id] ?: emptyList()
    val isMarked = markedForReview.contains(currentQ.id)

    val answeredCount = activeQuestions.count { q ->
        val ans = userAnswers[q.id] ?: emptyList()
        ans.isNotEmpty() && ans.any { it.isNotBlank() }
    }
    val unansweredCount = totalCount - answeredCount

    // Format timer MM:SS
    val minutes = timeRemaining / 60
    val seconds = timeRemaining % 60
    val timerText = String.format("%02d:%02d", minutes, seconds)
    val isTimeLow = timeRemaining < 120

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = activeTest?.title ?: "Test in Progress",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                        Text(
                            text = "Question ${currentIndex + 1} of $totalCount",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showExitConfirmDialog = true }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Exit")
                    }
                },
                actions = {
                    // Timer Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isTimeLow) ErrorRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (isTimeLow) ErrorRed else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = timerText,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTimeLow) ErrorRed else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    // Palette launcher
                    IconButton(onClick = { showPaletteSheet = true }) {
                        Icon(imageVector = Icons.Default.GridView, contentDescription = "Palette")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Button
                        OutlinedButton(
                            onClick = { if (currentIndex > 0) viewModel.goToQuestion(currentIndex - 1) },
                            enabled = currentIndex > 0,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Previous")
                        }

                        // Mark for review toggle
                        IconButton(onClick = { viewModel.toggleMarkForReview(currentQ.id) }) {
                            Icon(
                                imageVector = if (isMarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Review",
                                tint = if (isMarked) WarningYellow else MaterialTheme.colorScheme.outline
                            )
                        }

                        // Next / Submit Button
                        if (currentIndex < totalCount - 1) {
                            Button(
                                onClick = { viewModel.goToQuestion(currentIndex + 1) },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Next")
                            }
                        } else {
                            Button(
                                onClick = { showSubmitDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                            ) {
                                Text("Submit Test")
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Progress Bar
            LinearProgressIndicator(
                progress = { (currentIndex + 1f) / totalCount.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Question Metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DifficultyBadge(difficulty = currentQ.difficulty)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${currentQ.type.displayName} • +${currentQ.marks} / -${currentQ.negativeMarks}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (currentAnswers.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearAnswer(currentQ.id) }) {
                        Text("Clear Response", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Question Text (Rendered cleanly with math notation!)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    MarkdownMathView(markdownText = currentQ.questionText)
                }
            }

            // Answer Input Controls Per Question Type
            when (currentQ.type) {
                QuestionType.MCQ -> {
                    val opts = remember(currentQ.optionsJson) {
                        try {
                            val arr = JSONArray(currentQ.optionsJson)
                            (0 until arr.length()).map { arr.optString(it) }
                        } catch (e: Exception) {
                            emptyList()
                        }
                    }
                    opts.forEachIndexed { idx, opt ->
                        val isSelected = currentAnswers.contains(idx.toString())
                        OptionCard(
                            letter = ('A' + idx).toChar(),
                            text = opt,
                            isSelected = isSelected,
                            isMultiple = false,
                            onClick = { viewModel.selectAnswer(currentQ.id, idx.toString(), isMultiple = false) }
                        )
                    }
                }

                QuestionType.MULTIPLE_CORRECT -> {
                    val opts = remember(currentQ.optionsJson) {
                        try {
                            val arr = JSONArray(currentQ.optionsJson)
                            (0 until arr.length()).map { arr.optString(it) }
                        } catch (e: Exception) {
                            emptyList()
                        }
                    }
                    Text(
                        text = "Multiple options may be correct. Check all that apply:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    opts.forEachIndexed { idx, opt ->
                        val isSelected = currentAnswers.contains(idx.toString())
                        OptionCard(
                            letter = ('A' + idx).toChar(),
                            text = opt,
                            isSelected = isSelected,
                            isMultiple = true,
                            onClick = { viewModel.selectAnswer(currentQ.id, idx.toString(), isMultiple = true) }
                        )
                    }
                }

                QuestionType.TRUE_FALSE -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isTrue = currentAnswers.firstOrNull().equals("true", ignoreCase = true)
                        val isFalse = currentAnswers.firstOrNull().equals("false", ignoreCase = true)

                        SelectableCard(
                            label = "TRUE",
                            isSelected = isTrue,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.selectAnswer(currentQ.id, "true") }
                        )
                        SelectableCard(
                            label = "FALSE",
                            isSelected = isFalse,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.selectAnswer(currentQ.id, "false") }
                        )
                    }
                }

                QuestionType.NUMERICAL -> {
                    val currentVal = currentAnswers.firstOrNull() ?: ""
                    OutlinedTextField(
                        value = currentVal,
                        onValueChange = { viewModel.selectAnswer(currentQ.id, it) },
                        label = { Text("Enter numerical answer") },
                        placeholder = { Text("e.g. 8 or 3.14") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                else -> {
                    val currentVal = currentAnswers.firstOrNull() ?: ""
                    OutlinedTextField(
                        value = currentVal,
                        onValueChange = { viewModel.selectAnswer(currentQ.id, it) },
                        label = { Text("Enter your response") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    // Question Palette Bottom Sheet
    if (showPaletteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPaletteSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Question Palette",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LegendItem(color = SuccessGreen, label = "Answered ($answeredCount)")
                    LegendItem(color = MaterialTheme.colorScheme.outline, label = "Unanswered ($unansweredCount)")
                    LegendItem(color = WarningYellow, label = "Review (${markedForReview.size})")
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(48.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(220.dp)
                ) {
                    itemsIndexed(activeQuestions) { idx, q ->
                        val ans = userAnswers[q.id] ?: emptyList()
                        val isAns = ans.isNotEmpty() && ans.any { it.isNotBlank() }
                        val isRev = markedForReview.contains(q.id)
                        val isCur = idx == currentIndex

                        val cellBg = when {
                            isCur -> MaterialTheme.colorScheme.primary
                            isRev -> WarningYellow
                            isAns -> SuccessGreen
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(cellBg)
                                .clickable {
                                    viewModel.goToQuestion(idx)
                                    showPaletteSheet = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${idx + 1}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAns || isCur || isRev) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        showPaletteSheet = false
                        showSubmitDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Submit Test")
                }
            }
        }
    }

    // Submit Confirmation Dialog
    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("Submit Test Confirmation", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Are you sure you want to finish and submit?")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("✅ Answered: $answeredCount")
                    Text("⚠️ Unanswered: $unansweredCount")
                    Text("🔖 Marked for Review: ${markedForReview.size}")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitDialog = false
                        viewModel.submitActiveTest { aId ->
                            onTestSubmitted(aId)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("Confirm Submit")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSubmitDialog = false }) {
                    Text("Continue Test")
                }
            }
        )
    }

    // Exit Confirmation Dialog
    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = { Text("Exit Test?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Your progress is saved as a draft. You can resume or submit now.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        onCancel()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Exit to Menu")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showExitConfirmDialog = false }) {
                    Text("Keep Testing")
                }
            }
        )
    }
}

@Composable
fun OptionCard(
    letter: Char,
    text: String,
    isSelected: Boolean,
    isMultiple: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isMultiple) {
                Checkbox(checked = isSelected, onCheckedChange = { onClick() })
            } else {
                RadioButton(selected = isSelected, onClick = { onClick() })
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$letter)",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                MarkdownMathView(markdownText = text)
            }
        }
    }
}

@Composable
fun SelectableCard(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = modifier
            .border(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}
