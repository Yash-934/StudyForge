package com.studyforge.app.ui.screens.planner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studyforge.app.data.local.entities.JournalEntity
import com.studyforge.app.data.local.entities.TimetableEntity
import com.studyforge.app.domain.model.Mood
import com.studyforge.app.ui.components.EmptyState
import com.studyforge.app.viewmodel.StudyViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    viewModel: StudyViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val timetable by viewModel.timetable.collectAsState()
    val journals by viewModel.journals.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Timetable, 1 = Journal
    var showAddScheduleDialog by remember { mutableStateOf(false) }

    // Today's journal state
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val todayMillis = cal.timeInMillis

    var todayJournal by remember { mutableStateOf<JournalEntity?>(null) }
    var journalText by remember { mutableStateOf("") }
    var accomplishments by remember { mutableStateOf("") }
    var difficulties by remember { mutableStateOf("") }
    var tomorrowPlan by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf(Mood.GOOD) }

    LaunchedEffect(journals) {
        val found = journals.find { it.dateMillis == todayMillis }
        if (found != null) {
            todayJournal = found
            journalText = found.freeText
            accomplishments = found.accomplishments
            difficulties = found.difficulties
            tomorrowPlan = found.tomorrowPlan
            selectedMood = found.mood
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Study Planner & Journal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddScheduleDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Schedule Slot")
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Timetable Schedule") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Daily Study Journal") }
                )
            }

            if (selectedTab == 0) {
                // TIMETABLE
                if (timetable.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.Schedule,
                        title = "No Schedule Slots Yet",
                        message = "Organize recurring weekly study blocks for subjects and chapters with alarms.",
                        actionLabel = "Add Schedule Slot",
                        onAction = { showAddScheduleDialog = true }
                    )
                } else {
                    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(timetable) { slot ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${days.getOrElse(slot.dayOfWeek - 1) { "Day" }} • ${slot.startTime} - ${slot.endTime}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (slot.reminderEnabled) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Alarm,
                                                            contentDescription = "Alarm Active",
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = "Alarm On",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = slot.subjectName.ifBlank { "Study Session" },
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = slot.chapterOrTask,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                viewModel.toggleTimetableAlarm(slot, !slot.reminderEnabled, context)
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (slot.reminderEnabled) Icons.Default.Alarm else Icons.Default.AlarmOff,
                                                contentDescription = if (slot.reminderEnabled) "Alarm Active (tap to disable)" else "Alarm Off (tap to enable)",
                                                tint = if (slot.reminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                            )
                                        }

                                        IconButton(onClick = { viewModel.deleteTimetableEntry(slot, context) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Slot",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(72.dp)) }
                    }
                }
            } else {
                // JOURNAL TAB
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
                        Text(
                            text = sdf.format(Date(todayMillis)),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    item {
                        Text("How was your study energy today?", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        // Horizontally scrollable row prevents vertical text overflow and letter squeezing
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Mood.values().forEach { m ->
                                FilterChip(
                                    selected = selectedMood == m,
                                    onClick = { selectedMood = m },
                                    label = { Text("${m.emoji} ${m.label}", maxLines = 1) }
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = accomplishments,
                            onValueChange = { accomplishments = it },
                            label = { Text("What did you accomplish today?") },
                            placeholder = { Text("e.g. Mastered Integration by Parts, solved 15 calculus problems") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = difficulties,
                            onValueChange = { difficulties = it },
                            label = { Text("What challenges or doubts arose?") },
                            placeholder = { Text("e.g. Struggled with partial fraction decomposition") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = tomorrowPlan,
                            onValueChange = { tomorrowPlan = it },
                            label = { Text("Plan for tomorrow:") },
                            placeholder = { Text("e.g. Definite integrals drill + mistake revision") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = journalText,
                            onValueChange = { journalText = it },
                            label = { Text("Freeform Study Thoughts / Notes:") },
                            placeholder = { Text("Reflections, motivations, ideas...") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                viewModel.saveJournal(
                                    JournalEntity(
                                        dateMillis = todayMillis,
                                        freeText = journalText,
                                        mood = selectedMood,
                                        accomplishments = accomplishments,
                                        difficulties = difficulties,
                                        tomorrowPlan = tomorrowPlan
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (todayJournal != null) "Update Today's Journal" else "Save Journal Entry")
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    if (showAddScheduleDialog) {
        AddScheduleDialog(
            onDismiss = { showAddScheduleDialog = false },
            onConfirm = { sub, task, day, start, end, alarm ->
                viewModel.addTimetableEntry(
                    subjectName = sub,
                    chapterTask = task,
                    dayOfWeek = day,
                    startTime = start,
                    endTime = end,
                    reminderEnabled = alarm,
                    context = context
                )
                showAddScheduleDialog = false
            }
        )
    }
}

@Composable
fun AddScheduleDialog(
    onDismiss: () -> Unit,
    onConfirm: (sub: String, task: String, day: Int, start: String, end: String, alarm: Boolean) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var task by remember { mutableStateOf("") }
    var dayOfWeek by remember { mutableIntStateOf(1) } // 1 = Mon
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:30") }
    var enableAlarm by remember { mutableStateOf(true) }

    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Schedule Block", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject *") },
                    placeholder = { Text("e.g. Mathematics") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = task,
                    onValueChange = { task = it },
                    label = { Text("Task / Focus *") },
                    placeholder = { Text("e.g. Practice questions & notes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Day of Week:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    days.forEachIndexed { idx, d ->
                        FilterChip(
                            selected = dayOfWeek == (idx + 1),
                            onClick = { dayOfWeek = idx + 1 },
                            label = { Text(d) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        placeholder = { Text("09:00") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time") },
                        placeholder = { Text("10:30") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Set Study Alarm", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Ring alarm when time arrives", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = enableAlarm,
                            onCheckedChange = { enableAlarm = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subject.isNotBlank() && task.isNotBlank()) {
                        onConfirm(subject, task, dayOfWeek, startTime, endTime, enableAlarm)
                    }
                },
                enabled = subject.isNotBlank() && task.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
