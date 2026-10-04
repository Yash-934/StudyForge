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
import com.studyforge.app.domain.json.TestJsonParser
import com.studyforge.app.domain.model.Difficulty
import com.studyforge.app.domain.model.GlobalAnalytics
import com.studyforge.app.domain.model.QuestionDto
import com.studyforge.app.domain.model.QuestionType
import com.studyforge.app.domain.model.SmartTestDistribution
import com.studyforge.app.domain.model.SmartTestGenerator
import com.studyforge.app.domain.model.TestMode
import com.studyforge.app.domain.model.TestScoringSummary
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class StudyViewModel(
    val repository: StudyRepository,
    val preferencesRepository: PreferencesRepository,
    private val db: AppDatabase
) : ViewModel() {

    // Preferences
    val themeMode: StateFlow<String> = preferencesRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val geminiApiKeyOverride: StateFlow<String> = preferencesRepository.geminiApiKeyOverride
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // AI Assistant
    val aiAssistant: AiStudyAssistant = GeminiAiStudyAssistant {
        geminiApiKeyOverride.value
    }

    private val _aiResponseState = MutableStateFlow<AiResponse<String>>(AiResponse.Idle)
    val aiResponseState: StateFlow<AiResponse<String>> = _aiResponseState.asStateFlow()

    private val _generatedQuestionsState = MutableStateFlow<AiResponse<List<QuestionDto>>>(AiResponse.Idle)
    val generatedQuestionsState: StateFlow<AiResponse<List<QuestionDto>>> = _generatedQuestionsState.asStateFlow()

    // Global Streams
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

    val unresolvedMistakes: StateFlow<List<MistakeEntity>> = repository.unresolvedMistakes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMistakes: StateFlow<List<MistakeEntity>> = repository.allMistakes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val flashcards: StateFlow<List<FlashcardEntity>> = repository.allFlashcards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<StudySessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timetable: StateFlow<List<TimetableEntity>> = repository.allTimetable
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val journals: StateFlow<List<JournalEntity>> = repository.allJournals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val revisionDueItems: StateFlow<List<RevisionItemEntity>> = repository.getRevisionDueItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val analytics: StateFlow<GlobalAnalytics> = repository.getGlobalAnalytics()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            GlobalAnalytics(0, 0, 0, 0.0, 0.0, 0, 0, 0, 0, emptyList(), emptyList(), emptyList(), emptyList())
        )

    // User Message Toast Event
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // -------------------------------------------------------------
    // Active Test Taking State
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

    private val _questionTimeSpent = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val questionTimeSpent: StateFlow<Map<Long, Int>> = _questionTimeSpent.asStateFlow()

    private var activeAttemptId: Long = 0L
    private var timerJob: Job? = null

    private val _lastSubmissionSummary = MutableStateFlow<TestScoringSummary?>(null)
    val lastSubmissionSummary: StateFlow<TestScoringSummary?> = _lastSubmissionSummary.asStateFlow()

    init {
        viewModelScope.launch {
            // Seed sample data on first run if database is empty
            repository.seedSampleDataIfEmpty()
        }
    }

    fun showMessage(msg: String) {
        viewModelScope.launch { _userMessage.emit(msg) }
    }

    // -------------------------------------------------------------
    // Batch, Subject, Chapter CRUD
    // -------------------------------------------------------------
    fun createBatch(name: String, courseOrClass: String, session: String, goal: String, description: String) {
        viewModelScope.launch {
            val id = repository.insertBatch(
                BatchEntity(
                    name = name.trim(),
                    courseOrClass = courseOrClass.trim(),
                    session = session.trim(),
                    goal = goal.trim(),
                    description = description.trim()
                )
            )
            showMessage("Batch created successfully")
        }
    }

    fun deleteBatch(batch: BatchEntity) {
        viewModelScope.launch {
            repository.deleteBatch(batch)
            showMessage("Batch deleted")
        }
    }

    fun createSubject(batchId: Long, name: String, iconName: String = "book", colorHex: String = "#4F46E5") {
        viewModelScope.launch {
            repository.insertSubject(
                SubjectEntity(batchId = batchId, name = name.trim(), iconName = iconName, colorHex = colorHex)
            )
            showMessage("Subject created")
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
            showMessage("Subject deleted")
        }
    }

    fun createChapter(subjectId: Long, batchId: Long, name: String) {
        viewModelScope.launch {
            repository.insertChapter(
                ChapterEntity(subjectId = subjectId, batchId = batchId, name = name.trim())
            )
            showMessage("Chapter created")
        }
    }

    fun deleteChapter(chapter: ChapterEntity) {
        viewModelScope.launch {
            repository.deleteChapter(chapter)
            showMessage("Chapter deleted")
        }
    }

    // -------------------------------------------------------------
    // Notes CRUD
    // -------------------------------------------------------------
    fun saveNote(
        id: Long = 0,
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
                title = title.ifBlank { "Untitled Note" },
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
    // Formulas CRUD
    // -------------------------------------------------------------
    fun saveFormula(
        id: Long = 0,
        chapterId: Long,
        subjectId: Long,
        batchId: Long,
        title: String,
        formulaLatex: String,
        explanation: String = ""
    ) {
        viewModelScope.launch {
            val formula = FormulaEntity(
                id = id,
                chapterId = chapterId,
                subjectId = subjectId,
                batchId = batchId,
                title = title.trim(),
                formulaLatex = formulaLatex.trim(),
                explanation = explanation.trim()
            )
            if (id == 0L) {
                repository.insertFormula(formula)
                showMessage("Formula added to Vault")
            } else {
                repository.updateFormula(formula)
                showMessage("Formula updated")
            }
        }
    }

    fun deleteFormula(formula: FormulaEntity) {
        viewModelScope.launch {
            repository.deleteFormula(formula)
            showMessage("Formula deleted")
        }
    }

    // -------------------------------------------------------------
    // Questions CRUD
    // -------------------------------------------------------------
    fun saveQuestion(
        id: Long = 0,
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
                showMessage("Question added to Question Bank")
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
    // Manual & Smart Test Creation
    // -------------------------------------------------------------
    fun createManualTest(
        title: String,
        batchId: Long?,
        subjectId: Long?,
        chapterId: Long?,
        mode: TestMode,
        durationMinutes: Int,
        negativeMarks: Double,
        questionIds: List<Long>
    ) {
        viewModelScope.launch {
            val questions = repository.getQuestionsByIds(questionIds)
            val totalMarks = questions.sumOf { it.marks }
            val test = TestEntity(
                batchId = batchId,
                subjectId = subjectId,
                chapterId = chapterId,
                title = title.trim(),
                mode = mode,
                durationMinutes = durationMinutes,
                totalMarks = totalMarks,
                negativeMarksPerWrong = negativeMarks,
                questionIdsJson = JSONArray(questionIds).toString()
            )
            repository.insertTest(test)
            showMessage("Test created with ${questionIds.size} questions")
        }
    }

    fun createSmartTest(
        title: String,
        batchId: Long?,
        subjectId: Long?,
        chapterId: Long?,
        targetCount: Int,
        durationMinutes: Int,
        distribution: SmartTestDistribution
    ) {
        viewModelScope.launch {
            val allQ = if (chapterId != null && chapterId > 0) {
                questions.value.filter { it.chapterId == chapterId }
            } else if (subjectId != null && subjectId > 0) {
                questions.value.filter { it.subjectId == subjectId }
            } else {
                questions.value
            }

            val mistakesList = allMistakes.value
            val selected = SmartTestGenerator.selectQuestions(
                allQuestions = allQ,
                allMistakes = mistakesList,
                targetQuestionCount = targetCount,
                distribution = distribution
            )

            if (selected.isEmpty()) {
                showMessage("Not enough questions to generate Smart Test")
                return@launch
            }

            val selectedIds = selected.map { it.id }
            val test = TestEntity(
                batchId = batchId,
                subjectId = subjectId,
                chapterId = chapterId,
                title = title.trim(),
                mode = TestMode.ADAPTIVE,
                durationMinutes = durationMinutes,
                totalMarks = selected.sumOf { it.marks },
                negativeMarksPerWrong = 0.25,
                questionIdsJson = JSONArray(selectedIds).toString()
            )
            repository.insertTest(test)
            showMessage("Smart Test generated with ${selected.size} adaptive questions!")
        }
    }

    // -------------------------------------------------------------
    // Test Taking Engine
    // -------------------------------------------------------------
    fun startTest(testId: Long, onReady: () -> Unit) {
        viewModelScope.launch {
            val test = repository.getTestById(testId) ?: return@launch
            val ids = try {
                val arr = JSONArray(test.questionIdsJson)
                (0 until arr.length()).map { arr.optLong(it) }
            } catch (e: Exception) {
                emptyList()
            }

            val qList = repository.getQuestionsByIds(ids)
            if (qList.isEmpty()) {
                showMessage("This test has no questions.")
                return@launch
            }

            _activeTest.value = test
            _activeQuestions.value = if (test.isRandomized) qList.shuffled() else qList
            _currentQuestionIndex.value = 0
            _userAnswers.value = emptyMap()
            _markedForReview.value = emptySet()
            _timeRemainingSeconds.value = test.durationMinutes * 60
            _questionTimeSpent.value = emptyMap()

            activeAttemptId = repository.startTestAttempt(test.id, test.title, qList.size)

            // Start countdown timer
            timerJob?.cancel()
            timerJob = viewModelScope.launch {
                while (_timeRemainingSeconds.value > 0) {
                    delay(1000)
                    _timeRemainingSeconds.value -= 1

                    // Track time spent on current question
                    val currentQ = _activeQuestions.value.getOrNull(_currentQuestionIndex.value)
                    if (currentQ != null) {
                        val currentSpent = _questionTimeSpent.value[currentQ.id] ?: 0
                        _questionTimeSpent.value = _questionTimeSpent.value + (currentQ.id to (currentSpent + 1))
                    }
                }
                // Time up! Auto-submit
                submitActiveTest {}
            }

            onReady()
        }
    }

    fun selectAnswer(questionId: Long, answer: String, isMultiple: Boolean = false) {
        val current = _userAnswers.value[questionId] ?: emptyList()
        val updated = if (isMultiple) {
            if (current.contains(answer)) current - answer else current + answer
        } else {
            listOf(answer)
        }
        _userAnswers.value = _userAnswers.value + (questionId to updated)

        // Autosave draft in background to prevent lost progress
        viewModelScope.launch {
            val json = JSONObject()
            _userAnswers.value.forEach { (qid, ans) ->
                json.put(qid.toString(), JSONArray(ans))
            }
            repository.updateAttemptDraft(activeAttemptId, json.toString())
        }
    }

    fun clearAnswer(questionId: Long) {
        _userAnswers.value = _userAnswers.value - questionId
    }

    fun toggleMarkForReview(questionId: Long) {
        val current = _markedForReview.value
        _markedForReview.value = if (current.contains(questionId)) current - questionId else current + questionId
    }

    fun goToQuestion(index: Int) {
        if (index in 0 until _activeQuestions.value.size) {
            _currentQuestionIndex.value = index
        }
    }

    fun submitActiveTest(onSubmitted: (Long) -> Unit) {
        timerJob?.cancel()
        val test = _activeTest.value ?: return
        val questions = _activeQuestions.value
        val attemptId = activeAttemptId

        viewModelScope.launch {
            val summary = repository.submitTest(
                attemptId = attemptId,
                test = test,
                questions = questions,
                userAnswers = _userAnswers.value,
                questionTimeSpent = _questionTimeSpent.value
            )
            _lastSubmissionSummary.value = summary
            _activeTest.value = null
            _activeQuestions.value = emptyList()
            onSubmitted(attemptId)
        }
    }

    // -------------------------------------------------------------
    // Mistake Book & Revision Actions
    // -------------------------------------------------------------
    fun resolveMistake(mistakeId: Long, note: String) {
        viewModelScope.launch {
            repository.resolveMistake(mistakeId, note)
            showMessage("Mistake marked as resolved! Keep up the great work.")
        }
    }

    fun completeRevisionItem(item: RevisionItemEntity, qualityRating: Int) {
        viewModelScope.launch {
            repository.completeRevisionItem(item, qualityRating)
            showMessage("Revision recorded! Next interval scheduled.")
        }
    }

    // -------------------------------------------------------------
    // Flashcard Actions
    // -------------------------------------------------------------
    fun createFlashcard(chapterId: Long, subjectId: Long, front: String, back: String, difficulty: Difficulty) {
        viewModelScope.launch {
            repository.insertFlashcard(
                FlashcardEntity(
                    chapterId = chapterId,
                    subjectId = subjectId,
                    front = front.trim(),
                    back = back.trim(),
                    difficulty = difficulty
                )
            )
            showMessage("Flashcard added")
        }
    }

    fun rateFlashcard(card: FlashcardEntity, quality: Int) {
        viewModelScope.launch {
            repository.rateFlashcard(card, quality)
        }
    }

    // -------------------------------------------------------------
    // Timetable & Journal Actions
    // -------------------------------------------------------------
    fun addTimetableEntry(
        subjectName: String,
        chapterTask: String,
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        reminderEnabled: Boolean = false,
        context: Context? = null
    ) {
        viewModelScope.launch {
            val entry = TimetableEntity(
                subjectName = subjectName.trim(),
                chapterOrTask = chapterTask.trim(),
                dayOfWeek = dayOfWeek,
                startTime = startTime.trim(),
                endTime = endTime.trim(),
                reminderEnabled = reminderEnabled
            )
            val id = repository.insertTimetableEntry(entry)
            if (reminderEnabled && context != null) {
                StudyAlarmScheduler.scheduleAlarm(context, entry.copy(id = id))
                showMessage("⏰ Schedule slot & Alarm added for ${entry.subjectName}!")
            } else {
                showMessage("Timetable entry added")
            }
        }
    }

    fun toggleTimetableAlarm(slot: TimetableEntity, enabled: Boolean, context: Context) {
        viewModelScope.launch {
            val updated = slot.copy(reminderEnabled = enabled)
            repository.updateTimetableEntry(updated)
            if (enabled) {
                StudyAlarmScheduler.scheduleAlarm(context, updated)
                showMessage("⏰ Alarm set for ${slot.subjectName} (${slot.startTime})")
            } else {
                StudyAlarmScheduler.cancelAlarm(context, slot.id)
                showMessage("Alarm turned off for ${slot.subjectName}")
            }
        }
    }

    fun deleteTimetableEntry(entry: TimetableEntity, context: Context? = null) {
        viewModelScope.launch {
            if (entry.reminderEnabled && context != null) {
                StudyAlarmScheduler.cancelAlarm(context, entry.id)
            }
            repository.deleteTimetableEntry(entry)
            showMessage("Timetable entry deleted")
        }
    }

    fun saveJournal(journal: JournalEntity) {
        viewModelScope.launch {
            repository.insertJournalEntry(journal)
            showMessage("Study journal saved")
        }
    }

    fun logStudySession(minutes: Int, questionsSolved: Int, notes: String, chapterId: Long? = null) {
        viewModelScope.launch {
            db.studySessionDao().insertSession(
                StudySessionEntity(
                    chapterId = chapterId,
                    durationMinutes = minutes,
                    questionsSolved = questionsSolved,
                    notes = notes
                )
            )
            showMessage("Study session logged")
        }
    }

    // -------------------------------------------------------------
    // JSON Test Import / Export
    // -------------------------------------------------------------
    fun validateJson(jsonString: String): TestValidationResult {
        return TestJsonParser.parseAndValidate(jsonString)
    }

    fun importValidatedTest(
        testDto: com.studyforge.app.domain.model.TestImportDto,
        importMode: Int, // 1 = as Test, 2 = Question Bank only, 3 = assign to chapter
        targetBatchId: Long,
        targetSubjectId: Long,
        targetChapterId: Long
    ) {
        viewModelScope.launch {
            // Save questions into database
            val questionEntities = testDto.questions.map { q ->
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
                    source = q.source
                )
            }
            val insertedIds = repository.insertQuestions(questionEntities)

            if (importMode == 1 || importMode == 3) {
                // Also create a test entity
                val test = TestEntity(
                    batchId = targetBatchId,
                    subjectId = targetSubjectId,
                    chapterId = targetChapterId,
                    title = testDto.title.ifBlank { "Imported Test" },
                    mode = TestMode.PRACTICE,
                    durationMinutes = testDto.durationMinutes,
                    totalMarks = testDto.totalMarks,
                    negativeMarksPerWrong = 0.25,
                    questionIdsJson = JSONArray(insertedIds).toString()
                )
                repository.insertTest(test)
                showMessage("Imported ${insertedIds.size} questions as test: ${test.title}")
            } else {
                showMessage("Imported ${insertedIds.size} questions into Question Bank")
            }
        }
    }

    // -------------------------------------------------------------
    // Full Backup & Restore
    // -------------------------------------------------------------
    suspend fun exportFullBackup(): String {
        return BackupManager.createFullBackupJson(db)
    }

    suspend fun restoreFullBackup(jsonString: String): Result<String> {
        val result = BackupManager.restoreFromBackupJson(db, jsonString)
        if (result.isSuccess) {
            showMessage("Database successfully restored!")
        }
        return result
    }

    // -------------------------------------------------------------
    // AI Study Assistant Actions
    // -------------------------------------------------------------
    fun askAiToExplain(concept: String, contextText: String) {
        viewModelScope.launch {
            _aiResponseState.value = AiResponse.Loading
            val res = aiAssistant.explainConcept(concept, contextText)
            res.fold(
                onSuccess = { _aiResponseState.value = AiResponse.Success(it) },
                onFailure = { _aiResponseState.value = AiResponse.Error(it.message ?: "AI request failed") }
            )
        }
    }

    fun askAiToSimplify(noteTitle: String, noteContent: String) {
        viewModelScope.launch {
            _aiResponseState.value = AiResponse.Loading
            val res = aiAssistant.simplifyNote(noteTitle, noteContent)
            res.fold(
                onSuccess = { _aiResponseState.value = AiResponse.Success(it) },
                onFailure = { _aiResponseState.value = AiResponse.Error(it.message ?: "AI request failed") }
            )
        }
    }

    fun askAiToGenerateQuestions(noteTitle: String, noteContent: String, count: Int = 3) {
        viewModelScope.launch {
            _generatedQuestionsState.value = AiResponse.Loading
            val res = aiAssistant.generateQuestionsFromNote(noteTitle, noteContent, count)
            res.fold(
                onSuccess = { _generatedQuestionsState.value = AiResponse.Success(it) },
                onFailure = { _generatedQuestionsState.value = AiResponse.Error(it.message ?: "AI request failed") }
            )
        }
    }

    fun resetAiState() {
        _aiResponseState.value = AiResponse.Idle
        _generatedQuestionsState.value = AiResponse.Idle
    }

    // Settings
    fun setThemeMode(mode: String) {
        viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    }

    fun setGeminiApiKeyOverride(key: String) {
        viewModelScope.launch {
            preferencesRepository.setGeminiApiKeyOverride(key)
            showMessage("API Key saved securely")
        }
    }
}
