package com.studyforge.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studyforge.app.data.local.entities.StudySessionEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DayActivity(
    val dateMillis: Long,
    val dayLabel: String,
    val minutesStudied: Int,
    val questionsSolved: Int,
    val testsCount: Int
)

/**
 * Ultra-fast 120 FPS Canvas-based Study Activity Heatmap.
 * Replaces 84 nested Box/Border/Clip composables with a single hardware-accelerated Canvas draw pass.
 */
@Composable
fun StudyHeatmap(
    sessions: List<StudySessionEntity>,
    modifier: Modifier = Modifier
) {
    val daysList = remember(sessions) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayMillis = calendar.timeInMillis
        val daysCount = 84 // 12 weeks
        val list = ArrayList<DayActivity>(daysCount)
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

        for (i in (daysCount - 1) downTo 0) {
            cal.timeInMillis = todayMillis
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dMillis = cal.timeInMillis
            val nextDayMillis = dMillis + 86400000L

            var totalMins = 0
            var qSolved = 0
            var tests = 0
            for (s in sessions) {
                if (s.dateMillis in dMillis until nextDayMillis) {
                    totalMins += s.durationMinutes
                    qSolved += s.questionsSolved
                    if (s.notes.contains("test", ignoreCase = true)) tests++
                }
            }

            list.add(
                DayActivity(
                    dateMillis = dMillis,
                    dayLabel = sdf.format(Date(dMillis)),
                    minutesStudied = totalMins,
                    questionsSolved = qSolved,
                    testsCount = tests
                )
            )
        }
        list
    }

    val weeks = remember(daysList) { daysList.chunked(7) }
    var selectedDay by remember { mutableStateOf<DayActivity?>(null) }
    val scrollState = rememberScrollState(Int.MAX_VALUE)

    val emptyCellColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    val level1Color = Color(0xFF86EFAC)
    val level2Color = Color(0xFF22C55E)
    val level3Color = Color(0xFF15803D)

    val density = LocalDensity.current
    val cellSizeDp = 16.dp
    val spacingDp = 4.dp
    val cornerRadiusDp = 4.dp

    val numWeeks = weeks.size
    val canvasWidthDp = (cellSizeDp * numWeeks) + (spacingDp * (numWeeks - 1).coerceAtLeast(0))
    val canvasHeightDp = (cellSizeDp * 7) + (spacingDp * 6)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Study Activity Heatmap",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "Past 12 Weeks",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                Canvas(
                    modifier = Modifier
                        .width(canvasWidthDp)
                        .height(canvasHeightDp)
                        .pointerInput(weeks) {
                            detectTapGestures { offset ->
                                val cellPx = with(density) { cellSizeDp.toPx() }
                                val spacePx = with(density) { spacingDp.toPx() }
                                val stepPx = cellPx + spacePx
                                val col = (offset.x / stepPx).toInt()
                                val row = (offset.y / stepPx).toInt()
                                if (col in weeks.indices) {
                                    val week = weeks[col]
                                    if (row in week.indices) {
                                        selectedDay = week[row]
                                    }
                                }
                            }
                        }
                ) {
                    val cellPx = cellSizeDp.toPx()
                    val spacePx = spacingDp.toPx()
                    val stepPx = cellPx + spacePx
                    val radiusPx = cornerRadiusDp.toPx()

                    for (wIdx in weeks.indices) {
                        val week = weeks[wIdx]
                        val x = wIdx * stepPx
                        for (dIdx in week.indices) {
                            val day = week[dIdx]
                            val y = dIdx * stepPx
                            val cellColor = when {
                                day.minutesStudied == 0 -> emptyCellColor
                                day.minutesStudied < 25 -> level1Color
                                day.minutesStudied < 60 -> level2Color
                                else -> level3Color
                            }
                            drawRoundRect(
                                color = cellColor,
                                topLeft = Offset(x, y),
                                size = Size(cellPx, cellPx),
                                cornerRadius = CornerRadius(radiusPx, radiusPx)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Less",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                listOf(emptyCellColor, level1Color, level2Color, level3Color).forEach { c ->
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(c)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    "More",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    selectedDay?.let { day ->
        AlertDialog(
            onDismissRequest = { selectedDay = null },
            title = { Text(day.dayLabel, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("⏱ Study Time: ${day.minutesStudied} minutes", style = MaterialTheme.typography.bodyLarge)
                    Text("🎯 Questions Solved: ${day.questionsSolved}", style = MaterialTheme.typography.bodyLarge)
                    Text("📝 Tests Completed: ${day.testsCount}", style = MaterialTheme.typography.bodyLarge)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedDay = null }) {
                    Text("Close")
                }
            }
        )
    }
}
