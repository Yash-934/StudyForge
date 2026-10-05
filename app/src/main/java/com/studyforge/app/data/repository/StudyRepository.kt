package com.studyforge.app.data.repository

import com.studyforge.app.data.local.AppDatabase
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
import com.studyforge.app.domain.json.CurriculumValidationResult
import com.studyforge.app.domain.model.ChapterProgressInfo
import com.studyforge.app.domain.model.Difficulty
import com.studyforge.app.domain.model.GlobalAnalytics
import com.studyforge.app.domain.model.MasteryCalculator
import com.studyforge.app.domain.model.QuestionEvaluationResult
import com.studyforge.app.domain.model.QuestionType
import com.studyforge.app.domain.model.RevisionItemType
import com.studyforge.app.domain.model.SubjectMasteryInfo
import com.studyforge.app.domain.model.TestScoringSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

class StudyRepository(private val db: AppDatabase) {

    val allBatches: Flow<List<BatchEntity>> = db.batchDao().getAllBatches()
    val allSubjects: Flow<List<SubjectEntity>> = db.subjectDao().getAllSubjects()
    val allChapters: Flow<List<ChapterEntity>> = db.chapterDao().getAllChapters()
    val allNotes: Flow<List<NoteEntity>> = db.noteDao().getAllNotes()
    val allFormulas: Flow<List<FormulaEntity>> = db.formulaDao().getAllFormulas()
    val allQuestions: Flow<List<QuestionEntity>> = db.questionDao().getAllQuestions()
    val allTests: Flow<List<TestEntity>> = db.testDao().getAllTests()
    val completedAttempts: Flow<List<TestAttemptEntity>> = db.testDao().getCompletedAttempts()
    val unresolvedMistakes: Flow<List<MistakeEntity>> = db.mistakeDao().getUnresolvedMistakes()
    val allMistakes: Flow<List<MistakeEntity>> = db.mistakeDao().getAllMistakes()
    val allFlashcards: Flow<List<FlashcardEntity>> = db.flashcardDao().getAllFlashcards()
    val allSessions: Flow<List<StudySessionEntity>> = db.studySessionDao().getAllSessions()
    val allTimetable: Flow<List<TimetableEntity>> = db.timetableDao().getAllTimetableEntries()
    val allJournals: Flow<List<JournalEntity>> = db.journalDao().getAllJournalEntries()

    fun getSubjectsForBatch(batchId: Long): Flow<List<SubjectEntity>> =
        db.subjectDao().getSubjectsForBatch(batchId)

    fun getChaptersForSubject(subjectId: Long): Flow<List<ChapterEntity>> =
        db.chapterDao().getChaptersForSubject(subjectId)

    fun getNotesForChapter(chapterId: Long): Flow<List<NoteEntity>> =
        db.noteDao().getNotesForChapter(chapterId)

    fun getFormulasForChapter(chapterId: Long): Flow<List<FormulaEntity>> =
        db.formulaDao().getFormulasForChapter(chapterId)

    fun getQuestionsForChapter(chapterId: Long): Flow<List<QuestionEntity>> =
        db.questionDao().getQuestionsForChapter(chapterId)

    fun getTestsForChapter(chapterId: Long): Flow<List<TestEntity>> =
        db.testDao().getTestsForChapter(chapterId)

    fun getMistakesForChapter(chapterId: Long): Flow<List<MistakeEntity>> =
        db.mistakeDao().getMistakesForChapter(chapterId)

    fun getFlashcardsForChapter(chapterId: Long): Flow<List<FlashcardEntity>> =
        db.flashcardDao().getFlashcardsForChapter(chapterId)

    fun getRevisionDueItems(): Flow<List<RevisionItemEntity>> =
        db.revisionDao().getRevisionDueItems(System.currentTimeMillis())

    // CRUD Batches
    suspend fun insertBatch(batch: BatchEntity): Long = db.batchDao().insertBatch(batch)
    suspend fun updateBatch(batch: BatchEntity) = db.batchDao().updateBatch(batch)
    suspend fun deleteBatch(batch: BatchEntity) = db.batchDao().deleteBatch(batch)

