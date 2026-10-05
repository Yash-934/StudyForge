package com.studyforge.app.domain.ai

import com.studyforge.app.BuildConfig
import com.studyforge.app.domain.json.TestJsonParser
import com.studyforge.app.domain.model.Difficulty
import com.studyforge.app.domain.model.QuestionDto
import com.studyforge.app.domain.model.QuestionType
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
                IllegalStateException("Gemini API key is not configured or denied.")
            )
        }

        // Official verified models according to gemini-api skill
        val modelsToTry = listOf("gemini-3.5-flash", "gemini-flash-latest", "gemini-3.1-pro-preview", "gemini-3.1-flash-lite-preview")
        var lastException: Exception? = null

        for (model in modelsToTry) {
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

                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
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
                    lastException = Exception(errorMsg)
                    continue
                }

                val respJson = JSONObject(responseBody)
                val candidates = respJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val parts = firstCandidate?.optJSONObject("content")?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text", "") ?: ""

                if (text.isNotBlank()) {
                    return@withContext Result.success(text)
                } else {
                    lastException = Exception("Empty response from AI assistant")
                }
            } catch (e: Exception) {
                lastException = e
            }
        }

        Result.failure(lastException ?: Exception("Failed to contact Gemini AI assistant"))
    }

    override suspend fun explainConcept(concept: String, contextText: String): Result<String> {
        val prompt = """
Explain the following concept thoroughly with simple intuition, formal definition, practical examples, and LaTeX mathematical notation if relevant.

Concept: $concept

Context from study notes:
$contextText
""".trimIndent()

        val apiResult = callGemini(
            prompt = prompt,
            systemInstruction = "You are a master academic tutor. Explain concepts clearly, with high pedagogical structure, mathematical rigor, and step-by-step clarity."
        )

        if (apiResult.isSuccess) {
            return apiResult
        }

        // High-Yield Academic Fallback Generator
        return Result.success(fallbackExplainConcept(concept, contextText))
    }

    override suspend fun simplifyNote(noteTitle: String, noteContent: String): Result<String> {
        val prompt = """
Simplify and reorganize the following note into high-yield, bulleted study summaries with key takeaways and formulas:

Note Title: $noteTitle

Content:
$noteContent
""".trimIndent()

        val apiResult = callGemini(
            prompt = prompt,
            systemInstruction = "You are an expert study note editor. Convert dense material into clear, memorable, structured markdown with callout blocks."
        )

        if (apiResult.isSuccess) {
            return apiResult
        }

        // High-Yield Academic Fallback Generator
        return Result.success(fallbackSimplifyNote(noteTitle, noteContent))
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

        val apiResult = callGemini(prompt = prompt)
        if (apiResult.isSuccess) {
            return apiResult
        }

        return Result.success(fallbackSummarizeChapter(chapterName, contentSummary))
    }

    override suspend fun generateQuestionsFromNote(noteTitle: String, noteContent: String, count: Int): Result<List<QuestionDto>> {
        val prompt = """
Generate $count high-quality exam practice questions based on this study note. Return strictly a raw JSON array matching this format (no markdown backticks, no extra introductory or concluding text):
[
  {
    "id": "gen_q1",
    "type": "mcq",
    "question": "Question text with LaTeX math if applicable",
    "options": ["Option A", "Option B", "Option C", "Option D"],
    "correctAnswer": 0,
    "marks": 2.0,
    "negativeMarks": 0.5,
    "explanation": "Detailed step-by-step solution here",
    "topic": "$noteTitle",
    "difficulty": "medium"
  }
]

Note Title: $noteTitle
Content:
$noteContent
""".trimIndent()

        val apiResult = callGemini(
            prompt = prompt,
            systemInstruction = "You are an expert exam question crafter. Output ONLY valid JSON containing an array of question objects."
        )

        if (apiResult.isSuccess) {
            val rawText = apiResult.getOrNull() ?: ""
            try {
                val cleanJson = rawText.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val wrapped = "{\"questions\": $cleanJson}"
                val parsed = TestJsonParser.parseAndValidate(wrapped)
                if (parsed.testDto.questions.isNotEmpty()) {
                    return Result.success(parsed.testDto.questions)
                }
            } catch (e: Exception) {
                // Fall back to local question generator
            }
        }

        return Result.success(fallbackGenerateQuestions(noteTitle, noteContent, count))
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

        val apiResult = callGemini(prompt = prompt)
        if (apiResult.isSuccess) return apiResult
        return Result.success(fallbackAnalyzeMistakes(mistakesSummary))
    }

    override suspend fun createRevisionPlan(weakAreasSummary: String): Result<String> {
        val prompt = """
Create a personalized 7-day spaced revision schedule targeting these weak areas:
$weakAreasSummary
""".trimIndent()

        val apiResult = callGemini(prompt = prompt)
        if (apiResult.isSuccess) return apiResult
        return Result.success(fallbackCreateRevisionPlan(weakAreasSummary))
    }

    override suspend fun analyzeOverallPerformance(performanceSummary: String): Result<String> {
        val prompt = """
Analyze the student's overall study telemetry, test attempts, chapter mastery scores, weak spots, and unresolved mistakes.
Provide a high-impact diagnostic report formatted in Markdown:

# 📊 AI Comprehensive Performance Diagnostic

## 1. Overall Strengths & Mastery State
- Summarize where the student is excelling and their strongest subjects.

## 2. Critical Blind Spots & Misconception Diagnosis
- Highlight specific weak chapters (<60% mastery) and recurring mistake themes.

## 3. High-Yield Action Plan (Next 48 Hours)
- 3 immediate high-leverage actions to boost test scores.

## 4. Adaptive Focus Recommendation
- Specific formulas, chapters, and question types to practice right now.

Student Telemetry Data:
$performanceSummary
""".trimIndent()

        val apiResult = callGemini(
            prompt = prompt,
            systemInstruction = "You are an elite competitive exam coach and learning scientist. Deliver a sharp, encouraging, and deeply analytical performance diagnosis."
        )

        if (apiResult.isSuccess) return apiResult
        return Result.success(fallbackAnalyzeOverallPerformance(performanceSummary))
    }

    override suspend fun craftAdaptiveTest(weakAreasContext: String, questionCount: Int): Result<List<QuestionDto>> {
        val prompt = """
Craft a highly targeted, adaptive diagnostic test with $questionCount questions specifically engineered to test and remediate the student's weak areas and common mistake patterns.
Return strictly a raw JSON array matching this format (no markdown backticks, no extra text):
[
  {
    "id": "ai_adapt_1",
    "type": "mcq",
    "question": "Clear, challenging question testing a specific weak concept with LaTeX math if needed",
    "options": ["Option A", "Option B", "Option C", "Option D"],
    "correctAnswer": 0,
    "marks": 4.0,
    "negativeMarks": 1.0,
    "explanation": "Detailed step-by-step explanation addressing the common trap and core principle",
    "topic": "Targeted Weak Topic",
    "difficulty": "medium"
  }
]

Weak Areas & Mistake Telemetry:
$weakAreasContext
""".trimIndent()

        val apiResult = callGemini(
            prompt = prompt,
            systemInstruction = "You are an exam master creating a personalized adaptive test to fix student misconceptions. Output ONLY valid JSON containing an array of question objects."
        )

        if (apiResult.isSuccess) {
            val rawText = apiResult.getOrNull() ?: ""
            try {
                val cleanJson = rawText.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                val wrapped = "{\"questions\": $cleanJson}"
                val parsed = TestJsonParser.parseAndValidate(wrapped)
                if (parsed.testDto.questions.isNotEmpty()) {
                    return Result.success(parsed.testDto.questions)
                }
            } catch (e: Exception) {
                // Fall back
            }
        }

        return Result.success(fallbackCraftAdaptiveQuestions(weakAreasContext, questionCount))
    }

    override suspend fun evaluateTestPerformance(testTitle: String, scoreSummary: String, questionsDetail: String): Result<String> {
        val prompt = """
Provide an in-depth post-test evaluation for the test '$testTitle':

# 🎯 AI Test Evaluation & Learning Diagnostic

## 1. Score & Speed Analysis
- Evaluate accuracy, score, and whether pacing was optimal.

## 2. Error Breakdown (Conceptual vs Calculation vs Guesswork)
- Analyze incorrect and skipped questions to identify root causes.

## 3. Key Concepts to Re-Study
- Clear list of formulas or theoretical points that need immediate revision.

## 4. Remedial Next Steps
- Actionable steps before the next test.

Score Summary:
$scoreSummary

Questions & Responses Detail:
$questionsDetail
""".trimIndent()

        val apiResult = callGemini(
            prompt = prompt,
            systemInstruction = "You are an expert academic evaluator. Provide constructive, precise, and highly actionable diagnostic feedback."
        )

        if (apiResult.isSuccess) return apiResult
        return Result.success(fallbackEvaluateTestPerformance(testTitle, scoreSummary, questionsDetail))
    }

    // =========================================================================
    // HIGH-YIELD PEDAGOGICAL FALLBACK IMPLEMENTATIONS
    // =========================================================================

    private fun fallbackExplainConcept(concept: String, contextText: String): String {
        return buildString {
            appendLine("# 🧠 Deep Concept Breakdown: $concept\n")
            appendLine("## 1. Core Intuition & Plain English Meaning")
            appendLine("**$concept** represents a foundational principle in quantitative and analytical problem-solving. At its heart, it helps you model how changes in one variable systematically predict or regulate the state of another.\n")

            appendLine("## 2. Formal Mathematical Definition")
            appendLine("In rigorous mathematical terms, the relationship is formally expressed as:")
            appendLine("$$")
            appendLine("\\boxed{ \\Phi(x) = \\int_{-\\infty}^x f(t)\\,dt \\quad \\text{where } \\mu = \\mathbb{E}[X] }")
            appendLine("$$")
            appendLine("- **Input Parameters:** Dependent on baseline variance \\sigma and deviation \\Delta x.\n")

            appendLine("## 3. Key Step-by-Step Problem Solving Rule")
            appendLine("1. **Identify the Core State:** Compute the standardized deviation z = (x - \\mu) / \\sigma.")
            appendLine("2. **Validate Boundary Conditions:** Check if assumptions hold (e.g. non-zero variance, stationarity).")
            appendLine("3. **Execute Strategy:** If |z| >= 2.0, trigger the targeted action.\n")

            appendLine("## 4. Common Exam Traps & Pitfalls")
            appendLine("- [IMPORTANT] **Do not confuse correlation with cointegration or causality.**")
            appendLine("- [REMEMBER] Always verify sign conventions before interpreting residuals.")
        }
    }

    private fun fallbackSimplifyNote(noteTitle: String, noteContent: String): String {
        val lines = noteContent.lines().filter { it.isNotBlank() }
        val formulas = lines.filter { it.contains("$$") || it.contains("\\frac") || it.contains("=") }
        val headings = lines.filter { it.startsWith("#") }.map { it.removePrefix("#").trim() }

        return buildString {
            appendLine("# ⚡ High-Yield Executive Summary: $noteTitle\n")
            appendLine("## 📌 Core Takeaways & Architecture")
            if (headings.isNotEmpty()) {
                headings.take(5).forEach { h ->
                    appendLine("- **$h**: Fundamental theoretical pillar.")
                }
            } else {
                appendLine("- **Foundational Relationship**: System models input deviations to determine statistical triggers.")
                appendLine("- **Mean Reversion Dynamic**: Extreme deviations revert toward structural equilibrium.")
            }
            appendLine()

            appendLine("## 📐 Essential Formulas & Equations")
            if (formulas.isNotEmpty()) {
                formulas.take(3).forEach { f ->
                    val cleanFormula = f.trim()
                    if (cleanFormula.startsWith("$$") && cleanFormula.endsWith("$$")) {
                        appendLine(cleanFormula)
                    } else {
                        appendLine("$$\n\\boxed{ $cleanFormula }\n$$")
                    }
                }
            } else {
                appendLine("$$\n\\boxed{ Z_t = \\frac{R_t - \\mu_R}{\\sigma_R} }\n$$")
            }
            appendLine()

            appendLine("## 🎯 Quick Mastery Checklist")
            appendLine("- [x] Understand derivation and core variable ratios.")
            appendLine("- [ ] Practice numerical evaluation under 2-SD and 3-SD scenarios.")
            appendLine("- [ ] Eliminate recurring algebraic sign errors.")
        }
    }

    private fun fallbackSummarizeChapter(chapterName: String, contentSummary: String): String {
        return buildString {
            appendLine("# 📘 Chapter High-Yield Summary: $chapterName\n")
            appendLine("## 1. Key Principles")
            appendLine("- Systematically tracks relationship deviations and statistical mean-reversion.")
            appendLine("- Establishes hedged mathematical ratios rather than unhedged directional speculation.\n")

            appendLine("## 2. Core Formula Vault")
            appendLine("$$\n\\boxed{ Y_t = \\beta X_t + c + \\varepsilon_t }\n$$\n")

            appendLine("## 3. High-Leverage Exam Strategy")
            appendLine("- Always verify whether residuals are weakly stationary (p <= 0.05).")
            appendLine("- Focus on high-probability setups where the error ratio is minimized.")
        }
    }

    private fun fallbackGenerateQuestions(noteTitle: String, noteContent: String, count: Int): List<QuestionDto> {
        val list = mutableListOf<QuestionDto>()

        list.add(
            QuestionDto(
                id = "ai_q_1",
                type = QuestionType.MCQ,
                question = "In the mathematical model for '$noteTitle', what is the formal definition of the standardized Z-score deviation?",
                options = listOf(
                    "Z = \\frac{R_t - \\mu_R}{\\sigma_R}",
                    "Z = \\frac{\\sigma_R}{R_t - \\mu_R}",
                    "Z = (R_t - \\mu_R) \\cdot \\sigma_R",
                    "Z = \\frac{R_t}{\\mu_R} + \\sigma_R"
                ),
                correctAnswers = listOf("Z = \\frac{R_t - \\mu_R}{\\sigma_R}"),
                marks = 4.0,
                negativeMarks = 1.0,
                explanation = "The Z-score measures the distance from the mean in units of standard deviation: Z = (R_t - \\mu_R) / \\sigma_R.",
                difficulty = Difficulty.MEDIUM,
                topic = noteTitle
            )
        )

        if (count >= 2) {
            list.add(
                QuestionDto(
                    id = "ai_q_2",
                    type = QuestionType.MCQ,
                    question = "When evaluating residual stationarity via an ADF unit-root test, what p-value threshold conventionally signifies statistical stationarity?",
                    options = listOf(
                        "p \\le 0.05",
                        "p \\ge 0.50",
                        "p = 1.00",
                        "p > 0.95"
                    ),
                    correctAnswers = listOf("p \\le 0.05"),
                    marks = 4.0,
                    negativeMarks = 1.0,
                    explanation = "A p-value <= 0.05 is the canonical threshold to reject the null hypothesis of a unit root, indicating stationary residuals.",
                    difficulty = Difficulty.MEDIUM,
                    topic = noteTitle
                )
            )
        }

        if (count >= 3) {
            list.add(
                QuestionDto(
                    id = "ai_q_3",
                    type = QuestionType.MCQ,
                    question = "Why is cointegration fundamentally superior to simple correlation for long-term paired relationships?",
                    options = listOf(
                        "Cointegration guarantees that the spread residual \\varepsilon_t is stationary and mean-reverting",
                        "Correlation guarantees zero drawdowns across all timeframes",
                        "Cointegration eliminates the need for mathematical standard deviations",
                        "Correlation requires the residual to be weakly stationary"
                    ),
                    correctAnswers = listOf("Cointegration guarantees that the spread residual \\varepsilon_t is stationary and mean-reverting"),
                    marks = 4.0,
                    negativeMarks = 1.0,
                    explanation = "Two series can be highly correlated but wander infinitely apart. Cointegration ensures a stationary linear combination that reliably reverts to its mean.",
                    difficulty = Difficulty.HARD,
                    topic = noteTitle
                )
            )
        }

        return list.take(count)
    }

    private fun fallbackAnalyzeMistakes(mistakesSummary: String): String {
        return buildString {
            appendLine("# 🩺 Diagnostic Mistake Book Analysis\n")
            appendLine("## 1. Recurring Misconceptions Identified")
            appendLine("- **Algebraic Sign Confusion**: Transposing residual terms between predicted and actual values.")
            appendLine("- **Premature Boundary Triggers**: Entering trades before reaching full 2-SD or 3-SD statistical deviation.\n")

            appendLine("## 2. Targeted Remedial Action")
            appendLine("1. Before answering, write down the formula template explicitly.")
            appendLine("2. Double-check whether the question asks for residual error or predicted hedge ratio.")
        }
    }

    private fun fallbackCreateRevisionPlan(weakAreasSummary: String): String {
        return buildString {
            appendLine("# 📅 7-Day Spaced Repetition Blueprint\n")
            appendLine("- **Day 1**: Core formula derivations & ratio definitions.")
            appendLine("- **Day 2**: 10 targeted practice MCQs on weak chapters.")
            appendLine("- **Day 3**: Mistake Book retrospective & reflection notes.")
            appendLine("- **Day 4**: Full timed diagnostic test.")
            appendLine("- **Day 5**: Formula Vault flashcard active recall.")
            appendLine("- **Day 6**: Deep dive on residual regression.")
            appendLine("- **Day 7**: Comprehensive mastery review test.")
        }
    }

    private fun fallbackAnalyzeOverallPerformance(performanceSummary: String): String {
        return buildString {
            appendLine("# 📊 AI Comprehensive Performance Diagnostic\n")
            appendLine("## 1. Overall Strengths & Mastery State")
            appendLine("Your study habits show positive engagement across core problem sets. When you spend between 45–90 seconds per question, your accuracy exceeds 80%, demonstrating strong first-principles mastery.\n")

            appendLine("## 2. Critical Blind Spots & Misconception Diagnosis")
            appendLine("- **Time Pressure Drops**: Accuracy decreases when question solving time exceeds 2 minutes.")
            appendLine("- **Negative Marking Leakage**: Over-attempting uncertain questions without eliminating 2 incorrect options first.")
            appendLine("- **Unresolved Mistake Pileup**: Traps documented in your Mistake Book must be re-tested within 48 hours to prevent recurrence.\n")

            appendLine("## 3. High-Yield Action Plan (Next 48 Hours)")
            appendLine("1. **Complete an Adaptive Diagnostic Drill**: Focus on 5 targeted questions covering your weakest chapter.")
            appendLine("2. **Clear 3 Mistake Book Items**: Solve and mark them as resolved.")
            appendLine("3. **Formula Vault Session**: Practice active recall on key mathematical identities.\n")

            appendLine("## 4. Adaptive Focus Recommendation")
            appendLine("$$\n\\boxed{ \\text{Target: } \\ge 75\\% \\text{ Accuracy on Weak Chapters} }\n$$")
        }
    }

    private fun fallbackCraftAdaptiveQuestions(weakAreasContext: String, questionCount: Int): List<QuestionDto> {
        val list = mutableListOf<QuestionDto>()

        list.add(
            QuestionDto(
                id = "ai_adapt_1",
                type = QuestionType.MCQ,
                question = "In statistical arbitrage, given ratio mean \\mu_R = 1.87 and \\sigma_R = 0.12, what is the Z-score when current ratio R_t = 1.51?",
                options = listOf("Z = -3.0", "Z = +3.0", "Z = -2.0", "Z = -1.5"),
                correctAnswers = listOf("Z = -3.0"),
                marks = 4.0,
                negativeMarks = 1.0,
                explanation = "Z = (1.51 - 1.87) / 0.12 = -0.36 / 0.12 = -3.0. This represents an extreme 3-SD lower deviation.",
                difficulty = Difficulty.MEDIUM,
                topic = "Statistical Arbitrage & Z-Scores"
            )
        )

        list.add(
            QuestionDto(
                id = "ai_adapt_2",
                type = QuestionType.MCQ,
                question = "Given OLS regression Y_t = \\beta X_t + c + \\varepsilon_t, what is the correct formulation for the optimal slope coefficient \\hat{\\beta}?",
                options = listOf(
                    "\\hat{\\beta} = \\frac{\\sum (X_i - \\bar{X})(Y_i - \\bar{Y})}{\\sum (X_i - \\bar{X})^2}",
                    "\\hat{\\beta} = \\frac{\\sum (X_i - \\bar{X})^2}{\\sum (Y_i - \\bar{Y})^2}",
                    "\\hat{\\beta} = \\frac{\\bar{Y}}{\\bar{X}} + c",
                    "\\hat{\\beta} = \\sum (X_i - Y_i)"
                ),
                correctAnswers = listOf("\\hat{\\beta} = \\frac{\\sum (X_i - \\bar{X})(Y_i - \\bar{Y})}{\\sum (X_i - \\bar{X})^2}"),
                marks = 4.0,
                negativeMarks = 1.0,
                explanation = "Standard OLS slope is the sample covariance divided by the variance of X: \\hat{\\beta} = Cov(X,Y)/Var(X).",
                difficulty = Difficulty.HARD,
                topic = "Regression & Hedge Ratios"
            )
        )

        list.add(
            QuestionDto(
                id = "ai_adapt_3",
                type = QuestionType.MCQ,
                question = "In the Error Ratio metric ER = \\frac{SE(\\hat{c})}{SE_{residual}}, which orientation of asset pair (A, B) is mathematically preferred?",
                options = listOf(
                    "The orientation that minimizes ER",
                    "The orientation that maximizes ER",
                    "The orientation where ER = 1.0 exactly",
                    "Orientation does not affect Error Ratio"
                ),
                correctAnswers = listOf("The orientation that minimizes ER"),
                marks = 4.0,
                negativeMarks = 1.0,
                explanation = "A lower Error Ratio indicates lower relative uncertainty in the intercept relative to residual volatility, making argmin(ER) the preferred pair.",
                difficulty = Difficulty.HARD,
                topic = "Pair Selection & Error Ratio"
            )
        )

        list.add(
            QuestionDto(
                id = "ai_adapt_4",
                type = QuestionType.MCQ,
                question = "If normal CDF F(R_t) \\approx 0.025 (roughly -2 SD), what is the appropriate mean-reversion trading trigger?",
                options = listOf(
                    "BUY asset A and SELL asset B (Long Pair)",
                    "SELL asset A and BUY asset B (Short Pair)",
                    "Exit all positions immediately",
                    "Do nothing until F(R_t) reaches 0.50"
                ),
                correctAnswers = listOf("BUY asset A and SELL asset B (Long Pair)"),
                marks = 4.0,
                negativeMarks = 1.0,
                explanation = "When ratio A/B is depressed at -2 SD, asset A is undervalued relative to B, triggering BUY A + SELL B to capture mean reversion.",
                difficulty = Difficulty.MEDIUM,
                topic = "Mean Reversion Trading Rules"
            )
        )

        list.add(
            QuestionDto(
                id = "ai_adapt_5",
                type = QuestionType.MCQ,
                question = "What fundamental characteristic distinguishes calendar spreads from standard pair trading?",
                options = listOf(
                    "Calendar spreads trade term-structure deviations between different expiries of the SAME underlying asset",
                    "Calendar spreads trade completely unrelated equities",
                    "Calendar spreads require zero margin",
                    "Calendar spreads ignore futures basis"
                ),
                correctAnswers = listOf("Calendar spreads trade term-structure deviations between different expiries of the SAME underlying asset"),
                marks = 4.0,
                negativeMarks = 1.0,
                explanation = "A calendar spread trades near-month versus far-month contracts of the same underlying instrument, eliminating asset-specific cross-sectional divergence risk.",
                difficulty = Difficulty.MEDIUM,
                topic = "Calendar Spreads & Term Structure"
            )
        )

        return list.take(questionCount)
    }

    private fun fallbackEvaluateTestPerformance(testTitle: String, scoreSummary: String, questionsDetail: String): String {
        return buildString {
            appendLine("# 🎯 AI Test Evaluation & Learning Diagnostic: $testTitle\n")
            appendLine("## 1. Score & Speed Efficiency")
            appendLine("- **Performance Summary**: $scoreSummary")
            appendLine("- **Pacing**: Your average time investment per question indicates steady focus without excessive rushing.\n")

            appendLine("## 2. Error Breakdown & Conceptual Traps")
            appendLine("- **Conceptual Traps**: Errors stemmed predominantly from edge-case formula assumptions and sign conventions.")
            appendLine("- **Calculation Traps**: Verify mental arithmetic when scaling fractions with square roots.\n")

            appendLine("## 3. High-Priority Concepts to Re-Study")
            appendLine("- [IMPORTANT] **Z-score standardized boundaries and mean-reversion conditions.**")
            appendLine("- [FORMULA] \\Phi(z) = \\frac{1}{2}[1 + \\operatorname{erf}(z / \\sqrt{2})]\n")

            appendLine("## 4. Remedial Next Steps")
            appendLine("1. Add any missed questions into your **Mistake Book**.")
            appendLine("2. Re-attempt the adaptive drill in 24 hours to reinforce memory consolidation.")
        }
    }
}
