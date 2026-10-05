package com.studyforge.app.domain.json

import com.studyforge.app.domain.model.Difficulty
import com.studyforge.app.domain.model.QuestionDto
import com.studyforge.app.domain.model.QuestionType
import org.json.JSONArray
import org.json.JSONObject

data class ParsedBatchDto(
    val name: String,
    val courseOrClass: String = "",
    val session: String = "",
    val goal: String = "",
    val description: String = ""
)

data class ParsedNoteDto(
    val title: String,
    val content: String,
    val tags: List<String> = emptyList()
)

data class ParsedFormulaDto(
    val title: String,
    val latex: String,
    val explanation: String = ""
)

data class ParsedChapterDto(
    val name: String,
    val orderIndex: Int = 0,
    val notes: List<ParsedNoteDto> = emptyList(),
    val formulas: List<ParsedFormulaDto> = emptyList(),
    val questions: List<QuestionDto> = emptyList()
)

data class ParsedSubjectDto(
    val name: String,
    val iconName: String = "book",
    val colorHex: String = "#4F46E5",
    val chapters: List<ParsedChapterDto> = emptyList()
)

data class CurriculumValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val batch: ParsedBatchDto? = null,
    val subjects: List<ParsedSubjectDto> = emptyList(),
    val totalChaptersCount: Int = 0,
    val totalNotesCount: Int = 0,
    val totalFormulasCount: Int = 0,
    val totalQuestionsCount: Int = 0
)

object CurriculumJsonParser {

    const val EXAMPLE_BATCH_SUBJECTS_JSON = """{
  "batch": {
    "name": "JEE Advanced 2026",
    "courseOrClass": "Class 12 / Dropper",
    "session": "2025-2026",
    "goal": "AIR Under 500",
    "description": "Comprehensive Science & Mathematics preparation"
  },
  "subjects": [
    {
      "name": "Physics",
      "iconName": "science",
      "colorHex": "#3B82F6",
      "chapters": [
        {
          "name": "Rotational Mechanics",
          "notes": [
            {
              "title": "Moment of Inertia Theorems",
              "content": "### Parallel Axis Theorem:\nI = I_cm + M * d^2\n\n### Perpendicular Axis Theorem:\nI_z = I_x + I_y"
            }
          ],
          "formulas": [
            {
              "title": "Kinetic Energy of Rolling",
              "latex": "K = \\frac{1}{2} M v_{cm}^2 + \\frac{1}{2} I_{cm} \\omega^2",
              "explanation": "Translational plus rotational kinetic energy."
            }
          ]
        },
        "Electrostatics & Gauss Law",
        "Electromagnetic Induction",
        "Wave Optics"
      ]
    },
    {
      "name": "Chemistry",
      "iconName": "atom",
      "colorHex": "#10B981",
      "chapters": [
        "Thermodynamics & Thermochemistry",
        "Chemical Equilibrium",
        "Aldehydes, Ketones & Carboxylic Acids",
        "Coordination Chemistry"
      ]
    },
    {
      "name": "Mathematics",
      "iconName": "calculate",
      "colorHex": "#8B5CF6",
      "chapters": [
        {
          "name": "Definite Integrals",
          "notes": [
            {
              "title": "King's Property",
              "content": "Definite Integral Property: integral_a^b f(x) dx = integral_a^b f(a + b - x) dx"
            }
          ]
        },
        "Differential Equations",
        "Matrices and Determinants",
        "Probability Distributions"
      ]
    }
  ]
}"""

    const val EXAMPLE_SUBJECTS_ONLY_JSON = """{
  "subjects": [
    {
      "name": "Computer Science & Engineering",
      "iconName": "code",
      "colorHex": "#06B6D4",
      "chapters": [
        "Data Structures & Algorithms",
        "Operating Systems & Multithreading",
        "Computer Networks (OSI & TCP/IP)",
        "Database Management Systems & SQL"
      ]
    },
    {
      "name": "English & Communication",
      "iconName": "language",
      "colorHex": "#EC4899",
      "chapters": [
        "Technical Writing & Documentation",
        "Verbal Aptitude & Comprehension",
        "Professional Communication"
      ]
    }
  ]
}"""

