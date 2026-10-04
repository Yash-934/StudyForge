package com.studyforge.app.domain.json

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
import com.studyforge.app.data.local.entities.TestAttemptEntity
import com.studyforge.app.data.local.entities.TestEntity
import com.studyforge.app.data.local.entities.TimetableEntity
import com.studyforge.app.domain.model.Difficulty
import com.studyforge.app.domain.model.Mood
import com.studyforge.app.domain.model.QuestionType
import com.studyforge.app.domain.model.RevisionItemType
import com.studyforge.app.domain.model.TestMode
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

object BackupManager {

    suspend fun createFullBackupJson(db: AppDatabase): String {
        val root = JSONObject()
        root.put("app", "StudyForge")
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())

        // Batches
        val batches = db.batchDao().getAllBatches().first()
        val batchesArr = JSONArray()
        batches.forEach { b ->
            batchesArr.put(JSONObject().apply {
                put("id", b.id)
                put("name", b.name)
                put("courseOrClass", b.courseOrClass)
                put("session", b.session)
                put("goal", b.goal)
                put("description", b.description)
                put("isPinned", b.isPinned)
                put("createdAt", b.createdAt)
            })
        }
        root.put("batches", batchesArr)

        // Subjects
        val subjects = db.subjectDao().getAllSubjects().first()
        val subjectsArr = JSONArray()
        subjects.forEach { s ->
            subjectsArr.put(JSONObject().apply {
                put("id", s.id)
                put("batchId", s.batchId)
                put("name", s.name)
                put("iconName", s.iconName)
                put("colorHex", s.colorHex)
                put("isPinned", s.isPinned)
                put("createdAt", s.createdAt)
            })
        }
        root.put("subjects", subjectsArr)

        // Chapters
        val chapters = db.chapterDao().getAllChapters().first()
        val chaptersArr = JSONArray()
        chapters.forEach { c ->
            chaptersArr.put(JSONObject().apply {
                put("id", c.id)
                put("subjectId", c.subjectId)
                put("batchId", c.batchId)
                put("name", c.name)
                put("orderIndex", c.orderIndex)
                put("masteryScore", c.masteryScore)
                put("isPinned", c.isPinned)
                put("createdAt", c.createdAt)
            })
        }
        root.put("chapters", chaptersArr)

        // Notes
        val notes = db.noteDao().getAllNotes().first()
        val notesArr = JSONArray()
        notes.forEach { n ->
            notesArr.put(JSONObject().apply {
                put("id", n.id)
                put("chapterId", n.chapterId)
                put("subjectId", n.subjectId)
                put("batchId", n.batchId)
                put("title", n.title)
                put("contentMarkdown", n.contentMarkdown)
                put("tagsJson", n.tagsJson)
                put("isFavorite", n.isFavorite)
                put("isPinned", n.isPinned)
                put("createdAt", n.createdAt)
                put("updatedAt", n.updatedAt)
            })
        }
        root.put("notes", notesArr)

        // Formulas
        val formulas = db.formulaDao().getAllFormulas().first()
        val formulasArr = JSONArray()
        formulas.forEach { f ->
            formulasArr.put(JSONObject().apply {
                put("id", f.id)
                put("chapterId", f.chapterId)
                put("subjectId", f.subjectId)
                put("batchId", f.batchId)
                put("title", f.title)
                put("formulaLatex", f.formulaLatex)
                put("explanation", f.explanation)
                put("tagsJson", f.tagsJson)
                put("isFavorite", f.isFavorite)
                put("createdAt", f.createdAt)
            })
        }
        root.put("formulas", formulasArr)

        // Questions
        val questions = db.questionDao().getAllQuestions().first()
        val questionsArr = JSONArray()
        questions.forEach { q ->
            questionsArr.put(JSONObject().apply {
                put("id", q.id)
                put("chapterId", q.chapterId)
                put("subjectId", q.subjectId)
                put("batchId", q.batchId)
                put("type", q.type.name)
                put("questionText", q.questionText)
                put("optionsJson", q.optionsJson)
                put("correctAnswersJson", q.correctAnswersJson)
                put("explanation", q.explanation)
                put("detailedSolution", q.detailedSolution)
                put("marks", q.marks)
                put("negativeMarks", q.negativeMarks)
                put("difficulty", q.difficulty.name)
                put("topic", q.topic)
                put("tagsJson", q.tagsJson)
                put("hint", q.hint)
                put("timesAttempted", q.timesAttempted)
                put("timesCorrect", q.timesCorrect)
                put("createdAt", q.createdAt)
            })
        }
        root.put("questions", questionsArr)

