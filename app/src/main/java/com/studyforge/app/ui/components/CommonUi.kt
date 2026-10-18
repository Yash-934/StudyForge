package com.studyforge.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyforge.app.domain.model.Difficulty
import com.studyforge.app.ui.theme.DarkSlateHeroEnd
import com.studyforge.app.ui.theme.DarkSlateHeroStart
import com.studyforge.app.ui.theme.ErrorRed
import com.studyforge.app.ui.theme.GoldenAmberBar
import com.studyforge.app.ui.theme.JetBrainsMono
import com.studyforge.app.ui.theme.SoftMintPillBg
import com.studyforge.app.ui.theme.SoftMintPillText
import com.studyforge.app.ui.theme.SoftOrangePillBg
import com.studyforge.app.ui.theme.SoftOrangePillText
import com.studyforge.app.ui.theme.SuccessGreen
import com.studyforge.app.ui.theme.WarmOrangeBadge
import com.studyforge.app.ui.theme.WarningYellow

/**
 * Maps standard Material Icons to vibrant colorful pictorial symbols matching the reference UI style.
 */
fun pictorialSymbolForIcon(icon: ImageVector): String {
    return when (icon) {
        Icons.Default.Home -> "🏠"
        Icons.Default.PlayCircle, Icons.Default.PlayArrow -> "🏋️"
        Icons.Default.Folder -> "📂"
        Icons.Default.QueryStats -> "📊"
        Icons.Default.CalendarMonth -> "📅"
        Icons.Default.Settings -> "⚙️"
        Icons.AutoMirrored.Filled.MenuBook -> "📝"
        Icons.Default.Calculate -> "🔢"
        Icons.Default.Quiz -> "🧩"
        Icons.Default.FactCheck -> "✅"
        Icons.Default.FitnessCenter -> "🏋️"
        Icons.Default.Style -> "🃏"
        Icons.Default.ErrorOutline -> "🎯"
        Icons.Default.NotificationsActive -> "⏰"
        Icons.Default.AutoAwesome -> "✨"
        Icons.Default.Lightbulb -> "💡"
        Icons.Default.Timer -> "⏱️"
        Icons.Default.CheckCircle -> "✅"
        Icons.Default.FileUpload, Icons.Default.UploadFile, Icons.Default.Layers -> "📥"
        Icons.Default.Science -> "🔬"
        Icons.Default.Psychology -> "🧠"
        Icons.Default.Search -> "🔍"
        Icons.Default.LocalFireDepartment -> "🔥"
        Icons.Default.Info -> "📘"
        else -> "📌"
    }
}

/**
 * Crisp white editorial card with subtle slate border matching the reference screenshots.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudyCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.85f),
    elevation: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null || onLongClick != null) {
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation, pressedElevation = elevation),
            border = BorderStroke(1.dp, borderColor),
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .combinedClickable(
                    onClick = { onClick?.invoke() },
                    onLongClick = onLongClick
                )
        ) {
            content()
        }
    } else {
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            border = BorderStroke(1.dp, borderColor),
            modifier = modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}

/**
 * Colorful pictorial icon badge matching the reference app's vibrant icon style.
 */
