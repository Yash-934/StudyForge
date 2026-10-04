package com.studyforge.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.studyforge.app.data.local.AppDatabase
import com.studyforge.app.data.repository.PreferencesRepository
import com.studyforge.app.data.repository.StudyRepository

class StudyViewModelFactory(
    private val repository: StudyRepository,
    private val preferencesRepository: PreferencesRepository,
    private val db: AppDatabase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyViewModel::class.java)) {
            return StudyViewModel(repository, preferencesRepository, db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
