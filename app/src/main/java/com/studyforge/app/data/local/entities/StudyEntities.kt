package com.studyforge.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.studyforge.app.domain.model.Difficulty
import com.studyforge.app.domain.model.Mood
import com.studyforge.app.domain.model.QuestionType
import com.studyforge.app.domain.model.RevisionItemType
import com.studyforge.app.domain.model.TestMode

@Entity(tableName = "batches")
data class BatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val courseOrClass: String = "",
    val session: String = "",
    val goal: String = "",
    val description: String = "",
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "subjects",
    foreignKeys = [
        ForeignKey(
            entity = BatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["batchId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("batchId")]
)
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchId: Long,
    val name: String,
    val iconName: String = "book",
    val colorHex: String = "#4F46E5",
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId"), Index("batchId")]
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val batchId: Long,
    val name: String,
    val orderIndex: Int = 0,
    val masteryScore: Int = 0, // 0 to 100
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("chapterId"), Index("subjectId"), Index("batchId")]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chapterId: Long,
    val subjectId: Long,
    val batchId: Long,
    val title: String,
    val contentMarkdown: String,
    val tagsJson: String = "[]",
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "formulas",
    foreignKeys = [
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("chapterId"), Index("subjectId")]
)
data class FormulaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chapterId: Long,
    val subjectId: Long,
    val batchId: Long,
    val title: String,
    val formulaLatex: String,
    val explanation: String = "",
    val tagsJson: String = "[]",
    val isFavorite: Boolean = false,
    val revisionDueAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("chapterId"), Index("subjectId"), Index("type"), Index("difficulty")]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chapterId: Long,
    val subjectId: Long,
    val batchId: Long,
    val type: QuestionType = QuestionType.MCQ,
    val questionText: String,
    val optionsJson: String = "[]", // List<String> JSON
    val correctAnswersJson: String = "[]", // List<String> JSON
    val explanation: String = "",
    val detailedSolution: String = "",
    val marks: Double = 1.0,
    val negativeMarks: Double = 0.0,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val topic: String = "",
    val tagsJson: String = "[]",
    val source: String = "",
    val hint: String = "",
    val imageUrl: String = "",
    val timeLimitSeconds: Int = 0,
    val isFavorite: Boolean = false,
    val isBookmarked: Boolean = false,
    val timesAttempted: Int = 0,
    val timesCorrect: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tests",
    foreignKeys = [
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("chapterId"), Index("subjectId"), Index("batchId")]
)
data class TestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchId: Long? = null,
    val subjectId: Long? = null,
    val chapterId: Long? = null,
    val title: String,
    val mode: TestMode = TestMode.PRACTICE,
    val durationMinutes: Int = 30,
    val totalMarks: Double = 0.0,
    val negativeMarksPerWrong: Double = 0.0,
    val questionIdsJson: String = "[]", // List<Long> of questions in order
    val isRandomized: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "test_attempts",
    foreignKeys = [
        ForeignKey(
            entity = TestEntity::class,
            parentColumns = ["id"],
            childColumns = ["testId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("testId"), Index("completedAt")]
)
data class TestAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val testId: Long,
    val testTitle: String,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long = 0L,
    val isCompleted: Boolean = false,
    val totalQuestions: Int = 0,
    val answeredCount: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val skippedCount: Int = 0,
    val totalScore: Double = 0.0,
    val maxScore: Double = 0.0,
    val accuracyPercentage: Double = 0.0,
    val timeSpentSeconds: Int = 0,
    val answersDraftJson: String = "{}" // In-progress answers map (questionId -> answers list)
)

@Entity(
    tableName = "test_answers",
    foreignKeys = [
        ForeignKey(
            entity = TestAttemptEntity::class,
            parentColumns = ["id"],
            childColumns = ["attemptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("attemptId"), Index("questionId")]
)
data class TestAnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val attemptId: Long,
    val questionId: Long,
    val userSelectedJson: String = "[]",
    val isCorrect: Boolean = false,
    val isSkipped: Boolean = false,
    val marksAwarded: Double = 0.0,
    val timeSpentSeconds: Int = 0
)

@Entity(
    tableName = "mistakes",
    foreignKeys = [
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("questionId"), Index("chapterId"), Index("isResolved")]
)
data class MistakeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: Long,
    val chapterId: Long,
    val subjectId: Long,
    val testAttemptId: Long? = null,
    val userAnswerJson: String = "[]",
    val correctAnswerJson: String = "[]",
    val userReflectionNote: String = "",
    val attemptCount: Int = 1,
    val isResolved: Boolean = false,
    val resolvedAt: Long? = null,
    val scheduledRevisionAt: Long = System.currentTimeMillis() + 86400000L, // Default 1 day
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "revision_items", indices = [Index("nextReviewDate"), Index("itemType", "itemId")])
data class RevisionItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemType: RevisionItemType,
    val itemId: Long,
    val chapterId: Long,
    val title: String,
    val intervalLevel: Int = 0, // 0=1d, 1=3d, 2=7d, 3=14d, 4=30d
    val nextReviewDate: Long = System.currentTimeMillis(),
    val lastReviewedDate: Long = 0L,
    val repetitions: Int = 0
)

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("chapterId"), Index("nextReviewDate")]
)
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chapterId: Long,
    val subjectId: Long,
    val front: String,
    val back: String,
    val tagsJson: String = "[]",
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val reviewCount: Int = 0,
    val nextReviewDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_sessions", indices = [Index("dateMillis"), Index("chapterId")])
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchId: Long? = null,
    val subjectId: Long? = null,
    val chapterId: Long? = null,
    val durationMinutes: Int,
    val questionsSolved: Int = 0,
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "timetable_entries", indices = [Index("dayOfWeek")])
data class TimetableEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long? = null,
    val subjectName: String = "",
    val chapterOrTask: String,
    val dayOfWeek: Int, // 1 (Mon) to 7 (Sun)
    val startTime: String, // "09:00"
    val endTime: String,   // "10:30"
    val reminderEnabled: Boolean = false
)

@Entity(tableName = "journal_entries", indices = [Index("dateMillis", unique = true)])
data class JournalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long, // Start of day timestamp
    val freeText: String = "",
    val mood: Mood = Mood.GOOD,
    val accomplishments: String = "",
    val difficulties: String = "",
    val tomorrowPlan: String = "",
    val studyTimeMinutes: Int = 0,
    val questionsSolved: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
