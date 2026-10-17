package com.studyforge.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.studyforge.app.R

/**
 * Primary UI FontFamily using native multi-weight system SansSerif (Roboto + Noto Sans Devanagari).
 * Provides true hardware-rendered Regular (400), Medium (500), SemiBold (600), Bold (700), and ExtraBold (800)
 * weights across English, Hindi (Devanagari), and mathematical symbols without clipping matras.
 */
val AppSansFont = FontFamily.SansSerif

val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium),
    Font(R.font.jetbrains_mono, FontWeight.Bold)
)

private val DefaultPlatformTextStyle = PlatformTextStyle(
    includeFontPadding = true
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 44.sp,
        lineHeight = 54.sp,
        letterSpacing = 0.sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    displayMedium = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    displaySmall = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    headlineLarge = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    headlineMedium = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    headlineSmall = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 27.sp,
        letterSpacing = 0.sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    titleLarge = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    titleMedium = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 23.sp,
        letterSpacing = 0.sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    titleSmall = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    bodyLarge = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.Normal,
        fontSize = 15.5.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    bodyMedium = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.1.sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    bodySmall = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.15.sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    labelLarge = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    labelMedium = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.15.sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    labelSmall = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp,
        platformStyle = DefaultPlatformTextStyle
    )
)
