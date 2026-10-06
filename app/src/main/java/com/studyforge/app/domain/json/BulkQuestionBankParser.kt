package com.studyforge.app.domain.json

import com.studyforge.app.domain.model.Difficulty
import com.studyforge.app.domain.model.QuestionDto
import com.studyforge.app.domain.model.QuestionType
import org.json.JSONArray
import org.json.JSONObject

data class ChapterQuestionGroup(
    val chapterName: String,
    val subjectName: String? = null,
    val questions: List<QuestionDto>
)

data class BulkQuestionBankValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val batchName: String? = null,
    val defaultSubjectName: String? = null,
    val chapterGroups: List<ChapterQuestionGroup> = emptyList(),
    val totalQuestionsCount: Int = 0,
    val totalChaptersCount: Int = 0
)

data class BulkQuestionBankImportSummary(
    val batchName: String,
    val subjectsAffected: Int,
    val chaptersAffected: Int,
    val totalQuestionsImported: Int,
    val newChaptersCreated: Int,
    val existingChaptersMatched: Int,
    val chapterDetails: List<Pair<String, Int>> // (chapterName, questionsCount)
)

object BulkQuestionBankParser {

    val EXAMPLE_MULTI_CHAPTER_JSON: String = """{
  "batch": "JEE Advanced 2026",
  "subject": "Physics",
  "chapters": [
    {
      "name": "Kinematics",
      "questions": [
        {
          "question": "A particle moves along a straight line such that its displacement is x = 2t^3 - 6t + 5. At what time is the velocity zero?",
          "type": "mcq",
          "options": [
            "t = 1 s",
            "t = 2 s",
            "t = 0.5 s",
            "t = 3 s"
          ],
          "correctAnswer": "t = 1 s",
          "explanation": "Velocity v = dx/dt = 6t^2 - 6. Setting v = 0 gives 6t^2 = 6 => t = 1 s.",
          "difficulty": "easy",
          "marks": 4,
          "negativeMarks": 1,
          "topic": "1D Motion"
        },
        {
          "question": "A stone is dropped from a balloon rising upwards with velocity 10 m/s. The balloon is at height 40 m when the stone is dropped. Taking g = 10 m/s^2, find the time taken by the stone to reach the ground.",
          "type": "numerical",
          "correctAnswer": "4",
          "explanation": "Using -h = u*t - 0.5*g*t^2 => -40 = 10t - 5t^2 => 5t^2 - 10t - 40 = 0 => t^2 - 2t - 8 = 0 => (t-4)(t+2) = 0 => t = 4 s.",
          "difficulty": "medium",
          "marks": 4,
          "negativeMarks": 0,
          "topic": "Free Fall"
        }
      ]
    },
    {
      "name": "Laws of Motion & Friction",
      "questions": [
        {
          "question": "A body of mass 5 kg rests on a rough horizontal surface with coefficient of static friction mu = 0.4. What is the maximum horizontal force that can be applied without moving the body? (g = 9.8 m/s^2)",
          "type": "mcq",
          "options": [
            "19.6 N",
            "39.2 N",
            "49.0 N",
            "9.8 N"
          ],
          "correctAnswer": "19.6 N",
          "explanation": "Limiting friction f_lim = mu * m * g = 0.4 * 5 * 9.8 = 19.6 N.",
          "difficulty": "easy",
          "marks": 4,
          "negativeMarks": 1,
          "topic": "Static Friction"
        },
        {
          "question": "Which of the following statements about Newton's third law are correct?",
          "type": "multiple_correct",
          "options": [
            "Action and reaction act on two different bodies",
            "Action and reaction are always equal in magnitude and opposite in direction",
            "Action and reaction cancel each other out to produce equilibrium on a single body",
            "Action and reaction forces occur simultaneously"
          ],
          "correctAnswer": [
            "Action and reaction act on two different bodies",
            "Action and reaction are always equal in magnitude and opposite in direction",
            "Action and reaction forces occur simultaneously"
          ],
          "explanation": "Action and reaction never act on the same body, hence they do not cancel each other out.",
          "difficulty": "medium",
          "marks": 4,
          "negativeMarks": 2,
          "topic": "Newton's Laws"
        }
      ]
    },
    {
      "name": "Work, Power and Energy",
      "questions": [
        {
          "question": "A particle of mass m moves under a conservative force with potential energy U(x) = ax^2 - bx. The equilibrium position is at:",
          "type": "mcq",
          "options": [
            "x = b / (2a)",
            "x = a / (2b)",
            "x = 2b / a",
            "x = b / a"
          ],
          "correctAnswer": "x = b / (2a)",
          "explanation": "For equilibrium, dU/dx = 0 => 2ax - b = 0 => x = b / (2a).",
          "difficulty": "medium",
          "marks": 4,
          "negativeMarks": 1,
          "topic": "Potential Energy Curve"
        },
        {
          "question": "Work done by a conservative force along any closed path is always zero.",
          "type": "true_false",
          "options": [
            "True",
            "False"
          ],
          "correctAnswer": "True",
          "explanation": "By definition, the line integral of a conservative force around any closed loop is zero.",
          "difficulty": "easy",
          "marks": 2,
          "negativeMarks": 0.5,
          "topic": "Conservative Forces"
        }
      ]
    }
  ]
}"""

