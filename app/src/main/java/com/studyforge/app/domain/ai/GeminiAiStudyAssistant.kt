package com.studyforge.app.domain.ai

import com.studyforge.app.BuildConfig
import com.studyforge.app.domain.json.TestJsonParser
import com.studyforge.app.domain.model.QuestionDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAiStudyAssistant(
    private val userApiKeyProvider: () -> String
) : AiStudyAssistant {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun getResolvedApiKey(): String {
        val userKey = userApiKeyProvider().trim()
        if (userKey.isNotBlank()) return userKey
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
    }

    private suspend fun callGemini(prompt: String, systemInstruction: String = ""): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getResolvedApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please enter your key in Settings or configure the AI Studio Secrets panel.")
            )
        }

        try {
            val root = JSONObject()

            if (systemInstruction.isNotBlank()) {
                root.put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
                })
            }

            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                })
            }
            root.put("contents", contents)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val body = root.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
                } catch (e: Exception) {
                    "HTTP ${response.code}"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val respJson = JSONObject(responseBody)
            val candidates = respJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val parts = firstCandidate?.optJSONObject("content")?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text", "") ?: ""

            if (text.isBlank()) {
                Result.failure(Exception("Empty response from AI assistant"))
            } else {
                Result.success(text)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun explainConcept(concept: String, contextText: String): Result<String> {
        val prompt = """
Explain the following concept thoroughly with simple intuition, formal definition, practical examples, and LaTeX mathematical notation if relevant.

Concept: $concept

Context from study notes:
$contextText
""".trimIndent()

        return callGemini(
            prompt = prompt,
            systemInstruction = "You are a master academic tutor. Explain concepts clearly, with high pedagogical structure, mathematical rigor, and step-by-step clarity."
        )
    }

    override suspend fun simplifyNote(noteTitle: String, noteContent: String): Result<String> {
        val prompt = """
Simplify and reorganize the following note into high-yield, bulleted study summaries with key takeaways and formulas:

Note Title: $noteTitle

Content:
$noteContent
""".trimIndent()

        return callGemini(
            prompt = prompt,
            systemInstruction = "You are an expert study note editor. Convert dense material into clear, memorable, structured markdown with callout blocks."
        )
    }

    override suspend fun summarizeChapter(chapterName: String, contentSummary: String): Result<String> {
        val prompt = """
Provide a high-yield executive summary of the chapter '$chapterName', listing:
1. Core Principles
2. Essential Formulas & Theorems
3. Common Pitfalls to Avoid
4. Quick Review Checklist

Content:
$contentSummary
""".trimIndent()

        return callGemini(prompt = prompt)
    }

    override suspend fun generateQuestionsFromNote(noteTitle: String, noteContent: String, count: Int): Result<List<QuestionDto>> {
        val prompt = """
Generate $count high-quality study practice questions based on this note. Return strictly a raw JSON array matching this format (no markdown backticks, no extra text):
[
  {
    "id": "gen_q1",
    "type": "mcq",
    "question": "Question text here",
    "options": ["Option A", "Option B", "Option C", "Option D"],
    "correctAnswer": 0,
    "marks": 2.0,
    "negativeMarks": 0.5,
    "explanation": "Detailed explanation here",
    "topic": "$noteTitle",
    "difficulty": "medium"
  }
]

Note Title: $noteTitle
Content:
$noteContent
""".trimIndent()

        val result = callGemini(
            prompt = prompt,
            systemInstruction = "You are an exam creator. Output ONLY valid JSON containing an array of question objects."
        )

        return result.mapCatching { rawText ->
            val cleanJson = rawText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            // Wrap in test object for parser
            val wrapped = "{\"questions\": $cleanJson}"
            val parsed = TestJsonParser.parseAndValidate(wrapped)
            if (parsed.testDto.questions.isEmpty()) {
                throw Exception("Could not parse generated questions JSON.")
            }
            parsed.testDto.questions
        }
    }

    override suspend fun analyzeMistakes(mistakesSummary: String): Result<String> {
        val prompt = """
Analyze the student's recent mistakes and diagnose recurring conceptual misconceptions. Provide:
1. Root-cause diagnostic
2. Targeted remedial concepts to re-read
3. 3-step action plan to eliminate these errors

Mistakes Data:
$mistakesSummary
""".trimIndent()

        return callGemini(prompt = prompt)
    }

    override suspend fun createRevisionPlan(weakAreasSummary: String): Result<String> {
        val prompt = """
Create a personalized 7-day spaced revision schedule targeting these weak areas:
$weakAreasSummary
""".trimIndent()

        return callGemini(prompt = prompt)
    }
}