        // Tests
        val tests = db.testDao().getAllTests().first()
        val testsArr = JSONArray()
        tests.forEach { t ->
            testsArr.put(JSONObject().apply {
                put("id", t.id)
                put("batchId", t.batchId)
                put("subjectId", t.subjectId)
                put("chapterId", t.chapterId)
                put("title", t.title)
                put("mode", t.mode.name)
                put("durationMinutes", t.durationMinutes)
                put("totalMarks", t.totalMarks)
                put("questionIdsJson", t.questionIdsJson)
                put("createdAt", t.createdAt)
            })
        }
        root.put("tests", testsArr)

        // Mistakes
        val mistakes = db.mistakeDao().getAllMistakes().first()
        val mistakesArr = JSONArray()
        mistakes.forEach { m ->
            mistakesArr.put(JSONObject().apply {
                put("id", m.id)
                put("questionId", m.questionId)
                put("chapterId", m.chapterId)
                put("subjectId", m.subjectId)
                put("userAnswerJson", m.userAnswerJson)
                put("correctAnswerJson", m.correctAnswerJson)
                put("userReflectionNote", m.userReflectionNote)
                put("attemptCount", m.attemptCount)
                put("isResolved", m.isResolved)
                put("resolvedAt", m.resolvedAt)
                put("scheduledRevisionAt", m.scheduledRevisionAt)
            })
        }
        root.put("mistakes", mistakesArr)

        // Revisions
        val revisions = db.revisionDao().getAllRevisionItems().first()
        val revArr = JSONArray()
        revisions.forEach { r ->
            revArr.put(JSONObject().apply {
                put("id", r.id)
                put("itemType", r.itemType.name)
                put("itemId", r.itemId)
                put("chapterId", r.chapterId)
                put("title", r.title)
                put("intervalLevel", r.intervalLevel)
                put("nextReviewDate", r.nextReviewDate)
                put("repetitions", r.repetitions)
            })
        }
        root.put("revisions", revArr)

        // Flashcards
        val flashcards = db.flashcardDao().getAllFlashcards().first()
        val flashArr = JSONArray()
        flashcards.forEach { fc ->
            flashArr.put(JSONObject().apply {
                put("id", fc.id)
                put("chapterId", fc.chapterId)
                put("subjectId", fc.subjectId)
                put("front", fc.front)
                put("back", fc.back)
                put("difficulty", fc.difficulty.name)
                put("reviewCount", fc.reviewCount)
                put("nextReviewDate", fc.nextReviewDate)
            })
        }
        root.put("flashcards", flashArr)

        // Journal
        val journals = db.journalDao().getAllJournalEntries().first()
        val journalArr = JSONArray()
        journals.forEach { j ->
            journalArr.put(JSONObject().apply {
                put("id", j.id)
                put("dateMillis", j.dateMillis)
                put("freeText", j.freeText)
                put("mood", j.mood.name)
                put("accomplishments", j.accomplishments)
                put("difficulties", j.difficulties)
                put("tomorrowPlan", j.tomorrowPlan)
                put("studyTimeMinutes", j.studyTimeMinutes)
                put("questionsSolved", j.questionsSolved)
            })
        }
        root.put("journals", journalArr)

        // Timetable
        val timetable = db.timetableDao().getAllTimetableEntries().first()
        val ttArr = JSONArray()
        timetable.forEach { tt ->
            ttArr.put(JSONObject().apply {
                put("id", tt.id)
                put("subjectName", tt.subjectName)
                put("chapterOrTask", tt.chapterOrTask)
                put("dayOfWeek", tt.dayOfWeek)
                put("startTime", tt.startTime)
                put("endTime", tt.endTime)
            })
        }
        root.put("timetable", ttArr)

