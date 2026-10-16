package com.studyforge.app.ui.screens.questions

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.studyforge.app.domain.json.BulkQuestionBankImportSummary
import com.studyforge.app.domain.json.BulkQuestionBankParser
import com.studyforge.app.domain.json.BulkQuestionBankValidationResult
import com.studyforge.app.ui.components.SquircleIconBadge
import com.studyforge.app.ui.theme.ErrorRed
import com.studyforge.app.ui.theme.SuccessGreen
import com.studyforge.app.ui.theme.WarningYellow
import com.studyforge.app.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportBulkQuestionBankDialog(
    viewModel: StudyViewModel,
    preselectedBatchId: Long? = null,
    preselectedSubjectId: Long? = null,
    onDismiss: () -> Unit,
    onSuccess: (BulkQuestionBankImportSummary) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val batches by viewModel.batches.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val chapters by viewModel.chapters.collectAsState()

    var jsonText by remember { mutableStateOf(BulkQuestionBankParser.EXAMPLE_MULTI_CHAPTER_JSON) }
    var validationResult by remember { mutableStateOf<BulkQuestionBankValidationResult?>(null) }
    var isImporting by remember { mutableStateOf(false) }
    var showChapterPreview by remember { mutableStateOf(true) }

    // Target Selection: initialize with preselected or 0L (auto-match/create)
    var selectedBatchId by remember(preselectedBatchId) {
        mutableStateOf(preselectedBatchId ?: 0L)
    }
    var userManuallySelectedBatch by remember(preselectedBatchId) {
        mutableStateOf(preselectedBatchId != null && preselectedBatchId > 0L)
    }

    var selectedSubjectId by remember(preselectedSubjectId) {
        mutableStateOf(preselectedSubjectId ?: 0L)
    }

    // When validationResult changes, if user hasn't explicitly picked a batch, auto-match or set 0L (auto-create)
    LaunchedEffect(validationResult, batches) {
        if (!userManuallySelectedBatch) {
            val jsonBatchName = validationResult?.batchName?.trim()
            if (!jsonBatchName.isNullOrBlank()) {
                val matched = batches.find { it.name.equals(jsonBatchName, ignoreCase = true) }
                if (matched != null) {
                    selectedBatchId = matched.id
                } else {
                    selectedBatchId = 0L
                }
            } else if (preselectedBatchId != null && preselectedBatchId > 0L) {
                selectedBatchId = preselectedBatchId
            }
        }
    }

    LaunchedEffect(selectedBatchId, subjects) {
        if (selectedBatchId > 0L) {
            val batchSubs = subjects.filter { it.batchId == selectedBatchId }
            if (selectedSubjectId != 0L && batchSubs.none { it.id == selectedSubjectId }) {
                selectedSubjectId = batchSubs.firstOrNull()?.id ?: 0L
            }
        }
    }

    var batchDropdownExpanded by remember { mutableStateOf(false) }
    var subjectDropdownExpanded by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var isReadingFile by remember { mutableStateOf(false) }
    var isValidating by remember { mutableStateOf(false) }
    var showAllChapters by remember { mutableStateOf(false) }

    // File picker launcher
    val filePicker = rememberLauncherForActivityResult(
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
                    if (content != null) {
                        jsonText = content
                        viewModel.showMessage("Loaded file (${(content.length / 1024).coerceAtLeast(1)} KB)")
                    }
                } catch (t: Throwable) {
                    viewModel.showMessage("Failed to read file: ${t.localizedMessage ?: "Unknown error"}")
                } finally {
                    isReadingFile = false
                }
            }
        }
    }

    // Auto-validate whenever jsonText changes (debounced in background)
    LaunchedEffect(jsonText) {
        if (jsonText.isBlank()) {
            validationResult = null
            isValidating = false
            return@LaunchedEffect
        }
        isValidating = true
        delay(200)
        try {
            validationResult = viewModel.validateBulkQuestionBankJson(jsonText)
        } catch (t: Throwable) {
            validationResult = BulkQuestionBankValidationResult(
                isValid = false,
                errorMessage = "Parse error: ${t.localizedMessage ?: "Failed to process JSON"}"
            )
        } finally {
            isValidating = false
        }
    }

    val availableSubjectsForBatch = remember(selectedBatchId, subjects) {
        if (selectedBatchId > 0L) {
            subjects.filter { it.batchId == selectedBatchId }
        } else {
            subjects
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isImporting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SquircleIconBadge(
                    icon = Icons.Default.Quiz,
                    accentColor = MaterialTheme.colorScheme.primary,
                    size = 42.dp
                )
                Column {
                    Text(
                        text = "Import All Chapters Question Bank",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Multi-chapter questions import from JSON in 1-click",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Preset Chips Row
                Text(
                    text = "Quick Template Presets:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = jsonText == BulkQuestionBankParser.EXAMPLE_MULTI_CHAPTER_JSON,
                        onClick = { jsonText = BulkQuestionBankParser.EXAMPLE_MULTI_CHAPTER_JSON },
                        label = { Text("Multi-Chapter (Physics)") },
                        leadingIcon = { Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected = jsonText == BulkQuestionBankParser.EXAMPLE_MULTI_SUBJECT_JSON,
                        onClick = { jsonText = BulkQuestionBankParser.EXAMPLE_MULTI_SUBJECT_JSON },
                        label = { Text("Multi-Subject (STEM)") },
                        leadingIcon = { Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected = jsonText == BulkQuestionBankParser.EXAMPLE_FLAT_QUESTIONS_JSON,
                        onClick = { jsonText = BulkQuestionBankParser.EXAMPLE_FLAT_QUESTIONS_JSON },
                        label = { Text("Flat Chapter-Tagged") }
                    )
                }

                // Batch and Subject Targets
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Destination Setup",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Target Batch Dropdown
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Target Batch:", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(95.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                val currentBatch = batches.find { it.id == selectedBatchId }
                                val currentBatchName = when {
                                    currentBatch != null -> "${currentBatch.name} (${currentBatch.courseOrClass.ifBlank { "Standard" }})"
                                    !validationResult?.batchName.isNullOrBlank() -> "✨ Auto-create: ${validationResult?.batchName}"
                                    else -> "Auto-create / Select Batch"
                                }

                                OutlinedButton(
                                    onClick = { batchDropdownExpanded = true },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = currentBatchName,
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
                                    DropdownMenuItem(
                                        text = { Text("✨ Auto-create or Auto-match from JSON") },
                                        onClick = {
                                            selectedBatchId = 0L
                                            selectedSubjectId = 0L
                                            userManuallySelectedBatch = true
                                            batchDropdownExpanded = false
                                        }
                                    )
                                    batches.forEach { b ->
                                        DropdownMenuItem(
                                            text = { Text("📁 ${b.name} (${b.courseOrClass.ifBlank { "Standard" }})") },
                                            onClick = {
                                                selectedBatchId = b.id
                                                val batchSubs = subjects.filter { it.batchId == b.id }
                                                selectedSubjectId = batchSubs.firstOrNull()?.id ?: 0L
                                                userManuallySelectedBatch = true
                                                batchDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Target Default Subject Dropdown
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Default Subject:", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(95.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                val currentSubName = availableSubjectsForBatch.find { it.id == selectedSubjectId }?.name
                                    ?: validationResult?.defaultSubjectName
                                    ?: "Auto-match from JSON"

                                OutlinedButton(
                                    onClick = { subjectDropdownExpanded = true },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = currentSubName,
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
                                    DropdownMenuItem(
                                        text = { Text("✨ Auto-match from JSON / Create Subject") },
                                        onClick = {
                                            selectedSubjectId = 0L
                                            subjectDropdownExpanded = false
                                        }
                                    )
                                    availableSubjectsForBatch.forEach { s ->
                                        DropdownMenuItem(
                                            text = { Text("📖 ${s.name}") },
                                            onClick = {
                                                selectedSubjectId = s.id
                                                subjectDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Helper Action Buttons: Paste, Pick File, Clear
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            try {
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) {
                                    jsonText = clip
                                    viewModel.showMessage("Pasted ${(clip.length / 1024).coerceAtLeast(1)} KB from clipboard")
                                } else {
                                    viewModel.showMessage("Clipboard is empty.")
                                }
                            } catch (t: Throwable) {
                                viewModel.showMessage("Failed to paste: ${t.localizedMessage}")
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Paste", style = MaterialTheme.typography.bodySmall, maxLines = 1, softWrap = false)
                    }

                    OutlinedButton(
                        onClick = { filePicker.launch("application/json") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("File", style = MaterialTheme.typography.bodySmall, maxLines = 1, softWrap = false)
                    }

                    OutlinedButton(
                        onClick = {
                            jsonText = ""
                            validationResult = null
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Clear", style = MaterialTheme.typography.bodySmall, maxLines = 1, softWrap = false)
                    }
                }

                // Loading / Parsing indicator
                if (isReadingFile || isValidating) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text(
                            text = if (isReadingFile) "Reading large JSON file from disk..." else "Analyzing & validating questions in background...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // High-performance payload display: protect Compose from massive text OOM/ANR
                if (jsonText.length <= 15_000) {
                    OutlinedTextField(
                        value = jsonText,
                        onValueChange = { jsonText = it },
                        label = { Text("Multi-Chapter JSON") },
                        placeholder = { Text("Paste JSON with chapters and question lists...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    )
                } else {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FilePresent,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Large JSON Payload Loaded",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${(jsonText.length / 1024).coerceAtLeast(1)} KB • ~${jsonText.count { it == '\n' } + 1} lines in memory",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "⚡ Full file is held safely in background memory. Text editor is bypassed for files > 15 KB to prevent phone freezing. Preview:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 130.dp)
                            ) {
                                val previewSnippet = remember(jsonText) {
                                    val lines = jsonText.lines()
                                    lines.take(20).joinToString("\n") + if (lines.size > 20) "\n... [${lines.size - 20} more lines ready for import]" else ""
                                }
                                Text(
                                    text = previewSnippet,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .verticalScroll(rememberScrollState())
                                )
                            }
                        }
                    }
                }

                // Live Validation Diagnostic Feedback
                validationResult?.let { res ->
                    if (res.isValid) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.12f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Valid Question Bank JSON",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = SuccessGreen
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    IconButton(
                                        onClick = { showChapterPreview = !showChapterPreview },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (showChapterPreview) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Toggle Preview"
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "• ${res.totalQuestionsCount} Questions across ${res.totalChaptersCount} Chapters",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )

                                if (showChapterPreview) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val displayedGroups = if (showAllChapters) res.chapterGroups else res.chapterGroups.take(10)
                                    displayedGroups.forEach { group ->
                                        val existing = chapters.any { c ->
                                            c.name.equals(group.chapterName, ignoreCase = true)
                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "📖 ${group.chapterName}${if (!group.subjectName.isNullOrBlank()) " (${group.subjectName})" else ""}",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.weight(1f),
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "${group.questions.size} Qs",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (existing) "[Matched ✅]" else "[New Chapter ➕]",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (existing) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (!showAllChapters && res.chapterGroups.size > 10) {
                                        TextButton(
                                            onClick = { showAllChapters = true },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Show all ${res.chapterGroups.size} chapters (${res.chapterGroups.size - 10} more)")
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.12f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = ErrorRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = res.errorMessage ?: "Invalid JSON syntax.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ErrorRed
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validRes = validationResult
                    if (validRes != null && validRes.isValid) {
                        isImporting = true
                        val bId = if (selectedBatchId > 0L) {
                            selectedBatchId
                        } else {
                            val matched = batches.find { it.name.equals(validRes.batchName, ignoreCase = true) }
                            matched?.id ?: (batches.firstOrNull()?.id ?: 0L)
                        }
                        val sId = if (selectedSubjectId > 0L && subjects.any { it.id == selectedSubjectId && it.batchId == bId }) {
                            selectedSubjectId
                        } else null

                        viewModel.importBulkQuestionBank(
                            result = validRes,
                            targetBatchId = bId,
                            fallbackSubjectId = sId
                        ) { summary ->
                            isImporting = false
                            onSuccess(summary)
                            onDismiss()
                        }
                    }
                },
                enabled = validationResult?.isValid == true && !isImporting,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isImporting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Importing Questions...")
                } else {
                    val qCount = validationResult?.totalQuestionsCount ?: 0
                    val cCount = validationResult?.totalChaptersCount ?: 0
                    Text(if (qCount > 0) "Import All ($qCount Qs • $cCount Chapters)" else "Import Questions")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isImporting
            ) {
                Text("Cancel")
            }
        }
    )
}
