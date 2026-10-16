package com.studyforge.app.domain.ai

sealed class AiResponse<out T> {
    object Idle : AiResponse<Nothing>()
    object Loading : AiResponse<Nothing>()
    data class Success<out T>(val data: T) : AiResponse<T>()
    data class Error(val message: String) : AiResponse<Nothing>()
}