    val EXAMPLE_MULTI_SUBJECT_JSON: String = """{
  "batch": "NEET / JEE Foundation",
  "subjects": [
    {
      "name": "Physics",
      "chapters": [
        {
          "name": "Electrostatics",
          "questions": [
            {
              "question": "The electric potential at the center of a uniformly charged conducting spherical shell is equal to:",
              "type": "mcq",
              "options": [
                "Zero",
                "Potential on its surface",
                "Twice the surface potential",
                "Infinite"
              ],
              "correctAnswer": "Potential on its surface",
              "explanation": "Electric field inside conductor is zero, so potential remains constant and equal to surface potential."
            }
          ]
        },
        {
          "name": "Current Electricity",
          "questions": [
            {
              "question": "When temperature of a metallic conductor is increased, its electrical resistance:",
              "type": "mcq",
              "options": [
                "Increases",
                "Decreases",
                "Remains constant",
                "First decreases then increases"
              ],
              "correctAnswer": "Increases",
              "explanation": "Higher temperature causes more lattice collisions, decreasing relaxation time and increasing resistance."
            }
          ]
        }
      ]
    },
    {
      "name": "Chemistry",
      "chapters": [
        {
          "name": "Chemical Bonding",
          "questions": [
            {
              "question": "What is the hybridization and geometry of SF6 molecule?",
              "type": "mcq",
              "options": [
                "sp3d2, Octahedral",
                "sp3d, Trigonal Bipyramidal",
                "sp3, Tetrahedral",
                "dsp2, Square Planar"
              ],
              "correctAnswer": "sp3d2, Octahedral",
              "explanation": "Sulfur has 6 valence electrons, forming 6 bond pairs and 0 lone pairs => steric number 6 => sp3d2 octahedral."
            }
          ]
        },
        {
          "name": "Thermodynamics",
          "questions": [
            {
              "question": "For an isolated system, the change in internal energy (Delta U) during any process is:",
              "type": "mcq",
              "options": [
                "Zero",
                "Positive",
                "Negative",
                "Depends on temperature"
              ],
              "correctAnswer": "Zero",
              "explanation": "An isolated system exchanges neither heat (q=0) nor work (w=0). By 1st law, Delta U = q + w = 0."
            }
          ]
        }
      ]
    }
  ]
}"""

    val EXAMPLE_FLAT_QUESTIONS_JSON: String = """[
  {
    "chapter": "Wave Optics",
    "subject": "Physics",
    "question": "In Young's double slit experiment, if the distance between the two slits is halved and distance to the screen is doubled, the fringe width becomes:",
    "options": [
      "Four times",
      "Doubled",
      "Halved",
      "Unchanged"
    ],
    "correctAnswer": "Four times",
    "explanation": "Fringe width beta = lambda * D / d. If d becomes d/2 and D becomes 2D, beta becomes 4 times."
  },
  {
    "chapter": "Wave Optics",
    "subject": "Physics",
    "question": "Two coherent monochromatic light beams of intensities I and 4I are superimposed. The maximum possible intensity is:",
    "type": "numerical",
    "correctAnswer": "9I",
    "explanation": "I_max = (sqrt(I) + sqrt(4I))^2 = (1 + 2)^2 * I = 9I."
  },
  {
    "chapter": "Semiconductors",
    "subject": "Physics",
    "question": "At absolute zero temperature (0 K), an intrinsic semiconductor behaves as a:",
    "options": [
      "Perfect insulator",
      "Conductor",
      "Superconductor",
      "Variable resistor"
    ],
    "correctAnswer": "Perfect insulator",
    "explanation": "No thermal energy is available to excite electrons from valence band to conduction band."
  },
  {
    "chapter": "Coordination Compounds",
    "subject": "Chemistry",
    "question": "What is the coordination number of cobalt in [Co(en)3]3+?",
    "type": "numerical",
    "correctAnswer": "6",
    "explanation": "Ethylenediamine (en) is a bidentate ligand. 3 bidentate ligands coordinate with 3 * 2 = 6 donor atoms."
  }
]"""

