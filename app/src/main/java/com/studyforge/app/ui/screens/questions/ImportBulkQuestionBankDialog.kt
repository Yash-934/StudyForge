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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val coroutineScope = rememberCoroutineScope()
    val batches by viewModel.batches.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val chapters by viewModel.chapters.collectAsState()

    var rawJsonText by remember { mutableStateOf(BulkQuestionBankParser.EXAMPLE_MULTI_CHAPTER_JSON) }
    var loadedFileName by remember { mutableStateOf<String?>(null) }
    var isReadingFile by remember { mutableStateOf(false) }
    var isValidating by remember { mutableStateOf(false) }
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

    // File picker launcher - Asynchronous background reading
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
                    if (!content.isNullOrBlank()) {
                        rawJsonText = content
                        loadedFileName = "Uploaded File (${content.length / 1024} KB)"
                        viewModel.showMessage("File loaded successfully (${content.length / 1024} KB). Validating...")
                    } else {
                        viewModel.showMessage("Selected file was empty.")
                    }
                } catch (e: Exception) {
                    viewModel.showMessage("Failed to read file: ${e.message}")
                } finally {
                    isReadingFile = false
                }
            }
        }
    }

    // Auto-validate whenever rawJsonText changes (Debounced and on Dispatchers.Default)
    LaunchedEffect(rawJsonText) {
        if (rawJsonText.isBlank()) {
            validationResult = null
            isValidating = false
            return@LaunchedEffect
        }
        isValidating = true
        delay(300) // Debounce rapid edits
        try {
            validationResult = viewModel.validateBulkQuestionBankJsonAsync(rawJsonText)
        } catch (t: Throwable) {
            validationResult = BulkQuestionBankValidationResult(
                isValid = false,
                errorMessage = if (t is OutOfMemoryError) "File is too large for device memory." else "Validation error: ${t.localizedMessage}"
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

    val isLargeContent = rawJsonText.length > 4000 || loadedFileName != null
    val estimatedLineCount = remember(rawJsonText) {
        if (rawJsonText.isEmpty()) 0 else rawJsonText.count { it == '\n' } + 1
    }
    val safePreviewSnippet = remember(rawJsonText) {
        if (rawJsonText.isEmpty()) "" else rawJsonText.take(400).lines().take(4).joinToString("\n")
    }
    val existingChapterNames = remember(chapters) {
        chapters.map { it.name.trim().lowercase() }.toSet()
    }

    AlertDialog(
        onDismissRequest = { if (!isImporting && !isReadingFile) onDismiss() },
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
                        selected = !isLargeContent && rawJsonText == BulkQuestionBankParser.EXAMPLE_MULTI_CHAPTER_JSON,
                        onClick = {
                            rawJsonText = BulkQuestionBankParser.EXAMPLE_MULTI_CHAPTER_JSON
                            loadedFileName = null
                        },
                        label = { Text("Multi-Chapter (Physics)") },
                        leadingIcon = { Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected = !isLargeContent && rawJsonText == BulkQuestionBankParser.EXAMPLE_MULTI_SUBJECT_JSON,
                        onClick = {
                            rawJsonText = BulkQuestionBankParser.EXAMPLE_MULTI_SUBJECT_JSON
                            loadedFileName = null
                        },
                        label = { Text("Multi-Subject (STEM)") },
                        leadingIcon = { Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected = !isLargeContent && rawJsonText == BulkQuestionBankParser.EXAMPLE_FLAT_QUESTIONS_JSON,
                        onClick = {
                            rawJsonText = BulkQuestionBankParser.EXAMPLE_FLAT_QUESTIONS_JSON
                            loadedFileName = null
                        },
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
                            val clip = clipboardManager.getText()?.text
                            if (!clip.isNullOrBlank()) {
                                if (clip.length > 4000) {
                                    loadedFileName = "Pasted Clipboard Content (${clip.length / 1024} KB)"
                                    viewModel.showMessage("Pasted large question bank (${clip.length / 1024} KB). Validating...")
                                } else {
                                    loadedFileName = null
                                }
                                rawJsonText = clip
                            } else {
                                viewModel.showMessage("Clipboard is empty.")
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
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        enabled = !isReadingFile
                    ) {
                        if (isReadingFile) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(15.dp))
                        }
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(if (isReadingFile) "Reading..." else "File", style = MaterialTheme.typography.bodySmall, maxLines = 1, softWrap = false)
                    }

                    OutlinedButton(
                        onClick = {
                            rawJsonText = ""
                            loadedFileName = null
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

                // JSON Content Display: Large File Shielded vs Regular TextField
                if (isLargeContent) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = loadedFileName ?: "Large JSON Document Loaded",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${rawJsonText.length / 1024} KB • $estimatedLineCount lines ready for parsing",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        rawJsonText = ""
                                        loadedFileName = null
                                        validationResult = null
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = safePreviewSnippet + if (estimatedLineCount > 4) "\n..." else "",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = rawJsonText,
                        onValueChange = { newText ->
                            if (newText.length > 4000) {
                                loadedFileName = "Pasted Large JSON (${newText.length / 1024} KB)"
                            }
                            rawJsonText = newText
                        },
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
                }

                // Live Validation Diagnostic Feedback
                if (isValidating) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Validating and parsing questions in background...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else {
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
                                        val displayGroups = res.chapterGroups.take(10)
                                        displayGroups.forEach { group ->
                                            val existing = existingChapterNames.contains(group.chapterName.trim().lowercase())
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
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = "${group.questions.size} Qs",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (existing) "[Matched ✅]" else "[New ➕]",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (existing) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        if (res.chapterGroups.size > 10) {
                                            val remaining = res.chapterGroups.size - 10
                                            val remainingQs = res.chapterGroups.drop(10).sumOf { it.questions.size }
                                            Text(
                                                text = "+ $remaining more chapters ($remainingQs more questions)",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
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
                enabled = validationResult?.isValid == true && !isImporting && !isValidating && !isReadingFile,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isImporting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Importing Questions...")
                } else if (isValidating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Validating...")
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
                enabled = !isImporting && !isReadingFile
            ) {
                Text("Cancel")
            }
        }
    )
}