    fun parseAndValidate(jsonString: String): CurriculumValidationResult {
        val trimmed = jsonString.trim()
        if (trimmed.isBlank()) {
            return CurriculumValidationResult(isValid = false, errorMessage = "JSON content is empty.")
        }

        try {
            var parsedBatch: ParsedBatchDto? = null
            val parsedSubjects = mutableListOf<ParsedSubjectDto>()

            if (trimmed.startsWith("[")) {
                // Direct array of subjects
                val arr = JSONArray(trimmed)
                for (i in 0 until arr.length()) {
                    val sObj = arr.optJSONObject(i) ?: continue
                    parsedSubjects.add(parseSubjectObject(sObj))
                }
            } else {
                val root = JSONObject(trimmed)

                // 1. Check for "batch" object or top-level batch properties
                if (root.has("batch")) {
                    val bObj = root.optJSONObject("batch")
                    if (bObj != null) {
                        parsedBatch = ParsedBatchDto(
                            name = bObj.optString("name", "Imported Batch").ifBlank { "Imported Batch" },
                            courseOrClass = bObj.optString("courseOrClass", bObj.optString("course", "")),
                            session = bObj.optString("session", ""),
                            goal = bObj.optString("goal", ""),
                            description = bObj.optString("description", "")
                        )
                    }
                } else if (root.has("batchName") || (root.has("name") && root.has("subjects"))) {
                    parsedBatch = ParsedBatchDto(
                        name = root.optString("batchName", root.optString("name", "Imported Batch")),
                        courseOrClass = root.optString("courseOrClass", root.optString("course", "")),
                        session = root.optString("session", ""),
                        goal = root.optString("goal", ""),
                        description = root.optString("description", "")
                    )
                }

                // 2. Check for "subjects" array
                if (root.has("subjects")) {
                    val sArr = root.optJSONArray("subjects") ?: JSONArray()
                    for (i in 0 until sArr.length()) {
                        val sObj = sArr.optJSONObject(i) ?: continue
                        parsedSubjects.add(parseSubjectObject(sObj))
                    }
                } else if (root.has("subject")) {
                    // Single subject inside root
                    val sObj = root.optJSONObject("subject")
                    if (sObj != null) {
                        parsedSubjects.add(parseSubjectObject(sObj))
                    }
                } else if (root.has("name") && root.has("chapters") && parsedBatch == null) {
                    // Single subject as root object
                    parsedSubjects.add(parseSubjectObject(root))
                }
            }

            if (parsedBatch == null && parsedSubjects.isEmpty()) {
                return CurriculumValidationResult(
                    isValid = false,
                    errorMessage = "No valid batch or subjects found in JSON. Expected 'batch' and/or 'subjects' fields."
                )
            }

            var totalChapters = 0
            var totalNotes = 0
            var totalFormulas = 0
            var totalQuestions = 0

            parsedSubjects.forEach { sub ->
                totalChapters += sub.chapters.size
                sub.chapters.forEach { chap ->
                    totalNotes += chap.notes.size
                    totalFormulas += chap.formulas.size
                    totalQuestions += chap.questions.size
                }
            }

            return CurriculumValidationResult(
                isValid = true,
                batch = parsedBatch,
                subjects = parsedSubjects,
                totalChaptersCount = totalChapters,
                totalNotesCount = totalNotes,
                totalFormulasCount = totalFormulas,
                totalQuestionsCount = totalQuestions
            )
        } catch (e: Exception) {
            return CurriculumValidationResult(
                isValid = false,
                errorMessage = "Invalid JSON syntax: ${e.message ?: "Could not parse JSON"}"
            )
        }
    }

