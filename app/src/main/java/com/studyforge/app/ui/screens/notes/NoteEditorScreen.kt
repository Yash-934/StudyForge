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
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.studyforge.app.domain.pdf.NotePdfExporter
import com.studyforge.app.ui.components.LocalReaderStyle
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

    var activeNoteId by remember(noteId) { mutableStateOf(noteId) }
    val chapterNotes = remember(notes, chapterId) { notes.filter { it.chapterId == chapterId } }

    val activeNote = remember(activeNoteId, notes) {
        if (activeNoteId > 0L) notes.find { it.id == activeNoteId } else null
    }

    // Default to Preview mode when opening existing notes, Edit mode only when creating a new note
    var isEditing by remember(activeNoteId) { mutableStateOf(activeNoteId <= 0L) }
    var title by remember(activeNoteId) { mutableStateOf(activeNote?.title ?: "") }
    var contentMarkdown by remember(activeNoteId) { mutableStateOf(activeNote?.contentMarkdown ?: "") }
    var isFavorite by remember(activeNoteId) { mutableStateOf(activeNote?.isFavorite ?: false) }
    var isPinned by remember(activeNoteId) { mutableStateOf(activeNote?.isPinned ?: false) }
    var showAiDialog by remember { mutableStateOf(false) }
    var showNoteListMenu by remember { mutableStateOf(false) }
    var showMoreActionsMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Reader Customization, Annotations & Listen Note (TTS) State
    val readerPrefs = rememberReaderPreferences(context)
    var showAppearancePanel by remember { mutableStateOf(false) }
    var showAnnotationBar by remember { mutableStateOf(false) }
    var activeAnnotationTool by remember { mutableStateOf(AnnotationTool.HIGHLIGHT_YELLOW) }
    var wordSelectionMode by remember { mutableStateOf(false) }
    var wordPickerNodeIndex by remember { mutableIntStateOf(-1) }
    var showListenBar by remember { mutableStateOf(false) }
    var currentSpokenIndex by remember { mutableIntStateOf(-1) }
    val readerListState = rememberLazyListState()

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

    val pdfExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val res = NotePdfExporter.exportNoteToPdfUri(
                    context = context,
                    outputUri = uri,
                    noteTitle = title.ifBlank { "StudyForge Note" },
                    chapterName = chapter?.name ?: "Chapter",
                    markdownContent = contentMarkdown
                )
                if (res.isSuccess) {
                    viewModel.showMessage("Note exported as PDF successfully! 📄")
                } else {
                    viewModel.showMessage("PDF export failed: ${res.exceptionOrNull()?.message}")
                }
            }
        }
    }

    LaunchedEffect(activeNoteId, activeNote?.id) {
        if (activeNote != null && !isEditing) {
            title = activeNote.title
            contentMarkdown = activeNote.contentMarkdown
            isFavorite = activeNote.isFavorite
            isPinned = activeNote.isPinned
        }
    }

    // Handle back button: if in edit mode for existing note, return to preview mode
    BackHandler(enabled = isEditing && activeNoteId > 0L) {
        isEditing = false
    }

    val onSaveNote = {
        val saveId = if (activeNoteId > 0L) activeNoteId else 0L
        viewModel.saveNote(
            id = saveId,
            chapterId = chapterId,
            subjectId = chapter?.subjectId ?: 1L,
            batchId = chapter?.batchId ?: 1L,
            title = title.ifBlank { "Untitled Note" },
            contentMarkdown = contentMarkdown,
            isFavorite = isFavorite,
            isPinned = isPinned
        )
        if (activeNoteId > 0L) {
            isEditing = false
        } else {
            onBack()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    if (isEditing) {
                        Text(
                            text = if (activeNote != null) "Edit Note" else "New Note",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = 22.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = "Back",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isEditing && activeNoteId > 0L) {
                                isEditing = false
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isEditing && activeNoteId > 0L) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Quick Switcher between notes in this chapter
                    if (chapterNotes.isNotEmpty()) {
                        Box {
                            Surface(
                                onClick = { showNoteListMenu = true },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = "📚", fontSize = 16.sp)
                                }
                            }
                            DropdownMenu(
                                expanded = showNoteListMenu,
                                onDismissRequest = { showNoteListMenu = false }
                            ) {
                                Text(
                                    text = "Chapter Notes (${chapterNotes.size})",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                                HorizontalDivider()
                                chapterNotes.forEach { n ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = n.title.ifBlank { "Untitled Note" },
                                                style = if (n.id == activeNoteId) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        leadingIcon = {
                                            if (n.id == activeNoteId) {
                                                Icon(Icons.Default.Check, contentDescription = "Active", tint = MaterialTheme.colorScheme.primary)
                                            } else {
                                                Text("📓", fontSize = 15.sp)
                                            }
                                        },
                                        onClick = {
                                            activeNoteId = n.id
                                            isEditing = false
                                            showNoteListMenu = false
                                        }
                                    )
                                }
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("+ Create New Note", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = "New Note", tint = MaterialTheme.colorScheme.primary) },
                                    onClick = {
                                        activeNoteId = -1L
                                        title = ""
                                        contentMarkdown = ""
                                        isFavorite = false
                                        isPinned = false
                                        isEditing = true
                                        showNoteListMenu = false
                                    }
                                )
                            }
                        }
                    }

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
                        if (activeNoteId > 0L) {
                            IconButton(onClick = { isEditing = false }) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "Preview Note",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        IconButton(onClick = { onSaveNote() }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save Note",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        // 1. Annotate (Highlight & Underline)
                        Surface(
                            onClick = {
                                showAnnotationBar = !showAnnotationBar
                                if (showAnnotationBar) showAppearancePanel = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (showAnnotationBar) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            },
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "🖍️", fontSize = 15.sp)
                            }
                        }

                        // 2. Reading Theme, Font & Text Size (Aa)
                        Surface(
                            onClick = {
                                showAppearancePanel = !showAppearancePanel
                                if (showAppearancePanel) showAnnotationBar = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (showAppearancePanel) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            },
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Aa",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = if (showAppearancePanel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // 3. Listen Note (Text-to-Speech Audio Reader)
                        Surface(
                            onClick = { showListenBar = !showListenBar },
                            shape = RoundedCornerShape(10.dp),
                            color = if (showListenBar) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            },
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "🔊", fontSize = 15.sp)
                            }
                        }

                        // 4. AI Study Assistant
                        Surface(
                            onClick = { showAiDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "✨", fontSize = 15.sp)
                            }
                        }

                        // 5. Edit Note
                        Surface(
                            onClick = { isEditing = true },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "✏️", fontSize = 14.sp)
                            }
                        }

                        Box {
                            IconButton(onClick = { showMoreActionsMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More Options"
                                )
                            }
                            DropdownMenu(
                                expanded = showMoreActionsMenu,
                                onDismissRequest = { showMoreActionsMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Highlight & Underline Tools") },
                                    leadingIcon = { Text(text = "🖍️", fontSize = 15.sp) },
                                    onClick = {
                                        showMoreActionsMenu = false
                                        showAnnotationBar = true
                                        showAppearancePanel = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Reading Theme, Font & Size") },
                                    leadingIcon = { Text(text = "🔤", fontSize = 15.sp) },
                                    onClick = {
                                        showMoreActionsMenu = false
                                        showAppearancePanel = true
                                        showAnnotationBar = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (showListenBar) "Stop Listen Note" else "Listen Note (Audio TTS)") },
                                    leadingIcon = { Text(text = "🔊", fontSize = 15.sp) },
                                    onClick = {
                                        showMoreActionsMenu = false
                                        showListenBar = !showListenBar
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Export Note as PDF") },
                                    leadingIcon = {
                                        Text(text = "📄", fontSize = 15.sp)
                                    },
                                    onClick = {
                                        showMoreActionsMenu = false
                                        val safeFileName = title.ifBlank { "StudyForge_Note" }
                                            .replace(Regex("[^a-zA-Z0-9_\\- ]"), "")
                                            .trim()
                                            .replace(" ", "_") + ".pdf"
                                        pdfExportLauncher.launch(safeFileName)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (isPinned) "Unpin Note" else "Pin Note") },
                                    leadingIcon = {
                                        Text(text = "📌", fontSize = 15.sp)
                                    },
                                    onClick = {
                                        isPinned = !isPinned
                                        showMoreActionsMenu = false
                                        if (activeNoteId > 0L) {
                                            viewModel.saveNote(
                                                id = activeNoteId,
                                                chapterId = chapterId,
                                                subjectId = chapter?.subjectId ?: 1L,
                                                batchId = chapter?.batchId ?: 1L,
                                                title = title.ifBlank { "Untitled Note" },
                                                contentMarkdown = contentMarkdown,
                                                isFavorite = isFavorite,
                                                isPinned = isPinned
                                            )
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (isFavorite) "Remove Favorite" else "Mark Favorite") },
                                    leadingIcon = {
                                        Text(text = if (isFavorite) "❤️" else "🤍", fontSize = 15.sp)
                                    },
                                    onClick = {
                                        isFavorite = !isFavorite
                                        showMoreActionsMenu = false
                                        if (activeNoteId > 0L) {
                                            viewModel.saveNote(
                                                id = activeNoteId,
                                                chapterId = chapterId,
                                                subjectId = chapter?.subjectId ?: 1L,
                                                batchId = chapter?.batchId ?: 1L,
                                                title = title.ifBlank { "Untitled Note" },
                                                contentMarkdown = contentMarkdown,
                                                isFavorite = isFavorite,
                                                isPinned = isPinned
                                            )
                                        }
                                    }
                                )
                                if (activeNoteId > 0L) {
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("Delete Note", color = MaterialTheme.colorScheme.error) },
                                        leadingIcon = { Text(text = "🗑️", fontSize = 15.sp) },
                                        onClick = {
                                            showMoreActionsMenu = false
                                            showDeleteDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (isEditing) {
                FloatingActionButton(
                    onClick = { onSaveNote() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Save Note")
                }
            } else {
                // In full screen preview mode, an edit action button
                ExtendedFloatingActionButton(
                    onClick = { isEditing = true },
                    icon = { Text("✏️", fontSize = 15.sp) },
                    text = { Text("Edit Note", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
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
            // DIRECT FULL SCREEN PREVIEW MODE (Clean 120 FPS virtualized reader with custom Themes, Fonts, Size, Annotations & TTS)
            val parsedNodes = remember(contentMarkdown) {
                parseMarkdownToNodes(contentMarkdown.ifBlank { "*No content entered yet.*" })
            }
            val styleConfig = readerPrefs.toStyleConfig()
            val sheetColor = readerPrefs.theme.sheetColor
            val readerBgColor = readerPrefs.theme.backgroundColor

            // Auto-scroll to the active spoken paragraph when Listen Note TTS advances
            LaunchedEffect(currentSpokenIndex) {
                if (currentSpokenIndex >= 0 && currentSpokenIndex < parsedNodes.size) {
                    readerListState.animateScrollToItem((currentSpokenIndex + 1).coerceAtLeast(0))
                }
            }

            fun persistUpdatedMarkdown(updatedMd: String) {
                if (updatedMd != contentMarkdown) {
                    contentMarkdown = updatedMd
                    if (activeNoteId > 0L) {
                        viewModel.saveNote(
                            id = activeNoteId,
                            chapterId = chapterId,
                            subjectId = chapter?.subjectId ?: 1L,
                            batchId = chapter?.batchId ?: 1L,
                            title = title.ifBlank { "Untitled Note" },
                            contentMarkdown = updatedMd,
                            isFavorite = isFavorite,
                            isPinned = isPinned
                        )
                    }
                }
            }

            CompositionLocalProvider(LocalReaderStyle provides styleConfig) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(readerBgColor)
                        .padding(padding)
                ) {
                    // Collapsible Reader Appearance Panel (Theme, Font, Text Size)
                    ReaderAppearancePanel(
                        visible = showAppearancePanel,
                        readerPrefs = readerPrefs,
                        onClose = { showAppearancePanel = false }
                    )

                    // Collapsible Interactive Annotation Dock (Highlight Colors, Underline, Clear)
                    ReaderAnnotationBar(
                        visible = showAnnotationBar,
                        activeTool = activeAnnotationTool,
                        onSelectTool = { activeAnnotationTool = it },
                        wordSelectionMode = wordSelectionMode,
                        onToggleWordSelectionMode = { wordSelectionMode = it },
                        onClose = { showAnnotationBar = false }
                    )

                    // Collapsible Listen Note Audio TTS Bar
                    ListenNotePlayerBar(
                        visible = showListenBar,
                        context = context,
                        parsedNodes = parsedNodes,
                        noteTitle = title,
                        readerPrefs = readerPrefs,
                        currentSpokenIndex = currentSpokenIndex,
                        onSpokenIndexChange = { currentSpokenIndex = it },
                        onClose = { showListenBar = false }
                    )

                    LazyColumn(
                        state = readerListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        item(key = "note_title_header") {
                            Surface(
                                color = sheetColor,
                                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 8.dp)
                                ) {
                                    Text(
                                        text = title.ifBlank { "Untitled Note" },
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = (22f * readerPrefs.fontScale).sp,
                                            lineHeight = (31f * readerPrefs.fontScale).sp,
                                            fontFamily = readerPrefs.fontChoice.fontFamily
                                        ),
                                        color = readerPrefs.theme.textColor,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 3.dp, bottom = 2.dp)
                                    )

                                    if (chapter != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = readerPrefs.theme.accentColor.copy(alpha = 0.14f)
                                        ) {
                                            Text(
                                                text = "📓 ${chapter.name}",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    lineHeight = 18.sp
                                                ),
                                                color = readerPrefs.theme.accentColor,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(
                                        thickness = 1.2.dp,
                                        color = readerPrefs.theme.textColor.copy(alpha = 0.14f)
                                    )
                                }
                            }
                        }

                        items(
                            count = parsedNodes.size,
                            key = { index ->
                                val node = parsedNodes[index]
                                "${node.javaClass.simpleName}_${index}_${node.hashCode()}"
                            }
                        ) { index ->
                            val node = parsedNodes[index]
                            val isSpokenActive = currentSpokenIndex == index
                            val isAnnotatable = showAnnotationBar &&
                                activeAnnotationTool != AnnotationTool.NONE &&
                                extractNodeAnchorText(node).isNotBlank()

                            Surface(
                                color = sheetColor,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 2.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .then(
                                            if (isSpokenActive) {
                                                Modifier
                                                    .background(readerPrefs.theme.accentColor.copy(alpha = 0.12f))
                                                    .border(
                                                        1.5.dp,
                                                        readerPrefs.theme.accentColor,
                                                        RoundedCornerShape(10.dp)
                                                    )
                                            } else if (isAnnotatable) {
                                                Modifier.clickable {
                                                    if (wordSelectionMode && activeAnnotationTool != AnnotationTool.ERASER) {
                                                        wordPickerNodeIndex = index
                                                    } else {
                                                        val updated = applyAnnotationToMarkdown(
                                                            contentMarkdown = contentMarkdown,
                                                            nodeIndex = index,
                                                            parsedNodes = parsedNodes,
                                                            tool = activeAnnotationTool
                                                        )
                                                        persistUpdatedMarkdown(updated)
                                                    }
                                                }
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    RenderMarkdownNode(node)
                                }
                            }
                        }

                        item(key = "note_sheet_footer") {
                            Surface(
                                color = sheetColor,
                                shape = RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(22.dp)
                            ) {}
                        }

                        item(key = "note_bottom_spacer") {
                            Spacer(modifier = Modifier.height(88.dp))
                        }
                    }
                }
            }

            if (wordPickerNodeIndex in parsedNodes.indices) {
                val targetAnchor = extractNodeAnchorText(parsedNodes[wordPickerNodeIndex])
                if (targetAnchor.isNotBlank()) {
                    WordAnnotationPickerDialog(
                        rawBlockText = targetAnchor,
                        activeTool = activeAnnotationTool,
                        onApplyFullLine = {
                            val idx = wordPickerNodeIndex
                            wordPickerNodeIndex = -1
                            val updated = applyAnnotationToMarkdown(
                                contentMarkdown = contentMarkdown,
                                nodeIndex = idx,
                                parsedNodes = parsedNodes,
                                tool = activeAnnotationTool
                            )
                            persistUpdatedMarkdown(updated)
                        },
                        onApplyWordRange = { phrase ->
                            val idx = wordPickerNodeIndex
                            wordPickerNodeIndex = -1
                            val updated = applyAnnotationToMarkdown(
                                contentMarkdown = contentMarkdown,
                                nodeIndex = idx,
                                parsedNodes = parsedNodes,
                                tool = activeAnnotationTool,
                                selectedPhrase = phrase
                            )
                            persistUpdatedMarkdown(updated)
                        },
                        onDismiss = { wordPickerNodeIndex = -1 }
                    )
                } else {
                    wordPickerNodeIndex = -1
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
                    id = if (activeNoteId > 0L) activeNoteId else 0L,
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

    if (showDeleteDialog && activeNote != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete '${activeNote.title.ifBlank { "this note" }}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteNote(activeNote)
                        onBack()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
