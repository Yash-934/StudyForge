package com.studyforge.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.studyforge.app.ui.screens.analytics.AnalyticsScreen
import com.studyforge.app.ui.screens.chapter.ChapterDetailScreen
import com.studyforge.app.ui.screens.flashcards.FlashcardsScreen
import com.studyforge.app.ui.screens.formula.FormulaVaultScreen
import com.studyforge.app.ui.screens.home.HomeScreen
import com.studyforge.app.ui.screens.jsonimport.JsonImportScreen
import com.studyforge.app.ui.screens.jsonimport.JsonSchemaHelpScreen
import com.studyforge.app.ui.screens.library.LibraryScreen
import com.studyforge.app.ui.screens.mistakes.MistakeBookScreen
import com.studyforge.app.ui.screens.notes.NoteEditorScreen
import com.studyforge.app.ui.screens.planner.PlannerScreen
import com.studyforge.app.ui.screens.practice.PracticeHubScreen
import com.studyforge.app.ui.screens.questions.QuestionBankScreen
import com.studyforge.app.ui.screens.questions.QuestionEditorScreen
import com.studyforge.app.ui.screens.revision.RevisionScreen
import com.studyforge.app.ui.screens.search.GlobalSearchScreen
import com.studyforge.app.ui.screens.settings.HowToUseScreen
import com.studyforge.app.ui.screens.settings.SettingsScreen
import com.studyforge.app.ui.screens.subject.SubjectDetailScreen
import com.studyforge.app.ui.screens.test.SmartTestCreateScreen
import com.studyforge.app.ui.screens.test.TestCreateScreen
import com.studyforge.app.ui.screens.test.TestListScreen
import com.studyforge.app.ui.screens.test.TestPlayerScreen
import com.studyforge.app.ui.screens.test.TestResultScreen
import com.studyforge.app.ui.screens.test.TestReviewScreen
import com.studyforge.app.viewmodel.StudyViewModel

