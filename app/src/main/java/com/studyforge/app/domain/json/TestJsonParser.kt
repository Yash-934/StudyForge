package com.studyforge.app.domain.json

import com.studyforge.app.domain.model.Difficulty
import com.studyforge.app.domain.model.QuestionDto
import com.studyforge.app.domain.model.QuestionType
import com.studyforge.app.domain.model.TestImportDto
import com.studyforge.app.domain.model.TestValidationResult
import com.studyforge.app.domain.model.ValidationIssue
import org.json.JSONArray
import org.json.JSONObject

object TestJsonParser {

    const val EXAMPLE_TEST_JSON = """{
  "title": "Calculus Chapter Test 01",
  "subject": "Mathematics",
  "chapter": "Integration",
  "durationMinutes": 30,
  "totalMarks": 10,
  "questions": [
    {
      "id": "q1",
      "type": "mcq",
      "question": "Evaluate \int x^2 dx",
      "options": [
        "x^3/3 + C",
        "2x + C",
        "x^2/2 + C",
        "x^3 + C"
      ],
      "correctAnswer": 0,
      "marks": 2.0,
      "negativeMarks": 0.5,
      "explanation": "Using the power rule: \int x^n dx = x^{n+1}/(n+1) + C.",
      "topic": "Indefinite Integrals",
      "difficulty": "easy"
    },
    {
      "id": "q2",
      "type": "multiple_correct",
      "question": "Which of the following functions are antiderivatives of 2x?",
      "options": [
        "x^2 + 5",
        "x^2 - 10",
        "2x^2",
        "x^2"
      ],
      "correctAnswer": [0, 1, 3],
      "marks": 4.0,
      "negativeMarks": 1.0,
      "explanation": "The derivative of x^2 + C with respect to x is 2x for any constant C.",
      "topic": "Antiderivatives",
      "difficulty": "medium"
    },
    {
      "id": "q3",
      "type": "numerical",
      "question": "Calculate \int_0^2 3x^2 dx",
      "options": [],
      "correctAnswer": "8",
      "marks": 4.0,
      "negativeMarks": 0.0,
      "explanation": "The antiderivative is x^3 evaluated from 0 to 2: 2^3 - 0 = 8.",
      "topic": "Definite Integrals",
      "difficulty": "medium"
    }
  ]
}"""