    // CRUD Subjects
    suspend fun insertSubject(subject: SubjectEntity): Long = db.subjectDao().insertSubject(subject)
    suspend fun updateSubject(subject: SubjectEntity) = db.subjectDao().updateSubject(subject)
    suspend fun deleteSubject(subject: SubjectEntity) = db.subjectDao().deleteSubject(subject)

    // CRUD Chapters
    suspend fun insertChapter(chapter: ChapterEntity): Long = db.chapterDao().insertChapter(chapter)
    suspend fun updateChapter(chapter: ChapterEntity) = db.chapterDao().updateChapter(chapter)
    suspend fun deleteChapter(chapter: ChapterEntity) = db.chapterDao().deleteChapter(chapter)

    // Notes
    suspend fun insertNote(note: NoteEntity): Long = db.noteDao().insertNote(note)
    suspend fun updateNote(note: NoteEntity) = db.noteDao().updateNote(note)
    suspend fun deleteNote(note: NoteEntity) = db.noteDao().deleteNote(note)
    suspend fun getNoteById(id: Long): NoteEntity? = db.noteDao().getNoteById(id)

    // Formulas
    suspend fun insertFormula(formula: FormulaEntity): Long = db.formulaDao().insertFormula(formula)
    suspend fun updateFormula(formula: FormulaEntity) = db.formulaDao().updateFormula(formula)
    suspend fun deleteFormula(formula: FormulaEntity) = db.formulaDao().deleteFormula(formula)

    // Questions
    suspend fun insertQuestion(question: QuestionEntity): Long = db.questionDao().insertQuestion(question)
    suspend fun insertQuestions(questions: List<QuestionEntity>): List<Long> = db.questionDao().insertQuestions(questions)
    suspend fun updateQuestion(question: QuestionEntity) = db.questionDao().updateQuestion(question)
    suspend fun deleteQuestion(question: QuestionEntity) = db.questionDao().deleteQuestion(question)
    suspend fun getQuestionsByIds(ids: List<Long>): List<QuestionEntity> = db.questionDao().getQuestionsByIds(ids)

    // Tests
    suspend fun insertTest(test: TestEntity): Long = db.testDao().insertTest(test)
    suspend fun deleteTest(test: TestEntity) = db.testDao().deleteTest(test)
    suspend fun getTestById(id: Long): TestEntity? = db.testDao().getTestById(id)
    suspend fun getAttemptById(id: Long): TestAttemptEntity? = db.testDao().getAttemptById(id)
    suspend fun getAnswersForAttempt(attemptId: Long): List<TestAnswerEntity> = db.testDao().getAnswersForAttempt(attemptId)

    // Test Taking & Scoring Engine
    suspend fun startTestAttempt(testId: Long, testTitle: String, totalQuestions: Int): Long {
        val attempt = TestAttemptEntity(
            testId = testId,
            testTitle = testTitle,
            startedAt = System.currentTimeMillis(),
            totalQuestions = totalQuestions,
            isCompleted = false
        )
        return db.testDao().insertAttempt(attempt)
    }

    suspend fun updateAttemptDraft(attemptId: Long, answersJson: String) {
        val existing = db.testDao().getAttemptById(attemptId) ?: return
        db.testDao().updateAttempt(existing.copy(answersDraftJson = answersJson))
    }

