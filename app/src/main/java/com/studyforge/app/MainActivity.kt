package com.studyforge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.studyforge.app.ui.screens.library.LibraryScreen
import com.studyforge.app.ui.screens.questions.QuestionBankScreen
import com.studyforge.app.ui.screens.revision.ChapterRevisionScreen
import com.studyforge.app.ui.screens.settings.SettingsScreen
import com.studyforge.app.ui.theme.StudyForgeTheme
import com.studyforge.app.viewmodel.StudyViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: StudyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyForgeTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: StudyViewModel) {
    val context = LocalContext.current

    val batches by viewModel.batches.collectAsState()
    val subjects by viewModel.currentSubjects.collectAsState()
    val chapters by viewModel.currentChapters.collectAsState()
    val questions by viewModel.currentQuestions.collectAsState()

    val selectedBatchId by viewModel.selectedBatchId.collectAsState()
    val selectedSubjectId by viewModel.selectedSubjectId.collectAsState()
    val selectedChapterId by viewModel.selectedChapterId.collectAsState()

    val isImporting by viewModel.isImporting.collectAsState()
    val importProgress by viewModel.importProgressMessage.collectAsState()
    val importError by viewModel.importError.collectAsState()
    val importSuccess by viewModel.importSuccess.collectAsState()

    // Navigation state
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Q-Bank, 1: Curriculum, 2: Settings

    // Revision Screen State
    var revisingChapter by remember { mutableStateOf<Pair<Long, String>?>(null) }

    if (revisingChapter != null) {
        val (chId, chName) = revisingChapter!!
        val chapterQuestions = questions.filter { it.chapterId == chId }

        ChapterRevisionScreen(
            chapterName = chName,
            questions = chapterQuestions,
            onToggleBookmark = { viewModel.toggleBookmark(it) },
            onBack = { revisingChapter = null }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.QuestionAnswer, contentDescription = "Q-Bank") },
                    label = { Text("Q-Bank") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.AutoStories, contentDescription = "Curriculum") },
                    label = { Text("Curriculum") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> {
                QuestionBankScreen(
                    batches = batches,
                    subjects = subjects,
                    chapters = chapters,
                    questions = questions,
                    selectedBatchId = selectedBatchId,
                    selectedSubjectId = selectedSubjectId,
                    selectedChapterId = selectedChapterId,
                    isImporting = isImporting,
                    importProgress = importProgress,
                    importError = importError,
                    importSuccess = importSuccess,
                    onSelectBatch = { viewModel.selectBatch(it) },
                    onSelectSubject = { viewModel.selectSubject(it) },
                    onSelectChapter = { viewModel.selectChapter(it) },
                    onToggleBookmark = { viewModel.toggleBookmark(it) },
                    onStartRevision = { id, name -> revisingChapter = Pair(id, name) },
                    onImportRawJson = { targetBatchId, rawJson ->
                        viewModel.importRawJson(targetBatchId, rawJson)
                    },
                    onImportFileUri = { targetBatchId, uri ->
                        viewModel.importFromUri(context, targetBatchId, uri)
                    },
                    onClearImportStatus = { viewModel.clearImportStatus() }
                )
            }
            1 -> {
                LibraryScreen(
                    batches = batches,
                    subjects = subjects,
                    chapters = chapters,
                    selectedBatchId = selectedBatchId,
                    selectedSubjectId = selectedSubjectId,
                    onSelectBatch = { viewModel.selectBatch(it) },
                    onSelectSubject = { viewModel.selectSubject(it) },
                    onStartRevision = { id, name -> revisingChapter = Pair(id, name) },
                    onCreateBatch = { name, desc -> viewModel.createBatch(name, desc) },
                    onCreateSubject = { name -> viewModel.createSubject(name) },
                    onCreateChapter = { name -> viewModel.createChapter(name) }
                )
            }
            2 -> {
                SettingsScreen(
                    onExportJson = { viewModel.exportBackupJson() },
                    onImportRawJson = { rawJson -> viewModel.importRawJson(null, rawJson) },
                    onBack = { selectedTab = 0 }
                )
            }
        }
    }
}