    fun parseAndValidate(jsonString: String): TestValidationResult {
        val issues = mutableListOf<ValidationIssue>()

        if (jsonString.isBlank()) {
            return TestValidationResult(
                totalQuestionsCount = 0,
                validQuestionsCount = 0,
                invalidQuestionsCount = 0,
                issues = listOf(ValidationIssue(0, "", "JSON content is empty", isFatal = true)),
                testDto = TestImportDto()
            )
        }

        val rootObj: JSONObject = try {
            JSONObject(jsonString)
        } catch (e: Exception) {
            // Could it be an array of questions directly?
            try {
                val array = JSONArray(jsonString)
                JSONObject().apply {
                    put("title", "Imported Questions")
                    put("questions", array)
                }
            } catch (ex2: Exception) {
                return TestValidationResult(
                    totalQuestionsCount = 0,
                    validQuestionsCount = 0,
                    invalidQuestionsCount = 0,
                    issues = listOf(ValidationIssue(0, "", "Invalid JSON format: ${e.message}", isFatal = true)),
                    testDto = TestImportDto()
                )
            }
        }

        val title = rootObj.optString("title", "").ifBlank {
            rootObj.optString("test_title", "Imported Test")
        }
        val subject = rootObj.optString("subject", "").ifBlank {
            rootObj.optString("subject_name", "")
        }
        val chapter = rootObj.optString("chapter", "").ifBlank {
            rootObj.optString("chapter_name", "")
        }
        val durationMinutes = rootObj.optInt("durationMinutes", rootObj.optInt("duration_minutes", 30))

        val questionsArray = rootObj.optJSONArray("questions")
            ?: rootObj.optJSONArray("questionList")
            ?: rootObj.optJSONArray("items")
            ?: JSONArray()

        val parsedQuestions = mutableListOf<QuestionDto>()
        var validCount = 0
        var invalidCount = 0

        for (i in 0 until questionsArray.length()) {
            val qObj = questionsArray.optJSONObject(i)
            if (qObj == null) {
                issues.add(ValidationIssue(i + 1, "q${i + 1}", "Item at index ${i + 1} is not a valid JSON object", isFatal = true))
                invalidCount++
                continue
            }

            val rawId = qObj.optString("id", "").ifBlank { "q${i + 1}" }
            val questionText = qObj.optString("question", "").ifBlank {
                qObj.optString("questionText", "").ifBlank {
                    qObj.optString("text", "")
                }
            }

            if (questionText.isBlank()) {
                issues.add(ValidationIssue(i + 1, rawId, "Question text is missing or blank", isFatal = true))
                invalidCount++
                continue
            }

            val rawType = qObj.optString("type", "mcq")
            val questionType = QuestionType.fromString(rawType)

            // Extract options
            val optionsList = mutableListOf<String>()
            val optArray = qObj.optJSONArray("options") ?: qObj.optJSONArray("choices")
            if (optArray != null) {
                for (j in 0 until optArray.length()) {
                    optionsList.add(optArray.optString(j, ""))
                }
            }

            // Extract correct answers (support int, string, array)
            val correctAnswers = mutableListOf<String>()
            when {
                qObj.has("correctAnswer") -> {
                    val rawAns = qObj.get("correctAnswer")
                    when (rawAns) {
                        is JSONArray -> {
                            for (k in 0 until rawAns.length()) {
                                correctAnswers.add(rawAns.optString(k))
                            }
                        }
                        is Int -> correctAnswers.add(rawAns.toString())
                        else -> correctAnswers.add(rawAns.toString().trim())
                    }
                }
                qObj.has("correct_answer") -> {
                    val rawAns = qObj.get("correct_answer")
                    when (rawAns) {
                        is JSONArray -> {
                            for (k in 0 until rawAns.length()) {
                                correctAnswers.add(rawAns.optString(k))
                            }
                        }
                        is Int -> correctAnswers.add(rawAns.toString())
                        else -> correctAnswers.add(rawAns.toString().trim())
                    }
                }
                qObj.has("correctAnswers") -> {
                    val arr = qObj.optJSONArray("correctAnswers")
                    if (arr != null) {
                        for (k in 0 until arr.length()) {
                            correctAnswers.add(arr.optString(k))
                        }
                    } else {
                        correctAnswers.add(qObj.optString("correctAnswers"))
                    }
                }
                qObj.has("answer") -> {
                    correctAnswers.add(qObj.optString("answer").trim())
                }
            }

            // Validation rules per question type
            var questionHasFatalError = false

            if ((questionType == QuestionType.MCQ || questionType == QuestionType.MULTIPLE_CORRECT) && optionsList.isEmpty()) {
                issues.add(ValidationIssue(i + 1, rawId, "${questionType.displayName} must have at least 2 options", isFatal = true))
                questionHasFatalError = true
            }

            if (correctAnswers.isEmpty()) {
                issues.add(ValidationIssue(i + 1, rawId, "No correct answer specified", isFatal = false)) // Warning
            }

            val marks = qObj.optDouble("marks", 1.0).coerceAtLeast(0.5)
            val negativeMarks = qObj.optDouble("negativeMarks", qObj.optDouble("negative_marks", 0.0))
            val explanation = qObj.optString("explanation", "").ifBlank {
                qObj.optString("solution", "")
            }
            val solution = qObj.optString("detailedSolution", "").ifBlank {
                qObj.optString("detailed_solution", explanation)
            }
            val topic = qObj.optString("topic", "")
            val difficulty = Difficulty.fromString(qObj.optString("difficulty", "medium"))
            val hint = qObj.optString("hint", "")
            val source = qObj.optString("source", "")
            val timeLimit = qObj.optInt("timeLimitSeconds", qObj.optInt("time_limit", 0))

            val tagsList = mutableListOf<String>()
            val tagsArr = qObj.optJSONArray("tags")
            if (tagsArr != null) {
                for (t in 0 until tagsArr.length()) {
                    tagsList.add(tagsArr.optString(t))
                }
            }

            if (questionHasFatalError) {
                invalidCount++
            } else {
                validCount++
                parsedQuestions.add(
                    QuestionDto(
                        id = rawId,
                        type = questionType,
                        question = questionText,
                        options = optionsList,
                        correctAnswers = correctAnswers,
                        marks = marks,
                        negativeMarks = negativeMarks,
                        explanation = explanation,
                        detailedSolution = solution,
                        topic = topic,
                        difficulty = difficulty,
                        tags = tagsList,
                        hint = hint,
                        source = source,
                        timeLimitSeconds = timeLimit
                    )
                )
            }
        }

        val totalCalculatedMarks = rootObj.optDouble("totalMarks", parsedQuestions.sumOf { it.marks })

        val testDto = TestImportDto(
            title = title,
            subject = subject,
            chapter = chapter,
            durationMinutes = durationMinutes,
            totalMarks = totalCalculatedMarks,
            questions = parsedQuestions
        )

        return TestValidationResult(
            totalQuestionsCount = questionsArray.length(),
            validQuestionsCount = validCount,
            invalidQuestionsCount = invalidCount,
            issues = issues,
            testDto = testDto
        )
    }

    fun exportTestToJson(
        title: String,
        subject: String,
        chapter: String,
        durationMinutes: Int,
        questions: List<QuestionDto>
    ): String {
        val root = JSONObject()
        root.put("title", title)
        root.put("subject", subject)
        root.put("chapter", chapter)
        root.put("durationMinutes", durationMinutes)
        root.put("totalMarks", questions.sumOf { it.marks })

        val qArr = JSONArray()
        questions.forEach { q ->
            val qObj = JSONObject()
            qObj.put("id", q.id)
            qObj.put("type", q.type.name.lowercase())
            qObj.put("question", q.question)

            val optArr = JSONArray()
            q.options.forEach { optArr.put(it) }
            qObj.put("options", optArr)

            if (q.type == QuestionType.MULTIPLE_CORRECT) {
                val ansArr = JSONArray()
                q.correctAnswers.forEach {
                    val idx = it.toIntOrNull()
                    if (idx != null) ansArr.put(idx) else ansArr.put(it)
                }
                qObj.put("correctAnswer", ansArr)
            } else if (q.type == QuestionType.MCQ) {
                val idx = q.correctAnswers.firstOrNull()?.toIntOrNull()
                if (idx != null) qObj.put("correctAnswer", idx) else qObj.put("correctAnswer", q.correctAnswers.firstOrNull() ?: "")
            } else {
                qObj.put("correctAnswer", q.correctAnswers.firstOrNull() ?: "")
            }

            qObj.put("marks", q.marks)
            qObj.put("negativeMarks", q.negativeMarks)
            qObj.put("explanation", q.explanation)
            qObj.put("detailedSolution", q.detailedSolution)
            qObj.put("topic", q.topic)
            qObj.put("difficulty", q.difficulty.name.lowercase())
            qObj.put("hint", q.hint)
            qObj.put("source", q.source)

            qArr.put(qObj)
        }

        root.put("questions", qArr)
        return root.toString(2)
    }
}
