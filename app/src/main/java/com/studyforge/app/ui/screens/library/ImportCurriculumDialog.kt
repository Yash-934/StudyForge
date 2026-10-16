package com.studyforge.app.ui.screens.library

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.studyforge.app.domain.json.CurriculumJsonParser
import com.studyforge.app.domain.json.CurriculumValidationResult
import com.studyforge.app.ui.components.SquircleIconBadge
import com.studyforge.app.ui.components.StudyCard
import com.studyforge.app.ui.theme.ErrorRed
import com.studyforge.app.ui.theme.SuccessGreen
import com.studyforge.app.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportCurriculumDialog(
    viewModel: StudyViewModel,
    preselectedBatchId: Long? = null,
    onDismiss: () -> Unit,
    onSuccess: (Long) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val batches by viewModel.batches.collectAsState()

    var jsonText by remember { mutableStateOf(CurriculumJsonParser.EXAMPLE_BATCH_SUBJECTS_JSON) }
    var validationResult by remember { mutableStateOf<CurriculumValidationResult?>(null) }
    var isImporting by remember { mutableStateOf(false) }

    // Target batch selection: 0L = Create new batch or auto-detect from JSON; > 0L = target existing batch
    var targetBatchId by remember { mutableStateOf(preselectedBatchId ?: 0L) }
    var batchDropdownExpanded by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var isReadingFile by remember { mutableStateOf(false) }
    var isValidating by remember { mutableStateOf(false) }

    // File picker for .json
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
            validationResult = viewModel.validateCurriculumJson(jsonText)
        } catch (t: Throwable) {
            validationResult = CurriculumValidationResult(
                isValid = false,
                errorMessage = "Parse error: ${t.localizedMessage ?: "Failed to parse JSON"}"
            )
        } finally {
            isValidating = false
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
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SquircleIconBadge(
                    icon = Icons.Default.Layers,
                    accentColor = MaterialTheme.colorScheme.primary,
                    size = 40.dp
                )
                Column {
                    Text(
                        text = "Import Batch & Subjects",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Import curriculum knowledge base from JSON",
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
                // Template Presets Chips
                Text(
                    text = "Load Example Template:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = jsonText == CurriculumJsonParser.EXAMPLE_BATCH_SUBJECTS_JSON,
                        onClick = {
                            jsonText = CurriculumJsonParser.EXAMPLE_BATCH_SUBJECTS_JSON
                            targetBatchId = 0L
                        },
                        label = { Text("Full Batch Template") }
                    )
                    FilterChip(
                        selected = jsonText == CurriculumJsonParser.EXAMPLE_SUBJECTS_ONLY_JSON,
                        onClick = {
                            jsonText = CurriculumJsonParser.EXAMPLE_SUBJECTS_ONLY_JSON
                            if (targetBatchId == 0L && batches.isNotEmpty()) {
                                targetBatchId = batches.first().id
                            }
                        },
                        label = { Text("Subjects Only") }
                    )
                }

                // Target Batch selector
                if (batches.isNotEmpty()) {
                    Text(
                        text = "Import Target Destination:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    ExposedDropdownMenuBox(
                        expanded = batchDropdownExpanded,
                        onExpandedChange = { batchDropdownExpanded = !batchDropdownExpanded }
                    ) {
                        val currentTargetText = if (targetBatchId == 0L) {
                            "Create New Batch from JSON"
                        } else {
                            val found = batches.find { it.id == targetBatchId }
                            "Import into: ${found?.name ?: "Batch $targetBatchId"}"
                        }

                        OutlinedTextField(
                            value = currentTargetText,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = batchDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = batchDropdownExpanded,
                            onDismissRequest = { batchDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("✨ Create New Batch (from JSON)") },
                                onClick = {
                                    targetBatchId = 0L
                                    batchDropdownExpanded = false
                                }
                            )
                            batches.forEach { b ->
                                DropdownMenuItem(
                                    text = { Text("📁 ${b.name} (${b.courseOrClass.ifBlank { "General" }})") },
                                    onClick = {
                                        targetBatchId = b.id
                                        batchDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // File Upload, Paste & Clear quick actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { filePicker.launch("application/json") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open File")
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) {
                                    jsonText = clip
                                    viewModel.showMessage("Pasted ${(clip.length / 1024).coerceAtLeast(1)} KB from clipboard")
                                } else {
                                    viewModel.showMessage("Clipboard is empty")
                                }
                            } catch (t: Throwable) {
                                viewModel.showMessage("Failed to paste: ${t.localizedMessage}")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Paste")
                    }

                    OutlinedButton(
                        onClick = {
                            jsonText = ""
                            validationResult = null
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear")
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
                            text = if (isReadingFile) "Reading large curriculum JSON from disk..." else "Analyzing curriculum structure in background...",
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
                        label = { Text("Curriculum JSON Code") },
                        placeholder = { Text("Paste your batch and subjects JSON here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp, max = 220.dp),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
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
                                        text = "Large Curriculum Payload Loaded",
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

                // Validation Status Card
                val res = validationResult
                if (res != null) {
                    if (res.isValid) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.12f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Valid Curriculum JSON",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = SuccessGreen
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                if (res.batch != null) {
                                    Text(
                                        text = "• Batch: ${res.batch.name} (${res.batch.courseOrClass.ifBlank { "Standard" }})",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Text(
                                    text = "• Subjects: ${res.subjects.size} (${res.subjects.joinToString { it.name }})",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "• Total Chapters: ${res.totalChaptersCount}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (res.totalNotesCount > 0 || res.totalFormulasCount > 0 || res.totalQuestionsCount > 0) {
                                    Text(
                                        text = "• Includes: ${res.totalNotesCount} notes, ${res.totalFormulasCount} formulas, ${res.totalQuestionsCount} questions",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
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
                                modifier = Modifier.padding(14.dp),
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
                        val targetId = if (targetBatchId > 0L) targetBatchId else null
                        viewModel.importCurriculum(validRes, targetId) { createdBatchId ->
                            isImporting = false
                            onSuccess(createdBatchId)
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
                    Text("Importing...")
                } else {
                    Text("Import to Knowledge Base")
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
