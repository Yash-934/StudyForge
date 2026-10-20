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
 * Primary UI FontFamily using multi-weight Plus Jakarta Sans (400, 500, 600, 700, 800)
 * matching the reference screenshots' rounded geometric sans-serif typography, with automatic
 * system fallback to Noto Sans Devanagari for Hindi glyphs at matching weights.
 */
val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_400, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_500, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_600, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_700, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_800, FontWeight.ExtraBold)
)

val AppSansFont = PlusJakartaSans

val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium),
    Font(R.font.jetbrains_mono, FontWeight.Bold)
)

val LoraSerifFont = FontFamily(
    Font(R.font.lora, FontWeight.Normal),
    Font(R.font.lora, FontWeight.Medium),
    Font(R.font.lora, FontWeight.SemiBold),
    Font(R.font.lora, FontWeight.Bold)
)

val OpenSansFont = FontFamily(
    Font(R.font.open_sans, FontWeight.Normal),
    Font(R.font.open_sans, FontWeight.Medium),
    Font(R.font.open_sans, FontWeight.SemiBold),
    Font(R.font.open_sans, FontWeight.Bold)
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
        letterSpacing = (-0.8).sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    displayMedium = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.6).sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    displaySmall = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    headlineLarge = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    headlineMedium = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.4).sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    headlineSmall = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 19.sp,
        lineHeight = 27.sp,
        letterSpacing = (-0.3).sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    titleLarge = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.25).sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    titleMedium = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 23.sp,
        letterSpacing = (-0.15).sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    titleSmall = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.1).sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    bodyLarge = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.Normal,
        fontSize = 15.5.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    bodyMedium = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    bodySmall = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.05.sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    labelLarge = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    labelMedium = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.05.sp,
        platformStyle = DefaultPlatformTextStyle
    ),
    labelSmall = TextStyle(
        fontFamily = AppSansFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp,
        platformStyle = DefaultPlatformTextStyle
    )
)
