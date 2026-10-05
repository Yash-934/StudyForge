package com.studyforge.app.domain.model

data class QuestionDto(
    val id: String = "",
    val type: QuestionType = QuestionType.MCQ,
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctAnswers: List<String> = emptyList(), // Store normalized as string list (indices or text)
    val marks: Double = 1.0,
    val negativeMarks: Double = 0.0,
    val explanation: String = "",
    val detailedSolution: String = "",
    val topic: String = "",
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val tags: List<String> = emptyList(),
    val hint: String = "",
    val imageUrl: String = "",
    val source: String = "",
    val timeLimitSeconds: Int = 0
)

data class TestImportDto(
    val title: String = "",
    val subject: String = "",
    val chapter: String = "",
    val durationMinutes: Int = 30,
    val totalMarks: Double = 0.0,
    val questions: List<QuestionDto> = emptyList()
)

data class ValidationIssue(
    val questionIndex: Int,
    val questionId: String,
    val message: String,
    val isFatal: Boolean // Fatal prevents question import; non-fatal is a warning
)

data class TestValidationResult(
    val totalQuestionsCount: Int,
    val validQuestionsCount: Int,
    val invalidQuestionsCount: Int,
    val issues: List<ValidationIssue>,
    val testDto: TestImportDto
)

data class QuestionEvaluationResult(
    val questionId: Long,
    val isCorrect: Boolean,
    val isSkipped: Boolean,
    val marksAwarded: Double,
    val userSelectedAnswers: List<String>,
    val correctAnswers: List<String>,
    val timeSpentSeconds: Int
)

data class TestScoringSummary(
    val totalScore: Double,
    val maxScore: Double,
    val percentage: Double,
    val accuracy: Double,
    val correctCount: Int,
    val wrongCount: Int,
    val skippedCount: Int,
    val totalTimeSeconds: Int,
    val avgTimePerQuestionSeconds: Double,
    val evaluations: List<QuestionEvaluationResult>
)

data class ChapterProgressInfo(
    val chapterId: Long,
    val chapterName: String,
    val subjectName: String,
    val masteryScore: Int,
    val notesCount: Int,
    val formulasCount: Int,
    val questionsCount: Int,
    val testsCount: Int,
    val solvedQuestionsCount: Int,
    val mistakeCount: Int
)

data class SubjectMasteryInfo(
    val subjectId: Long,
    val subjectName: String,
    val masteryScore: Int,
    val chapterCount: Int,
    val testsCompleted: Int,
    val accuracyPercentage: Int
)

data class GlobalAnalytics(
    val totalStudyTimeMinutes: Long = 0L,
    val totalQuestionsSolved: Int = 0,
    val totalTestsCompleted: Int = 0,
    val overallAverageScore: Double = 0.0,
    val overallAccuracy: Double = 0.0,
    val totalMistakesRecorded: Int = 0,
    val resolvedMistakesCount: Int = 0,
    val revisionDueTodayCount: Int = 0,
    val currentStreakDays: Int = 0,
    val subjectMasteries: List<SubjectMasteryInfo> = emptyList(),
    val weakChapters: List<ChapterProgressInfo> = emptyList(),
    val strongChapters: List<ChapterProgressInfo> = emptyList(),
    val recentScoreTrend: List<Pair<String, Double>> = emptyList() // Date/Title to Score %
)