    suspend fun submitTest(
        attemptId: Long,
        test: TestEntity,
        questions: List<QuestionEntity>,
        userAnswers: Map<Long, List<String>>, // questionId -> selected answers (strings)
        questionTimeSpent: Map<Long, Int>
    ): TestScoringSummary = withContext(Dispatchers.IO) {
        var totalScore = 0.0
        var maxScore = 0.0
        var correctCount = 0
        var wrongCount = 0
        var skippedCount = 0
        var totalTimeSec = 0

        val evaluations = mutableListOf<QuestionEvaluationResult>()
        val answerEntities = mutableListOf<TestAnswerEntity>()

        for (q in questions) {
            maxScore += q.marks
            val userSelected = userAnswers[q.id] ?: emptyList()
            val timeSpent = questionTimeSpent[q.id] ?: 0
            totalTimeSec += timeSpent

            val correctList = try {
                val arr = JSONArray(q.correctAnswersJson)
                (0 until arr.length()).map { arr.optString(it).trim().lowercase() }
            } catch (e: Exception) {
                emptyList()
            }

            val isSkipped = userSelected.isEmpty() || userSelected.all { it.isBlank() }
            var isCorrect = false
            var marksAwarded = 0.0

            if (isSkipped) {
                skippedCount++
                marksAwarded = 0.0
            } else {
                when (q.type) {
                    QuestionType.MCQ, QuestionType.TRUE_FALSE -> {
                        val firstSelected = userSelected.firstOrNull()?.trim()?.lowercase() ?: ""
                        isCorrect = correctList.contains(firstSelected)
                        if (isCorrect) {
                            correctCount++
                            marksAwarded = q.marks
                            totalScore += q.marks
                        } else {
                            wrongCount++
                            marksAwarded = -q.negativeMarks
                            totalScore -= q.negativeMarks
                        }
                    }
                    QuestionType.MULTIPLE_CORRECT -> {
                        val normalizedUser = userSelected.map { it.trim().lowercase() }.toSet()
                        val normalizedCorrect = correctList.toSet()
                        isCorrect = normalizedUser == normalizedCorrect
                        if (isCorrect) {
                            correctCount++
                            marksAwarded = q.marks
                            totalScore += q.marks
                        } else {
                            wrongCount++
                            marksAwarded = -q.negativeMarks
                            totalScore -= q.negativeMarks
                        }
                    }
                    QuestionType.NUMERICAL -> {
                        val userNum = userSelected.firstOrNull()?.trim()?.toDoubleOrNull()
                        val correctNum = correctList.firstOrNull()?.toDoubleOrNull()
                        if (userNum != null && correctNum != null && kotlin.math.abs(userNum - correctNum) < 0.001) {
                            isCorrect = true
                            correctCount++
                            marksAwarded = q.marks
                            totalScore += q.marks
                        } else {
                            isCorrect = false
                            wrongCount++
                            marksAwarded = -q.negativeMarks
                            totalScore -= q.negativeMarks
                        }
                    }
                    else -> {
                        // FILL_BLANK, SHORT_ANSWER, MATCH_FOLLOWING, etc.
                        val userAns = userSelected.firstOrNull()?.trim()?.lowercase() ?: ""
                        isCorrect = correctList.any { it.equals(userAns, ignoreCase = true) }
                        if (isCorrect) {
                            correctCount++
                            marksAwarded = q.marks
                            totalScore += q.marks
                        } else {
                            wrongCount++
                            marksAwarded = -q.negativeMarks
                            totalScore -= q.negativeMarks
                        }
                    }
                }
            }

            // Update question statistics
            db.questionDao().recordAttempt(q.id, if (isCorrect) 1 else 0)

            // If wrong, automatically record in Mistake Book & schedule revision!
            if (!isCorrect && !isSkipped) {
                recordMistakeAndScheduleRevision(q, userSelected, correctList, attemptId)
            }

            evaluations.add(
                QuestionEvaluationResult(
                    questionId = q.id,
                    isCorrect = isCorrect,
                    isSkipped = isSkipped,
                    marksAwarded = marksAwarded,
                    userSelectedAnswers = userSelected,
                    correctAnswers = correctList,
                    timeSpentSeconds = timeSpent
                )
            )

            answerEntities.add(
                TestAnswerEntity(
                    attemptId = attemptId,
                    questionId = q.id,
                    userSelectedJson = JSONArray(userSelected).toString(),
                    isCorrect = isCorrect,
                    isSkipped = isSkipped,
                    marksAwarded = marksAwarded,
                    timeSpentSeconds = timeSpent
                )
            )
        }

        // Save answers
        db.testDao().insertTestAnswers(answerEntities)

        val totalQuestions = questions.size
        val answeredCount = totalQuestions - skippedCount
        val accuracy = if (answeredCount > 0) (correctCount.toDouble() / answeredCount.toDouble() * 100.0) else 0.0
        val percentage = if (maxScore > 0) ((totalScore.coerceAtLeast(0.0) / maxScore) * 100.0) else 0.0
        val avgTime = if (totalQuestions > 0) totalTimeSec.toDouble() / totalQuestions.toDouble() else 0.0

        // Update Test Attempt
        val existingAttempt = db.testDao().getAttemptById(attemptId)
        if (existingAttempt != null) {
            db.testDao().updateAttempt(
                existingAttempt.copy(
                    completedAt = System.currentTimeMillis(),
                    isCompleted = true,
                    answeredCount = answeredCount,
                    correctCount = correctCount,
                    wrongCount = wrongCount,
                    skippedCount = skippedCount,
                    totalScore = totalScore,
                    maxScore = maxScore,
                    accuracyPercentage = accuracy,
                    timeSpentSeconds = totalTimeSec
                )
            )
        }

        // Automatically log a study session for analytics
        val studyMinutes = (totalTimeSec / 60).coerceAtLeast(1)
        db.studySessionDao().insertSession(
            StudySessionEntity(
                batchId = test.batchId,
                subjectId = test.subjectId,
                chapterId = test.chapterId,
                durationMinutes = studyMinutes,
                questionsSolved = correctCount,
                notes = "Completed test: ${test.title}"
            )
        )

        // Trigger chapter mastery update if chapterId is present
        test.chapterId?.let { recalculateMastery(it) }

        TestScoringSummary(
            totalScore = totalScore,
            maxScore = maxScore,
            percentage = percentage,
            accuracy = accuracy,
            correctCount = correctCount,
            wrongCount = wrongCount,
            skippedCount = skippedCount,
            totalTimeSeconds = totalTimeSec,
            avgTimePerQuestionSeconds = avgTime,
            evaluations = evaluations
        )
    }

