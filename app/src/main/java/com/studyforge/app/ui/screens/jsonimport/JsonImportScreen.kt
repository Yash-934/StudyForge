package com.studyforge.app.ui.screens.jsonimport

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.UploadFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.LaunchedEffect
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
import com.studyforge.app.ui.screens.library.ImportCurriculumDialog
import com.studyforge.app.ui.screens.questions.ImportBulkQuestionBankDialog
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

    val coroutineScope = rememberCoroutineScope()
    var jsonInput by remember { mutableStateOf(TestJsonParser.EXAMPLE_TEST_JSON) }
    var loadedFileName by remember { mutableStateOf<String?>(null) }
    var isReadingFile by remember { mutableStateOf(false) }
    var isValidating by remember { mutableStateOf(false) }
    var selectedInputTab by remember { mutableIntStateOf(0) } // 0 = Paste, 1 = File
    var validationResult by remember { mutableStateOf<TestValidationResult?>(null) }
    var importMode by remember { mutableIntStateOf(1) } // 1 = As Test, 2 = Question Bank, 3 = Assign Chapter
    var showCurriculumDialog by remember { mutableStateOf(false) }
    var showBulkQuestionBankDialog by remember { mutableStateOf(false) }

    val isLargeInput = jsonInput.length > 4000 || loadedFileName != null
    val estimatedLineCount = remember(jsonInput) {
        if (jsonInput.isEmpty()) 0 else jsonInput.count { it == '\n' } + 1
    }
    val safePreviewSnippet = remember(jsonInput) {
        if (jsonInput.isEmpty()) "" else jsonInput.take(400).lines().take(4).joinToString("\n")
    }

    var selectedBatchId by remember { mutableStateOf(batches.firstOrNull()?.id ?: 1L) }
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: 1L) }
    var selectedChapterId by remember { mutableStateOf(chapters.firstOrNull()?.id ?: 1L) }

    var batchDropdownExpanded by remember { mutableStateOf(false) }
    var subjectDropdownExpanded by remember { mutableStateOf(false) }
    var chapterDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(batches) {
        if (batches.isNotEmpty() && batches.none { it.id == selectedBatchId }) {
            selectedBatchId = batches.first().id
        }
    }

    LaunchedEffect(selectedBatchId, subjects) {
        val subs = subjects.filter { it.batchId == selectedBatchId }
        if (subs.isNotEmpty() && subs.none { it.id == selectedSubjectId }) {
            selectedSubjectId = subs.first().id
        }
    }

    LaunchedEffect(selectedSubjectId, chapters) {
        val chaps = chapters.filter { it.subjectId == selectedSubjectId }
        if (chaps.isNotEmpty() && chaps.none { it.id == selectedChapterId }) {
            selectedChapterId = chaps.first().id
        }
    }

    // File picker launcher - Background Dispatchers.IO reading
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isReadingFile = true
                try {
                    val content = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            stream.bufferedReader().use { it.readText() }
                        }
                    }
                    if (!content.isNullOrBlank()) {
                        jsonInput = content
                        loadedFileName = "Uploaded File (${content.length / 1024} KB)"
                        validationResult = viewModel.validateJsonAsync(content)
                        viewModel.showMessage("File loaded (${content.length / 1024} KB) and validated.")
                    } else {
                        viewModel.showMessage("Selected file is empty.")
                    }
                } catch (e: Exception) {
                    viewModel.showMessage("Failed to read file: ${e.message}")
                } finally {
                    isReadingFile = false
                }
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
                    FilledTonalButton(
                        onClick = { showBulkQuestionBankDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("All Chapters Q-Bank", style = MaterialTheme.typography.labelSmall)
                    }
                    FilledTonalButton(
                        onClick = { showCurriculumDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Batch & Subjects", style = MaterialTheme.typography.labelSmall)
                    }
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
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SquircleIconBadge(
                            icon = Icons.Default.Quiz,
                            accentColor = MaterialTheme.colorScheme.secondary,
                            size = 38.dp,
                            iconSize = 20.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Import All Chapters Question Bank",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Multi-chapter questions auto-mapped to all chapters via JSON",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = { showBulkQuestionBankDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Open", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SquircleIconBadge(
                            icon = Icons.Default.Layers,
                            accentColor = MaterialTheme.colorScheme.primary,
                            size = 38.dp,
                            iconSize = 20.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Import Batch & Subjects JSON",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Full curriculum with subjects, chapters, notes & formulas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = { showCurriculumDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Open", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

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
                if (isLargeInput) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = loadedFileName ?: "Large JSON Document Loaded",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${jsonInput.length / 1024} KB • $estimatedLineCount lines",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            jsonInput = ""
                                            loadedFileName = null
                                            validationResult = null
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = safePreviewSnippet + if (estimatedLineCount > 4) "\n..." else "",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    item {
                        OutlinedTextField(
                            value = jsonInput,
                            onValueChange = {
                                if (it.length > 4000) {
                                    loadedFileName = "Pasted JSON Content (${it.length / 1024} KB)"
                                }
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
                            coroutineScope.launch {
                                isValidating = true
                                try {
                                    validationResult = viewModel.validateJsonAsync(jsonInput)
                                } catch (t: Throwable) {
                                    viewModel.showMessage("Failed to validate JSON: ${t.localizedMessage}")
                                } finally {
                                    isValidating = false
                                }
                            }
                        },
                        enabled = !isValidating && jsonInput.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isValidating) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Validating...")
                        } else {
                            Text("Validate JSON")
                        }
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
                        val activeBatch = batches.find { it.id == selectedBatchId } ?: batches.firstOrNull()
                        val availableSubs = subjects.filter { it.batchId == (activeBatch?.id ?: 0L) }
                        val activeSub = availableSubs.find { it.id == selectedSubjectId } ?: availableSubs.firstOrNull()
                        val availableChaps = chapters.filter { it.subjectId == (activeSub?.id ?: 0L) }
                        val activeChap = availableChaps.find { it.id == selectedChapterId } ?: availableChaps.firstOrNull()

                        StudyCard(
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("Assign Destination", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

                                // Batch selector
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Batch:", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(75.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        OutlinedButton(
                                            onClick = { batchDropdownExpanded = true },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = activeBatch?.let { "${it.name} (${it.courseOrClass.ifBlank { "Standard" }})" } ?: "Select Batch",
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 1,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                        }

                                        DropdownMenu(
                                            expanded = batchDropdownExpanded,
                                            onDismissRequest = { batchDropdownExpanded = false }
                                        ) {
                                            batches.forEach { b ->
                                                DropdownMenuItem(
                                                    text = { Text("📁 ${b.name} (${b.courseOrClass.ifBlank { "Standard" }})") },
                                                    onClick = {
                                                        selectedBatchId = b.id
                                                        val subs = subjects.filter { it.batchId == b.id }
                                                        selectedSubjectId = subs.firstOrNull()?.id ?: 0L
                                                        val chaps = chapters.filter { it.subjectId == selectedSubjectId }
                                                        selectedChapterId = chaps.firstOrNull()?.id ?: 0L
                                                        batchDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Subject selector
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Subject:", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(75.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        OutlinedButton(
                                            onClick = { subjectDropdownExpanded = true },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = activeSub?.name ?: "Auto-create Subject",
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 1,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                        }

                                        DropdownMenu(
                                            expanded = subjectDropdownExpanded,
                                            onDismissRequest = { subjectDropdownExpanded = false }
                                        ) {
                                            availableSubs.forEach { s ->
                                                DropdownMenuItem(
                                                    text = { Text("📖 ${s.name}") },
                                                    onClick = {
                                                        selectedSubjectId = s.id
                                                        val chaps = chapters.filter { it.subjectId == s.id }
                                                        selectedChapterId = chaps.firstOrNull()?.id ?: 0L
                                                        subjectDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Chapter selector
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Chapter:", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(75.dp))
                                    Box(modifier = Modifier.weight(1f)) {
                                        OutlinedButton(
                                            onClick = { chapterDropdownExpanded = true },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = activeChap?.name ?: "Auto-create Chapter",
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 1,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                        }

                                        DropdownMenu(
                                            expanded = chapterDropdownExpanded,
                                            onDismissRequest = { chapterDropdownExpanded = false }
                                        ) {
                                            availableChaps.forEach { c ->
                                                DropdownMenuItem(
                                                    text = { Text("🔖 ${c.name}") },
                                                    onClick = {
                                                        selectedChapterId = c.id
                                                        chapterDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Final Import Action Button
                    item {
                        Button(
                            onClick = {
                                val activeBatch = batches.find { it.id == selectedBatchId } ?: batches.firstOrNull()
                                val availableSubs = subjects.filter { it.batchId == (activeBatch?.id ?: 0L) }
                                val activeSub = availableSubs.find { it.id == selectedSubjectId } ?: availableSubs.firstOrNull()
                                val availableChaps = chapters.filter { it.subjectId == (activeSub?.id ?: 0L) }
                                val activeChap = availableChaps.find { it.id == selectedChapterId } ?: availableChaps.firstOrNull()

                                viewModel.importValidatedTest(
                                    testDto = res.testDto,
                                    importMode = importMode,
                                    targetBatchId = activeBatch?.id ?: 1L,
                                    targetSubjectId = activeSub?.id ?: 1L,
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

                    // Preview of Questions (Capped to first 10 to avoid WebView memory exhaustion)
                    val previewQuestions = res.testDto.questions.take(10)

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Question Preview (${previewQuestions.size}/${res.testDto.questions.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (res.testDto.questions.size > 10) {
                                Text(
                                    text = "Showing first 10",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    itemsIndexed(previewQuestions) { idx, q ->
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

                    if (res.testDto.questions.size > 10) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "✨ + ${res.testDto.questions.size - 10} more questions ready to import.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    if (showCurriculumDialog) {
        ImportCurriculumDialog(
            viewModel = viewModel,
            onDismiss = { showCurriculumDialog = false },
            onSuccess = { createdBatchId ->
                showCurriculumDialog = false
                selectedBatchId = createdBatchId
            }
        )
    }

    if (showBulkQuestionBankDialog) {
        ImportBulkQuestionBankDialog(
            viewModel = viewModel,
            preselectedBatchId = selectedBatchId,
            preselectedSubjectId = selectedSubjectId,
            onDismiss = { showBulkQuestionBankDialog = false },
            onSuccess = {
                showBulkQuestionBankDialog = false
            }
        )
    }
}
