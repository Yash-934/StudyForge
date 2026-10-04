package com.studyforge.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studyforge.app.data.local.entities.BatchEntity
import com.studyforge.app.data.local.entities.ChapterEntity
import com.studyforge.app.data.local.entities.FlashcardEntity
import com.studyforge.app.data.local.entities.FormulaEntity
import com.studyforge.app.data.local.entities.JournalEntity
import com.studyforge.app.data.local.entities.MistakeEntity
import com.studyforge.app.data.local.entities.NoteEntity
import com.studyforge.app.data.local.entities.QuestionEntity
import com.studyforge.app.data.local.entities.RevisionItemEntity
import com.studyforge.app.data.local.entities.StudySessionEntity
import com.studyforge.app.data.local.entities.SubjectEntity
import com.studyforge.app.data.local.entities.TestAnswerEntity
import com.studyforge.app.data.local.entities.TestAttemptEntity
import com.studyforge.app.data.local.entities.TestEntity
import com.studyforge.app.data.local.entities.TimetableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BatchDao {
    @Query("SELECT * FROM batches ORDER BY isPinned DESC, createdAt DESC")
    fun getAllBatches(): Flow<List<BatchEntity>>

    @Query("SELECT * FROM batches WHERE id = :id LIMIT 1")
    suspend fun getBatchById(id: Long): BatchEntity?

    @Query("SELECT * FROM batches WHERE id = :id LIMIT 1")
    fun getBatchByIdFlow(id: Long): Flow<BatchEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: BatchEntity): Long

    @Update
    suspend fun updateBatch(batch: BatchEntity)

    @Delete
    suspend fun deleteBatch(batch: BatchEntity)
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects WHERE batchId = :batchId ORDER BY isPinned DESC, createdAt ASC")
    fun getSubjectsForBatch(batchId: Long): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects ORDER BY isPinned DESC, createdAt DESC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getSubjectById(id: Long): SubjectEntity?

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    fun getSubjectByIdFlow(id: Long): Flow<SubjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)
}

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId ORDER BY orderIndex ASC, createdAt ASC")
    fun getChaptersForSubject(subjectId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters ORDER BY createdAt DESC")
    fun getAllChapters(): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :id LIMIT 1")
    suspend fun getChapterById(id: Long): ChapterEntity?

    @Query("SELECT * FROM chapters WHERE id = :id LIMIT 1")
    fun getChapterByIdFlow(id: Long): Flow<ChapterEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Query("UPDATE chapters SET masteryScore = :score WHERE id = :chapterId")
    suspend fun updateMasteryScore(chapterId: Long, score: Int)

    @Delete
    suspend fun deleteChapter(chapter: ChapterEntity)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE chapterId = :chapterId ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesForChapter(chapterId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE subjectId = :subjectId ORDER BY updatedAt DESC")
    fun getNotesForSubject(subjectId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun getNoteByIdFlow(id: Long): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR contentMarkdown LIKE '%' || :query || '%'")
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)
}

@Dao
interface FormulaDao {
    @Query("SELECT * FROM formulas WHERE chapterId = :chapterId ORDER BY isFavorite DESC, createdAt ASC")
    fun getFormulasForChapter(chapterId: Long): Flow<List<FormulaEntity>>

    @Query("SELECT * FROM formulas WHERE subjectId = :subjectId ORDER BY isFavorite DESC, createdAt ASC")
    fun getFormulasForSubject(subjectId: Long): Flow<List<FormulaEntity>>

    @Query("SELECT * FROM formulas ORDER BY isFavorite DESC, createdAt DESC")
    fun getAllFormulas(): Flow<List<FormulaEntity>>

    @Query("SELECT * FROM formulas WHERE id = :id LIMIT 1")
    suspend fun getFormulaById(id: Long): FormulaEntity?

    @Query("SELECT * FROM formulas WHERE title LIKE '%' || :query || '%' OR explanation LIKE '%' || :query || '%'")
    fun searchFormulas(query: String): Flow<List<FormulaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormula(formula: FormulaEntity): Long

    @Update
    suspend fun updateFormula(formula: FormulaEntity)

    @Delete
    suspend fun deleteFormula(formula: FormulaEntity)
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE chapterId = :chapterId ORDER BY createdAt DESC")
    fun getQuestionsForChapter(chapterId: Long): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE subjectId = :subjectId ORDER BY createdAt DESC")
    fun getQuestionsForSubject(subjectId: Long): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions ORDER BY createdAt DESC")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    suspend fun getQuestionById(id: Long): QuestionEntity?

    @Query("SELECT * FROM questions WHERE id IN (:ids)")
    suspend fun getQuestionsByIds(ids: List<Long>): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE questionText LIKE '%' || :query || '%' OR topic LIKE '%' || :query || '%'")
    fun searchQuestions(query: String): Flow<List<QuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>): List<Long>

    @Update
    suspend fun updateQuestion(question: QuestionEntity)

    @Query("UPDATE questions SET timesAttempted = timesAttempted + 1, timesCorrect = timesCorrect + :wasCorrect WHERE id = :id")
    suspend fun recordAttempt(id: Long, wasCorrect: Int)

    @Delete
    suspend fun deleteQuestion(question: QuestionEntity)
}

@Dao
interface TestDao {
    @Query("SELECT * FROM tests WHERE chapterId = :chapterId ORDER BY createdAt DESC")
    fun getTestsForChapter(chapterId: Long): Flow<List<TestEntity>>

    @Query("SELECT * FROM tests ORDER BY createdAt DESC")
    fun getAllTests(): Flow<List<TestEntity>>