    private suspend fun recordMistakeAndScheduleRevision(
        question: QuestionEntity,
        userSelected: List<String>,
        correctAnswers: List<String>,
        attemptId: Long
    ) {
        val existingMistake = db.mistakeDao().getMistakeByQuestionId(question.id)
        if (existingMistake != null) {
            db.mistakeDao().updateMistake(
                existingMistake.copy(
                    attemptCount = existingMistake.attemptCount + 1,
                    isResolved = false,
                    resolvedAt = null,
                    userAnswerJson = JSONArray(userSelected).toString(),
                    scheduledRevisionAt = System.currentTimeMillis() + 86400000L
                )
            )
        } else {
            db.mistakeDao().insertMistake(
                MistakeEntity(
                    questionId = question.id,
                    chapterId = question.chapterId,
                    subjectId = question.subjectId,
                    testAttemptId = attemptId,
                    userAnswerJson = JSONArray(userSelected).toString(),
                    correctAnswerJson = JSONArray(correctAnswers).toString(),
                    scheduledRevisionAt = System.currentTimeMillis() + 86400000L
                )
            )
        }

        // Add to Revision Queue
        db.revisionDao().insertRevisionItem(
            RevisionItemEntity(
                itemType = RevisionItemType.MISTAKE,
                itemId = question.id,
                chapterId = question.chapterId,
                title = "Mistake: ${question.questionText.take(40)}...",
                intervalLevel = 0,
                nextReviewDate = System.currentTimeMillis() + 86400000L
            )
        )
    }

    suspend fun resolveMistake(mistakeId: Long, note: String = "") {
        val mistake = db.mistakeDao().getMistakeById(mistakeId) ?: return
        db.mistakeDao().updateMistake(
            mistake.copy(
                isResolved = true,
                resolvedAt = System.currentTimeMillis(),
                userReflectionNote = note.ifBlank { mistake.userReflectionNote }
            )
        )
        recalculateMastery(mistake.chapterId)
    }

    suspend fun completeRevisionItem(item: RevisionItemEntity, qualityRating: Int) {
        // Quality rating: 1 (Hard), 2 (Good), 3 (Easy)
        val intervalsDays = listOf(1, 3, 7, 14, 30)
        val nextLevel = if (qualityRating >= 2) {
            (item.intervalLevel + 1).coerceAtMost(intervalsDays.lastIndex)
        } else {
            0 // Reset on failure
        }
        val nextDays = intervalsDays[nextLevel]
        val nextReview = System.currentTimeMillis() + (nextDays * 86400000L)

        db.revisionDao().updateRevisionItem(
            item.copy(
                intervalLevel = nextLevel,
                repetitions = item.repetitions + 1,
                lastReviewedDate = System.currentTimeMillis(),
                nextReviewDate = nextReview
            )
        )
        recalculateMastery(item.chapterId)
    }