@Composable
fun SquircleIconBadge(
    icon: ImageVector,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color? = null,
    tint: Color = accentColor,
    backgroundColor: Color? = null,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    pictorialEmoji: String? = null,
    modifier: Modifier = Modifier
) {
    val finalTint = if (tint != MaterialTheme.colorScheme.primary || accentColor == MaterialTheme.colorScheme.primary) tint else accentColor
    val finalBg = backgroundColor ?: containerColor ?: finalTint.copy(alpha = 0.12f)
    val emoji = pictorialEmoji ?: pictorialSymbolForIcon(icon)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(finalBg)
            .border(0.75.dp, finalTint.copy(alpha = 0.22f), shape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = (iconSize.value * 0.95f).sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Custom vector-rendered StudyForge Brand Logo on a light theme squircle badge
 * using the app's signature Deep Editorial Navy (#162238), Warm Orange (#DF6900),
 * Golden Amber (#F5A623), and Mint Emerald (#059669) colors.
 */
@Composable
fun StudyForgeBrandLogo(
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val bgBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(Color(0xFFF8FAFC), Color(0xFFE6EEF8))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFEBF2FA))
        )
    }
    val borderColor = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        border = BorderStroke(1.2.dp, borderColor),
        shadowElevation = 2.dp,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(bgBrush)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Subtle warm orange ambient halo
            drawCircle(
                color = Color(0xFFDF6900).copy(alpha = 0.10f),
                radius = w * 0.38f,
                center = Offset(w * 0.5f, h * 0.48f)
            )

            // Left Book Wing (Deep Editorial Navy #162238)
            val leftWing = Path().apply {
                moveTo(w * 0.18f, h * 0.40f)
                lineTo(w * 0.47f, h * 0.46f)
                lineTo(w * 0.47f, h * 0.76f)
                lineTo(w * 0.18f, h * 0.70f)
                close()
            }
            drawPath(leftWing, color = Color(0xFF162238))

            // Right Book Wing (Slate Navy #24344D)
            val rightWing = Path().apply {
                moveTo(w * 0.82f, h * 0.40f)
                lineTo(w * 0.53f, h * 0.46f)
                lineTo(w * 0.53f, h * 0.76f)
                lineTo(w * 0.82f, h * 0.70f)
                close()
            }
            drawPath(rightWing, color = Color(0xFF24344D))

            // Inner Crisp White Left Page
            val leftPage = Path().apply {
                moveTo(w * 0.22f, h * 0.37f)
                lineTo(w * 0.46f, h * 0.43f)
                lineTo(w * 0.46f, h * 0.71f)
                lineTo(w * 0.22f, h * 0.65f)
                close()
            }
            drawPath(leftPage, color = Color.White)

            // Inner Crisp Alabaster Right Page
            val rightPage = Path().apply {
                moveTo(w * 0.78f, h * 0.37f)
                lineTo(w * 0.54f, h * 0.43f)
                lineTo(w * 0.54f, h * 0.71f)
                lineTo(w * 0.78f, h * 0.65f)
                close()
            }
            drawPath(rightPage, color = Color(0xFFF4F7FA))

            // Page accent lines
            val strokeW = w * 0.042f
            drawLine(
                color = Color(0xFF162238),
                start = Offset(w * 0.27f, h * 0.46f),
                end = Offset(w * 0.41f, h * 0.49f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFDF6900),
                start = Offset(w * 0.27f, h * 0.55f),
                end = Offset(w * 0.41f, h * 0.58f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF162238),
                start = Offset(w * 0.59f, h * 0.49f),
                end = Offset(w * 0.73f, h * 0.46f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFF5A623),
                start = Offset(w * 0.59f, h * 0.58f),
                end = Offset(w * 0.73f, h * 0.55f),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )

            // Center Spine in Warm Orange (#DF6900)
            drawRoundRect(
                color = Color(0xFFDF6900),
                topLeft = Offset(w * 0.465f, h * 0.40f),
                size = Size(w * 0.07f, h * 0.37f),
                cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
            )

            // Rising 4-Point Knowledge Spark (Warm Orange #DF6900 outer + Golden Amber #F5A623 inner)
            val outerStar = Path().apply {
                moveTo(w * 0.50f, h * 0.14f)
                lineTo(w * 0.55f, h * 0.26f)
                lineTo(w * 0.67f, h * 0.31f)
                lineTo(w * 0.55f, h * 0.36f)
                lineTo(w * 0.50f, h * 0.48f)
                lineTo(w * 0.45f, h * 0.36f)
                lineTo(w * 0.33f, h * 0.31f)
                lineTo(w * 0.45f, h * 0.26f)
                close()
            }
            drawPath(outerStar, color = Color(0xFFDF6900))

            val innerStar = Path().apply {
                moveTo(w * 0.50f, h * 0.19f)
                lineTo(w * 0.532f, h * 0.278f)
                lineTo(w * 0.62f, h * 0.31f)
                lineTo(w * 0.532f, h * 0.342f)
                lineTo(w * 0.50f, h * 0.43f)
                lineTo(w * 0.468f, h * 0.342f)
                lineTo(w * 0.38f, h * 0.31f)
                lineTo(w * 0.468f, h * 0.278f)
                close()
            }
            drawPath(innerStar, color = Color(0xFFF5A623))

            drawCircle(
                color = Color.White,
                radius = w * 0.032f,
                center = Offset(w * 0.50f, h * 0.31f)
            )

            // Mint Emerald Mastery Pedestal Base (#059669)
            val baseRibbon = Path().apply {
                moveTo(w * 0.33f, h * 0.80f)
                lineTo(w * 0.67f, h * 0.80f)
                lineTo(w * 0.62f, h * 0.86f)
                lineTo(w * 0.38f, h * 0.86f)
                close()
            }
            drawPath(baseRibbon, color = Color(0xFF059669))
        }
    }
}