@Composable
fun AppNavigation(
    viewModel: StudyViewModel,
    navController: NavHostController = rememberNavController()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Listen for toast/messages
    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val bottomNavRoutes = listOf(
        Screen.Home.route,
        Screen.Practice.route,
        Screen.Library.route,
        Screen.Analytics.route,
        Screen.Planner.route,
        Screen.Settings.route
    )
    val shouldShowBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (shouldShowBottomBar) {
                Surface(
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.navigationBarsPadding()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val items = remember {
                                listOf(
                                    Triple(Screen.Home.route, Icons.Default.Home, "Home"),
                                    Triple(Screen.Practice.route, Icons.Default.PlayCircle, "Practice"),
                                    Triple(Screen.Library.route, Icons.Default.Folder, "Library"),
                                    Triple(Screen.Analytics.route, Icons.Default.QueryStats, "Stats"),
                                    Triple(Screen.Planner.route, Icons.Default.CalendarMonth, "Planner"),
                                    Triple(Screen.Settings.route, Icons.Default.Settings, "Settings")
                                )
                            }

                            items.forEach { (route, icon, label) ->
                                val selected = currentRoute == route
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            if (currentRoute != route) {
                                                navController.navigate(route) {
                                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(46.dp)
                                                .height(28.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(
                                                    if (selected) MaterialTheme.colorScheme.primaryContainer
                                                    else Color.Transparent
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = label,
                                                modifier = Modifier.size(20.dp),
                                                tint = if (selected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
                                            ),
                                            color = if (selected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
            // Main Bottom Bar Screens
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToChapter = { cId -> navController.navigate(Screen.ChapterDetail.createRoute(cId)) },
                    onNavigateToNoteEditor = { cId -> navController.navigate(Screen.NoteEditor.createRoute(cId, -1L)) },
                    onNavigateToQuestionEditor = { cId -> navController.navigate(Screen.QuestionEditor.createRoute(cId, -1L)) },
                    onNavigateToTestCreate = { cId -> navController.navigate(Screen.TestCreate.createRoute(cId)) },
                    onNavigateToSmartTest = { cId -> navController.navigate(Screen.SmartTestCreate.createRoute(cId)) },
                    onNavigateToJsonImport = { navController.navigate(Screen.JsonImport.route) },
                    onNavigateToRevision = { navController.navigate(Screen.RevisionQueue.route) },
                    onNavigateToMistakes = { cId -> navController.navigate(Screen.MistakeBook.createRoute(cId)) },
                    onNavigateToSearch = { navController.navigate(Screen.GlobalSearch.route) },
                    onNavigateToPractice = { navController.navigate(Screen.Practice.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            composable(Screen.Practice.route) {
                PracticeHubScreen(
                    viewModel = viewModel,
                    onNavigateToSmartTest = { cId -> navController.navigate(Screen.SmartTestCreate.createRoute(cId)) },
                    onNavigateToMistakes = { cId -> navController.navigate(Screen.MistakeBook.createRoute(cId)) },
                    onNavigateToRevision = { navController.navigate(Screen.RevisionQueue.route) },
                    onNavigateToFlashcards = { cId -> navController.navigate(Screen.Flashcards.createRoute(cId)) },
                    onNavigateToFormulaVault = { cId -> navController.navigate(Screen.FormulaVault.createRoute(cId, -1L)) },
                    onNavigateToTests = { cId -> navController.navigate(Screen.TestList.createRoute(cId)) },
                    onNavigateToQuestionBank = { cId -> navController.navigate(Screen.QuestionBank.createRoute(cId, -1L)) },
                    onNavigateToResult = { aId -> navController.navigate(Screen.TestResult.createRoute(aId)) },
                    onNavigateToTestPlayer = { tId -> navController.navigate(Screen.TestPlayer.createRoute(tId)) }
                )
            }

            composable(Screen.Library.route) {
                LibraryScreen(
                    viewModel = viewModel,
                    onNavigateToSubject = { sId -> navController.navigate(Screen.SubjectDetail.createRoute(sId)) },
                    onNavigateToChapter = { cId -> navController.navigate(Screen.ChapterDetail.createRoute(cId)) }
                )
            }

            composable(Screen.Analytics.route) {
                AnalyticsScreen(
                    viewModel = viewModel,
                    onNavigateToTestPlayer = { testId -> navController.navigate(Screen.TestPlayer.createRoute(testId)) }
                )
            }

            // Subject Detail
            composable(
                route = Screen.SubjectDetail.route,
                arguments = listOf(navArgument("subjectId") { type = NavType.LongType })
            ) { backStack ->
                val sId = backStack.arguments?.getLong("subjectId") ?: 1L
                SubjectDetailScreen(
                    subjectId = sId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToChapter = { cId -> navController.navigate(Screen.ChapterDetail.createRoute(cId)) },
                    onNavigateToNoteEditor = { chId, nId -> navController.navigate(Screen.NoteEditor.createRoute(chId, nId)) }
                )
            }

            // Chapter Detail
            composable(
                route = Screen.ChapterDetail.route,
                arguments = listOf(navArgument("chapterId") { type = NavType.LongType })
            ) { backStack ->
                val cId = backStack.arguments?.getLong("chapterId") ?: 1L
                ChapterDetailScreen(
                    chapterId = cId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToNoteEditor = { chId, nId -> navController.navigate(Screen.NoteEditor.createRoute(chId, nId)) },
                    onNavigateToFormulaVault = { chId -> navController.navigate(Screen.FormulaVault.createRoute(chId, -1L)) },
                    onNavigateToQuestionBank = { chId -> navController.navigate(Screen.QuestionBank.createRoute(chId, -1L)) },
                    onNavigateToQuestionEditor = { chId -> navController.navigate(Screen.QuestionEditor.createRoute(chId, -1L)) },
                    onNavigateToTests = { chId -> navController.navigate(Screen.TestList.createRoute(chId)) },
                    onNavigateToTestCreate = { chId -> navController.navigate(Screen.TestCreate.createRoute(chId)) },
                    onNavigateToSmartTest = { chId -> navController.navigate(Screen.SmartTestCreate.createRoute(chId)) },
                    onNavigateToMistakes = { chId -> navController.navigate(Screen.MistakeBook.createRoute(chId)) },
                    onNavigateToChapterRevision = { chId -> navController.navigate(Screen.ChapterRevision.createRoute(chId)) },
                    onNavigateToFlashcards = { chId -> navController.navigate(Screen.Flashcards.createRoute(chId)) }
                )
            }

            // Note Editor
            composable(
                route = Screen.NoteEditor.route,
                arguments = listOf(
                    navArgument("chapterId") { type = NavType.LongType },
                    navArgument("noteId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) { backStack ->
                val cId = backStack.arguments?.getLong("chapterId") ?: 1L
                val nId = backStack.arguments?.getLong("noteId") ?: -1L
                NoteEditorScreen(
                    chapterId = cId,
                    noteId = nId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Formula Vault
            composable(
                route = Screen.FormulaVault.route,
                arguments = listOf(
                    navArgument("chapterId") { type = NavType.LongType; defaultValue = -1L },
                    navArgument("subjectId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) { backStack ->
                val cId = backStack.arguments?.getLong("chapterId") ?: -1L
                val sId = backStack.arguments?.getLong("subjectId") ?: -1L
                FormulaVaultScreen(
                    chapterId = cId,
                    subjectId = sId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Question Bank
            composable(
                route = Screen.QuestionBank.route,
                arguments = listOf(
                    navArgument("chapterId") { type = NavType.LongType; defaultValue = -1L },
                    navArgument("subjectId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) { backStack ->
                val cId = backStack.arguments?.getLong("chapterId") ?: -1L
                val sId = backStack.arguments?.getLong("subjectId") ?: -1L
                QuestionBankScreen(
                    chapterId = cId,
                    subjectId = sId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToCreateQuestion = { chId -> navController.navigate(Screen.QuestionEditor.createRoute(chId, -1L)) },
                    onNavigateToEditQuestion = { chId, qId -> navController.navigate(Screen.QuestionEditor.createRoute(chId, qId)) },
                    onNavigateToJsonImport = { navController.navigate(Screen.JsonImport.route) }
                )
            }

            // Question Editor
            composable(
                route = Screen.QuestionEditor.route,
                arguments = listOf(
                    navArgument("chapterId") { type = NavType.LongType },
                    navArgument("questionId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) { backStack ->
                val cId = backStack.arguments?.getLong("chapterId") ?: 1L
                val qId = backStack.arguments?.getLong("questionId") ?: -1L
                QuestionEditorScreen(
                    chapterId = cId,
                    questionId = qId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Test List
            composable(
                route = Screen.TestList.route,
                arguments = listOf(navArgument("chapterId") { type = NavType.LongType; defaultValue = -1L })
            ) { backStack ->
                val cId = backStack.arguments?.getLong("chapterId") ?: -1L
                TestListScreen(
                    chapterId = cId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToCreateManual = { chId -> navController.navigate(Screen.TestCreate.createRoute(chId)) },
                    onNavigateToCreateSmart = { chId -> navController.navigate(Screen.SmartTestCreate.createRoute(chId)) },
                    onStartTest = { tId -> navController.navigate(Screen.TestPlayer.createRoute(tId)) }
                )
            }

            // Test Create (Manual)
            composable(
                route = Screen.TestCreate.route,
                arguments = listOf(navArgument("chapterId") { type = NavType.LongType; defaultValue = -1L })
            ) { backStack ->
                val cId = backStack.arguments?.getLong("chapterId") ?: -1L
                TestCreateScreen(
                    chapterId = cId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Smart Test Create
            composable(
                route = Screen.SmartTestCreate.route,
                arguments = listOf(navArgument("chapterId") { type = NavType.LongType; defaultValue = -1L })
            ) { backStack ->
                val cId = backStack.arguments?.getLong("chapterId") ?: -1L
                SmartTestCreateScreen(
                    chapterId = cId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToTestPlayer = { tId -> navController.navigate(Screen.TestPlayer.createRoute(tId)) }
                )
            }

            // Test Player
            composable(
                route = Screen.TestPlayer.route,
                arguments = listOf(
                    navArgument("testId") { type = NavType.LongType },
                    navArgument("attemptId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) { backStack ->
                val tId = backStack.arguments?.getLong("testId") ?: 1L
                val aId = backStack.arguments?.getLong("attemptId") ?: -1L
                TestPlayerScreen(
                    testId = tId,
                    attemptId = aId,
                    viewModel = viewModel,
                    onTestSubmitted = { completedAttemptId ->
                        navController.navigate(Screen.TestResult.createRoute(completedAttemptId)) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }

            // Test Result Scorecard
            composable(
                route = Screen.TestResult.route,
                arguments = listOf(navArgument("attemptId") { type = NavType.LongType })
            ) { backStack ->
                val aId = backStack.arguments?.getLong("attemptId") ?: 1L
                TestResultScreen(
                    attemptId = aId,
                    viewModel = viewModel,
                    onNavigateToReview = { attId -> navController.navigate(Screen.TestReview.createRoute(attId)) },
                    onFinish = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            // Test Review
            composable(
                route = Screen.TestReview.route,
                arguments = listOf(navArgument("attemptId") { type = NavType.LongType })
            ) { backStack ->
                val aId = backStack.arguments?.getLong("attemptId") ?: 1L
                TestReviewScreen(
                    attemptId = aId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Mistake Book
            composable(
                route = Screen.MistakeBook.route,
                arguments = listOf(navArgument("chapterId") { type = NavType.LongType; defaultValue = -1L })
            ) { backStack ->
                val cId = backStack.arguments?.getLong("chapterId") ?: -1L
                MistakeBookScreen(
                    chapterId = cId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Revision Queue
            composable(Screen.RevisionQueue.route) {
                RevisionScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Chapter MCQ Revision
            composable(
                route = Screen.ChapterRevision.route,
                arguments = listOf(navArgument("chapterId") { type = NavType.LongType })
            ) { backStack ->
                val cId = backStack.arguments?.getLong("chapterId") ?: 1L
                com.studyforge.app.ui.screens.revision.ChapterRevisionScreen(
                    chapterId = cId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Flashcards
            composable(
                route = Screen.Flashcards.route,
                arguments = listOf(navArgument("chapterId") { type = NavType.LongType; defaultValue = -1L })
            ) { backStack ->
                val cId = backStack.arguments?.getLong("chapterId") ?: -1L
                FlashcardsScreen(
                    chapterId = cId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // JSON Import
            composable(Screen.JsonImport.route) {
                JsonImportScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToSchemaHelp = { navController.navigate(Screen.JsonSchemaHelp.route) }
                )
            }

            // JSON Schema Help
            composable(Screen.JsonSchemaHelp.route) {
                JsonSchemaHelpScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Planner & Journal
            composable(Screen.Planner.route) {
                PlannerScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Settings
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToHowToUse = { navController.navigate(Screen.HowToUse.route) }
                )
            }

            // How To Use Guide & JSON Format Examples
            composable(Screen.HowToUse.route) {
                HowToUseScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToJsonImport = { navController.navigate(Screen.JsonImport.route) }
                )
            }

            // Global Search
            composable(Screen.GlobalSearch.route) {
                GlobalSearchScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToChapter = { cId -> navController.navigate(Screen.ChapterDetail.createRoute(cId)) }
                )
            }
        }
    }
}
}