    suspend fun recalculateMastery(chapterId: Long) = withContext(Dispatchers.IO) {
        val questions = db.questionDao().getQuestionsForChapter(chapterId).first()
        val tests = db.testDao().getTestsForChapter(chapterId).first()
        val testIds = tests.map { it.id }
        val allAttempts = db.testDao().getCompletedAttempts().first().filter { it.testId in testIds }
        val mistakes = db.mistakeDao().getMistakesForChapter(chapterId).first()
        val revisions = db.revisionDao().getRevisionItemsForChapter(chapterId).first()

        val score = MasteryCalculator.calculateChapterMastery(questions, allAttempts, mistakes, revisions)
        db.chapterDao().updateMasteryScore(chapterId, score)
    }

    // Flashcard Actions
    suspend fun insertFlashcard(flashcard: FlashcardEntity): Long = db.flashcardDao().insertFlashcard(flashcard)
    suspend fun rateFlashcard(card: FlashcardEntity, quality: Int) {
        val intervalsDays = listOf(1, 3, 7, 14, 30)
        val nextLevel = if (quality >= 2) (card.reviewCount + 1).coerceAtMost(intervalsDays.lastIndex) else 0
        val nextDate = System.currentTimeMillis() + (intervalsDays[nextLevel] * 86400000L)
        db.flashcardDao().updateFlashcard(
            card.copy(
                reviewCount = card.reviewCount + 1,
                nextReviewDate = nextDate
            )
        )
    }

    suspend fun updateFlashcard(flashcard: FlashcardEntity) = db.flashcardDao().updateFlashcard(flashcard)
    suspend fun deleteFlashcard(flashcard: FlashcardEntity) = db.flashcardDao().deleteFlashcard(flashcard)

    // Timetable & Journal
    suspend fun insertTimetableEntry(entry: TimetableEntity) = db.timetableDao().insertEntry(entry)
    suspend fun updateTimetableEntry(entry: TimetableEntity) = db.timetableDao().updateEntry(entry)
    suspend fun deleteTimetableEntry(entry: TimetableEntity) = db.timetableDao().deleteEntry(entry)
    suspend fun insertJournalEntry(entry: JournalEntity) = db.journalDao().insertOrUpdateJournal(entry)
    suspend fun getJournalForDate(dateMillis: Long): JournalEntity? = db.journalDao().getEntryForDate(dateMillis)

    data class CurriculumImportSummary(
        val batchId: Long,
        val batchName: String,
        val subjectsCount: Int,
        val chaptersCount: Int,
        val notesCount: Int,
        val formulasCount: Int,
        val questionsCount: Int
    )