        return root.toString(2)
    }

    suspend fun restoreFromBackupJson(db: AppDatabase, jsonString: String): Result<String> {
        return try {
            val root = JSONObject(jsonString)
            if (root.optString("app") != "StudyForge") {
                return Result.failure(IllegalArgumentException("Invalid backup file: not a StudyForge backup."))
            }

            // Restore Batches
            val batchesArr = root.optJSONArray("batches") ?: JSONArray()
            for (i in 0 until batchesArr.length()) {
                val b = batchesArr.getJSONObject(i)
                db.batchDao().insertBatch(
                    BatchEntity(
                        id = b.optLong("id", 0),
                        name = b.optString("name", "Restored Batch"),
                        courseOrClass = b.optString("courseOrClass", ""),
                        session = b.optString("session", ""),
                        goal = b.optString("goal", ""),
                        description = b.optString("description", ""),
                        isPinned = b.optBoolean("isPinned", false),
                        createdAt = b.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            // Restore Subjects
            val subjectsArr = root.optJSONArray("subjects") ?: JSONArray()
            for (i in 0 until subjectsArr.length()) {
                val s = subjectsArr.getJSONObject(i)
                db.subjectDao().insertSubject(
                    SubjectEntity(
                        id = s.optLong("id", 0),
                        batchId = s.optLong("batchId", 1),
                        name = s.optString("name", "Restored Subject"),
                        iconName = s.optString("iconName", "book"),
                        colorHex = s.optString("colorHex", "#4F46E5"),
                        isPinned = s.optBoolean("isPinned", false),
                        createdAt = s.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            // Restore Chapters
            val chaptersArr = root.optJSONArray("chapters") ?: JSONArray()
            for (i in 0 until chaptersArr.length()) {
                val c = chaptersArr.getJSONObject(i)
                db.chapterDao().insertChapter(
                    ChapterEntity(
                        id = c.optLong("id", 0),
                        subjectId = c.optLong("subjectId", 1),
                        batchId = c.optLong("batchId", 1),
                        name = c.optString("name", "Restored Chapter"),
                        orderIndex = c.optInt("orderIndex", 0),
                        masteryScore = c.optInt("masteryScore", 0),
                        isPinned = c.optBoolean("isPinned", false),
                        createdAt = c.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            // Restore Notes
            val notesArr = root.optJSONArray("notes") ?: JSONArray()
            for (i in 0 until notesArr.length()) {
                val n = notesArr.getJSONObject(i)
                db.noteDao().insertNote(
                    NoteEntity(
                        id = n.optLong("id", 0),
                        chapterId = n.optLong("chapterId", 1),
                        subjectId = n.optLong("subjectId", 1),
                        batchId = n.optLong("batchId", 1),
                        title = n.optString("title", "Untitled Note"),
                        contentMarkdown = n.optString("contentMarkdown", ""),
                        tagsJson = n.optString("tagsJson", "[]"),
                        isFavorite = n.optBoolean("isFavorite", false),
                        isPinned = n.optBoolean("isPinned", false)
                    )
                )
            }

            // Restore Questions
            val questionsArr = root.optJSONArray("questions") ?: JSONArray()
            for (i in 0 until questionsArr.length()) {
                val q = questionsArr.getJSONObject(i)
                db.questionDao().insertQuestion(
                    QuestionEntity(
                        id = q.optLong("id", 0),
                        chapterId = q.optLong("chapterId", 1),
                        subjectId = q.optLong("subjectId", 1),
                        batchId = q.optLong("batchId", 1),
                        type = QuestionType.fromString(q.optString("type", "MCQ")),
                        questionText = q.optString("questionText", ""),
                        optionsJson = q.optString("optionsJson", "[]"),
                        correctAnswersJson = q.optString("correctAnswersJson", "[]"),
                        explanation = q.optString("explanation", ""),
                        detailedSolution = q.optString("detailedSolution", ""),
                        marks = q.optDouble("marks", 1.0),
                        negativeMarks = q.optDouble("negativeMarks", 0.0),
                        difficulty = Difficulty.fromString(q.optString("difficulty", "MEDIUM")),
                        topic = q.optString("topic", ""),
                        hint = q.optString("hint", "")
                    )
                )
            }

            // Restore Formulas
            val formulasArr = root.optJSONArray("formulas") ?: JSONArray()
            for (i in 0 until formulasArr.length()) {
                val f = formulasArr.getJSONObject(i)
                db.formulaDao().insertFormula(
                    FormulaEntity(
                        id = f.optLong("id", 0),
                        chapterId = f.optLong("chapterId", 1),
                        subjectId = f.optLong("subjectId", 1),
                        batchId = f.optLong("batchId", 1),
                        title = f.optString("title", ""),
                        formulaLatex = f.optString("formulaLatex", ""),
                        explanation = f.optString("explanation", "")
                    )
                )
            }

            // Restore Flashcards
            val flashArr = root.optJSONArray("flashcards") ?: JSONArray()
            for (i in 0 until flashArr.length()) {
                val fc = flashArr.getJSONObject(i)
                db.flashcardDao().insertFlashcard(
                    FlashcardEntity(
                        id = fc.optLong("id", 0),
                        chapterId = fc.optLong("chapterId", 1),
                        subjectId = fc.optLong("subjectId", 1),
                        front = fc.optString("front", ""),
                        back = fc.optString("back", ""),
                        difficulty = Difficulty.fromString(fc.optString("difficulty", "MEDIUM"))
                    )
                )
            }

            Result.success("Restored successfully! Batches: ${batchesArr.length()}, Questions: ${questionsArr.length()}, Notes: ${notesArr.length()}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
