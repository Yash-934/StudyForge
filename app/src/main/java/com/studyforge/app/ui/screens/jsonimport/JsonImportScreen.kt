package com.studyforge.app.ui.screens.jsonimport

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyforge.app.domain.json.TestJsonParser
import com.studyforge.app.domain.model.TestValidationResult
import com.studyforge.app.ui.components.DifficultyBadge
import com.studyforge.app.ui.components.EmptyState
import com.studyforge.app.ui.components.MarkdownMathView
import com.studyforge.app.ui.components.SquircleIconBadge
import com.studyforge.app.ui.components.StudyCard
import com.studyforge.app.ui.theme.ErrorRed
import com.studyforge.app.ui.theme.SuccessGreen
import com.studyforge.app.ui.theme.WarningYellow
import com.studyforge.app.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JsonImportScreen(
    viewModel: StudyViewModel,
    onBack: () -> Unit,
    onNavigateToSchemaHelp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val batches by viewModel.batches.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val chapters by viewModel.chapters.collectAsState()

    var jsonInput by remember { mutableStateOf(TestJsonParser.EXAMPLE_TEST_JSON) }
    var selectedInputTab by remember { mutableIntStateOf(0) } // 0 = Paste, 1 = File
    var validationResult by remember { mutableStateOf<TestValidationResult?>(null) }
    var importMode by remember { mutableIntStateOf(1) } // 1 = As Test, 2 = Question Bank, 3 = Assign Chapter

    var selectedBatchId by remember { mutableStateOf(batches.firstOrNull()?.id ?: 1L) }
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: 1L) }
    var selectedChapterId by remember { mutableStateOf(chapters.firstOrNull()?.id ?: 1L) }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val content = stream.bufferedReader().use { it.readText() }
                    jsonInput = content
                    validationResult = viewModel.validateJson(content)
                }
            } catch (e: Exception) {
                viewModel.showMessage("Failed to read file: ${e.message}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Test JSON", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSchemaHelp) {
                        Icon(imageVector = Icons.Filled.HelpOutline, contentDescription = "JSON Schema Help")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                PrimaryTabRow(selectedTabIndex = selectedInputTab) {
                    Tab(
                        selected = selectedInputTab == 0,
                        onClick = { selectedInputTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Paste JSON")
                            }
                        }
                    )
                    Tab(
                        selected = selectedInputTab == 1,
                        onClick = {
                            selectedInputTab = 1
                            filePickerLauncher.launch("application/json")
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pick File")
                            }
                        }
                    )
                }
            }

            if (selectedInputTab == 0) {
                item {
                    OutlinedTextField(
                        value = jsonInput,
                        onValueChange = {
                            jsonInput = it
                            validationResult = null // Reset on edit
                        },
                        placeholder = { Text("Paste JSON here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            } else {
                item {
                    StudyCard(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { filePickerLauncher.launch("application/json") }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            SquircleIconBadge(
                                icon = Icons.Default.UploadFile,
                                tint = MaterialTheme.colorScheme.primary,
                                size = 52.dp,
                                iconSize = 28.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Choose .json file from device storage", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tap here to open the Android File Picker", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Validate Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            validationResult = viewModel.validateJson(jsonInput)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Validate JSON")
                    }

                    OutlinedButton(
                        onClick = onNavigateToSchemaHelp,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("View Schema")
                    }
                }
            }

            // Validation Results Diagnostic Display
            validationResult?.let { res ->
                item {
                    StudyCard(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SquircleIconBadge(
                                    icon = if (res.validQuestionsCount > 0) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                    tint = if (res.validQuestionsCount > 0) SuccessGreen else ErrorRed,
                                    size = 44.dp,
                                    iconSize = 24.dp,
                                    backgroundColor = if (res.validQuestionsCount > 0) SuccessGreen.copy(alpha = 0.12f) else ErrorRed.copy(alpha = 0.12f)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Validation: ${res.validQuestionsCount} Valid Questions",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Title: ${res.testDto.title} • ${res.testDto.durationMinutes} mins • ${res.testDto.totalMarks} marks",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (res.issues.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Issues & Warnings:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                res.issues.forEach { issue ->
                                    Text(
                                        text = "• [Q${issue.questionIndex}] ${issue.message}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (issue.isFatal) ErrorRed else WarningYellow
                                    )
                                }
                            }
                        }
                    }
                }

                if (res.validQuestionsCount > 0) {
                    // Import Mode Selector
                    item {
                        StudyCard(
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Select Import Mode", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = importMode == 1, onClick = { importMode = 1 })
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("Import as Runnable Test", fontWeight = FontWeight.SemiBold)
                                        Text("Creates a ready-to-take test with scorecards", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = importMode == 2, onClick = { importMode = 2 })
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("Import into Question Bank Only", fontWeight = FontWeight.SemiBold)
                                        Text("Add questions to bank for future practice", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    // Chapter assignment
                    item {
                        val activeChap = chapters.find { it.id == selectedChapterId } ?: chapters.firstOrNull()
                        val activeSub = subjects.find { it.id == activeChap?.subjectId } ?: subjects.firstOrNull()
                        val activeBatch = batches.find { it.id == activeSub?.batchId } ?: batches.firstOrNull()

                        StudyCard(
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Assign Destination", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Assign to: ${activeBatch?.name ?: "Batch"} → ${activeSub?.name ?: "Subject"} → ${activeChap?.name ?: "Chapter"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Final Import Action Button
                    item {
                        Button(
                            onClick = {
                                val activeChap = chapters.find { it.id == selectedChapterId } ?: chapters.firstOrNull()
                                viewModel.importValidatedTest(
                                    testDto = res.testDto,
                                    importMode = importMode,
                                    targetBatchId = activeChap?.batchId ?: 1L,
                                    targetSubjectId = activeChap?.subjectId ?: 1L,
                                    targetChapterId = activeChap?.id ?: 1L
                                )
                                onBack()
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirm & Import (${res.validQuestionsCount} Questions)")
                        }
                    }

                    // Preview of Questions
                    item {
                        Text(
                            text = "Question Preview (${res.testDto.questions.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    itemsIndexed(res.testDto.questions) { idx, q ->
                        StudyCard(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Q${idx + 1}. ${q.type.displayName}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    DifficultyBadge(difficulty = q.difficulty)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                MarkdownMathView(markdownText = q.question)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}