    suspend fun importCurriculum(
        curriculum: CurriculumValidationResult,
        targetBatchId: Long? = null
    ): CurriculumImportSummary = withContext(Dispatchers.IO) {
        val resolvedBatchId: Long = when {
            targetBatchId != null && targetBatchId > 0L -> targetBatchId
            curriculum.batch != null -> {
                val b = curriculum.batch
                insertBatch(
                    BatchEntity(
                        name = b.name,
                        courseOrClass = b.courseOrClass,
                        session = b.session,
                        goal = b.goal,
                        description = b.description
                    )
                )
            }
            else -> {
                val existing = db.batchDao().getAllBatches().first().firstOrNull()
                existing?.id ?: insertBatch(BatchEntity(name = "Imported Batch"))
            }
        }

        val targetBatch = db.batchDao().getAllBatches().first().find { it.id == resolvedBatchId }
        val batchName = targetBatch?.name ?: curriculum.batch?.name ?: "Imported Batch"

        var subjectsCount = 0
        var chaptersCount = 0
        var notesCount = 0
        var formulasCount = 0
        var questionsCount = 0

        for (subDto in curriculum.subjects) {
            val subjectId = insertSubject(
                SubjectEntity(
                    batchId = resolvedBatchId,
                    name = subDto.name,
                    iconName = subDto.iconName,
                    colorHex = subDto.colorHex
                )
            )
            subjectsCount++

            for (chapDto in subDto.chapters) {
                val chapterId = insertChapter(
                    ChapterEntity(
                        subjectId = subjectId,
                        batchId = resolvedBatchId,
                        name = chapDto.name,
                        orderIndex = chapDto.orderIndex
                    )
                )
                chaptersCount++

                for (noteDto in chapDto.notes) {
                    insertNote(
                        NoteEntity(
                            chapterId = chapterId,
                            subjectId = subjectId,
                            batchId = resolvedBatchId,
                            title = noteDto.title,
                            contentMarkdown = noteDto.content,
                            tagsJson = "[\"Imported\"]"
                        )
                    )
                    notesCount++
                }

                for (formulaDto in chapDto.formulas) {
                    insertFormula(
                        FormulaEntity(
                            chapterId = chapterId,
                            subjectId = subjectId,
                            batchId = resolvedBatchId,
                            title = formulaDto.title,
                            formulaLatex = formulaDto.latex,
                            explanation = formulaDto.explanation,
                            tagsJson = "[\"Imported\"]"
                        )
                    )
                    formulasCount++
                }

                for (qDto in chapDto.questions) {
                    insertQuestion(
                        QuestionEntity(
                            chapterId = chapterId,
                            subjectId = subjectId,
                            batchId = resolvedBatchId,
                            type = qDto.type,
                            questionText = qDto.question,
                            optionsJson = JSONArray(qDto.options).toString(),
                            correctAnswersJson = JSONArray(qDto.correctAnswers).toString(),
                            marks = qDto.marks,
                            negativeMarks = qDto.negativeMarks,
                            difficulty = qDto.difficulty,
                            topic = qDto.topic,
                            hint = qDto.hint,
                            explanation = qDto.explanation,
                            detailedSolution = qDto.detailedSolution,
                            tagsJson = "[\"Imported\"]"
                        )
                    )
                    questionsCount++
                }
            }
        }

        CurriculumImportSummary(
            batchId = resolvedBatchId,
            batchName = batchName,
            subjectsCount = subjectsCount,
            chaptersCount = chaptersCount,
            notesCount = notesCount,
            formulasCount = formulasCount,
            questionsCount = questionsCount
        )
    }

    private data class AnalyticsQuad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    // Global Analytics Stream
    fun getGlobalAnalytics(): Flow<GlobalAnalytics> {
        val baseFlow = combine(allChapters, allSubjects, allQuestions) { chapters, subjects, questions ->
            Triple(chapters, subjects, questions)
        }
        val activityFlow = combine(completedAttempts, allMistakes, allSessions, getRevisionDueItems()) { attempts, mistakes, sessions, revisionDue ->
            AnalyticsQuad(attempts, mistakes, sessions, revisionDue)
        }

        return combine(baseFlow, activityFlow) { base, act ->
            val chapters = base.first
            val subjects = base.second
            val questions = base.third
            val attempts = act.first
            val mistakes = act.second
            val sessions = act.third
            val revisionDue = act.fourth

            val totalStudyMinutes = sessions.sumOf { it.durationMinutes.toLong() }
            val totalQuestionsSolved = questions.sumOf { it.timesCorrect }
            val totalTestsCompleted = attempts.size
            val avgScore = if (attempts.isNotEmpty()) attempts.sumOf { it.accuracyPercentage } / attempts.size else 0.0
            val overallAccuracy = if (questions.sumOf { it.timesAttempted } > 0) {
                (questions.sumOf { it.timesCorrect }.toDouble() / questions.sumOf { it.timesAttempted }.toDouble() * 100.0)
            } else {
                0.0
            }
            val resolvedMistakes = mistakes.count { it.isResolved }

            // Subject Masteries
            val subjectMasteryList = subjects.map { s ->
                val chapForSub = chapters.filter { it.subjectId == s.id }
                val avgMastery = if (chapForSub.isNotEmpty()) chapForSub.sumOf { it.masteryScore } / chapForSub.size else 0
                SubjectMasteryInfo(
                    subjectId = s.id,
                    subjectName = s.name,
                    masteryScore = avgMastery,
                    chapterCount = chapForSub.size,
                    testsCompleted = attempts.count { it.testTitle.contains(s.name, ignoreCase = true) },
                    accuracyPercentage = avgMastery
                )
            }

            // Chapter progress
            val chapterInfos = chapters.map { c ->
                val subName = subjects.find { it.id == c.subjectId }?.name ?: ""
                ChapterProgressInfo(
                    chapterId = c.id,
                    chapterName = c.name,
                    subjectName = subName,
                    masteryScore = c.masteryScore,
                    notesCount = 0,
                    formulasCount = 0,
                    questionsCount = questions.count { it.chapterId == c.id },
                    testsCount = 0,
                    solvedQuestionsCount = questions.filter { it.chapterId == c.id }.sumOf { it.timesCorrect },
                    mistakeCount = mistakes.count { it.chapterId == c.id && !it.isResolved }
                )
            }

            val weak = chapterInfos.filter { it.masteryScore < 60 }.sortedBy { it.masteryScore }
            val strong = chapterInfos.filter { it.masteryScore >= 60 }.sortedByDescending { it.masteryScore }

            val recentTrend = attempts.take(10).map {
                it.testTitle.take(12) to it.accuracyPercentage
            }

            // Study streak calculation
            val distinctDays = sessions.map { it.dateMillis / 86400000L }.distinct().sortedDescending()
            var streak = 0
            val todayDay = System.currentTimeMillis() / 86400000L
            if (distinctDays.contains(todayDay) || distinctDays.contains(todayDay - 1)) {
                var currentCheck = if (distinctDays.contains(todayDay)) todayDay else todayDay - 1
                while (distinctDays.contains(currentCheck)) {
                    streak++
                    currentCheck--
                }
            }

            GlobalAnalytics(
                totalStudyTimeMinutes = totalStudyMinutes,
                totalQuestionsSolved = totalQuestionsSolved,
                totalTestsCompleted = totalTestsCompleted,
                overallAverageScore = avgScore,
                overallAccuracy = overallAccuracy,
                totalMistakesRecorded = mistakes.size,
                resolvedMistakesCount = resolvedMistakes,
                revisionDueTodayCount = revisionDue.size,
                currentStreakDays = streak,
                subjectMasteries = subjectMasteryList,
                weakChapters = weak,
                strongChapters = strong,
                recentScoreTrend = recentTrend
            )
        }.flowOn(Dispatchers.IO)
    }

