package com.studyforge.app.domain.ai

import com.studyforge.app.domain.model.QuestionDto

interface AiStudyAssistant {
    suspend fun explainConcept(concept: String, contextText: String): Result<String>
    suspend fun simplifyNote(noteTitle: String, noteContent: String): Result<String>
    suspend fun summarizeChapter(chapterName: String, contentSummary: String): Result<String>
    suspend fun generateQuestionsFromNote(noteTitle: String, noteContent: String, count: Int): Result<List<QuestionDto>>
    suspend fun analyzeMistakes(mistakesSummary: String): Result<String>
    suspend fun createRevisionPlan(weakAreasSummary: String): Result<String>
    suspend fun analyzeOverallPerformance(performanceSummary: String): Result<String>
    suspend fun craftAdaptiveTest(weakAreasContext: String, questionCount: Int): Result<List<QuestionDto>>
    suspend fun evaluateTestPerformance(testTitle: String, scoreSummary: String, questionsDetail: String): Result<String>
}
