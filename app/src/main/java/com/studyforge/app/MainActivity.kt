package com.studyforge.app

import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
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

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_SCROLL) {
            val vScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
            if (vScroll != 0f) {
                val downTime = SystemClock.uptimeMillis()
                val eventTime = SystemClock.uptimeMillis()
                val x = if (event.x > 0f) event.x else resources.displayMetrics.widthPixels / 2f
                val y = if (event.y > 0f) event.y else resources.displayMetrics.heightPixels / 2f
                // Positive vScroll means wheel scrolled up (pull content down = positive drag)
                // Negative vScroll means wheel scrolled down (push content up = negative drag)
                val scrollDelta = vScroll * 160f

                val down = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0)
                window.decorView.dispatchTouchEvent(down)
                down.recycle()

                val move = MotionEvent.obtain(downTime, eventTime + 10, MotionEvent.ACTION_MOVE, x, y + scrollDelta, 0)
                window.decorView.dispatchTouchEvent(move)
                move.recycle()

                val up = MotionEvent.obtain(downTime, eventTime + 20, MotionEvent.ACTION_UP, x, y + scrollDelta, 0)
                window.decorView.dispatchTouchEvent(up)
                up.recycle()

                return true
            }
        }
        return super.onGenericMotionEvent(event)
    }
}