    // Optional sample data seeder for development/testing
    suspend fun seedSampleDataIfEmpty() = withContext(Dispatchers.IO) {
        val existing = db.batchDao().getAllBatches().first()
        if (existing.isNotEmpty()) return@withContext

        val batchId = db.batchDao().insertBatch(
            BatchEntity(
                name = "Engineering & Tech Batch 2026",
                courseOrClass = "Undergraduate & Competitive Prep",
                session = "2026-2027",
                goal = "Master Calculus, Physics & Algorithms",
                description = "Focused learning system for deep mastery and high-accuracy test preparation."
            )
        )

        val mathSubId = db.subjectDao().insertSubject(
            SubjectEntity(
                batchId = batchId,
                name = "Mathematics",
                iconName = "calculate",
                colorHex = "#4F46E5"
            )
        )

        val physicsSubId = db.subjectDao().insertSubject(
            SubjectEntity(
                batchId = batchId,
                name = "Physics",
                iconName = "science",
                colorHex = "#0EA5E9"
            )
        )

        val intChapId = db.chapterDao().insertChapter(
            ChapterEntity(
                subjectId = mathSubId,
                batchId = batchId,
                name = "Integration & Calculus",
                orderIndex = 1,
                masteryScore = 78
            )
        )

        val limitsChapId = db.chapterDao().insertChapter(
            ChapterEntity(
                subjectId = mathSubId,
                batchId = batchId,
                name = "Limits & Continuity",
                orderIndex = 2,
                masteryScore = 65
            )
        )

        // Seed sample note with markdown and LaTeX
        db.noteDao().insertNote(
            NoteEntity(
                chapterId = intChapId,
                subjectId = mathSubId,
                batchId = batchId,
                title = "Fundamental Theorem of Calculus & Methods",
                contentMarkdown = "# Fundamental Theorem of Calculus\n\nThe fundamental theorem links differentiation and integration:\n\n$$\\int_a^b f(x) \\, dx = F(b) - F(a)$$\n\nwhere \$F'(x) = f(x)\$.\n\n[CONCEPT]\nIntegration is the continuous analog of summation, finding the signed area under a curve.\n\n### Integration by Parts\n$$\\int u \\, dv = uv - \\int v \\, du$$\n\nChoose u according to the **LIATE** rule:\n- **L**ogarithmic\n- **I**nverse trigonometric\n- **A**lgebraic\n- **T**rigonometric\n- **E**xponential\n\n[COMMON MISTAKE]\nForgetting the integration constant + C in indefinite integrals or switching the order of u and v.\n\n[FORMULA]\n$$\\int \\frac{1}{\\sqrt{a^2 - x^2}} \\, dx = \\arcsin\\left(\\frac{x}{a}\\right) + C$$\n"
            )
        )

        // Seed formula
        db.formulaDao().insertFormula(
            FormulaEntity(
                chapterId = intChapId,
                subjectId = mathSubId,
                batchId = batchId,
                title = "Standard Power Rule",
                formulaLatex = "\\int x^n dx = \\frac{x^{n+1}}{n+1} + C, \\quad n \\neq -1",
                explanation = "Applies to all real powers except n = -1 where the integral yields ln|x|."
            )
        )

        // Seed Questions
        val q1Id = db.questionDao().insertQuestion(
            QuestionEntity(
                chapterId = intChapId,
                subjectId = mathSubId,
                batchId = batchId,
                type = QuestionType.MCQ,
                questionText = "What is the antiderivative of \$\\int \\sin(2x) \\, dx\$?",
                optionsJson = JSONArray(listOf("-\\frac{1}{2}\\cos(2x) + C", "\\frac{1}{2}\\cos(2x) + C", "-2\\cos(2x) + C", "2\\cos(2x) + C")).toString(),
                correctAnswersJson = JSONArray(listOf("0")).toString(),
                explanation = "Using substitution u = 2x, du = 2dx, so \\int \\sin(u) \\frac{du}{2} = -\\frac{1}{2}\\cos(2x) + C.",
                marks = 2.0,
                negativeMarks = 0.5,
                topic = "Trigonometric Integrals",
                difficulty = Difficulty.EASY
            )
        )

        val q2Id = db.questionDao().insertQuestion(
            QuestionEntity(
                chapterId = intChapId,
                subjectId = mathSubId,
                batchId = batchId,
                type = QuestionType.MULTIPLE_CORRECT,
                questionText = "Which of the following functions have a derivative of 2x?",
                optionsJson = JSONArray(listOf("x^2 + 7", "x^2 - \\pi", "x^2", "2x^2")).toString(),
                correctAnswersJson = JSONArray(listOf("0", "1", "2")).toString(),
                explanation = "Any expression of the form x^2 + C has derivative 2x.",
                marks = 4.0,
                negativeMarks = 1.0,
                topic = "Indefinite Integrals",
                difficulty = Difficulty.MEDIUM
            )
        )

        val q3Id = db.questionDao().insertQuestion(
            QuestionEntity(
                chapterId = intChapId,
                subjectId = mathSubId,
                batchId = batchId,
                type = QuestionType.NUMERICAL,
                questionText = "Evaluate \$\\int_0^3 2x \\, dx\$.",
                optionsJson = "[]",
                correctAnswersJson = JSONArray(listOf("9")).toString(),
                explanation = "[x^2]_0^3 = 3^2 - 0^2 = 9.",
                marks = 3.0,
                negativeMarks = 0.0,
                topic = "Definite Integrals",
                difficulty = Difficulty.EASY
            )
        )

        // Seed a sample test
        db.testDao().insertTest(
            TestEntity(
                batchId = batchId,
                subjectId = mathSubId,
                chapterId = intChapId,
                title = "Calculus Diagnostic Exam 01",
                mode = com.studyforge.app.domain.model.TestMode.EXAM,
                durationMinutes = 20,
                totalMarks = 9.0,
                negativeMarksPerWrong = 0.5,
                questionIdsJson = JSONArray(listOf(q1Id, q2Id, q3Id)).toString()
            )
        )

        // Log sample study session
        db.studySessionDao().insertSession(
            StudySessionEntity(
                batchId = batchId,
                subjectId = mathSubId,
                chapterId = intChapId,
                durationMinutes = 45,
                questionsSolved = 8,
                notes = "Calculus theorem review & practice"
            )
        )
    }
}
