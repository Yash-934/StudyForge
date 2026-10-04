package com.studyforge.app

import com.studyforge.app.data.local.entities.ChapterEntity
import com.studyforge.app.data.local.entities.MistakeEntity
import com.studyforge.app.data.local.entities.QuestionEntity
import com.studyforge.app.data.local.entities.RevisionItemEntity
import com.studyforge.app.data.local.entities.TestAttemptEntity
import com.studyforge.app.domain.json.TestJsonParser
import com.studyforge.app.domain.model.Difficulty
import com.studyforge.app.domain.model.MasteryCalculator
import com.studyforge.app.domain.model.QuestionType
import com.studyforge.app.domain.model.RevisionItemType
import com.studyforge.app.domain.model.SmartTestDistribution
import com.studyforge.app.domain.model.SmartTestGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StudyForgeBusinessLogicTest {

    @Test
    fun testJsonParser_validExample_parsesCorrectly() {
        val result = TestJsonParser.parseAndValidate(TestJsonParser.EXAMPLE_TEST_JSON)
        assertEquals(3, result.validQuestionsCount)
        assertEquals(0, result.invalidQuestionsCount)
        assertEquals("Calculus Chapter Test 01", result.testDto.title)
        assertEquals(30, result.testDto.durationMinutes)
        assertEquals(3, result.testDto.questions.size)

        val q1 = result.testDto.questions[0]
        assertEquals(QuestionType.MCQ, q1.type)
        assertEquals(4, q1.options.size)
        assertEquals(listOf("0"), q1.correctAnswers)
        assertEquals(2.0, q1.marks, 0.001)
        assertEquals(0.5, q1.negativeMarks, 0.001)

        val q2 = result.testDto.questions[1]
        assertEquals(QuestionType.MULTIPLE_CORRECT, q2.type)
        assertEquals(listOf("0", "1", "3"), q2.correctAnswers)

        val q3 = result.testDto.questions[2]
        assertEquals(QuestionType.NUMERICAL, q3.type)
        assertEquals(listOf("8"), q3.correctAnswers)
    }

    @Test
    fun testJsonParser_missingQuestionText_reportsFatalIssue() {
        val invalidJson = """
        {
          "title": "Invalid Test",
          "questions": [
            {
              "type": "mcq",
              "options": ["A", "B"],
              "correctAnswer": 0
            }
          ]
        }
        """.trimIndent()

        val result = TestJsonParser.parseAndValidate(invalidJson)
        assertEquals(0, result.validQuestionsCount)
        assertEquals(1, result.invalidQuestionsCount)
        assertTrue(result.issues.any { it.isFatal })
    }

    @Test
    fun testJsonParser_mcqWithoutOptions_reportsFatalIssue() {
        val invalidMcq = """
        {
          "title": "MCQ Without Options",
          "questions": [
            {
              "type": "mcq",
              "question": "What is 2+2?",
              "correctAnswer": 0
            }
          ]
        }
        """.trimIndent()

        val result = TestJsonParser.parseAndValidate(invalidMcq)
        assertEquals(0, result.validQuestionsCount)
        assertEquals(1, result.invalidQuestionsCount)
    }

    @Test
    fun testJsonExport_roundTrip() {
        val result = TestJsonParser.parseAndValidate(TestJsonParser.EXAMPLE_TEST_JSON)
        val exported = TestJsonParser.exportTestToJson(
            title = result.testDto.title,
            subject = result.testDto.subject,
            chapter = result.testDto.chapter,
            durationMinutes = result.testDto.durationMinutes,
            questions = result.testDto.questions
        )

        assertNotNull(exported)
        assertTrue(exported.contains("Calculus Chapter Test 01"))

        val reimported = TestJsonParser.parseAndValidate(exported)
        assertEquals(result.testDto.questions.size, reimported.testDto.questions.size)
    }

    @Test
    fun testMasteryScoreCalculation() {
        val questions = listOf(
            QuestionEntity(
                id = 1,
                chapterId = 1,
                subjectId = 1,
                batchId = 1,
                questionText = "Q1",
                timesAttempted = 10,
                timesCorrect = 9
            ),
            QuestionEntity(
                id = 2,
                chapterId = 1,
                subjectId = 1,
                batchId = 1,
                questionText = "Q2",
                timesAttempted = 10,
                timesCorrect = 8
            )
        )

        val attempts = listOf(
            TestAttemptEntity(
                id = 1,
                testId = 1,
                testTitle = "Calculus Test",
                accuracyPercentage = 85.0
            )
        )

        val mistakes = listOf(
            MistakeEntity(
                id = 1,
                questionId = 2,
                chapterId = 1,
                subjectId = 1,
                isResolved = true
            )
        )

        val revisions = listOf(
            RevisionItemEntity(
                id = 1,
                itemType = RevisionItemType.QUESTION,
                itemId = 1,
                chapterId = 1,
                title = "Rev Q1",
                intervalLevel = 3
            )
        )

        val score = MasteryCalculator.calculateChapterMastery(questions, attempts, mistakes, revisions)
        assertTrue(score in 80..95)
    }

    @Test
    fun testSmartTestGenerator_selectsCorrectTargetCount() {
        val mockQuestions = (1..30).map { i ->
            QuestionEntity(
                id = i.toLong(),
                chapterId = 1,
                subjectId = 1,
                batchId = 1,
                questionText = "Question $i",
                difficulty = if (i % 3 == 0) Difficulty.HARD else if (i % 2 == 0) Difficulty.MEDIUM else Difficulty.EASY,
                timesAttempted = if (i <= 10) 5 else 0,
                timesCorrect = if (i <= 5) 1 else 4
            )
        }

        val mistakes = listOf(
            MistakeEntity(
                id = 1,
                questionId = 2,
                chapterId = 1,
                subjectId = 1
            )
        )

        val selected = SmartTestGenerator.selectQuestions(
            allQuestions = mockQuestions,
            allMistakes = mistakes,
            targetQuestionCount = 10,
            distribution = SmartTestDistribution(40, 30, 20, 10)
        )

        assertEquals(10, selected.size)
        // Ensure no duplicates
        val uniqueIds = selected.map { it.id }.toSet()
        assertEquals(10, uniqueIds.size)
    }
}
