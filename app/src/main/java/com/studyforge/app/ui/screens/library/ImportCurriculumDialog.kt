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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val coroutineScope = rememberCoroutineScope()
    val batches by viewModel.batches.collectAsState()

    var rawJsonText by remember { mutableStateOf(CurriculumJsonParser.EXAMPLE_BATCH_SUBJECTS_JSON) }
    var loadedFileName by remember { mutableStateOf<String?>(null) }
    var isReadingFile by remember { mutableStateOf(false) }
    var isValidating by remember { mutableStateOf(false) }
    var validationResult by remember { mutableStateOf<CurriculumValidationResult?>(null) }
    var isImporting by remember { mutableStateOf(false) }

    // Target batch selection: 0L = Create new batch or auto-detect from JSON; > 0L = target existing batch
    var targetBatchId by remember { mutableStateOf(preselectedBatchId ?: 0L) }
    var batchDropdownExpanded by remember { mutableStateOf(false) }

    // File picker for .json with background Dispatchers.IO reading
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
                        loadedFileName = "Curriculum File (${content.length / 1024} KB)"
                        viewModel.showMessage("Curriculum file loaded (${content.length / 1024} KB). Validating...")
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

    // Auto-validate whenever rawJsonText changes (Debounced on background thread)
    LaunchedEffect(rawJsonText) {
        if (rawJsonText.isBlank()) {
            validationResult = null
            isValidating = false
            return@LaunchedEffect
        }
        isValidating = true
        delay(300)
        try {
            validationResult = viewModel.validateCurriculumJsonAsync(rawJsonText)
        } catch (t: Throwable) {
            validationResult = CurriculumValidationResult(
                isValid = false,
                errorMessage = if (t is OutOfMemoryError) "File is too large for device memory." else "Validation error: ${t.localizedMessage}"
            )
        } finally {
            isValidating = false
        }
    }

    val isLargeContent = rawJsonText.length > 4000 || loadedFileName != null
    val estimatedLineCount = remember(rawJsonText) {
        if (rawJsonText.isEmpty()) 0 else rawJsonText.count { it == '\n' } + 1
    }
    val safePreviewSnippet = remember(rawJsonText) {
        if (rawJsonText.isEmpty()) "" else rawJsonText.take(400).lines().take(4).joinToString("\n")
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
                        selected = !isLargeContent && rawJsonText == CurriculumJsonParser.EXAMPLE_BATCH_SUBJECTS_JSON,
                        onClick = {
                            rawJsonText = CurriculumJsonParser.EXAMPLE_BATCH_SUBJECTS_JSON
                            loadedFileName = null
                            targetBatchId = 0L
                        },
                        label = { Text("Full Batch Template") }
                    )
                    FilterChip(
                        selected = !isLargeContent && rawJsonText == CurriculumJsonParser.EXAMPLE_SUBJECTS_ONLY_JSON,
                        onClick = {
                            rawJsonText = CurriculumJsonParser.EXAMPLE_SUBJECTS_ONLY_JSON
                            loadedFileName = null
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
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
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

                // File Upload & Paste quick actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { filePicker.launch("application/json") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isReadingFile
                    ) {
                        if (isReadingFile) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isReadingFile) "Reading..." else "Open File", maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = {
                            val clip = clipboardManager.getText()?.text
                            if (!clip.isNullOrBlank()) {
                                if (clip.length > 4000) {
                                    loadedFileName = "Pasted Clipboard Content (${clip.length / 1024} KB)"
                                    viewModel.showMessage("Pasted large curriculum (${clip.length / 1024} KB). Validating...")
                                } else {
                                    loadedFileName = null
                                }
                                rawJsonText = clip
                            } else {
                                viewModel.showMessage("Clipboard is empty")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Paste", maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = {
                            rawJsonText = ""
                            loadedFileName = null
                            validationResult = null
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
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
                                        text = loadedFileName ?: "Large Curriculum Document Loaded",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${rawJsonText.length / 1024} KB • $estimatedLineCount lines ready for import",
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
                                loadedFileName = "Pasted Large Curriculum (${newText.length / 1024} KB)"
                            }
                            rawJsonText = newText
                        },
                        label = { Text("Curriculum JSON Code") },
                        placeholder = { Text("Paste your batch and subjects JSON here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp, max = 220.dp),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                }

                // Validation Status Card
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
                            Text("Validating curriculum structure...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else {
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
                                    val subNames = res.subjects.take(5).joinToString { it.name }
                                    Text(
                                        text = "• Subjects: ${res.subjects.size} ($subNames${if (res.subjects.size > 5) "..." else ""})",
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
                enabled = validationResult?.isValid == true && !isImporting && !isValidating && !isReadingFile,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isImporting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Importing...")
                } else if (isValidating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Validating...")
                } else {
                    Text("Import to Knowledge Base")
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