    private fun parseSubjectObject(obj: JSONObject): ParsedSubjectDto {
        val name = obj.optString("name", "Untitled Subject").ifBlank { "Untitled Subject" }
        val icon = obj.optString("iconName", obj.optString("icon", "book")).ifBlank { "book" }
        val color = obj.optString("colorHex", obj.optString("color", "#4F46E5")).ifBlank { "#4F46E5" }

        val chaptersList = mutableListOf<ParsedChapterDto>()
        val chapArr = obj.optJSONArray("chapters") ?: JSONArray()

        for (i in 0 until chapArr.length()) {
            val item = chapArr.opt(i)
            if (item is String) {
                if (item.isNotBlank()) {
                    chaptersList.add(ParsedChapterDto(name = item.trim(), orderIndex = i))
                }
            } else if (item is JSONObject) {
                val cName = item.optString("name", item.optString("title", "Chapter ${i + 1}"))
                val notes = mutableListOf<ParsedNoteDto>()
                val formulas = mutableListOf<ParsedFormulaDto>()
                val questions = mutableListOf<QuestionDto>()

                // Parse embedded notes
                val nArr = item.optJSONArray("notes") ?: JSONArray()
                for (nIdx in 0 until nArr.length()) {
                    val nObj = nArr.optJSONObject(nIdx) ?: continue
                    val nTitle = nObj.optString("title", "Note ${nIdx + 1}")
                    val nContent = nObj.optString("content", nObj.optString("contentMarkdown", ""))
                    if (nContent.isNotBlank()) {
                        notes.add(ParsedNoteDto(title = nTitle, content = nContent))
                    }
                }

                // Parse embedded formulas
                val fArr = item.optJSONArray("formulas") ?: JSONArray()
                for (fIdx in 0 until fArr.length()) {
                    val fObj = fArr.optJSONObject(fIdx) ?: continue
                    val fTitle = fObj.optString("title", "Formula ${fIdx + 1}")
                    val fLatex = fObj.optString("latex", fObj.optString("formulaLatex", ""))
                    val fExpl = fObj.optString("explanation", "")
                    if (fLatex.isNotBlank()) {
                        formulas.add(ParsedFormulaDto(title = fTitle, latex = fLatex, explanation = fExpl))
                    }
                }

                // Parse embedded questions
                val qArr = item.optJSONArray("questions") ?: JSONArray()
                for (qIdx in 0 until qArr.length()) {
                    val qObj = qArr.optJSONObject(qIdx) ?: continue
                    val qText = qObj.optString("question", qObj.optString("questionText", ""))
                    if (qText.isNotBlank()) {
                        val typeStr = qObj.optString("type", "mcq").lowercase()
                        val qType = when (typeStr) {
                            "multiple_correct" -> QuestionType.MULTIPLE_CORRECT
                            "numerical" -> QuestionType.NUMERICAL
                            "true_false" -> QuestionType.TRUE_FALSE
                            "fill_blank" -> QuestionType.FILL_BLANK
                            else -> QuestionType.MCQ
                        }

                        val options = mutableListOf<String>()
                        val optArr = qObj.optJSONArray("options") ?: JSONArray()
                        for (o in 0 until optArr.length()) {
                            options.add(optArr.optString(o))
                        }

                        val correctAnswers = mutableListOf<String>()
                        val ansRaw = qObj.opt("correctAnswer") ?: qObj.opt("correctAnswers")
                        if (ansRaw is JSONArray) {
                            for (a in 0 until ansRaw.length()) {
                                correctAnswers.add(ansRaw.optString(a))
                            }
                        } else if (ansRaw != null) {
                            correctAnswers.add(ansRaw.toString())
                        }

                        questions.add(
                            QuestionDto(
                                type = qType,
                                question = qText,
                                options = options,
                                correctAnswers = correctAnswers,
                                marks = qObj.optDouble("marks", 1.0),
                                negativeMarks = qObj.optDouble("negativeMarks", 0.0),
                                explanation = qObj.optString("explanation", ""),
                                detailedSolution = qObj.optString("detailedSolution", ""),
                                difficulty = when (qObj.optString("difficulty", "medium").lowercase()) {
                                    "easy" -> Difficulty.EASY
                                    "hard" -> Difficulty.HARD
                                    else -> Difficulty.MEDIUM
                                },
                                topic = qObj.optString("topic", "")
                            )
                        )
                    }
                }

                chaptersList.add(
                    ParsedChapterDto(
                        name = cName.trim(),
                        orderIndex = i,
                        notes = notes,
                        formulas = formulas,
                        questions = questions
                    )
                )
            }
        }

        return ParsedSubjectDto(
            name = name.trim(),
            iconName = icon,
            colorHex = color,
            chapters = chaptersList
        )
    }
}
