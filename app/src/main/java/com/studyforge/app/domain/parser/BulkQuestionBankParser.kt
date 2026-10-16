package com.studyforge.app.domain.parser

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.Reader

data class ParsedQuestion(
    val questionText: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String = "",
    val topic: String = "",
    val difficulty: String = "Medium"
)

data class ParsedChapter(
    val name: String,
    val questions: List<ParsedQuestion>
)

data class ParsedSubject(
    val name: String,
    val chapters: List<ParsedChapter>
)

data class ParsedImportResult(
    val batchName: String?,
    val defaultSubject: String?,
    val subjects: List<ParsedSubject>,
    val totalQuestionsCount: Int
)

object BulkQuestionBankParser {

    /**
     * Parse from a Reader (e.g. FileReader or InputStreamReader from SAF Uri)
     * avoiding full-string in-memory bloat.
     */
    suspend fun parseFromReader(reader: Reader): ParsedImportResult = withContext(Dispatchers.Default) {
        val bufferedReader = if (reader is BufferedReader) reader else BufferedReader(reader)
        val sb = StringBuilder()
        val buffer = CharArray(16384)
        var read: Int
        while (bufferedReader.read(buffer).also { read = it } != -1) {
            sb.append(buffer, 0, read)
        }
        parseFromString(sb.toString())
    }

    suspend fun parseFromString(rawText: String): ParsedImportResult = withContext(Dispatchers.Default) {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) {
            throw IllegalArgumentException("Input JSON is empty")
        }

