package com.studyforge.app.ui.screens.mistakes

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.studyforge.app.data.local.entities.MistakeEntity
import com.studyforge.app.data.local.entities.QuestionEntity
import com.studyforge.app.ui.components.EmptyState
import com.studyforge.app.ui.components.MarkdownMathView
import com.studyforge.app.ui.components.StudyCard
import com.studyforge.app.ui.theme.ErrorRed
import com.studyforge.app.ui.theme.SuccessGreen
import com.studyforge.app.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MistakeBookScreen(
    chapterId: Long,
    viewModel: StudyViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allMistakes by viewModel.allMistakes.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val chapters by viewModel.chapters.collectAsState()

    var showOnlyUnresolved by remember { mutableStateOf(true) }
    var mistakeToResolve by remember { mutableStateOf<MistakeEntity?>(null) }
    var reflectionNoteInput by remember { mutableStateOf("") }

    val filteredMistakes = remember(allMistakes, chapterId, showOnlyUnresolved) {
        allMistakes.filter { m ->
            val matchesChapter = chapterId <= 0 || m.chapterId == chapterId
            val matchesResolved = !showOnlyUnresolved || !m.isResolved
            matchesChapter && matchesResolved
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mistake Book", fontWeight = FontWeight.Bold) },
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = showOnlyUnresolved,
                    onClick = { showOnlyUnresolved = true },
                    label = { Text("Unresolved (${allMistakes.count { !it.isResolved }})") }
                )
                FilterChip(
                    selected = !showOnlyUnresolved,
                    onClick = { showOnlyUnresolved = false },
                    label = { Text("All Mistakes (${allMistakes.size})") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredMistakes.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.CheckCircle,
                    title = "No Mistakes Here!",
                    message = "Every incorrect test question is automatically saved here so you can master your blind spots."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredMistakes) { mistake ->
                        val q = questions.find { it.id == mistake.questionId }
                        val chap = chapters.find { it.id == mistake.chapterId }
                        if (q != null) {
                            MistakeCard(
                                mistake = mistake,
                                question = q,
                                chapterName = chap?.name ?: "",
                                onMarkResolved = {
                                    mistakeToResolve = mistake
                                    reflectionNoteInput = mistake.userReflectionNote
                                }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(48.dp)) }
                }
            }
        }
    }

    // Resolve dialog
    mistakeToResolve?.let { m ->
        AlertDialog(
            onDismissRequest = { mistakeToResolve = null },
            title = { Text("Resolve Mistake", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add a personal reflection note explaining why the mistake occurred and how to prevent it:")
                    OutlinedTextField(
                        value = reflectionNoteInput,
                        onValueChange = { reflectionNoteInput = it },
                        label = { Text("Personal Reflection Note") },
                        placeholder = { Text("e.g. Forgot the chain rule factor of 2 inside the sine derivative.") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resolveMistake(m.id, reflectionNoteInput)
                        mistakeToResolve = null
                    }
                ) {
                    Text("Mark as Resolved")
                }
            },
            dismissButton = {
                TextButton(onClick = { mistakeToResolve = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun MistakeCard(
    mistake: MistakeEntity,
    question: QuestionEntity,
    chapterName: String,
    onMarkResolved: () -> Unit
) {
    StudyCard(
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (mistake.isResolved) SuccessGreen.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (mistake.isResolved) "Resolved" else "Attempted ${mistake.attemptCount}x",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (mistake.isResolved) SuccessGreen else ErrorRed,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (chapterName.isNotBlank()) {
                    Text(
                        text = chapterName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            MarkdownMathView(markdownText = question.questionText)

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("What went wrong:", style = MaterialTheme.typography.labelSmall, color = ErrorRed, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Your Answer: ${mistake.userAnswerJson.replace("\"", "").replace("[", "").replace("]", "")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ErrorRed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Correct Answer: ${mistake.correctAnswerJson.replace("\"", "").replace("[", "").replace("]", "")}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = SuccessGreen
                    )
                }
            }

            if (mistake.userReflectionNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reflection: ${mistake.userReflectionNote}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (question.explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text("Solution & Logic:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                MarkdownMathView(markdownText = question.explanation)
            }

            if (!mistake.isResolved) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onMarkResolved,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reflect & Mark Resolved")
                }
            }
        }
    }
}
