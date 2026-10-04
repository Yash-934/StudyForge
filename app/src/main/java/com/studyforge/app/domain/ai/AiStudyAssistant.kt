package com.studyforge.app.domain.ai

import com.studyforge.app.domain.model.QuestionDto
import kotlinx.coroutines.flow.Flow

sealed class AiResponse<out T> {
    object Idle : AiResponse<Nothing>()
    object Loading : AiResponse<Nothing>()
    data class Success<out T>(val data: T) : AiResponse<T>()
    data class Error(val message: String) : AiResponse<Nothing>()
}

interface AiStudyAssistant {
    suspend fun explainConcept(concept: String, contextText: String): Result<String>
    suspend fun simplifyNote(noteTitle: String, noteContent: String): Result<String>
    suspend fun summarizeChapter(chapterName: String, contentSummary: String): Result<String>
    suspend fun generateQuestionsFromNote(noteTitle: String, noteContent: String, count: Int = 3): Result<List<QuestionDto>>
    suspend fun analyzeMistakes(mistakesSummary: String): Result<String>
    suspend fun createRevisionPlan(weakAreasSummary: String): Result<String>
}
