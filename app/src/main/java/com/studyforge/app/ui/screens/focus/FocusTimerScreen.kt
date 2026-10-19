package com.studyforge.app.ui.screens.focus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyforge.app.ui.components.SectionHeader
import com.studyforge.app.ui.components.SquircleIconBadge
import com.studyforge.app.ui.components.StudyCard
import com.studyforge.app.ui.theme.DarkSlateHeroEnd
import com.studyforge.app.ui.theme.DarkSlateHeroStart
import com.studyforge.app.ui.theme.GoldenAmberBar
import com.studyforge.app.ui.theme.MintTagText
import com.studyforge.app.ui.theme.SuccessGreen
import com.studyforge.app.ui.theme.WarmOrangeBadge
import com.studyforge.app.ui.theme.WarningYellow
import com.studyforge.app.viewmodel.StudyViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class FocusPreset(
    val id: String,
    val label: String,
    val subtitle: String,
    val minutes: Int,
    val isStopwatch: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerScreen(
    viewModel: StudyViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subjects by viewModel.subjects.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val dailyGoalMins by viewModel.dailyStudyGoalMins.collectAsState()

    val presets = remember {
        listOf(
            FocusPreset("pomo25", "Pomodoro 25m", "High-focus sprint", 25),
            FocusPreset("deep50", "Deep Work 50m", "Intense concept block", 50),
            FocusPreset("drill15", "Quick Drill 15m", "Formula & flashcard recall", 15),
            FocusPreset("exam90", "Exam Block 90m", "Full stamina session", 90),
            FocusPreset("stopwatch", "Stopwatch", "Open-ended focus", 0, isStopwatch = true)
        )
    }

    val focusTags = remember {
        listOf(
            "📖 Reading Notes",
            "✍️ Solving MCQs",
            "🎥 Video Lecture",
            "🧮 Formula Revision",
            "🧠 Active Recall"
        )
    }

    var selectedPresetId by rememberSaveable { mutableStateOf("pomo25") }
    val activePreset = remember(selectedPresetId) {
        presets.find { it.id == selectedPresetId } ?: presets.first()
    }

    var selectedTag by rememberSaveable { mutableStateOf(focusTags.first()) }
    var selectedSubjectId by rememberSaveable { mutableLongStateOf(-1L) }
    var selectedChapterId by rememberSaveable { mutableLongStateOf(-1L) }

    var isRunning by rememberSaveable { mutableStateOf(false) }
    var totalTargetSeconds by rememberSaveable { mutableIntStateOf(25 * 60) }
    var remainingSeconds by rememberSaveable { mutableIntStateOf(25 * 60) }
    var elapsedSeconds by rememberSaveable { mutableIntStateOf(0) }

    var showSaveDialog by remember { mutableStateOf(false) }
    var showOfflineLogDialog by remember { mutableStateOf(false) }

    // Today's study minutes calculation
    val todayStartMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val todaySessions = remember(sessions, todayStartMillis) {
        sessions.filter { it.dateMillis >= todayStartMillis }.sortedByDescending { it.dateMillis }
    }
    val todayMinutes = remember(todaySessions) {
        todaySessions.sumOf { it.durationMinutes }
    }

    LaunchedEffect(isRunning, activePreset.isStopwatch) {
        while (isRunning) {
            delay(1000L)
            elapsedSeconds += 1
            if (!activePreset.isStopwatch) {
                if (remainingSeconds > 1) {
                    remainingSeconds -= 1
                } else {
                    remainingSeconds = 0
                    isRunning = false
                    showSaveDialog = true
                }
            }
        }
    }

    val displaySeconds = if (activePreset.isStopwatch) elapsedSeconds else remainingSeconds
    val dispMins = displaySeconds / 60
    val dispSecs = displaySeconds % 60
    val formattedTime = String.format(Locale.US, "%02d:%02d", dispMins, dispSecs)

    val rawProgress = if (activePreset.isStopwatch) {
        ((elapsedSeconds % 3600) / 3600f).coerceIn(0f, 1f)
    } else {
        if (totalTargetSeconds > 0) {
            ((totalTargetSeconds - remainingSeconds).toFloat() / totalTargetSeconds.toFloat()).coerceIn(0f, 1f)
        } else 0f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(durationMillis = 450),
        label = "focus_dial_progress"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Pomodoro & Deep Focus",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Today: ${todayMinutes}m / ${dailyGoalMins}m daily goal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { showOfflineLogDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("log_offline_session_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Offline", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Mode Presets Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { preset ->
                        val selected = preset.id == selectedPresetId
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (!isRunning) {
                                    selectedPresetId = preset.id
                                    totalTargetSeconds = preset.minutes * 60
                                    remainingSeconds = preset.minutes * 60
                                    elapsedSeconds = 0
                                }
                            },
                            label = {
                                Text(
                                    text = preset.label,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        )
                    }
                }
            }

            // Hero Timer Dial Card
            item {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = Color.Transparent,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(DarkSlateHeroStart, DarkSlateHeroEnd)
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top Status Pill
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, GoldenAmberBar.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isRunning) "🔥 DEEP FOCUS ACTIVE" else "⚡ READY TO FOCUS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = GoldenAmberBar
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Circular Timer Canvas
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(220.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 14.dp.toPx()
                                val diameter = size.minDimension - strokeWidth
                                val topLeft = Offset(
                                    (size.width - diameter) / 2f,
                                    (size.height - diameter) / 2f
                                )
                                // Track ring
                                drawArc(
                                    color = Color.White.copy(alpha = 0.12f),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = Size(diameter, diameter),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                                // Progress arc
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        colors = listOf(WarmOrangeBadge, GoldenAmberBar, Color(0xFF10B981), WarmOrangeBadge)
                                    ),
                                    startAngle = -90f,
                                    sweepAngle = (animatedProgress * 360f).coerceAtLeast(4f),
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = Size(diameter, diameter),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = formattedTime,
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 46.sp,
                                        letterSpacing = (-1).sp
                                    ),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = selectedTag,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        // Control Buttons Row (Sleek, ergonomic, zero-text-wrapping)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Reset Squircle Button - guaranteed no squish
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.14f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.32f)),
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        isRunning = false
                                        elapsedSeconds = 0
                                        remainingSeconds = totalTargetSeconds
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Reset Timer",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            // Primary Action Button (Play / Pause / Resume)
                            Button(
                                onClick = { isRunning = !isRunning },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isRunning) WarningYellow else WarmOrangeBadge,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .testTag("start_pause_focus_button")
                            ) {
                                Icon(
                                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isRunning) "Pause" else if (elapsedSeconds > 0) "Resume" else "Start Focus",
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            if (!activePreset.isStopwatch) {
                                OutlinedButton(
                                    onClick = {
                                        totalTargetSeconds += 5 * 60
                                        remainingSeconds += 5 * 60
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.32f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                    modifier = Modifier.height(54.dp)
                                ) {
                                    Text("+5m", fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                                }
                            }
                        }

                        if (elapsedSeconds >= 60) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    isRunning = false
                                    showSaveDialog = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SuccessGreen,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("finish_save_session_button")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Complete & Log ${(elapsedSeconds / 60).coerceAtLeast(1)}m Session to Heatmap",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Activity Tag & Subject Selector Card
            item {
                StudyCard(shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Focus Activity & Subject Tag",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            focusTags.forEach { tag ->
                                FilterChip(
                                    selected = selectedTag == tag,
                                    onClick = { selectedTag = tag },
                                    label = { Text(tag, style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false) }
                                )
                            }
                        }

                        if (subjects.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Link to Subject (Optional):",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedSubjectId == -1L,
                                    onClick = {
                                        selectedSubjectId = -1L
                                        selectedChapterId = -1L
                                    },
                                    label = { Text("General Study", maxLines = 1, softWrap = false) }
                                )
                                subjects.forEach { sub ->
                                    FilterChip(
                                        selected = selectedSubjectId == sub.id,
                                        onClick = {
                                            selectedSubjectId = sub.id
                                            selectedChapterId = chapters.firstOrNull { it.subjectId == sub.id }?.id ?: -1L
                                        },
                                        label = { Text(sub.name, maxLines = 1, softWrap = false) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Today's Focus Sessions History
            item {
                SectionHeader(title = "Today's Logged Study Sessions (${todaySessions.size})")
            }

            if (todaySessions.isEmpty()) {
                item {
                    StudyCard(shape = RoundedCornerShape(18.dp)) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SquircleIconBadge(
                                icon = Icons.Default.History,
                                accentColor = MaterialTheme.colorScheme.primary,
                                size = 40.dp,
                                iconSize = 20.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "No study sessions logged today yet",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Start the Pomodoro timer above or tap 'Log Offline' to light up today's cell on your Study Heatmap!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                items(todaySessions, key = { it.id }) { session ->
                    val subjectName = subjects.find { it.id == session.subjectId }?.name ?: "General Focus"
                    val timeFmt = remember(session.dateMillis) {
                        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(session.dateMillis))
                    }
                    StudyCard(shape = RoundedCornerShape(18.dp)) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SquircleIconBadge(
                                icon = Icons.Default.LocalFireDepartment,
                                accentColor = WarmOrangeBadge,
                                size = 40.dp,
                                iconSize = 20.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${session.durationMinutes} mins • $subjectName",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = session.notes.ifBlank { "Focused Study Session" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = timeFmt,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (session.questionsSolved > 0) {
                                    Text(
                                        text = "+${session.questionsSolved} Qs",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MintTagText
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }

    if (showSaveDialog) {
        val completedMins = (elapsedSeconds / 60).coerceAtLeast(1)
        var questionsSolvedInput by remember { mutableStateOf("0") }
        var sessionNotesInput by remember { mutableStateOf("$selectedTag (${activePreset.label})") }

        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Focus Session 🎉", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Great job! You focused for $completedMins minutes. Save this session to update your Study Heatmap & Analytics.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = questionsSolvedInput,
                        onValueChange = { questionsSolvedInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Questions / MCQs Solved (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sessionNotesInput,
                        onValueChange = { sessionNotesInput = it },
                        label = { Text("Session Note / Topic") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sub = subjects.find { it.id == selectedSubjectId }
                        viewModel.logStudySession(
                            batchId = sub?.batchId,
                            subjectId = if (selectedSubjectId > 0) selectedSubjectId else null,
                            chapterId = if (selectedChapterId > 0) selectedChapterId else null,
                            durationMinutes = completedMins,
                            questionsSolved = questionsSolvedInput.toIntOrNull() ?: 0,
                            notes = sessionNotesInput
                        )
                        elapsedSeconds = 0
                        remainingSeconds = totalTargetSeconds
                        showSaveDialog = false
                    }
                ) {
                    Text("Save Session")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showOfflineLogDialog) {
        var minsInput by remember { mutableStateOf("45") }
        var qsInput by remember { mutableStateOf("15") }
        var noteInput by remember { mutableStateOf("Offline Book / Coaching Study") }

        AlertDialog(
            onDismissRequest = { showOfflineLogDialog = false },
            title = { Text("Log Offline Study Session", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Studied from a physical book, coaching class, or lecture? Log it here to keep your Study Consistency Heatmap accurate.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = minsInput,
                        onValueChange = { minsInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Duration (Minutes)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = qsInput,
                        onValueChange = { qsInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("MCQs / Questions Solved") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("What did you study?") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mins = (minsInput.toIntOrNull() ?: 30).coerceIn(1, 720)
                        val qs = (qsInput.toIntOrNull() ?: 0).coerceAtLeast(0)
                        val sub = subjects.find { it.id == selectedSubjectId }
                        viewModel.logStudySession(
                            batchId = sub?.batchId,
                            subjectId = if (selectedSubjectId > 0) selectedSubjectId else null,
                            chapterId = if (selectedChapterId > 0) selectedChapterId else null,
                            durationMinutes = mins,
                            questionsSolved = qs,
                            notes = noteInput
                        )
                        showOfflineLogDialog = false
                    }
                ) {
                    Text("Log to Heatmap")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOfflineLogDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