    fun parseAndValidate(
        jsonString: String,
        defaultSubjectName: String? = null
    ): BulkQuestionBankValidationResult {
        val trimmed = jsonString.trim()
        if (trimmed.isBlank()) {
            return BulkQuestionBankValidationResult(
                isValid = false,
                errorMessage = "JSON input is empty. Paste or upload a valid JSON file."
            )
        }

        try {
            var batchName: String? = null
            var defaultSub: String? = defaultSubjectName
            val chapterGroups = mutableListOf<ChapterQuestionGroup>()

            if (trimmed.startsWith("[")) {
                // Could be an array of chapter objects OR an array of flat questions
                val rootArr = JSONArray(trimmed)
                if (rootArr.length() == 0) {
                    return BulkQuestionBankValidationResult(
                        isValid = false,
                        errorMessage = "JSON array is empty."
                    )
                }

                val firstObj = rootArr.optJSONObject(0)
                if (firstObj != null && (firstObj.has("questions") || firstObj.has("questionList"))) {
                    // Array of Chapter objects: [ { "chapter": "...", "questions": [...] }, ... ]
                    for (i in 0 until rootArr.length()) {
                        val cObj = rootArr.optJSONObject(i) ?: continue
                        val cName = cObj.optString("chapter", cObj.optString("chapterName", cObj.optString("name", "Chapter ${i + 1}"))).trim()
                        val sName = cObj.optString("subject", cObj.optString("subjectName", defaultSub ?: "")).trim().ifBlank { defaultSub }
                        val questions = parseQuestionsArray(cObj.optJSONArray("questions") ?: cObj.optJSONArray("questionList"))
                        if (questions.isNotEmpty()) {
                            chapterGroups.add(ChapterQuestionGroup(chapterName = cName, subjectName = sName, questions = questions))
                        }
                    }
                } else if (firstObj != null && (firstObj.has("question") || firstObj.has("questionText") || firstObj.has("q"))) {
                    // Array of flat questions with chapter tags: [ { "chapter": "...", "question": "..." }, ... ]
                    val map = mutableMapOf<String, Pair<String?, MutableList<QuestionDto>>>()
                    for (i in 0 until rootArr.length()) {
                        val qObj = rootArr.optJSONObject(i) ?: continue
                        val cName = qObj.optString("chapter", qObj.optString("chapterName", "General Chapter")).trim().ifBlank { "General Chapter" }
                        val sName = qObj.optString("subject", qObj.optString("subjectName", defaultSub ?: "")).trim().ifBlank { defaultSub }
                        val qDto = parseSingleQuestion(qObj) ?: continue
                        val entry = map.getOrPut(cName) { Pair(sName, mutableListOf()) }
                        entry.second.add(qDto)
                    }
                    map.forEach { (cName, pair) ->
                        chapterGroups.add(ChapterQuestionGroup(chapterName = cName, subjectName = pair.first, questions = pair.second))
                    }
                } else {
                    return BulkQuestionBankValidationResult(
                        isValid = false,
                        errorMessage = "Unrecognized array format. Expected an array of chapters with 'questions' or an array of question objects."
                    )
                }
            } else {
                // Object root
                val root = JSONObject(trimmed)

                if (root.has("batch")) {
                    val bVal = root.opt("batch")
                    if (bVal is JSONObject) {
                        batchName = bVal.optString("name", "Imported Batch")
                    } else if (bVal != null) {
                        batchName = bVal.toString()
                    }
                } else if (root.has("batchName")) {
                    batchName = root.optString("batchName")
                }

                if (root.has("subject")) {
                    val sVal = root.opt("subject")
                    if (sVal is JSONObject) {
                        defaultSub = sVal.optString("name", defaultSub ?: "General")
                    } else if (sVal != null && sVal.toString().isNotBlank()) {
                        defaultSub = sVal.toString()
                    }
                } else if (root.has("subjectName")) {
                    defaultSub = root.optString("subjectName")
                }

                // 1. Nested Subjects: { "subjects": [ { "name": "...", "chapters": [...] } ] }
                if (root.has("subjects")) {
                    val sArr = root.optJSONArray("subjects") ?: JSONArray()
                    for (sIdx in 0 until sArr.length()) {
                        val sObj = sArr.optJSONObject(sIdx) ?: continue
                        val sName = sObj.optString("name", sObj.optString("subject", defaultSub ?: "Subject ${sIdx + 1}")).trim()
                        val cArr = sObj.optJSONArray("chapters") ?: JSONArray()
                        for (cIdx in 0 until cArr.length()) {
                            val cObj = cArr.optJSONObject(cIdx) ?: continue
                            val cName = cObj.optString("name", cObj.optString("chapter", "Chapter ${cIdx + 1}")).trim()
                            val questions = parseQuestionsArray(cObj.optJSONArray("questions") ?: cObj.optJSONArray("questionList"))
                            if (questions.isNotEmpty()) {
                                chapterGroups.add(ChapterQuestionGroup(chapterName = cName, subjectName = sName, questions = questions))
                            }
                        }
                    }
                }
                // 2. Chapters array: { "chapters": [ { "name": "...", "questions": [...] } ] }
                else if (root.has("chapters") && root.opt("chapters") is JSONArray) {
                    val cArr = root.optJSONArray("chapters") ?: JSONArray()
                    for (cIdx in 0 until cArr.length()) {
                        val cObj = cArr.optJSONObject(cIdx) ?: continue
                        val cName = cObj.optString("name", cObj.optString("chapter", cObj.optString("chapterName", "Chapter ${cIdx + 1}"))).trim()
                        val sName = cObj.optString("subject", cObj.optString("subjectName", defaultSub ?: "")).trim().ifBlank { defaultSub }
                        val questions = parseQuestionsArray(cObj.optJSONArray("questions") ?: cObj.optJSONArray("questionList"))
                        if (questions.isNotEmpty()) {
                            chapterGroups.add(ChapterQuestionGroup(chapterName = cName, subjectName = sName, questions = questions))
                        }
                    }
                }
                // 3. Questions array with chapter field on each question
                else if (root.has("questions") && root.opt("questions") is JSONArray) {
                    val qArr = root.optJSONArray("questions") ?: JSONArray()
                    val map = mutableMapOf<String, Pair<String?, MutableList<QuestionDto>>>()
                    for (i in 0 until qArr.length()) {
                        val qObj = qArr.optJSONObject(i) ?: continue
                        val cName = qObj.optString("chapter", qObj.optString("chapterName", "General Chapter")).trim().ifBlank { "General Chapter" }
                        val sName = qObj.optString("subject", qObj.optString("subjectName", defaultSub ?: "")).trim().ifBlank { defaultSub }
                        val qDto = parseSingleQuestion(qObj) ?: continue
                        val entry = map.getOrPut(cName) { Pair(sName, mutableListOf()) }
                        entry.second.add(qDto)
                    }
                    map.forEach { (cName, pair) ->
                        chapterGroups.add(ChapterQuestionGroup(chapterName = cName, subjectName = pair.first, questions = pair.second))
                    }
                }
                // 4. Dictionary format: { "Chapter 1": [ questions... ], "Chapter 2": [ questions... ] }
                else {
                    val keys = root.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        if (key in listOf("batch", "batchName", "subject", "subjectName", "description", "title", "version")) continue
                        val cVal = root.opt(key)
                        if (cVal is JSONArray) {
                            val questions = parseQuestionsArray(cVal)
                            if (questions.isNotEmpty()) {
                                chapterGroups.add(ChapterQuestionGroup(chapterName = key.trim(), subjectName = defaultSub, questions = questions))
                            }
                        } else if (cVal is JSONObject && cVal.has("questions")) {
                            val questions = parseQuestionsArray(cVal.optJSONArray("questions"))
                            val sName = cVal.optString("subject", defaultSub ?: "").ifBlank { defaultSub }
                            if (questions.isNotEmpty()) {
                                chapterGroups.add(ChapterQuestionGroup(chapterName = key.trim(), subjectName = sName, questions = questions))
                            }
                        }
                    }
                }
            }

            if (chapterGroups.isEmpty()) {
                return BulkQuestionBankValidationResult(
                    isValid = false,
                    errorMessage = "No chapters or valid questions found in JSON. Ensure questions are grouped by chapter."
                )
            }

            val totalQ = chapterGroups.sumOf { it.questions.size }
            if (totalQ == 0) {
                return BulkQuestionBankValidationResult(
                    isValid = false,
                    errorMessage = "Chapters were found, but no valid questions could be parsed."
                )
            }

            return BulkQuestionBankValidationResult(
                isValid = true,
                batchName = batchName,
                defaultSubjectName = defaultSub,
                chapterGroups = chapterGroups,
                totalQuestionsCount = totalQ,
                totalChaptersCount = chapterGroups.size
            )
        } catch (e: Exception) {
            return BulkQuestionBankValidationResult(
                isValid = false,
                errorMessage = "Invalid JSON syntax: ${e.message ?: "Failed to parse JSON"}"
            )
        }
    }

    private fun parseQuestionsArray(arr: JSONArray?): List<QuestionDto> {
        if (arr == null) return emptyList()
        val list = mutableListOf<QuestionDto>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val q = parseSingleQuestion(obj)
            if (q != null) list.add(q)
        }
        return list
    }

    private fun parseSingleQuestion(obj: JSONObject): QuestionDto? {
        val qText = obj.optString("question", obj.optString("questionText", obj.optString("q", obj.optString("title", "")))).trim()
        if (qText.isBlank()) return null

        val typeStr = obj.optString("type", "mcq").lowercase().trim()
        val qType = when (typeStr) {
            "multiple_correct", "multi_correct", "multiple_choice", "msq" -> QuestionType.MULTIPLE_CORRECT
            "numerical", "integer", "nat" -> QuestionType.NUMERICAL
            "true_false", "tf", "boolean" -> QuestionType.TRUE_FALSE
            "fill_blank", "fill_in_the_blank", "blank" -> QuestionType.FILL_BLANK
            else -> QuestionType.MCQ
        }

        val options = mutableListOf<String>()
        val optArr = obj.optJSONArray("options") ?: obj.optJSONArray("choices") ?: obj.optJSONArray("answers")
        if (optArr != null) {
            for (o in 0 until optArr.length()) {
                val optItem = optArr.opt(o)
                if (optItem != null) {
                    options.add(optItem.toString().trim())
                }
            }
        }

        val correctAnswers = mutableListOf<String>()
        val ansRaw = obj.opt("correctAnswer")
            ?: obj.opt("correctAnswers")
            ?: obj.opt("answer")
            ?: obj.opt("ans")
            ?: obj.opt("correct")

        if (ansRaw is JSONArray) {
            for (a in 0 until ansRaw.length()) {
                val item = ansRaw.opt(a)
                if (item != null) correctAnswers.add(item.toString().trim())
            }
        } else if (ansRaw is Number) {
            // Handle index or numerical answer
            val idx = ansRaw.toInt()
            if (options.isNotEmpty() && idx in 0 until options.size) {
                correctAnswers.add(options[idx])
            } else {
                correctAnswers.add(ansRaw.toString())
            }
        } else if (ansRaw != null && ansRaw.toString().isNotBlank()) {
            val ansStr = ansRaw.toString().trim()
            // Check if it's option index like "0", "1" or letter like "A", "B", "C", "D"
            if (options.isNotEmpty() && ansStr.length == 1 && ansStr[0].isDigit()) {
                val idx = ansStr.toIntOrNull()
                if (idx != null && idx in 0 until options.size) {
                    correctAnswers.add(options[idx])
                } else {
                    correctAnswers.add(ansStr)
                }
            } else if (options.isNotEmpty() && ansStr.uppercase() in listOf("A", "B", "C", "D", "E") && options.size >= 2) {
                val letterIdx = ansStr.uppercase()[0] - 'A'
                if (letterIdx in 0 until options.size) {
                    correctAnswers.add(options[letterIdx])
                } else {
                    correctAnswers.add(ansStr)
                }
            } else {
                correctAnswers.add(ansStr)
            }
        }

        val diffStr = obj.optString("difficulty", obj.optString("level", "medium")).lowercase()
        val diff = when (diffStr) {
            "easy", "beginner", "simple" -> Difficulty.EASY
            "hard", "advanced", "difficult", "tough" -> Difficulty.HARD
            else -> Difficulty.MEDIUM
        }

        val expl = obj.optString("explanation", obj.optString("solution", obj.optString("detailedSolution", ""))).trim()
        val topic = obj.optString("topic", obj.optString("subtopic", "")).trim()
        val marks = obj.optDouble("marks", obj.optDouble("score", if (qType == QuestionType.TRUE_FALSE) 2.0 else 4.0))
        val negMarks = obj.optDouble("negativeMarks", obj.optDouble("penalty", 0.0))
        val hint = obj.optString("hint", "").trim()

        return QuestionDto(
            type = qType,
            question = qText,
            options = options,
            correctAnswers = correctAnswers,
            marks = marks,
            negativeMarks = negMarks,
            explanation = expl,
            detailedSolution = expl,
            topic = topic,
            difficulty = diff,
            hint = hint
        )
    }
}
