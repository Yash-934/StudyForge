package com.studyforge.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyforge.app.data.local.AppDatabase
import com.studyforge.app.data.local.StudyAlarmScheduler
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
import com.studyforge.app.data.local.entities.TestAttemptEntity
import com.studyforge.app.data.local.entities.TestEntity
import com.studyforge.app.data.local.entities.TimetableEntity
import com.studyforge.app.data.repository.PreferencesRepository
import com.studyforge.app.data.repository.StudyRepository
import com.studyforge.app.domain.ai.AiResponse
import com.studyforge.app.domain.ai.AiStudyAssistant
import com.studyforge.app.domain.ai.GeminiAiStudyAssistant
import com.studyforge.app.domain.json.BackupManager
import com.studyforge.app.domain.json.BulkQuestionBankImportSummary
import com.studyforge.app.domain.json.BulkQuestionBankParser
import com.studyforge.app.domain.json.BulkQuestionBankValidationResult
import com.studyforge.app.domain.json.CurriculumJsonParser
import com.studyforge.app.domain.json.CurriculumValidationResult
import com.studyforge.app.domain.json.TestJsonParser
import com.studyforge.app.domain.model.Difficulty
import com.studyforge.app.domain.model.GlobalAnalytics
import com.studyforge.app.domain.model.Mood
import com.studyforge.app.domain.model.QuestionDto
import com.studyforge.app.domain.model.QuestionType
import com.studyforge.app.domain.model.SmartTestDistribution
import com.studyforge.app.domain.model.SmartTestGenerator
import com.studyforge.app.domain.model.TestImportDto
import com.studyforge.app.domain.model.TestMode
import com.studyforge.app.domain.model.TestValidationResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray

