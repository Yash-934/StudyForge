package com.studyforge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.studyforge.app.data.local.AppDatabase
import com.studyforge.app.data.repository.PreferencesRepository
import com.studyforge.app.data.repository.StudyRepository
import com.studyforge.app.ui.navigation.AppNavigation
import com.studyforge.app.ui.theme.StudyForgeTheme
import com.studyforge.app.viewmodel.StudyViewModel
import com.studyforge.app.viewmodel.StudyViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: StudyViewModel by viewModels {
        val db = AppDatabase.getInstance(applicationContext)
        val repository = StudyRepository(db)
        val preferencesRepository = PreferencesRepository(applicationContext)
        StudyViewModelFactory(repository, preferencesRepository, db)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyForgeTheme {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}
