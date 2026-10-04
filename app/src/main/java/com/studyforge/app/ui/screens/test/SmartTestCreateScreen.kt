package com.studyforge.app.ui.screens.test

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studyforge.app.domain.model.SmartTestDistribution
import com.studyforge.app.ui.theme.PurpleAccent
import com.studyforge.app.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartTestCreateScreen(
    chapterId: Long,
    viewModel: StudyViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chapters by viewModel.chapters.collectAsState()
    val chapter = chapters.find { it.id == chapterId }

    var title by remember { mutableStateOf("Smart Drill: ${chapter?.name ?: "All Chapters"}") }
    var targetCount by remember { mutableIntStateOf(10) }
    var durationMinutes by remember { mutableIntStateOf(20) }

    // Distribution Sliders
    var weakPct by remember { mutableFloatStateOf(40f) }
    var mediumPct by remember { mutableFloatStateOf(30f) }
    var recentPct by remember { mutableFloatStateOf(20f) }
    var incorrectPct by remember { mutableFloatStateOf(10f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Test Generator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.createSmartTest(
                        title = title,
                        batchId = chapter?.batchId,
                        subjectId = chapter?.subjectId,
                        chapterId = if (chapterId > 0) chapterId else null,
                        targetCount = targetCount,
                        durationMinutes = durationMinutes,
                        distribution = SmartTestDistribution(
                            weakAreasPercent = weakPct.toInt(),
                            mediumPerformancePercent = mediumPct.toInt(),
                            recentlyLearnedPercent = recentPct.toInt(),
                            previouslyIncorrectPercent = incorrectPct.toInt()
                        )
                    )
                    onBack()
                },
                containerColor = PurpleAccent,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Generate")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PurpleAccent.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = PurpleAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Smart Test analyzes your past attempts and mistakes to formulate an optimal question set tailored to boost your mastery.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Test Title *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = targetCount.toString(),
                    onValueChange = { targetCount = it.toIntOrNull() ?: 10 },
                    label = { Text("Questions Count") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = durationMinutes.toString(),
                    onValueChange = { durationMinutes = it.toIntOrNull() ?: 20 },
                    label = { Text("Duration (mins)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Text("Presets", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = weakPct == 50f && incorrectPct == 30f,
                    onClick = {
                        weakPct = 50f
                        incorrectPct = 30f
                        mediumPct = 10f
                        recentPct = 10f
                    },
                    label = { Text("Weak Area Blast") }
                )
                FilterChip(
                    selected = weakPct == 40f && mediumPct == 30f,
                    onClick = {
                        weakPct = 40f
                        mediumPct = 30f
                        recentPct = 20f
                        incorrectPct = 10f
                    },
                    label = { Text("Balanced Drill") }
                )
                FilterChip(
                    selected = recentPct == 60f,
                    onClick = {
                        recentPct = 60f
                        mediumPct = 20f
                        weakPct = 10f
                        incorrectPct = 10f
                    },
                    label = { Text("Fresh Revision") }
                )
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Configurable Adaptive Distribution",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Weak Areas: ${weakPct.toInt()}%", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = weakPct,
                        onValueChange = { weakPct = it },
                        valueRange = 0f..100f
                    )

                    Text("Medium Performance: ${mediumPct.toInt()}%", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = mediumPct,
                        onValueChange = { mediumPct = it },
                        valueRange = 0f..100f
                    )

                    Text("Recently Learned: ${recentPct.toInt()}%", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = recentPct,
                        onValueChange = { recentPct = it },
                        valueRange = 0f..100f
                    )

                    Text("Previously Incorrect Mistakes: ${incorrectPct.toInt()}%", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = incorrectPct,
                        onValueChange = { incorrectPct = it },
                        valueRange = 0f..100f
                    )
                }
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}
