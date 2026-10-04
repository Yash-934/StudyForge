package com.studyforge.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyforge.app.data.local.entities.StudySessionEntity
import com.studyforge.app.ui.theme.isAppDark
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

@Composable
fun StudyHeatmap(
    sessions: List<StudySessionEntity>,
    modifier: Modifier = Modifier
) {
    // Generate past 12 weeks of days (84 days)
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val todayMillis = calendar.timeInMillis
    val daysCount = 84 // 12 weeks
    val daysList = remember(sessions) {
        val list = mutableListOf<DayActivity>()
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

        for (i in (daysCount - 1) downTo 0) {
            cal.timeInMillis = todayMillis
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dMillis = cal.timeInMillis
            val nextDayMillis = dMillis + 86400000L

            val matchingSessions = sessions.filter { it.dateMillis in dMillis until nextDayMillis }
            val totalMins = matchingSessions.sumOf { it.durationMinutes }
            val qSolved = matchingSessions.sumOf { it.questionsSolved }
            val tests = matchingSessions.count { it.notes.contains("test", ignoreCase = true) }

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

    var selectedDay by remember { mutableStateOf<DayActivity?>(null) }
    val scrollState = rememberScrollState(Int.MAX_VALUE) // Scroll to most recent
    val isDark = isAppDark()

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (isDark) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        },
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 1.5.dp),
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
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Past 12 Weeks",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 7 rows (Mon-Sun) across 12 columns
            Row(
                modifier = Modifier.horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Chunk into weeks of 7
                val weeks = daysList.chunked(7)
                for (week in weeks) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (day in week) {
                            HeatmapCell(day = day) {
                                selectedDay = day
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Less", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                listOf(0, 15, 45, 90).forEach { mins ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(getHeatmapColor(mins))
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Text("More", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    selectedDay?.let { day ->
        AlertDialog(
            onDismissRequest = { selectedDay = null },
            title = { Text(day.dayLabel, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("⏱ Study Time: ${day.minutesStudied} minutes", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("🎯 Questions Solved: ${day.questionsSolved}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(6.dp))
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

@Composable
private fun HeatmapCell(
    day: DayActivity,
    onClick: () -> Unit
) {
    val color = getHeatmapColor(day.minutesStudied)
    Box(
        modifier = Modifier
            .size(15.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(color)
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
            .clickable { onClick() }
    )
}

@Composable
private fun getHeatmapColor(minutes: Int): Color {
    val primary = MaterialTheme.colorScheme.primary
    return when {
        minutes == 0 -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
        minutes < 25 -> primary.copy(alpha = 0.35f)
        minutes < 60 -> primary.copy(alpha = 0.65f)
        else -> primary
    }
}