    @Query("SELECT * FROM tests WHERE id = :id LIMIT 1")
    suspend fun getTestById(id: Long): TestEntity?

    @Query("SELECT * FROM tests WHERE id = :id LIMIT 1")
    fun getTestByIdFlow(id: Long): Flow<TestEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: TestEntity): Long

    @Update
    suspend fun updateTest(test: TestEntity)

    @Delete
    suspend fun deleteTest(test: TestEntity)

    // Test Attempts
    @Query("SELECT * FROM test_attempts WHERE testId = :testId ORDER BY startedAt DESC")
    fun getAttemptsForTest(testId: Long): Flow<List<TestAttemptEntity>>

    @Query("SELECT * FROM test_attempts ORDER BY startedAt DESC")
    fun getAllAttempts(): Flow<List<TestAttemptEntity>>

    @Query("SELECT * FROM test_attempts WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedAttempts(): Flow<List<TestAttemptEntity>>

    @Query("SELECT * FROM test_attempts WHERE id = :id LIMIT 1")
    suspend fun getAttemptById(id: Long): TestAttemptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: TestAttemptEntity): Long

    @Update
    suspend fun updateAttempt(attempt: TestAttemptEntity)

    // Test Answers
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestAnswers(answers: List<TestAnswerEntity>)

    @Query("SELECT * FROM test_answers WHERE attemptId = :attemptId")
    suspend fun getAnswersForAttempt(attemptId: Long): List<TestAnswerEntity>
}

@Dao
interface MistakeDao {
    @Query("SELECT * FROM mistakes WHERE isResolved = 0 ORDER BY createdAt DESC")
    fun getUnresolvedMistakes(): Flow<List<MistakeEntity>>

    @Query("SELECT * FROM mistakes ORDER BY createdAt DESC")
    fun getAllMistakes(): Flow<List<MistakeEntity>>

    @Query("SELECT * FROM mistakes WHERE chapterId = :chapterId ORDER BY isResolved ASC, createdAt DESC")
    fun getMistakesForChapter(chapterId: Long): Flow<List<MistakeEntity>>

    @Query("SELECT * FROM mistakes WHERE id = :id LIMIT 1")
    suspend fun getMistakeById(id: Long): MistakeEntity?

    @Query("SELECT * FROM mistakes WHERE questionId = :questionId LIMIT 1")
    suspend fun getMistakeByQuestionId(questionId: Long): MistakeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMistake(mistake: MistakeEntity): Long

    @Update
    suspend fun updateMistake(mistake: MistakeEntity)

    @Query("UPDATE mistakes SET isResolved = 1, resolvedAt = :time WHERE id = :id")
    suspend fun markResolved(id: Long, time: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteMistake(mistake: MistakeEntity)
}

@Dao
interface RevisionDao {
    @Query("SELECT * FROM revision_items WHERE nextReviewDate <= :currentTime ORDER BY nextReviewDate ASC")
    fun getRevisionDueItems(currentTime: Long): Flow<List<RevisionItemEntity>>

    @Query("SELECT * FROM revision_items ORDER BY nextReviewDate ASC")
    fun getAllRevisionItems(): Flow<List<RevisionItemEntity>>

    @Query("SELECT * FROM revision_items WHERE chapterId = :chapterId")
    fun getRevisionItemsForChapter(chapterId: Long): Flow<List<RevisionItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevisionItem(item: RevisionItemEntity): Long

    @Update
    suspend fun updateRevisionItem(item: RevisionItemEntity)

    @Delete
    suspend fun deleteRevisionItem(item: RevisionItemEntity)
}

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards WHERE chapterId = :chapterId ORDER BY nextReviewDate ASC")
    fun getFlashcardsForChapter(chapterId: Long): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE nextReviewDate <= :currentTime ORDER BY nextReviewDate ASC")
    fun getFlashcardsDue(currentTime: Long): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards ORDER BY createdAt DESC")
    fun getAllFlashcards(): Flow<List<FlashcardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: FlashcardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(flashcards: List<FlashcardEntity>): List<Long>

    @Update
    suspend fun updateFlashcard(flashcard: FlashcardEntity)

    @Delete
    suspend fun deleteFlashcard(flashcard: FlashcardEntity)
}

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY dateMillis DESC")
    fun getAllSessions(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE dateMillis >= :startMillis AND dateMillis <= :endMillis")
    fun getSessionsBetween(startMillis: Long, endMillis: Long): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity): Long
}

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable_entries ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllTimetableEntries(): Flow<List<TimetableEntity>>

    @Query("SELECT * FROM timetable_entries WHERE dayOfWeek = :day ORDER BY startTime ASC")
    fun getEntriesForDay(day: Int): Flow<List<TimetableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: TimetableEntity): Long

    @Update
    suspend fun updateEntry(entry: TimetableEntity)

    @Delete
    suspend fun deleteEntry(entry: TimetableEntity)
}

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY dateMillis DESC")
    fun getAllJournalEntries(): Flow<List<JournalEntity>>

    @Query("SELECT * FROM journal_entries WHERE dateMillis = :dateMillis LIMIT 1")
    suspend fun getEntryForDate(dateMillis: Long): JournalEntity?

    @Query("SELECT * FROM journal_entries WHERE dateMillis = :dateMillis LIMIT 1")
    fun getEntryForDateFlow(dateMillis: Long): Flow<JournalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateJournal(entry: JournalEntity): Long

    @Delete
    suspend fun deleteJournalEntry(entry: JournalEntity)
}
