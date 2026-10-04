package com.studyforge.app.ui.navigation

sealed class Screen(val route: String) {
    // Bottom Nav Destinations
    object Home : Screen("home")
    object Practice : Screen("practice")
    object Library : Screen("library")
    object Analytics : Screen("analytics")

    // Secondary Destinations
    object BatchDetail : Screen("batch_detail/{batchId}") {
        fun createRoute(batchId: Long) = "batch_detail/$batchId"
    }

    object SubjectDetail : Screen("subject_detail/{subjectId}") {
        fun createRoute(subjectId: Long) = "subject_detail/$subjectId"
    }

    object ChapterDetail : Screen("chapter_detail/{chapterId}") {
        fun createRoute(chapterId: Long) = "chapter_detail/$chapterId"
    }

    object NoteEditor : Screen("note_editor/{chapterId}?noteId={noteId}") {
        fun createRoute(chapterId: Long, noteId: Long = -1L) = "note_editor/$chapterId?noteId=$noteId"
    }

    object FormulaVault : Screen("formula_vault?chapterId={chapterId}&subjectId={subjectId}") {
        fun createRoute(chapterId: Long = -1L, subjectId: Long = -1L) = "formula_vault?chapterId=$chapterId&subjectId=$subjectId"
    }

    object QuestionBank : Screen("question_bank?chapterId={chapterId}&subjectId={subjectId}") {
        fun createRoute(chapterId: Long = -1L, subjectId: Long = -1L) = "question_bank?chapterId=$chapterId&subjectId=$subjectId"
    }

    object QuestionEditor : Screen("question_editor/{chapterId}?questionId={questionId}") {
        fun createRoute(chapterId: Long, questionId: Long = -1L) = "question_editor/$chapterId?questionId=$questionId"
    }

    object TestList : Screen("test_list?chapterId={chapterId}") {
        fun createRoute(chapterId: Long = -1L) = "test_list?chapterId=$chapterId"
    }

    object TestCreate : Screen("test_create?chapterId={chapterId}") {
        fun createRoute(chapterId: Long = -1L) = "test_create?chapterId=$chapterId"
    }

    object SmartTestCreate : Screen("smart_test_create?chapterId={chapterId}") {
        fun createRoute(chapterId: Long = -1L) = "smart_test_create?chapterId=$chapterId"
    }

    object TestPlayer : Screen("test_player/{testId}?attemptId={attemptId}") {
        fun createRoute(testId: Long, attemptId: Long = -1L) = "test_player/$testId?attemptId=$attemptId"
    }

    object TestResult : Screen("test_result/{attemptId}") {
        fun createRoute(attemptId: Long) = "test_result/$attemptId"
    }

    object TestReview : Screen("test_review/{attemptId}") {
        fun createRoute(attemptId: Long) = "test_review/$attemptId"
    }

    object MistakeBook : Screen("mistake_book?chapterId={chapterId}") {
        fun createRoute(chapterId: Long = -1L) = "mistake_book?chapterId=$chapterId"
    }

    object RevisionQueue : Screen("revision_queue")
    object Flashcards : Screen("flashcards?chapterId={chapterId}") {
        fun createRoute(chapterId: Long = -1L) = "flashcards?chapterId=$chapterId"
    }

    object JsonImport : Screen("json_import")
    object JsonSchemaHelp : Screen("json_schema_help")
    object Planner : Screen("planner")
    object Settings : Screen("settings")
    object HowToUse : Screen("how_to_use")
    object GlobalSearch : Screen("global_search")
}