/**
 * Dark Slate-Navy Dashboard Hero Card inspired by the reference app's "00-99 Coverage Dashboard" card:
 * - Deep navy gradient surface (#1C2638 -> #27344B)
 * - Golden-Amber progress bar (#F5A623) and large golden percentage readout
 * - Clean white typography and high-contrast status chips
 */
@Composable
fun HeroLearningCard(
    category: String = "CONTINUE LEARNING",
    title: String,
    subtitle: String? = null,
    masteryScore: Int? = null,
    watermarkSymbol: String? = null,
    watermarkIcon: ImageVector? = null,
    actionIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowForward,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val score = (masteryScore ?: 0).coerceIn(0, 100)
    val progress = (score / 100f).coerceIn(0.04f, 1f)

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(DarkSlateHeroStart, DarkSlateHeroEnd)
                )
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = "🎯",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = category.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            fontSize = 11.5.sp
                        ),
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (masteryScore != null) {
                    Text(
                        text = "${score}.0%",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = GoldenAmberBar
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = WarmOrangeBadge
                    ) {
                        Text(
                            text = "Start →",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 21.sp,
                    lineHeight = 28.sp
                ),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFCBD5E1),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (masteryScore != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(100))
                            .background(Color(0xFF334155))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = progress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(100))
                                .background(GoldenAmberBar)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(GoldenAmberBar)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector = Icons.Default.Info,
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SquircleIconBadge(
            icon = icon,
            size = 64.dp,
            iconSize = 30.dp,
            shape = RoundedCornerShape(20.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    subtext: String? = null,
    modifier: Modifier = Modifier
) {
    StudyCard(
        shape = RoundedCornerShape(18.dp),
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.85f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(end = 4.dp)
                )
                SquircleIconBadge(
                    icon = icon,
                    accentColor = accentColor,
                    size = 30.dp,
                    iconSize = 16.dp,
                    shape = RoundedCornerShape(10.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 21.sp,
                    letterSpacing = (-0.4).sp
                ),
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtext != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MasteryProgressBar(
    score: Int,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    height: Dp = 8.dp
) {
    val progress = (score / 100f).coerceIn(0f, 1f)
    val barColor = when {
        score >= 75 -> SuccessGreen
        score >= 40 -> GoldenAmberBar
        else -> WarmOrangeBadge
    }

    Column(modifier = modifier) {
        if (showLabel) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mastery Progress",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$score%",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = barColor
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(100))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (progress > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(100))
                        .background(barColor)
                )
            }
        }
    }
}

@Composable
fun DifficultyBadge(difficulty: Difficulty) {
    val (bg, textColor, label) = when (difficulty) {
        Difficulty.EASY -> Triple(SoftMintPillBg, SoftMintPillText, "✓ Easy (A-Tier)")
        Difficulty.MEDIUM -> Triple(SoftOrangePillBg, SoftOrangePillText, "Medium (B-Tier)")
        Difficulty.HARD -> Triple(Color(0xFFFEE2E2), ErrorRed, "Hard")
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg,
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(end = 8.dp)
        )
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = WarmOrangeBadge
                )
            }
        }
    }
}

/**
 * Explanation Card styled after the reference screenshot's MCQ Solution Card:
 * - Crisp white card surface (#FFFFFF) with subtle border
 * - Pictorial lightbulb "💡" + deep crimson/orange-red header ("💡 Solution & Explanation")
 * - Warm orange rounded square badge ("SOL") + full LaTeX/Markdown rendering
 * - Pictorial brain "🧠" topic/concept footer
 */
@Composable
fun ExplanationBox(
    explanation: String,
    modifier: Modifier = Modifier,
    topic: String? = null,
    correctAnswerLabel: String? = null
) {
    if (explanation.isBlank() && correctAnswerLabel.isNullOrBlank()) return

    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    } else {
        Color.White
    }
    val borderColor = if (isDark) {
        MaterialTheme.colorScheme.outline
    } else {
        Color(0xFFE2E8F0)
    }
    val headerColor = if (isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "💡",
                    fontSize = 19.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (!correctAnswerLabel.isNullOrBlank()) {
                        "Correct Answer: $correctAnswerLabel"
                    } else {
                        "Explanation & Solution"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    ),
                    color = headerColor,
                    modifier = Modifier.weight(1f)
                )
            }

            if (explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = WarmOrangeBadge,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "SOL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            ),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        MarkdownMathView(markdownText = explanation)
                    }
                }
            }

            if (!topic.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🧠", fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Concept / Sutra: $topic",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
