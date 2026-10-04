package com.studyforge.app.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyforge.app.ui.components.MasteryProgressBar
import com.studyforge.app.ui.components.SectionHeader
import com.studyforge.app.ui.components.StatCard
import com.studyforge.app.ui.components.StudyCard
import com.studyforge.app.ui.components.StudyHeatmap
import com.studyforge.app.ui.theme.ErrorRed
import com.studyforge.app.ui.theme.PurpleAccent
import com.studyforge.app.ui.theme.SuccessGreen
import com.studyforge.app.ui.theme.WarningYellow
import com.studyforge.app.viewmodel.StudyViewModel

@Composable
fun AnalyticsScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val analytics by viewModel.analytics.collectAsState()
    val sessions by viewModel.sessions.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Performance Analytics",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Deterministic Mastery & Learning Telemetry",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Top 4 Stat Metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Study Time",
                    value = "${analytics.totalStudyTimeMinutes}m",
                    icon = Icons.Default.Timer,
                    accentColor = MaterialTheme.colorScheme.primary,
                    subtext = "${analytics.currentStreakDays} days streak",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Questions Solved",
                    value = "${analytics.totalQuestionsSolved}",
                    icon = Icons.Default.CheckCircle,
                    accentColor = SuccessGreen,
                    subtext = "${String.format("%.1f", analytics.overallAccuracy)}% accuracy",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Tests Completed",
                    value = "${analytics.totalTestsCompleted}",
                    icon = Icons.Default.FitnessCenter,
                    accentColor = PurpleAccent,
                    subtext = "${String.format("%.1f", analytics.overallAverageScore)}% avg score",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Mistakes Resolved",
                    value = "${analytics.resolvedMistakesCount}/${analytics.totalMistakesRecorded}",
                    icon = Icons.Default.QueryStats,
                    accentColor = WarningYellow,
                    subtext = "Blind spots cleared",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Study Heatmap
        item {
            SectionHeader(title = "Study Heatmap & Consistency")
            StudyHeatmap(sessions = sessions)
        }

        // Subject Mastery Breakdown
        item {
            SectionHeader(title = "Subject Mastery Breakdown")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (analytics.subjectMasteries.isEmpty()) {
                    Text(
                        text = "No subjects registered yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    analytics.subjectMasteries.forEach { sub ->
                        StudyCard(
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sub.subjectName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${sub.masteryScore}%",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${sub.chapterCount} chapters • ${sub.testsCompleted} tests completed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                MasteryProgressBar(score = sub.masteryScore, showLabel = false)
                            }
                        }
                    }
                }
            }
        }

        // Strong vs Weak Chapters
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Weak chapters
                StudyCard(
                    shape = RoundedCornerShape(18.dp),
                    borderColor = ErrorRed.copy(alpha = 0.35f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ThumbDown, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Weak Areas (<60%)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = ErrorRed)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (analytics.weakChapters.isEmpty()) {
                            Text("None! Great shape.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            analytics.weakChapters.take(3).forEach { chap ->
                                Text("• ${chap.chapterName} (${chap.masteryScore}%)", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // Strong chapters
                StudyCard(
                    shape = RoundedCornerShape(18.dp),
                    borderColor = SuccessGreen.copy(alpha = 0.35f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ThumbUp, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Strong Areas (≥60%)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = SuccessGreen)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (analytics.strongChapters.isEmpty()) {
                            Text("Solve more tests to build strength.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            analytics.strongChapters.take(3).forEach { chap ->
                                Text("• ${chap.chapterName} (${chap.masteryScore}%)", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
