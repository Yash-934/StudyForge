package com.studyforge.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val PrimaryTeal = Color(0xFF00796B)
val OnPrimaryTeal = Color(0xFFFFFFFF)
val PrimaryContainerTeal = Color(0xFFB2DFDB)
val OnPrimaryContainerTeal = Color(0xFF004D40)

val SecondaryMint = Color(0xFF2E7D32)
val SecondaryContainerMint = Color(0xFFC8E6C9)

val DarkPrimaryTeal = Color(0xFF80CBC4)
val DarkPrimaryContainerTeal = Color(0xFF004D40)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryTeal,
    onPrimary = Color(0xFF003730),
    primaryContainer = DarkPrimaryContainerTeal,
    onPrimaryContainer = Color(0xFFB2DFDB),
    secondary = Color(0xFFA5D6A7),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryTeal,
    onPrimary = OnPrimaryTeal,
    primaryContainer = PrimaryContainerTeal,
    onPrimaryContainer = OnPrimaryContainerTeal,
    secondary = SecondaryMint,
    secondaryContainer = SecondaryContainerMint,
    background = Color(0xFFF9FBFB),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF191C1C),
    onSurface = Color(0xFF191C1C)
)

@Composable
fun StudyForgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