        if (trimmed.startsWith("[")) {
            // Root is an Array: either array of questions, or array of chapters
            val jsonArray = JSONArray(trimmed)
            parseRootArray(jsonArray)
        } else if (trimmed.startsWith("{")) {
            val rootObj = JSONObject(trimmed)
            parseRootObject(rootObj)
        } else {
            throw IllegalArgumentException("Invalid JSON: must start with '{' or '['")
        }
    }

    private fun parseRootObject(root: JSONObject): ParsedImportResult {
        val batchName = root.optString("batchName", root.optString("batch", "")).ifBlank { null }
        val defaultSubjectName = root.optString("subjectName", root.optString("subject", "General")).ifBlank { "General" }

        val subjectsList = mutableListOf<ParsedSubject>()
        var totalQuestions = 0

        if (root.has("subjects")) {
            val subjectsArr = root.optJSONArray("subjects") ?: JSONArray()
            for (i in 0 until subjectsArr.length()) {
                val subObj = subjectsArr.optJSONObject(i) ?: continue
                val sName = subObj.optString("name", "Subject ${i + 1}")
                val chapters = parseChaptersArray(subObj.optJSONArray("chapters"))
                val qCount = chapters.sumOf { it.questions.size }
                totalQuestions += qCount
                subjectsList.add(ParsedSubject(sName, chapters))
            }
        } else if (root.has("chapters")) {
            val chapters = parseChaptersArray(root.optJSONArray("chapters"))
            totalQuestions += chapters.sumOf { it.questions.size }
            subjectsList.add(ParsedSubject(defaultSubjectName, chapters))
        } else if (root.has("questions") || root.has("mcqs") || root.has("items")) {
            val qArray = root.optJSONArray("questions")
                ?: root.optJSONArray("mcqs")
                ?: root.optJSONArray("items")
                ?: JSONArray()
            val parsedQuestions = parseQuestionsArray(qArray)
            val chapterName = root.optString("chapterName", root.optString("chapter", "Imported Chapter")).ifBlank { "Imported Chapter" }
            totalQuestions += parsedQuestions.size
            subjectsList.add(
                ParsedSubject(
                    name = defaultSubjectName,
                    chapters = listOf(ParsedChapter(chapterName, parsedQuestions))
                )
            )
        } else {
            // Check if top-level fields represent a single question
            val singleQ = parseSingleQuestion(root)
            if (singleQ != null) {
                totalQuestions = 1
                subjectsList.add(
                    ParsedSubject(
                        name = defaultSubjectName,
                        chapters = listOf(ParsedChapter("Imported Chapter", listOf(singleQ)))
                    )
                )
            } else {
                throw IllegalArgumentException("Could not find any 'chapters', 'subjects', or 'questions' in JSON")
            }
        }

        return ParsedImportResult(
            batchName = batchName,
            defaultSubject = defaultSubjectName,
            subjects = subjectsList,
            totalQuestionsCount = totalQuestions
        )
    }

    private fun parseRootArray(array: JSONArray): ParsedImportResult {
        if (array.length() == 0) {
            return ParsedImportResult(null, "General", emptyList(), 0)
        }

        val firstObj = array.optJSONObject(0)
        if (firstObj == null) {
            throw IllegalArgumentException("Array contains non-object items")
        }

        // Determine if array is chapters or questions
        if (firstObj.has("questions") || firstObj.has("chapterName") || firstObj.has("name")) {
            // Check if it has questions field
            if (firstObj.has("questions")) {
                val chapters = parseChaptersArray(array)
                val totalQ = chapters.sumOf { it.questions.size }
                return ParsedImportResult(
                    batchName = null,
                    defaultSubject = "General",
                    subjects = listOf(ParsedSubject("General", chapters)),
                    totalQuestionsCount = totalQ
                )
            }
        }

        // Otherwise treat array as a list of questions directly
        val questions = parseQuestionsArray(array)
        return ParsedImportResult(
            batchName = null,
            defaultSubject = "General",
            subjects = listOf(
                ParsedSubject(
                    name = "General",
                    chapters = listOf(ParsedChapter("Imported Questions", questions))
                )
            ),
            totalQuestionsCount = questions.size
        )
    }

    private fun parseChaptersArray(chaptersArr: JSONArray?): List<ParsedChapter> {
        if (chaptersArr == null) return emptyList()
        val list = mutableListOf<ParsedChapter>()
        for (i in 0 until chaptersArr.length()) {
            val chObj = chaptersArr.optJSONObject(i) ?: continue
            val chName = chObj.optString("chapterName", chObj.optString("name", "Chapter ${i + 1}")).ifBlank { "Chapter ${i + 1}" }
            val qArray = chObj.optJSONArray("questions") ?: chObj.optJSONArray("mcqs") ?: JSONArray()
            val questions = parseQuestionsArray(qArray)
            list.add(ParsedChapter(chName, questions))
        }
        return list
    }

    private fun parseQuestionsArray(qArr: JSONArray?): List<ParsedQuestion> {
        if (qArr == null) return emptyList()
        val list = mutableListOf<ParsedQuestion>()
        for (i in 0 until qArr.length()) {
            val qObj = qArr.optJSONObject(i) ?: continue
            val q = parseSingleQuestion(qObj)
            if (q != null) {
                list.add(q)
            }
        }
        return list
    }

    private fun parseSingleQuestion(obj: JSONObject): ParsedQuestion? {
        val questionText = obj.optString("question", obj.optString("text", obj.optString("questionText", obj.optString("q", "")))).trim()
        if (questionText.isBlank()) return null

        val optionsList = mutableListOf<String>()
        val optArr = obj.optJSONArray("options") ?: obj.optJSONArray("choices")
        if (optArr != null) {
            for (j in 0 until optArr.length()) {
                optionsList.add(optArr.optString(j, "").trim())
            }
        } else {
            // Try individual keys: optionA, optionB, etc.
            val a = obj.optString("optionA", obj.optString("a", obj.optString("opt1", ""))).trim()
            val b = obj.optString("optionB", obj.optString("b", obj.optString("opt2", ""))).trim()
            val c = obj.optString("optionC", obj.optString("c", obj.optString("opt3", ""))).trim()
            val d = obj.optString("optionD", obj.optString("d", obj.optString("opt4", ""))).trim()
            if (a.isNotBlank() && b.isNotBlank()) {
                optionsList.add(a)
                optionsList.add(b)
                if (c.isNotBlank()) optionsList.add(c)
                if (d.isNotBlank()) optionsList.add(d)
            }
        }

        if (optionsList.size < 2) {
            // Must have at least 2 options for an MCQ
            return null
        }

        // Correct option index resolution
        var correctIndex = -1
        if (obj.has("correctOptionIndex")) {
            correctIndex = obj.optInt("correctOptionIndex", -1)
        } else if (obj.has("correctIndex")) {
            correctIndex = obj.optInt("correctIndex", -1)
        } else if (obj.has("answerIndex")) {
            correctIndex = obj.optInt("answerIndex", -1)
        } else {
            // Could be letter like "A", "B", "C", "D" or string answer
            val ansStr = obj.optString("answer", obj.optString("correctAnswer", obj.optString("ans", ""))).trim()
            if (ansStr.isNotBlank()) {
                when (ansStr.uppercase()) {
                    "A", "1", "OPTION A", "OPTION 1" -> correctIndex = 0
                    "B", "2", "OPTION B", "OPTION 2" -> correctIndex = 1
                    "C", "3", "OPTION C", "OPTION 3" -> correctIndex = 2
                    "D", "4", "OPTION D", "OPTION 4" -> correctIndex = 3
                    else -> {
                        // Match with option text
                        val match = optionsList.indexOfFirst { it.equals(ansStr, ignoreCase = true) }
                        correctIndex = if (match >= 0) match else 0
                    }
                }
            }
        }

        if (correctIndex !in optionsList.indices) {
            correctIndex = 0
        }

        val explanation = obj.optString("explanation", obj.optString("solution", obj.optString("exp", ""))).trim()
        val topic = obj.optString("topic", obj.optString("tag", "")).trim()
        val difficulty = obj.optString("difficulty", obj.optString("level", "Medium")).trim()

        return ParsedQuestion(
            questionText = questionText,
            options = optionsList,
            correctOptionIndex = correctIndex,
            explanation = explanation,
            topic = topic,
            difficulty = if (difficulty.isBlank()) "Medium" else difficulty
        )
    }
}
