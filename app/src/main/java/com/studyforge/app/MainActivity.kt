package com.studyforge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.studyforge.app.data.local.AppDatabase
import com.studyforge.app.data.repository.PreferencesRepository
import com.studyforge.app.data.repository.StudyRepository
import com.studyforge.app.ui.navigation.AppNavigation
import com.studyforge.app.ui.theme.StudyForgeTheme
import com.studyforge.app.viewmodel.StudyViewModel
import com.studyforge.app.viewmodel.StudyViewModelFactory

class MainActivity : ComponentActivity() {

    private val db by lazy { AppDatabase.getInstance(applicationContext) }
    private val preferencesRepository by lazy { PreferencesRepository(applicationContext) }
    private val studyRepository by lazy { StudyRepository(db) }

    private val viewModel: StudyViewModel by viewModels {
        StudyViewModelFactory(studyRepository, preferencesRepository, db)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> systemDark
            }

            StudyForgeTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
