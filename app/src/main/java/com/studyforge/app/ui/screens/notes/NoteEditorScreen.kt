package com.studyforge.app.ui.screens.notes

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.studyforge.app.ui.components.MarkdownMathView
import com.studyforge.app.ui.components.MathEditorToolbar
import com.studyforge.app.ui.components.RenderMarkdownNode
import com.studyforge.app.ui.components.parseMarkdownToNodes
import com.studyforge.app.ui.screens.ai.AiAssistantDialog
import com.studyforge.app.viewmodel.StudyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    chapterId: Long,
    noteId: Long,
    viewModel: StudyViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val notes by viewModel.notes.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val chapter = chapters.find { it.id == chapterId }

    val existingNote = remember(noteId, notes) {
        if (noteId > 0) notes.find { it.id == noteId } else null
    }

    // Default to Preview mode when opening existing notes, Edit mode only when creating a new note
    var isEditing by remember(noteId) { mutableStateOf(noteId <= 0L) }
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var contentMarkdown by remember { mutableStateOf(existingNote?.contentMarkdown ?: "") }
    var isFavorite by remember { mutableStateOf(existingNote?.isFavorite ?: false) }
    var isPinned by remember { mutableStateOf(existingNote?.isPinned ?: false) }
    var showAiDialog by remember { mutableStateOf(false) }

    // Zero-permission Photo Picker for importing images from device gallery
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val localFile = withContext(Dispatchers.IO) {
                        val imagesDir = File(context.filesDir, "note_images").apply { mkdirs() }
                        val destFile = File(imagesDir, "img_${System.currentTimeMillis()}.jpg")
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            FileOutputStream(destFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        destFile
                    }
                    val imageMarkdownTag = "\n\n![Imported Diagram](file://${localFile.absolutePath})\n\n"
                    contentMarkdown += imageMarkdownTag
                    Toast.makeText(context, "Image attached from gallery!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to import image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(existingNote) {
        if (existingNote != null) {
            title = existingNote.title
            contentMarkdown = existingNote.contentMarkdown
            isFavorite = existingNote.isFavorite
            isPinned = existingNote.isPinned
        }
    }

    // Handle back button: if in edit mode for existing note, return to preview mode
    BackHandler(enabled = isEditing && noteId > 0L) {
        isEditing = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) {
                            if (existingNote != null) "Edit Note" else "New Note"
                        } else {
                            title.ifBlank { "Note" }
                        },
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isEditing && noteId > 0L) {
                                isEditing = false
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isEditing && noteId > 0L) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Import Gallery Image",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (noteId > 0L) {
                            IconButton(onClick = { isEditing = false }) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "Preview Note",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                viewModel.saveNote(
                                    id = existingNote?.id ?: 0L,
                                    chapterId = chapterId,
                                    subjectId = chapter?.subjectId ?: 1L,
                                    batchId = chapter?.batchId ?: 1L,
                                    title = title.ifBlank { "Untitled Note" },
                                    contentMarkdown = contentMarkdown,
                                    isFavorite = isFavorite,
                                    isPinned = isPinned
                                )
                                if (noteId > 0L) {
                                    isEditing = false
                                } else {
                                    onBack()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save Note",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        // Full-screen Preview mode actions
                        IconButton(onClick = { isPinned = !isPinned }) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pin",
                                tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                        IconButton(onClick = { isFavorite = !isFavorite }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                            )
                        }
                        IconButton(onClick = { showAiDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Assistant",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { isEditing = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Note",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (isEditing) {
                FloatingActionButton(
                    onClick = {
                        viewModel.saveNote(
                            id = existingNote?.id ?: 0L,
                            chapterId = chapterId,
                            subjectId = chapter?.subjectId ?: 1L,
                            batchId = chapter?.batchId ?: 1L,
                            title = title.ifBlank { "Untitled Note" },
                            contentMarkdown = contentMarkdown,
                            isFavorite = isFavorite,
                            isPinned = isPinned
                        )
                        if (noteId > 0L) {
                            isEditing = false
                        } else {
                            onBack()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Save Note")
                }
            } else {
                // In full screen preview mode, an edit action button
                ExtendedFloatingActionButton(
                    onClick = { isEditing = true },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    text = { Text("Edit Note") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        if (isEditing) {
            // EDIT MODE
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
            ) {
                // Math & Formatting quick insertion toolbar with Gallery Image Picker
                MathEditorToolbar(
                    onInsertText = { toInsert ->
                        contentMarkdown += toInsert
                    },
                    onPickImage = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Note Title...") },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = contentMarkdown,
                        onValueChange = { contentMarkdown = it },
                        placeholder = {
                            Text("Write notes using Markdown, Tables, and LaTeX...\n\nExample:\n# 4.1 Mathematical Formulation\n$$\\Delta W_B = \\int_a^b f(x) dx$$\n\n| Field | Value |\n|---|---|\n| Author | Quant Team |\n\n[IMPORTANT]\nRemember integration constant +C.")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        } else {
            // DIRECT FULL SCREEN PREVIEW MODE (Clean 120 FPS virtualized reader)
            val parsedNodes = remember(contentMarkdown) {
                parseMarkdownToNodes(contentMarkdown.ifBlank { "*No content entered yet.*" })
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item(key = "note_title_header") {
                    Text(
                        text = title.ifBlank { "Untitled Note" },
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (chapter != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = chapter.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                items(
                    count = parsedNodes.size,
                    key = { index -> index }
                ) { index ->
                    RenderMarkdownNode(parsedNodes[index])
                }

                item(key = "note_bottom_spacer") {
                    Spacer(modifier = Modifier.height(88.dp))
                }
            }
        }
    }

    if (showAiDialog) {
        AiAssistantDialog(
            title = title.ifBlank { "Note" },
            contextText = contentMarkdown,
            viewModel = viewModel,
            chapterId = chapterId,
            subjectId = chapter?.subjectId ?: 1L,
            batchId = chapter?.batchId ?: 1L,
            onInsertIntoNote = { insertedContent ->
                val newContent = contentMarkdown + insertedContent
                contentMarkdown = newContent
                viewModel.saveNote(
                    id = existingNote?.id ?: 0L,
                    chapterId = chapterId,
                    subjectId = chapter?.subjectId ?: 1L,
                    batchId = chapter?.batchId ?: 1L,
                    title = title.ifBlank { "Untitled Note" },
                    contentMarkdown = newContent,
                    isFavorite = isFavorite,
                    isPinned = isPinned
                )
            },
            onDismiss = { showAiDialog = false }
        )
    }
}
