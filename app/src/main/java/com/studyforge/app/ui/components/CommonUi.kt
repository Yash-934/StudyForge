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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyforge.app.domain.model.Difficulty
import com.studyforge.app.ui.theme.ErrorRed
import com.studyforge.app.ui.theme.JetBrainsMono
import com.studyforge.app.ui.theme.SuccessGreen
import com.studyforge.app.ui.theme.WarningYellow

/**
 * High-performance Neo-Academic card with zero-overhead border and crisp surface hierarchy.
 * Uses flat/tonal rendering by default to avoid expensive GPU shadow rasterization during scrolling.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudyCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
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
 * Squircle badge for icons and actions with crisp contrast.
 */
@Composable
fun SquircleIconBadge(
    icon: ImageVector,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color? = null,
    tint: Color = accentColor,
    backgroundColor: Color? = null,
    size: Dp = 38.dp,
    iconSize: Dp = 20.dp,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    modifier: Modifier = Modifier
) {
    val finalTint = if (tint != MaterialTheme.colorScheme.primary || accentColor == MaterialTheme.colorScheme.primary) tint else accentColor
    val finalBg = backgroundColor ?: containerColor ?: finalTint.copy(alpha = 0.14f)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(finalBg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = finalTint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Redesigned Hero Learning Card with subtle atmospheric gradient, pill category badge,
 * mathematical watermark, and smooth mastery bar.
 */
@Composable
fun HeroLearningCard(
    category: String = "CONTINUE LEARNING",
    title: String,
    subtitle: String? = null,
    masteryScore: Int? = null,
    watermarkSymbol: String? = "∫dx",
    watermarkIcon: ImageVector? = null,
    actionIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowForward,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val primary = MaterialTheme.colorScheme.primary
    val gradientColors = if (isDark) {
        listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
        )
    } else {
        listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
        )
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, primary.copy(alpha = 0.25f)),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(gradientColors))
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (watermarkSymbol != null) {
                Text(
                    text = watermarkSymbol,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic
                    ),
                    color = primary.copy(alpha = 0.10f),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 52.dp, bottom = 12.dp)
                )
            } else if (watermarkIcon != null) {
                Icon(
                    imageVector = watermarkIcon,
                    contentDescription = null,
                    tint = primary.copy(alpha = 0.10f),
                    modifier = Modifier
                        .size(56.dp)
                        .align(Alignment.CenterEnd)
                        .padding(end = 52.dp)
                )
            }

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
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = category.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.1.sp,
                                fontSize = 11.sp
                            ),
                            color = primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = actionIcon,
                            contentDescription = "Open",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.4).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (masteryScore != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    MasteryProgressBar(score = masteryScore)
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
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
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
        borderColor = accentColor.copy(alpha = 0.22f),
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
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
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
    val (startColor, endColor) = when {
        score >= 75 -> SuccessGreen to Color(0xFF34D399)
        score >= 40 -> WarningYellow to Color(0xFFFBBF24)
        else -> MaterialTheme.colorScheme.primary to Color(0xFF818CF8)
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
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold
                    ),
                    color = startColor
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(100))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
        ) {
            if (progress > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(100))
                        .background(
                            Brush.horizontalGradient(listOf(startColor, endColor))
                        )
                )
            }
        }
    }
}

@Composable
fun DifficultyBadge(difficulty: Difficulty) {
    val (color, text) = when (difficulty) {
        Difficulty.EASY -> SuccessGreen to "Easy"
        Difficulty.MEDIUM -> WarningYellow to "Medium"
        Difficulty.HARD -> ErrorRed to "Hard"
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.35f)),
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(end = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

/**
 * Clean, flat, modern Explanation Box matching Material 3 theme design:
 * - Soothing, Material 3 theme-compliant background and crisp border.
 * - Bold "Explanation" heading with Lightbulb icon badge.
 * - Full support for LaTeX math formulas, KaTeX, Markdown, tables, bold/italic syntax.
 */
@Composable
fun ExplanationBox(
    explanation: String,
    modifier: Modifier = Modifier,
    topic: String? = null
) {
    if (explanation.isBlank()) return

    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
    }
    val borderColor = if (isDark) {
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
    }
    val titleColor = MaterialTheme.colorScheme.secondary
    val shape = RoundedCornerShape(20.dp)

    Surface(
        shape = shape,
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                SquircleIconBadge(
                    icon = Icons.Default.Lightbulb,
                    accentColor = titleColor,
                    size = 32.dp,
                    iconSize = 18.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Explanation",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    ),
                    color = titleColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            MarkdownMathView(markdownText = explanation)

            if (!topic.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Topic: $topic",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = titleColor.copy(alpha = 0.85f)
                )
            }
        }
    }
}
