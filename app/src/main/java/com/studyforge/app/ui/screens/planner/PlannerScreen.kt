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
import com.studyforge.app.ui.components.SquircleIconBadge
import com.studyforge.app.ui.components.StudyCard
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Study Planner & Journal",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
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
                            StudyCard(
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        SquircleIconBadge(
                                            icon = Icons.Default.Schedule,
                                            tint = MaterialTheme.colorScheme.primary,
                                            size = 46.dp,
                                            iconSize = 22.dp
                                        )
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column {
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
                                                        shape = RoundedCornerShape(6.dp),
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
                // JOURNAL TAB: Comprehensive Calendar, Entries List & Reflection Writer
                JournalSectionView(
                    viewModel = viewModel,
                    journals = journals,
                    modifier = Modifier.fillMaxSize()
                )
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = task,
                    onValueChange = { task = it },
                    label = { Text("Task / Focus *") },
                    placeholder = { Text("e.g. Practice questions & notes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
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
                            label = { Text(d, maxLines = 1, softWrap = false) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        placeholder = { Text("09:00") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time") },
                        placeholder = { Text("10:30") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
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
