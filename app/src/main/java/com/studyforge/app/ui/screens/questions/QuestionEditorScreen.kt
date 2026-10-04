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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studyforge.app.domain.model.Difficulty
import com.studyforge.app.domain.model.QuestionType
import com.studyforge.app.ui.components.MathEditorToolbar
import com.studyforge.app.viewmodel.StudyViewModel
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionEditorScreen(
    chapterId: Long,
    questionId: Long,
    viewModel: StudyViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val questions by viewModel.questions.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val chapter = chapters.find { it.id == chapterId }

    val existing = remember(questionId, questions) {
        if (questionId > 0) questions.find { it.id == questionId } else null
    }

    var selectedType by remember { mutableStateOf(existing?.type ?: QuestionType.MCQ) }
    var questionText by remember { mutableStateOf(existing?.questionText ?: "") }
    var marks by remember { mutableDoubleStateOf(existing?.marks ?: 2.0) }
    var negativeMarks by remember { mutableDoubleStateOf(existing?.negativeMarks ?: 0.5) }
    var difficulty by remember { mutableStateOf(existing?.difficulty ?: Difficulty.MEDIUM) }
    var topic by remember { mutableStateOf(existing?.topic ?: "") }
    var explanation by remember { mutableStateOf(existing?.explanation ?: "") }
    var detailedSolution by remember { mutableStateOf(existing?.detailedSolution ?: "") }
    var hint by remember { mutableStateOf(existing?.hint ?: "") }

    // Dynamic options for MCQ / Multiple Correct
    val options = remember {
        mutableStateListOf<String>().apply {
            if (existing != null) {
                try {
                    val arr = JSONArray(existing.optionsJson)
                    for (i in 0 until arr.length()) add(arr.optString(i))
                } catch (e: Exception) {}
            }
            if (isEmpty() && (selectedType == QuestionType.MCQ || selectedType == QuestionType.MULTIPLE_CORRECT)) {
                addAll(listOf("Option A", "Option B", "Option C", "Option D"))
            }
        }
    }

    // Selected correct answers (indices as strings or text)
    val correctAnswers = remember {
        mutableStateListOf<String>().apply {
            if (existing != null) {
                try {
                    val arr = JSONArray(existing.correctAnswersJson)
                    for (i in 0 until arr.length()) add(arr.optString(i))
                } catch (e: Exception) {}
            }
            if (isEmpty() && selectedType == QuestionType.MCQ) {
                add("0")
            }
        }
    }

    // For TRUE_FALSE: "true" or "false"
    var trueFalseAnswer by remember {
        mutableStateOf(
            if (existing?.type == QuestionType.TRUE_FALSE) {
                val ans = existing.correctAnswersJson
                if (ans.contains("false", ignoreCase = true) || ans.contains("1")) "false" else "true"
            } else "true"
        )
    }

    // For FILL_BLANK, NUMERICAL, SHORT_ANSWER
    var singleTextAnswer by remember {
        mutableStateOf(
            existing?.correctAnswersJson?.let {
                try { JSONArray(it).optString(0, "") } catch (e: Exception) { "" }
            } ?: ""
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing != null) "Edit Question" else "Create Question", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val finalOptions: List<String>
                    val finalCorrect: List<String>

                    when (selectedType) {
                        QuestionType.MCQ, QuestionType.MULTIPLE_CORRECT -> {
                            finalOptions = options.toList()
                            finalCorrect = correctAnswers.toList()
                        }
                        QuestionType.TRUE_FALSE -> {
                            finalOptions = listOf("True", "False")
                            finalCorrect = listOf(if (trueFalseAnswer == "false") "false" else "true")
                        }
                        QuestionType.FILL_BLANK, QuestionType.NUMERICAL, QuestionType.SHORT_ANSWER, QuestionType.LONG_ANSWER -> {
                            finalOptions = emptyList()
                            finalCorrect = listOf(singleTextAnswer.trim())
                        }
                        else -> {
                            finalOptions = options.toList()
                            finalCorrect = listOf(singleTextAnswer.trim())
                        }
                    }

                    viewModel.saveQuestion(
                        id = existing?.id ?: 0L,
                        chapterId = chapterId,
                        subjectId = chapter?.subjectId ?: 1L,
                        batchId = chapter?.batchId ?: 1L,
                        type = selectedType,
                        questionText = questionText,
                        options = finalOptions,
                        correctAnswers = finalCorrect,
                        explanation = explanation,
                        detailedSolution = detailedSolution,
                        marks = marks,
                        negativeMarks = negativeMarks,
                        difficulty = difficulty,
                        topic = topic,
                        hint = hint
                    )
                    onBack()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Save Question")
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
            // Question Type Selector with horizontal scroll supporting ALL question types
            Text("Question Type", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    QuestionType.MCQ,
                    QuestionType.MULTIPLE_CORRECT,
                    QuestionType.TRUE_FALSE,
                    QuestionType.FILL_BLANK,
                    QuestionType.NUMERICAL,
                    QuestionType.SHORT_ANSWER
                ).forEach { t ->
                    FilterChip(
                        selected = selectedType == t,
                        onClick = {
                            selectedType = t
                            if (t == QuestionType.MCQ || t == QuestionType.MULTIPLE_CORRECT) {
                                if (options.isEmpty()) options.addAll(listOf("Option A", "Option B", "Option C", "Option D"))
                            }
                        },
                        label = { Text(t.displayName, maxLines = 1) }
                    )
                }
            }

            // Math Quick Bar
            MathEditorToolbar(onInsertText = { toAdd -> questionText += toAdd })

            // Question Text Input
            OutlinedTextField(
                value = questionText,
                onValueChange = { questionText = it },
                label = { Text("Question Text (Markdown & LaTeX supported) *") },
                placeholder = {
                    when (selectedType) {
                        QuestionType.FILL_BLANK -> Text("e.g. The derivative of $\\sin(x)$ is _______.")
                        QuestionType.TRUE_FALSE -> Text("e.g. Every continuous function is differentiable.")
                        QuestionType.NUMERICAL -> Text("e.g. What is the value of $\\lim_{x \\to 0} \\frac{\\sin x}{x}$?")
                        else -> Text("e.g. Evaluate $\\int x^2 dx$")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            // Options & Correct Answer Configuration per Question Type
            when (selectedType) {
                QuestionType.MCQ, QuestionType.MULTIPLE_CORRECT -> {
                    Text(
                        text = if (selectedType == QuestionType.MCQ) "Options (Select Single Correct Radio)" else "Options (Check All That Apply)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )

                    options.forEachIndexed { idx, opt ->
                        val isCorrect = correctAnswers.contains(idx.toString())
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedType == QuestionType.MCQ) {
                                RadioButton(
                                    selected = isCorrect,
                                    onClick = {
                                        correctAnswers.clear()
                                        correctAnswers.add(idx.toString())
                                    }
                                )
                            } else {
                                Checkbox(
                                    checked = isCorrect,
                                    onCheckedChange = { chk ->
                                        if (chk) correctAnswers.add(idx.toString()) else correctAnswers.remove(idx.toString())
                                    }
                                )
                            }
                            OutlinedTextField(
                                value = opt,
                                onValueChange = { newVal -> options[idx] = newVal },
                                label = { Text("Option ${('A' + idx)}") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            IconButton(onClick = {
                                if (options.size > 2) {
                                    options.removeAt(idx)
                                    correctAnswers.remove(idx.toString())
                                }
                            }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Remove")
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { options.add("Option ${('A' + options.size)}") },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Option")
                    }
                }

                QuestionType.TRUE_FALSE -> {
                    Text("Correct Answer", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        FilterChip(
                            selected = trueFalseAnswer == "true",
                            onClick = { trueFalseAnswer = "true" },
                            label = { Text("✔ TRUE", fontWeight = FontWeight.Bold) }
                        )
                        FilterChip(
                            selected = trueFalseAnswer == "false",
                            onClick = { trueFalseAnswer = "false" },
                            label = { Text("✖ FALSE", fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                QuestionType.FILL_BLANK -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Use blank line (e.g. _______) in the question text. Enter the expected blank value below.", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    OutlinedTextField(
                        value = singleTextAnswer,
                        onValueChange = { singleTextAnswer = it },
                        label = { Text("Correct Blank Answer *") },
                        placeholder = { Text("e.g. cos(x)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                QuestionType.NUMERICAL -> {
                    OutlinedTextField(
                        value = singleTextAnswer,
                        onValueChange = { singleTextAnswer = it },
                        label = { Text("Exact Numerical Value *") },
                        placeholder = { Text("e.g. 1 or 3.14159 or 42") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                QuestionType.SHORT_ANSWER -> {
                    OutlinedTextField(
                        value = singleTextAnswer,
                        onValueChange = { singleTextAnswer = it },
                        label = { Text("Expected Answer / Key Terms *") },
                        placeholder = { Text("e.g. Heisenberg Uncertainty Principle") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                else -> {
                    OutlinedTextField(
                        value = singleTextAnswer,
                        onValueChange = { singleTextAnswer = it },
                        label = { Text("Correct Answer *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Marks & Negative Marks
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = marks.toString(),
                    onValueChange = { marks = it.toDoubleOrNull() ?: 1.0 },
                    label = { Text("Marks") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = negativeMarks.toString(),
                    onValueChange = { negativeMarks = it.toDoubleOrNull() ?: 0.0 },
                    label = { Text("Negative Marks") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            // Difficulty Selector
            Text("Difficulty", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Difficulty.values().forEach { d ->
                    FilterChip(
                        selected = difficulty == d,
                        onClick = { difficulty = d },
                        label = { Text(d.displayName) }
                    )
                }
            }

            // Topic Tag
            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text("Topic / Subtopic") },
                placeholder = { Text("e.g. Integration by Substitution") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            // Hint
            OutlinedTextField(
                value = hint,
                onValueChange = { hint = it },
                label = { Text("Hint (optional)") },
                placeholder = { Text("e.g. Try substituting u = x^2 + 1") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            // Explanation & Solution
            OutlinedTextField(
                value = explanation,
                onValueChange = { explanation = it },
                label = { Text("Explanation (shown in review)") },
                placeholder = { Text("Brief reason why this answer is correct...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = detailedSolution,
                onValueChange = { detailedSolution = it },
                label = { Text("Detailed Step-by-Step Solution (Markdown & LaTeX)") },
                placeholder = { Text("Full step-by-step derivation...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}