class StudyViewModel(
    val repository: StudyRepository,
    private val preferencesRepository: PreferencesRepository,
    private val db: AppDatabase
) : ViewModel() {

    // Preferences / Settings State
    val themeMode: StateFlow<String> = preferencesRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, "SYSTEM")

    val geminiApiKeyOverride: StateFlow<String> = preferencesRepository.geminiApiKeyOverride
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    // Global AI Assistant initialized with preferences provider
    val aiAssistant: AiStudyAssistant = GeminiAiStudyAssistant(
        userApiKeyProvider = { preferencesRepository.getGeminiApiKeyOverrideSync() }
    )

    // Data streams from repository
    val batches: StateFlow<List<BatchEntity>> = repository.allBatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chapters: StateFlow<List<ChapterEntity>> = repository.allChapters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val formulas: StateFlow<List<FormulaEntity>> = repository.allFormulas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val questions: StateFlow<List<QuestionEntity>> = repository.allQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tests: StateFlow<List<TestEntity>> = repository.allTests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attempts: StateFlow<List<TestAttemptEntity>> = repository.completedAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMistakes: StateFlow<List<MistakeEntity>> = repository.allMistakes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mistakes: StateFlow<List<MistakeEntity>> get() = allMistakes

    val unresolvedMistakes: StateFlow<List<MistakeEntity>> = repository.unresolvedMistakes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val flashcards: StateFlow<List<FlashcardEntity>> = repository.allFlashcards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val revisionDueItems: StateFlow<List<RevisionItemEntity>> = repository.getRevisionDueItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timetable: StateFlow<List<TimetableEntity>> = repository.allTimetable
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val journals: StateFlow<List<JournalEntity>> = repository.allJournals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<StudySessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val analytics: StateFlow<GlobalAnalytics> = repository.getGlobalAnalytics()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            GlobalAnalytics()
        )

    // UI Feedback events (Toasts/Snackbars)
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Import Validation State
    private val _importValidationResult = MutableStateFlow<TestValidationResult?>(null)
    val importValidationResult: StateFlow<TestValidationResult?> = _importValidationResult.asStateFlow()

    // AI Responses State
    private val _aiResponseState = MutableStateFlow<AiResponse<String>>(AiResponse.Idle)
    val aiResponseState: StateFlow<AiResponse<String>> = _aiResponseState.asStateFlow()

    private val _generatedQuestionsState = MutableStateFlow<AiResponse<List<QuestionDto>>>(AiResponse.Idle)
    val generatedQuestionsState: StateFlow<AiResponse<List<QuestionDto>>> = _generatedQuestionsState.asStateFlow()

    // -------------------------------------------------------------
    // Test Taking State
    // -------------------------------------------------------------
    private val _activeTest = MutableStateFlow<TestEntity?>(null)
    val activeTest: StateFlow<TestEntity?> = _activeTest.asStateFlow()

    private val _activeQuestions = MutableStateFlow<List<QuestionEntity>>(emptyList())
    val activeQuestions: StateFlow<List<QuestionEntity>> = _activeQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _userAnswers = MutableStateFlow<Map<Long, List<String>>>(emptyMap())
    val userAnswers: StateFlow<Map<Long, List<String>>> = _userAnswers.asStateFlow()

    private val _markedForReview = MutableStateFlow<Set<Long>>(emptySet())
    val markedForReview: StateFlow<Set<Long>> = _markedForReview.asStateFlow()

    private val _timeRemainingSeconds = MutableStateFlow(0)
    val timeRemainingSeconds: StateFlow<Int> = _timeRemainingSeconds.asStateFlow()

    private var activeAttemptId: Long = 0L
    private val questionTimeSpent = mutableMapOf<Long, Int>()
    private var testTimerJob: Job? = null

    fun showMessage(message: String) {
        viewModelScope.launch {
            _userMessage.emit(message)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setGeminiApiKeyOverride(key: String) {
        viewModelScope.launch {
            preferencesRepository.setGeminiApiKeyOverride(key.trim())
            showMessage(if (key.isBlank()) "Reset to default Gemini API" else "Gemini API key saved")
        }
    }

    // -------------------------------------------------------------
    // Batch Management
    // -------------------------------------------------------------
    fun createBatch(name: String, course: String, session: String, goal: String, description: String = "") {
        viewModelScope.launch {
            val b = BatchEntity(
                name = name.trim(),
                courseOrClass = course.trim(),
                session = session.trim(),
                goal = goal.trim(),
                description = description.trim()
            )
            repository.insertBatch(b)
            showMessage("Batch created: $name")
        }
    }

    fun deleteBatch(batch: BatchEntity) {
        viewModelScope.launch {
            repository.deleteBatch(batch)
            showMessage("Batch deleted")
        }
    }

    // -------------------------------------------------------------
    // Subject Management
    // -------------------------------------------------------------
    fun createSubject(batchId: Long, name: String, icon: String = "book", colorHex: String = "#4F46E5") {
        viewModelScope.launch {
            val s = SubjectEntity(
                batchId = batchId,
                name = name.trim(),
                iconName = icon,
                colorHex = colorHex
            )
            repository.insertSubject(s)
            showMessage("Subject created: $name")
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
            showMessage("Subject deleted")
        }
    }

    // -------------------------------------------------------------
    // Chapter Management
    // -------------------------------------------------------------
    fun createChapter(subjectId: Long, batchId: Long, name: String) {
        viewModelScope.launch {
            val c = ChapterEntity(
                subjectId = subjectId,
                batchId = batchId,
                name = name.trim()
            )
            repository.insertChapter(c)
            showMessage("Chapter created: $name")
        }
    }

    fun deleteChapter(chapter: ChapterEntity) {
        viewModelScope.launch {
            repository.deleteChapter(chapter)
            showMessage("Chapter deleted")
        }
    }

    // -------------------------------------------------------------
    // Note Management
    // -------------------------------------------------------------
    fun saveNote(
        id: Long = 0L,
        chapterId: Long,
        subjectId: Long,
        batchId: Long,
        title: String,
        contentMarkdown: String,
        tags: List<String> = emptyList(),
        isFavorite: Boolean = false,
        isPinned: Boolean = false
    ) {
        viewModelScope.launch {
            val note = NoteEntity(
                id = id,
                chapterId = chapterId,
                subjectId = subjectId,
                batchId = batchId,
                title = title.trim(),
                contentMarkdown = contentMarkdown,
                tagsJson = JSONArray(tags).toString(),
                isFavorite = isFavorite,
                isPinned = isPinned,
                updatedAt = System.currentTimeMillis()
            )
            if (id == 0L) {
                repository.insertNote(note)
                showMessage("Note created")
            } else {
                repository.updateNote(note)
                showMessage("Note saved")
            }
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
            showMessage("Note deleted")
        }
    }

    // -------------------------------------------------------------
    // Formula Vault
    // -------------------------------------------------------------
    fun saveFormula(
        id: Long = 0L,
        chapterId: Long,
        subjectId: Long,
        batchId: Long,
        title: String,
        formulaLatex: String,
        explanation: String = "",
        tags: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val formula = FormulaEntity(
                id = id,
                chapterId = chapterId,
                subjectId = subjectId,
                batchId = batchId,
                title = title.trim(),
                formulaLatex = formulaLatex.trim(),
                explanation = explanation.trim(),
                tagsJson = JSONArray(tags).toString()
            )
            if (id == 0L) {
                repository.insertFormula(formula)
                showMessage("Formula added to vault")
            } else {
                repository.updateFormula(formula)
                showMessage("Formula updated")
            }
        }
    }

    fun deleteFormula(formula: FormulaEntity) {
        viewModelScope.launch {
            repository.deleteFormula(formula)
            showMessage("Formula removed")
        }
    }

    // -------------------------------------------------------------
    // Question Bank
    // -------------------------------------------------------------
    fun saveQuestion(
        id: Long = 0L,
        chapterId: Long,
        subjectId: Long,
        batchId: Long,
        type: QuestionType,
        questionText: String,
        options: List<String>,
        correctAnswers: List<String>,
        explanation: String = "",
        detailedSolution: String = "",
        marks: Double = 1.0,
        negativeMarks: Double = 0.0,
        difficulty: Difficulty = Difficulty.MEDIUM,
        topic: String = "",
        hint: String = ""
    ) {
        viewModelScope.launch {
            val q = QuestionEntity(
                id = id,
                chapterId = chapterId,
                subjectId = subjectId,
                batchId = batchId,
                type = type,
                questionText = questionText.trim(),
                optionsJson = JSONArray(options).toString(),
                correctAnswersJson = JSONArray(correctAnswers).toString(),
                explanation = explanation.trim(),
                detailedSolution = detailedSolution.trim(),
                marks = marks,
                negativeMarks = negativeMarks,
                difficulty = difficulty,
                topic = topic.trim(),
                hint = hint.trim()
            )
            if (id == 0L) {
                repository.insertQuestion(q)
                showMessage("Question added to bank")
            } else {
                repository.updateQuestion(q)
                showMessage("Question updated")
            }
        }
    }

    fun deleteQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            repository.deleteQuestion(question)
            showMessage("Question deleted")
        }
    }

    // -------------------------------------------------------------
    // Flashcards & Spaced Repetition
    // -------------------------------------------------------------
    fun createFlashcard(
        chapterId: Long,
        subjectId: Long,
        front: String,
        back: String,
        difficulty: Difficulty = Difficulty.MEDIUM
    ) {
        viewModelScope.launch {
            val card = FlashcardEntity(
                chapterId = chapterId,
                subjectId = subjectId,
                front = front.trim(),
                back = back.trim(),
                difficulty = difficulty
            )
            repository.insertFlashcard(card)
            showMessage("Flashcard created")
        }
    }

    fun rateFlashcard(card: FlashcardEntity, quality: Int) {
        viewModelScope.launch {
            repository.rateFlashcard(card, quality)
        }
    }

    fun deleteFlashcard(card: FlashcardEntity) {
        viewModelScope.launch {
            repository.deleteFlashcard(card)
            showMessage("Flashcard deleted")
        }
    }

    fun completeRevisionItem(item: RevisionItemEntity, qualityRating: Int = 3) {
        viewModelScope.launch {
            repository.completeRevisionItem(item, qualityRating)
            showMessage("Revision item reviewed!")
        }
    }

    // -------------------------------------------------------------
    // Mistake Book
    // -------------------------------------------------------------
    fun resolveMistake(mistakeId: Long, note: String = "") {
        viewModelScope.launch {
            repository.resolveMistake(mistakeId, note)
            showMessage("Mistake marked as resolved! Great job.")
        }
    }

    fun resolveMistake(mistake: MistakeEntity, note: String = "") {
        resolveMistake(mistake.id, note)
    }

    // -------------------------------------------------------------
    // Timetable & Planner
    // -------------------------------------------------------------
    fun addTimetableEntry(
        subjectId: Long? = null,
        subjectName: String,
        chapterTask: String = "",
        chapterOrTask: String = chapterTask,
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        reminderEnabled: Boolean,
        context: Context? = null
    ) {
        viewModelScope.launch {
            val effectiveTask = if (chapterTask.isNotBlank()) chapterTask else chapterOrTask
            val entry = TimetableEntity(
                subjectId = subjectId,
                subjectName = subjectName.trim(),
                chapterOrTask = effectiveTask.trim(),
                dayOfWeek = dayOfWeek,
                startTime = startTime.trim(),
                endTime = endTime.trim(),
                reminderEnabled = reminderEnabled
            )
            val id = repository.insertTimetableEntry(entry)
            if (reminderEnabled && context != null) {
                StudyAlarmScheduler.scheduleAlarm(context, entry.copy(id = id))
            }
            showMessage("Timetable slot added")
        }
    }

    fun toggleTimetableAlarm(entry: TimetableEntity, enabled: Boolean, context: Context? = null) {
        viewModelScope.launch {
            val updated = entry.copy(reminderEnabled = enabled)
            repository.updateTimetableEntry(updated)
            if (context != null) {
                if (enabled) {
                    StudyAlarmScheduler.scheduleAlarm(context, updated)
                } else {
                    StudyAlarmScheduler.cancelAlarm(context, updated.id)
                }
            }
        }
    }

    fun deleteTimetableEntry(entry: TimetableEntity, context: Context? = null) {
        viewModelScope.launch {
            if (context != null && entry.reminderEnabled) {
                StudyAlarmScheduler.cancelAlarm(context, entry.id)
            }
            repository.deleteTimetableEntry(entry)
            showMessage("Timetable entry deleted")
        }
    }

    fun saveJournal(journal: JournalEntity) {
        viewModelScope.launch {
            repository.insertJournalEntry(journal)
            showMessage("Daily reflection saved")
        }
    }

    fun saveJournal(
        dateMillis: Long,
        freeText: String,
        mood: Mood = Mood.GOOD,
        accomplishments: String = "",
        difficulties: String = "",
        tomorrowPlan: String = "",
        studyTimeMinutes: Int = 0,
        questionsSolved: Int = 0
    ) {
        saveJournal(
            JournalEntity(
                dateMillis = dateMillis,
                freeText = freeText.trim(),
                mood = mood,
                accomplishments = accomplishments.trim(),
                difficulties = difficulties.trim(),
                tomorrowPlan = tomorrowPlan.trim(),
                studyTimeMinutes = studyTimeMinutes,
                questionsSolved = questionsSolved
            )
        )
    }

    // -------------------------------------------------------------
    // Test Generation & Management
    // -------------------------------------------------------------
    fun createManualTest(
        title: String,
        batchId: Long?,
        subjectId: Long?,
        chapterId: Long?,
        mode: TestMode,
        durationMinutes: Int,
        negativeMarks: Double = 0.0,
        questionIds: List<Long>,
        onCreated: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val test = TestEntity(
                title = title.trim().ifBlank { "Practice Test" },
                batchId = batchId,
                subjectId = subjectId,
                chapterId = chapterId,
                mode = mode,
                durationMinutes = durationMinutes,
                negativeMarksPerWrong = negativeMarks,
                questionIdsJson = JSONArray(questionIds).toString()
            )
            val id = repository.insertTest(test)
            showMessage("Test created!")
            onCreated(id)
        }
    }

    fun createSmartTest(
        title: String,
        batchId: Long?,
        subjectId: Long?,
        chapterId: Long?,
        targetCount: Int = 10,
        durationMinutes: Int = 30,
        distribution: SmartTestDistribution = SmartTestDistribution(),
        onCreated: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val allQ = questions.value.filter { q ->
                (chapterId == null || q.chapterId == chapterId) &&
                (subjectId == null || q.subjectId == subjectId)
            }
            val pool = if (allQ.isNotEmpty()) allQ else questions.value
            val allM = mistakes.value
            val selected = SmartTestGenerator.selectQuestions(
                allQuestions = pool,
                allMistakes = allM,
                targetQuestionCount = targetCount,
                distribution = distribution
            )
            val questionIds = selected.map { it.id }
            val test = TestEntity(
                title = title.trim().ifBlank { "Adaptive Smart Test" },
                batchId = batchId,
                subjectId = subjectId,
                chapterId = chapterId,
                mode = TestMode.PRACTICE,
                durationMinutes = durationMinutes,
                questionIdsJson = JSONArray(questionIds).toString()
            )
            val id = repository.insertTest(test)
            showMessage("Smart test generated with ${questionIds.size} questions!")
            onCreated(id)
        }
    }

    // -------------------------------------------------------------
    // Test Player Execution Engine
    // -------------------------------------------------------------
    fun startTest(testId: Long, onReady: () -> Unit = {}) {
        viewModelScope.launch {
            val test = repository.getTestById(testId) ?: return@launch
            val ids = try {
                val arr = JSONArray(test.questionIdsJson)
                (0 until arr.length()).map { arr.getLong(it) }
            } catch (e: Exception) {
                emptyList()
            }
            val qList = repository.getQuestionsByIds(ids)
            _activeTest.value = test
            _activeQuestions.value = qList
            _currentQuestionIndex.value = 0
            _userAnswers.value = emptyMap()
            _markedForReview.value = emptySet()
            _timeRemainingSeconds.value = test.durationMinutes * 60
            questionTimeSpent.clear()

            activeAttemptId = repository.startTestAttempt(test.id, test.title, qList.size)

            testTimerJob?.cancel()
            testTimerJob = viewModelScope.launch {
                while (_timeRemainingSeconds.value > 0) {
                    delay(1000)
                    _timeRemainingSeconds.value -= 1
                    val currentQ = _activeQuestions.value.getOrNull(_currentQuestionIndex.value)
                    if (currentQ != null) {
                        questionTimeSpent[currentQ.id] = (questionTimeSpent[currentQ.id] ?: 0) + 1
                    }
                }
            }

            onReady()
        }
    }

    fun goToQuestion(index: Int) {
        if (index in _activeQuestions.value.indices) {
            _currentQuestionIndex.value = index
        }
    }

    fun selectAnswer(questionId: Long, answer: String, isMultiple: Boolean = false) {
        val current = _userAnswers.value.toMutableMap()
        if (!isMultiple) {
            current[questionId] = listOf(answer)
        } else {
            val existing = current[questionId]?.toMutableList() ?: mutableListOf()
            if (existing.contains(answer)) {
                existing.remove(answer)
            } else {
                existing.add(answer)
            }
            current[questionId] = existing
        }
        _userAnswers.value = current

        // Save progress draft
        viewModelScope.launch {
            val draft = org.json.JSONObject()
            current.forEach { (qid, ans) ->
                draft.put(qid.toString(), JSONArray(ans))
            }
            repository.updateAttemptDraft(activeAttemptId, draft.toString())
        }
    }

    fun clearAnswer(questionId: Long) {
        val current = _userAnswers.value.toMutableMap()
        current.remove(questionId)
        _userAnswers.value = current
    }

    fun toggleMarkForReview(questionId: Long) {
        val current = _markedForReview.value.toMutableSet()
        if (current.contains(questionId)) {
            current.remove(questionId)
        } else {
            current.add(questionId)
        }
        _markedForReview.value = current
    }

    fun submitActiveTest(onCompleted: (Long) -> Unit) {
        testTimerJob?.cancel()
        val test = _activeTest.value ?: return
        val questions = _activeQuestions.value
        val answers = _userAnswers.value
        val attemptId = activeAttemptId

        viewModelScope.launch {
            repository.submitTest(
                attemptId = attemptId,
                test = test,
                questions = questions,
                userAnswers = answers,
                questionTimeSpent = questionTimeSpent
            )
            onCompleted(attemptId)
        }
    }

    // -------------------------------------------------------------
    // JSON Import & Backup
    // -------------------------------------------------------------
    fun validateJson(jsonString: String): TestValidationResult {
        val result = TestJsonParser.parseAndValidate(jsonString)
        _importValidationResult.value = result
        return result
    }

    fun importValidatedTest(
        testDto: TestImportDto,
        importMode: Int,
        targetBatchId: Long,
        targetSubjectId: Long,
        targetChapterId: Long
    ) {
        viewModelScope.launch {
            val entities = testDto.questions.map { q ->
                QuestionEntity(
                    chapterId = targetChapterId,
                    subjectId = targetSubjectId,
                    batchId = targetBatchId,
                    type = q.type,
                    questionText = q.question,
                    optionsJson = JSONArray(q.options).toString(),
                    correctAnswersJson = JSONArray(q.correctAnswers).toString(),
                    explanation = q.explanation,
                    detailedSolution = q.detailedSolution,
                    marks = q.marks,
                    negativeMarks = q.negativeMarks,
                    difficulty = q.difficulty,
                    topic = q.topic,
                    hint = q.hint,
                    imageUrl = q.imageUrl,
                    timeLimitSeconds = q.timeLimitSeconds
                )
            }
            val insertedIds = repository.insertQuestions(entities)
            if (importMode == 1) { // As Test
                val test = TestEntity(
                    title = testDto.title.ifBlank { "Imported Test" },
                    batchId = targetBatchId,
                    subjectId = targetSubjectId,
                    chapterId = targetChapterId,
                    durationMinutes = testDto.durationMinutes,
                    questionIdsJson = JSONArray(insertedIds).toString()
                )
                repository.insertTest(test)
                showMessage("Imported ${insertedIds.size} questions as Test: ${test.title}")
            } else {
                showMessage("Imported ${insertedIds.size} questions into Question Bank!")
            }
        }
    }

    suspend fun exportFullBackup(): String {
        return BackupManager.createFullBackupJson(db)
    }

    suspend fun restoreFullBackup(json: String): Result<Unit> {
        return BackupManager.restoreFromBackupJson(db, json).map { }
    }

    // -------------------------------------------------------------
    // Curriculum (Batch & Subjects) JSON Import
    // -------------------------------------------------------------
    fun validateCurriculumJson(jsonString: String): CurriculumValidationResult {
        return CurriculumJsonParser.parseAndValidate(jsonString)
    }

    fun importCurriculum(
        curriculum: CurriculumValidationResult,
        targetBatchId: Long? = null,
        onCompleted: (batchId: Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            if (!curriculum.isValid) {
                showMessage("Cannot import: JSON format is invalid.")
                return@launch
            }
            try {
                val summary = repository.importCurriculum(curriculum, targetBatchId)
                val msg = buildString {
                    append("Successfully imported into '${summary.batchName}': ")
                    append("${summary.subjectsCount} subjects, ${summary.chaptersCount} chapters")
                    if (summary.notesCount > 0) append(", ${summary.notesCount} notes")
                    if (summary.formulasCount > 0) append(", ${summary.formulasCount} formulas")
                    if (summary.questionsCount > 0) append(", ${summary.questionsCount} questions")
                    append("!")
                }
                showMessage(msg)
                onCompleted(summary.batchId)
            } catch (e: Exception) {
                showMessage("Failed to import curriculum: ${e.message}")
            }
        }
    }

    // -------------------------------------------------------------
    // Bulk Question Bank Import (All Chapters at Once)
    // -------------------------------------------------------------
    fun validateBulkQuestionBankJson(json: String, defaultSubjectName: String? = null): BulkQuestionBankValidationResult {
        return BulkQuestionBankParser.parseAndValidate(json, defaultSubjectName)
    }

    fun importBulkQuestionBank(
        result: BulkQuestionBankValidationResult,
        targetBatchId: Long,
        fallbackSubjectId: Long?,
        onCompleted: (summary: BulkQuestionBankImportSummary) -> Unit = {}
    ) {
        viewModelScope.launch {
            if (!result.isValid) {
                showMessage("Cannot import: JSON format is invalid.")
                return@launch
            }
            try {
                val summary = repository.importBulkQuestionBank(result, targetBatchId, fallbackSubjectId)
                val msg = "Imported ${summary.totalQuestionsImported} questions across ${summary.chaptersAffected} chapters into '${summary.batchName}' (${summary.existingChaptersMatched} matched, ${summary.newChaptersCreated} created)!"
                showMessage(msg)
                onCompleted(summary)
            } catch (e: Exception) {
                showMessage("Failed to import questions: ${e.message}")
            }
        }
    }

    // -------------------------------------------------------------
    // Gemini AI Assistant Integration
    // -------------------------------------------------------------
    fun resetAiState() {
        _aiResponseState.value = AiResponse.Idle
        _generatedQuestionsState.value = AiResponse.Idle
    }

    fun askAiToExplain(concept: String, context: String) {
        viewModelScope.launch {
            _aiResponseState.value = AiResponse.Loading
            val res = aiAssistant.explainConcept(concept, context)
            _aiResponseState.value = res.fold(
                onSuccess = { AiResponse.Success(it) },
                onFailure = { AiResponse.Error(it.message ?: "Failed to generate explanation.") }
            )
        }
    }

    fun askAiToSimplify(noteTitle: String, noteContent: String) {
        viewModelScope.launch {
            _aiResponseState.value = AiResponse.Loading
            val res = aiAssistant.simplifyNote(noteTitle, noteContent)
            _aiResponseState.value = res.fold(
                onSuccess = { AiResponse.Success(it) },
                onFailure = { AiResponse.Error(it.message ?: "Failed to simplify note.") }
            )
        }
    }

    fun askAiToGenerateQuestions(noteTitle: String, noteContent: String, count: Int = 3) {
        viewModelScope.launch {
            _generatedQuestionsState.value = AiResponse.Loading
            val res = aiAssistant.generateQuestionsFromNote(noteTitle, noteContent, count)
            _generatedQuestionsState.value = res.fold(
                onSuccess = { AiResponse.Success(it) },
                onFailure = { AiResponse.Error(it.message ?: "Failed to generate questions.") }
            )
        }
    }

    fun saveAiGeneratedSummaryAsNote(
        chapterId: Long,
        batchId: Long,
        subjectId: Long,
        title: String,
        content: String = "",
        contentMarkdown: String = content
    ) {
        viewModelScope.launch {
            val noteContent = if (contentMarkdown.isNotBlank()) contentMarkdown else content
            val note = NoteEntity(
                chapterId = chapterId,
                subjectId = subjectId,
                batchId = batchId,
                title = title.trim(),
                contentMarkdown = noteContent,
                tagsJson = "[\"AI_Generated\"]",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertNote(note)
            showMessage("AI Summary saved as note: $title")
        }
    }

    fun saveAiGeneratedQuestionsToChapter(
        chapterId: Long,
        batchId: Long,
        subjectId: Long,
        questions: List<QuestionDto> = emptyList(),
        questionsList: List<QuestionDto> = questions
    ) {
        viewModelScope.launch {
            val list = if (questionsList.isNotEmpty()) questionsList else questions
            val entities = list.map { q ->
                QuestionEntity(
                    chapterId = chapterId,
                    subjectId = subjectId,
                    batchId = batchId,
                    type = q.type,
                    questionText = q.question,
                    optionsJson = JSONArray(q.options).toString(),
                    correctAnswersJson = JSONArray(q.correctAnswers).toString(),
                    explanation = q.explanation,
                    detailedSolution = q.detailedSolution,
                    marks = q.marks,
                    negativeMarks = q.negativeMarks,
                    difficulty = q.difficulty,
                    topic = q.topic,
                    hint = q.hint,
                    tagsJson = "[\"AI_Generated\"]"
                )
            }
            val insertedIds = repository.insertQuestions(entities)
            showMessage("Saved ${insertedIds.size} AI questions to Question Bank!")
        }
    }

    // -------------------------------------------------------------
    // AI Smart Performance Analysis & Adaptive Test Crafter
    // -------------------------------------------------------------
    fun askAiForOverallPerformanceAnalysis() {
        viewModelScope.launch {
            _aiResponseState.value = AiResponse.Loading
            val stats = analytics.value
            val unresMistakes = unresolvedMistakes.value
            val completedAttemptsList = attempts.value

            val perfSummary = buildString {
                appendLine("Overall Study Time: ${stats.totalStudyTimeMinutes} minutes")
                appendLine("Total Questions Solved: ${stats.totalQuestionsSolved}")
                appendLine("Total Tests Completed: ${stats.totalTestsCompleted}")
                appendLine("Overall Average Score: ${String.format("%.1f", stats.overallAverageScore)}%")
                appendLine("Overall Accuracy: ${String.format("%.1f", stats.overallAccuracy)}%")
                appendLine("Current Learning Streak: ${stats.currentStreakDays} days")
                appendLine("Total Mistakes Logged: ${stats.totalMistakesRecorded} (Unresolved: ${unresMistakes.size})")

                if (stats.weakChapters.isNotEmpty()) {
                    appendLine("\nWeakest Chapters Needing Intervention:")
                    stats.weakChapters.take(5).forEach { c ->
                        appendLine("- ${c.chapterName} (${c.subjectName}): Mastery ${c.masteryScore}%, Mistakes: ${c.mistakeCount}")
                    }
                }

                if (stats.strongChapters.isNotEmpty()) {
                    appendLine("\nTop Strong Chapters:")
                    stats.strongChapters.take(3).forEach { c ->
                        appendLine("- ${c.chapterName} (${c.subjectName}): Mastery ${c.masteryScore}%")
                    }
                }

                if (completedAttemptsList.isNotEmpty()) {
                    appendLine("\nRecent Test Attempts History:")
                    completedAttemptsList.takeLast(4).forEach { a ->
                        appendLine("- Test '${a.testTitle}': Score ${a.totalScore}/${a.maxScore}, Accuracy ${String.format("%.1f", a.accuracyPercentage)}%, Wrong: ${a.wrongCount}")
                    }
                }
            }

            val res = aiAssistant.analyzeOverallPerformance(perfSummary)
            _aiResponseState.value = res.fold(
                onSuccess = { AiResponse.Success(it) },
                onFailure = { AiResponse.Error(it.message ?: "Failed to generate performance analysis.") }
            )
        }
    }

    fun craftAndSaveAiAdaptiveTest(questionCount: Int = 5, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch {
            _aiResponseState.value = AiResponse.Loading
            val stats = analytics.value
            val unresMistakes = unresolvedMistakes.value
            val allQ = questions.value

            val weakContext = buildString {
                appendLine("Student Stats: Accuracy ${String.format("%.1f", stats.overallAccuracy)}%, Tests Completed: ${stats.totalTestsCompleted}")
                if (stats.weakChapters.isNotEmpty()) {
                    appendLine("Target Weak Chapters: " + stats.weakChapters.take(3).joinToString { it.chapterName })
                }
                if (unresMistakes.isNotEmpty()) {
                    appendLine("Mistake Questions to reinforce count: ${unresMistakes.size}")
                }
            }

            val aiQuestionsRes = aiAssistant.craftAdaptiveTest(weakContext, questionCount)
            val generatedList = aiQuestionsRes.getOrNull() ?: emptyList()

            // If AI generated questions, save them to the DB and create an adaptive test!
            val targetChapter = stats.weakChapters.firstOrNull()?.let { wc ->
                chapters.value.find { it.id == wc.chapterId }
            } ?: chapters.value.firstOrNull()

            val chapterId = targetChapter?.id ?: 1L
            val subjectId = targetChapter?.subjectId ?: 1L
            val batchId = targetChapter?.batchId ?: 1L

            val questionsToUse = if (generatedList.isNotEmpty()) {
                val entities = generatedList.map { qDto ->
                    QuestionEntity(
                        chapterId = chapterId,
                        subjectId = subjectId,
                        batchId = batchId,
                        type = qDto.type,
                        questionText = qDto.question,
                        optionsJson = JSONArray(qDto.options).toString(),
                        correctAnswersJson = JSONArray(qDto.correctAnswers).toString(),
                        explanation = qDto.explanation,
                        detailedSolution = qDto.detailedSolution,
                        marks = qDto.marks,
                        negativeMarks = qDto.negativeMarks,
                        difficulty = qDto.difficulty,
                        topic = qDto.topic,
                        hint = qDto.hint,
                        tagsJson = "[\"AI_Adaptive\"]"
                    )
                }
                val insertedIds = repository.insertQuestions(entities)
                insertedIds
            } else {
                // Fallback to smart question selection from existing questions
                val selected = SmartTestGenerator.selectQuestions(
                    allQuestions = allQ,
                    allMistakes = allMistakes.value,
                    targetQuestionCount = questionCount
                )
                selected.map { it.id }
            }

            if (questionsToUse.isEmpty()) {
                _aiResponseState.value = AiResponse.Error("No questions available to craft test. Please create or import questions first.")
                return@launch
            }

            val test = TestEntity(
                title = "AI Adaptive Test: Smart Performance Challenge",
                batchId = batchId,
                subjectId = subjectId,
                chapterId = chapterId,
                mode = TestMode.PRACTICE,
                durationMinutes = (questionsToUse.size * 2).coerceAtLeast(10),
                questionIdsJson = JSONArray(questionsToUse).toString()
            )
            val newTestId = repository.insertTest(test)
            _aiResponseState.value = AiResponse.Success(
                "### ✅ AI Adaptive Test Created Successfully!\n\n" +
                "**Title:** ${test.title}\n\n" +
                "**Questions Crafted:** ${questionsToUse.size}\n\n" +
                "**Duration:** ${test.durationMinutes} minutes\n\n" +
                "This adaptive test was tailored specifically to test your weak points and reinforce mistake patterns. You can start it right now!"
            )
            showMessage("AI Adaptive Test ready with ${questionsToUse.size} questions!")
            onCreated(newTestId)
        }
    }

    fun askAiToEvaluateTestAttempt(attemptId: Long) {
        viewModelScope.launch {
            _aiResponseState.value = AiResponse.Loading
            val attempt = repository.getAttemptById(attemptId)
            if (attempt == null) {
                _aiResponseState.value = AiResponse.Error("Attempt not found.")
                return@launch
            }
            val test = repository.getTestById(attempt.testId)
            val answers = repository.getAnswersForAttempt(attemptId)
            val questionIds = answers.map { it.questionId }
            val questionMap = repository.getQuestionsByIds(questionIds).associateBy { it.id }

            val scoreSummary = buildString {
                appendLine("Total Score: ${attempt.totalScore} / ${attempt.maxScore}")
                appendLine("Accuracy: ${String.format("%.1f", attempt.accuracyPercentage)}%")
                appendLine("Total Questions: ${attempt.totalQuestions}")
                appendLine("Answered: ${attempt.answeredCount}, Correct: ${attempt.correctCount}, Wrong: ${attempt.wrongCount}, Skipped: ${attempt.skippedCount}")
                appendLine("Time Spent: ${attempt.timeSpentSeconds / 60}m ${attempt.timeSpentSeconds % 60}s")
            }

            val questionsDetail = buildString {
                answers.forEachIndexed { idx, ans ->
                    val q = questionMap[ans.questionId]
                    appendLine("Question ${idx + 1}: ${q?.questionText?.take(60) ?: "Question"}...")
                    appendLine("  Result: ${if (ans.isCorrect) "CORRECT" else if (ans.isSkipped) "SKIPPED" else "WRONG"}")
                    appendLine("  User Answer: ${ans.userSelectedJson}")
                    appendLine("  Correct Answer: ${q?.correctAnswersJson}")
                    appendLine("  Time Spent: ${ans.timeSpentSeconds}s")
                }
            }

            val res = aiAssistant.evaluateTestPerformance(
                testTitle = test?.title ?: attempt.testTitle,
                scoreSummary = scoreSummary,
                questionsDetail = questionsDetail
            )
            _aiResponseState.value = res.fold(
                onSuccess = { AiResponse.Success(it) },
                onFailure = { AiResponse.Error(it.message ?: "Failed to generate test evaluation.") }
            )
        }
    }
}
