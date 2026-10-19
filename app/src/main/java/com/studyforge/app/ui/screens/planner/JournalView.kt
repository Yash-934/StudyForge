package com.studyforge.app.ui.screens.planner

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyforge.app.data.local.entities.JournalEntity
import com.studyforge.app.domain.model.Mood
import com.studyforge.app.ui.components.EmptyState
import com.studyforge.app.ui.components.SectionHeader
import com.studyforge.app.ui.components.StudyCard
import com.studyforge.app.ui.theme.ErrorRed
import com.studyforge.app.ui.theme.GoldenAmberBar
import com.studyforge.app.ui.theme.SoftMintPillBg
import com.studyforge.app.ui.theme.SoftMintPillText
import com.studyforge.app.ui.theme.SoftOrangePillBg
import com.studyforge.app.ui.theme.SoftOrangePillText
import com.studyforge.app.ui.theme.SuccessGreen
import com.studyforge.app.ui.theme.WarmOrangeBadge
import com.studyforge.app.viewmodel.StudyViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Normalizes any timestamp to the midnight (00:00:00.000) of that day.
 */
fun normalizeToMidnight(millis: Long): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalSectionView(
    viewModel: StudyViewModel,
    journals: List<JournalEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val todayMidnight = remember { normalizeToMidnight(System.currentTimeMillis()) }

    // Sub-tab: 0 = Calendar View, 1 = All Entries List, 2 = Write/Edit
    var currentSubTab by rememberSaveable { mutableIntStateOf(0) }

    // Currently focused date in milliseconds
    var focusedDateMillis by rememberSaveable { mutableLongStateOf(todayMidnight) }

    // Calendar month offset (0 = current month, -1 = last month, etc.)
    var calendarYear by rememberSaveable { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var calendarMonth by rememberSaveable { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }

    // Form editing state
    var editingAccomplishments by rememberSaveable { mutableStateOf("") }
    var editingDifficulties by rememberSaveable { mutableStateOf("") }
    var editingTomorrowPlan by rememberSaveable { mutableStateOf("") }
    var editingJournalText by rememberSaveable { mutableStateOf("") }
    var editingMood by rememberSaveable { mutableStateOf(Mood.GOOD) }

    // Search and filter state for List view
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedMoodFilter by rememberSaveable { mutableStateOf<Mood?>(null) }

    // Delete confirmation state
    var entryToDelete by remember { mutableStateOf<JournalEntity?>(null) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // Helper to load an entry into the editor form
    val loadEntryForDate = { dateMillis: Long ->
        focusedDateMillis = normalizeToMidnight(dateMillis)
        val entry = journals.find { it.dateMillis == focusedDateMillis }
        if (entry != null) {
            editingAccomplishments = entry.accomplishments
            editingDifficulties = entry.difficulties
            editingTomorrowPlan = entry.tomorrowPlan
            editingJournalText = entry.freeText
            editingMood = entry.mood
        } else {
            editingAccomplishments = ""
            editingDifficulties = ""
            editingTomorrowPlan = ""
            editingJournalText = ""
            editingMood = Mood.GOOD
        }
    }

    // When focusedDateMillis changes and we switch to editor, keep it synced
    LaunchedEffect(focusedDateMillis, journals) {
        val entry = journals.find { it.dateMillis == focusedDateMillis }
        if (entry != null) {
            editingAccomplishments = entry.accomplishments
            editingDifficulties = entry.difficulties
            editingTomorrowPlan = entry.tomorrowPlan
            editingJournalText = entry.freeText
            editingMood = entry.mood
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Sub Navigation Segment
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Calendar Tab
                FilterChip(
                    selected = currentSubTab == 0,
                    onClick = { currentSubTab = 0 },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text("Calendar", maxLines = 1, softWrap = false) },
                    modifier = Modifier.weight(1f)
                )

                // All Entries Tab
                FilterChip(
                    selected = currentSubTab == 1,
                    onClick = { currentSubTab = 1 },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.FormatListBulleted,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Entries (${journals.size})",
                            maxLines = 1,
                            softWrap = false
                        )
                    },
                    modifier = Modifier.weight(1.2f)
                )

                // Write/Edit Tab
                FilterChip(
                    selected = currentSubTab == 2,
                    onClick = { currentSubTab = 2 },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text("Write", maxLines = 1, softWrap = false) },
                    modifier = Modifier.weight(0.9f)
                )
            }
        }

        when (currentSubTab) {
            0 -> {
                // CALENDAR VIEW
                JournalCalendarSubView(
                    journals = journals,
                    selectedDateMillis = focusedDateMillis,
                    calendarYear = calendarYear,
                    calendarMonth = calendarMonth,
                    onMonthChange = { newYear, newMonth ->
                        calendarYear = newYear
                        calendarMonth = newMonth
                    },
                    onSelectDate = { dateMillis ->
                        loadEntryForDate(dateMillis)
                    },
                    onWriteOrEdit = { dateMillis ->
                        loadEntryForDate(dateMillis)
                        currentSubTab = 2
                    },
                    onDelete = { entry ->
                        entryToDelete = entry
                    }
                )
            }
            1 -> {
                // ALL ENTRIES LIST VIEW
                JournalEntriesListSubView(
                    journals = journals,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedMoodFilter = selectedMoodFilter,
                    onMoodFilterChange = { selectedMoodFilter = it },
                    onEditEntry = { entry ->
                        loadEntryForDate(entry.dateMillis)
                        currentSubTab = 2
                    },
                    onDeleteEntry = { entry ->
                        entryToDelete = entry
                    },
                    onWriteNewEntry = {
                        loadEntryForDate(todayMidnight)
                        currentSubTab = 2
                    }
                )
            }
            2 -> {
                // WRITE / EDIT JOURNAL SUB-VIEW
                JournalEditorSubView(
                    dateMillis = focusedDateMillis,
                    accomplishments = editingAccomplishments,
                    onAccomplishmentsChange = { editingAccomplishments = it },
                    difficulties = editingDifficulties,
                    onDifficultiesChange = { editingDifficulties = it },
                    tomorrowPlan = editingTomorrowPlan,
                    onTomorrowPlanChange = { editingTomorrowPlan = it },
                    journalText = editingJournalText,
                    onJournalTextChange = { editingJournalText = it },
                    selectedMood = editingMood,
                    onMoodChange = { editingMood = it },
                    isExistingEntry = journals.any { it.dateMillis == focusedDateMillis },
                    onChangeDateClick = { showDatePickerDialog = true },
                    onSelectToday = { loadEntryForDate(todayMidnight) },
                    onSelectYesterday = {
                        val yesterdayCal = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, -1)
                        }
                        loadEntryForDate(yesterdayCal.timeInMillis)
                    },
                    onSave = {
                        viewModel.saveJournal(
                            JournalEntity(
                                dateMillis = focusedDateMillis,
                                freeText = editingJournalText.trim(),
                                mood = editingMood,
                                accomplishments = editingAccomplishments.trim(),
                                difficulties = editingDifficulties.trim(),
                                tomorrowPlan = editingTomorrowPlan.trim()
                            )
                        )
                        Toast.makeText(
                            context,
                            "Journal entry saved for ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(focusedDateMillis))}!",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Automatically switch to Calendar so the user immediately sees the saved reflection
                        currentSubTab = 0
                    }
                )
            }
        }
    }

    // Delete Confirmation Dialog
    entryToDelete?.let { entry ->
        val dateStr = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date(entry.dateMillis))
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed) },
            title = { Text("Delete Journal Entry?") },
            text = { Text("Are you sure you want to delete your journal reflection for $dateStr? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteJournal(entry)
                        entryToDelete = null
                        if (focusedDateMillis == entry.dateMillis) {
                            editingAccomplishments = ""
                            editingDifficulties = ""
                            editingTomorrowPlan = ""
                            editingJournalText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // DatePicker Dialog for backfilling/selecting dates
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = focusedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selected ->
                            loadEntryForDate(selected)
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("Select Date")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * 1. Interactive Calendar View with visual mood indicators and day preview card
 */
@Composable
fun JournalCalendarSubView(
    journals: List<JournalEntity>,
    selectedDateMillis: Long,
    calendarYear: Int,
    calendarMonth: Int,
    onMonthChange: (year: Int, month: Int) -> Unit,
    onSelectDate: (dateMillis: Long) -> Unit,
    onWriteOrEdit: (dateMillis: Long) -> Unit,
    onDelete: (entry: JournalEntity) -> Unit
) {
    val monthCal = remember(calendarYear, calendarMonth) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, calendarYear)
            set(Calendar.MONTH, calendarMonth)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    val monthName = remember(monthCal) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(monthCal.time)
    }

    // Days in current month
    val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    // Day of week for 1st of month: 1=Sun, 2=Mon, ..., 7=Sat
    // Convert to Monday=0, Sunday=6
    val firstDayOfWeek = (monthCal.get(Calendar.DAY_OF_WEEK) + 5) % 7

    val todayMidnight = remember { normalizeToMidnight(System.currentTimeMillis()) }

    // Map of normalized dayMillis to JournalEntity
    val journalMap = remember(journals) {
        journals.associateBy { it.dateMillis }
    }

    val selectedEntry = journalMap[selectedDateMillis]
    val selectedDateFormatted = remember(selectedDateMillis) {
        SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date(selectedDateMillis))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Month Header with Prev/Next
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val c = (monthCal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                            onMonthChange(c.get(Calendar.YEAR), c.get(Calendar.MONTH))
                        }
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                    }

                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = {
                            val c = (monthCal.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                            onMonthChange(c.get(Calendar.YEAR), c.get(Calendar.MONTH))
                        }
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                    }
                }
            }
        }

        // Days of week header (Mon - Sun)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Calendar Grid
        item {
            val totalCells = firstDayOfWeek + daysInMonth
            val totalRows = (totalCells + 6) / 7

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (row in 0 until totalRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - firstDayOfWeek + 1

                            if (dayNumber in 1..daysInMonth) {
                                val cellCal = (monthCal.clone() as Calendar).apply {
                                    set(Calendar.DAY_OF_MONTH, dayNumber)
                                }
                                val cellMillis = cellCal.timeInMillis
                                val isToday = cellMillis == todayMidnight
                                val isSelected = cellMillis == selectedDateMillis
                                val entry = journalMap[cellMillis]

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        entry != null -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        isToday -> MaterialTheme.colorScheme.surfaceVariant
                                        else -> Color.Transparent
                                    },
                                    border = when {
                                        isSelected -> null
                                        isToday -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                        entry != null -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                                        else -> null
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onSelectDate(cellMillis) }
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = "$dayNumber",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected || isToday || entry != null) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = when {
                                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                                entry != null -> MaterialTheme.colorScheme.primary
                                                isToday -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )

                                        if (entry != null) {
                                            Text(
                                                text = entry.mood.emoji,
                                                fontSize = 11.sp,
                                                lineHeight = 12.sp
                                            )
                                        } else if (isToday) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(
                                                        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                        CircleShape
                                                    )
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Empty spacer cell
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Selected Date Details Card
        item {
            Spacer(modifier = Modifier.height(4.dp))
            SectionHeader(title = if (selectedDateMillis == todayMidnight) "Today's Reflection" else "Selected Day: $selectedDateFormatted")
        }

        item {
            if (selectedEntry != null) {
                // Show Entry Preview Card
                StudyCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header: Date & Mood
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = selectedDateFormatted,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Logged on this day",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SoftMintPillBg,
                                border = BorderStroke(0.75.dp, SuccessGreen.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${selectedEntry.mood.emoji} ${selectedEntry.mood.label}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = SoftMintPillText
                                    )
                                }
                            }
                        }

                        // Accomplishments
                        if (selectedEntry.accomplishments.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SuccessGreen.copy(alpha = 0.08f),
                                border = BorderStroke(0.75.dp, SuccessGreen.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Accomplishments", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = SuccessGreen)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(selectedEntry.accomplishments, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        // Difficulties
                        if (selectedEntry.difficulties.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = WarmOrangeBadge.copy(alpha = 0.08f),
                                border = BorderStroke(0.75.dp, WarmOrangeBadge.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = WarmOrangeBadge, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Doubts & Challenges", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = WarmOrangeBadge)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(selectedEntry.difficulties, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        // Tomorrow's Plan
                        if (selectedEntry.tomorrowPlan.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Today, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Next Day's Plan", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(selectedEntry.tomorrowPlan, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        // Free Notes
                        if (selectedEntry.freeText.isNotBlank()) {
                            Column {
                                Text("Reflections & Notes:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(selectedEntry.freeText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onWriteOrEdit(selectedDateMillis) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit Entry")
                            }

                            OutlinedButton(
                                onClick = { onDelete(selectedEntry) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                                border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            } else {
                // Empty state for selected date
                StudyCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Text(
                            text = "No Journal Entry for this Date",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Log your achievements, learning struggles, and tomorrow's focus for $selectedDateFormatted.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = { onWriteOrEdit(selectedDateMillis) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Write Entry for this Date")
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

/**
 * 2. Searchable Chronological List of All Journal Entries
 */
@Composable
fun JournalEntriesListSubView(
    journals: List<JournalEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedMoodFilter: Mood?,
    onMoodFilterChange: (Mood?) -> Unit,
    onEditEntry: (JournalEntity) -> Unit,
    onDeleteEntry: (JournalEntity) -> Unit,
    onWriteNewEntry: () -> Unit
) {
    val sortedJournals = remember(journals) {
        journals.sortedByDescending { it.dateMillis }
    }

    val filteredJournals = remember(sortedJournals, searchQuery, selectedMoodFilter) {
        sortedJournals.filter { entry ->
            val matchesMood = selectedMoodFilter == null || entry.mood == selectedMoodFilter
            val matchesQuery = searchQuery.isBlank() ||
                entry.accomplishments.contains(searchQuery, ignoreCase = true) ||
                entry.difficulties.contains(searchQuery, ignoreCase = true) ||
                entry.tomorrowPlan.contains(searchQuery, ignoreCase = true) ||
                entry.freeText.contains(searchQuery, ignoreCase = true)
            matchesMood && matchesQuery
        }
    }

    val todayMidnight = remember { normalizeToMidnight(System.currentTimeMillis()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search accomplishments, challenges, notes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Mood Filter Chips Row (Scrollable, never wraps)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedMoodFilter == null,
                    onClick = { onMoodFilterChange(null) },
                    label = { Text("All (${sortedJournals.size})", maxLines = 1, softWrap = false) }
                )
                Mood.values().forEach { m ->
                    val count = sortedJournals.count { it.mood == m }
                    FilterChip(
                        selected = selectedMoodFilter == m,
                        onClick = { onMoodFilterChange(if (selectedMoodFilter == m) null else m) },
                        label = { Text("${m.emoji} ${m.label} ($count)", maxLines = 1, softWrap = false) }
                    )
                }
            }
        }

        if (filteredJournals.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.Event,
                    title = if (journals.isEmpty()) "No Journal Entries Yet" else "No Matching Entries",
                    message = if (journals.isEmpty()) {
                        "Reflect on your daily study sessions to build self-discipline and track learning progress over time."
                    } else {
                        "No journal reflections matched '$searchQuery'."
                    },
                    actionLabel = "Write Today's Journal",
                    onAction = onWriteNewEntry
                )
            }
        } else {
            items(filteredJournals, key = { it.id }) { entry ->
                val dateFormatted = remember(entry.dateMillis) {
                    SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date(entry.dateMillis))
                }
                val isToday = entry.dateMillis == todayMidnight

                StudyCard(
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Card Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = dateFormatted,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isToday) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "TODAY",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "${entry.mood.emoji} ${entry.mood.label}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Accomplishments
                        if (entry.accomplishments.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SuccessGreen.copy(alpha = 0.08f),
                                border = BorderStroke(0.75.dp, SuccessGreen.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("🎯 Accomplishments:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = SuccessGreen)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(entry.accomplishments, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        // Difficulties
                        if (entry.difficulties.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = WarmOrangeBadge.copy(alpha = 0.08f),
                                border = BorderStroke(0.75.dp, WarmOrangeBadge.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("⚠️ Doubts / Challenges:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = WarmOrangeBadge)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(entry.difficulties, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        // Tomorrow's Plan
                        if (entry.tomorrowPlan.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("🚀 Tomorrow's Plan:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(entry.tomorrowPlan, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        // Free text
                        if (entry.freeText.isNotBlank()) {
                            Text(
                                text = entry.freeText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Action Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { onEditEntry(entry) }) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = { onDeleteEntry(entry) },
                                colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

/**
 * 3. Daily Journal Writer / Editor Form
 */
@Composable
fun JournalEditorSubView(
    dateMillis: Long,
    accomplishments: String,
    onAccomplishmentsChange: (String) -> Unit,
    difficulties: String,
    onDifficultiesChange: (String) -> Unit,
    tomorrowPlan: String,
    onTomorrowPlanChange: (String) -> Unit,
    journalText: String,
    onJournalTextChange: (String) -> Unit,
    selectedMood: Mood,
    onMoodChange: (Mood) -> Unit,
    isExistingEntry: Boolean,
    onChangeDateClick: () -> Unit,
    onSelectToday: () -> Unit,
    onSelectYesterday: () -> Unit,
    onSave: () -> Unit
) {
    val dateFormatted = remember(dateMillis) {
        SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date(dateMillis))
    }
    val todayMidnight = remember { normalizeToMidnight(System.currentTimeMillis()) }
    val isToday = dateMillis == todayMidnight

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Date Switcher Header Card
            StudyCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isToday) "Today's Study Journal" else "Journal Reflection",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dateFormatted,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    FilledTonalButton(
                        onClick = onChangeDateClick,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Change", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        // Quick Date Shortcuts
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = isToday,
                    onClick = onSelectToday,
                    label = { Text("Today", maxLines = 1, softWrap = false) }
                )
                FilterChip(
                    selected = false,
                    onClick = onSelectYesterday,
                    label = { Text("Yesterday", maxLines = 1, softWrap = false) }
                )
                FilterChip(
                    selected = false,
                    onClick = onChangeDateClick,
                    label = { Text("Pick Another Date...", maxLines = 1, softWrap = false) }
                )
            }
        }

        // Energy / Mood Selector
        item {
            Text("How was your study energy and focus?", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Mood.values().forEach { m ->
                    FilterChip(
                        selected = selectedMood == m,
                        onClick = { onMoodChange(m) },
                        label = { Text("${m.emoji} ${m.label}", maxLines = 1, softWrap = false) }
                    )
                }
            }
        }

        // Accomplishments Field
        item {
            OutlinedTextField(
                value = accomplishments,
                onValueChange = onAccomplishmentsChange,
                label = { Text("🎯 What did you accomplish?") },
                placeholder = { Text("e.g. Completed Chapter 3 notes, solved 25 integration MCQs, revised formulas") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )
        }

        // Difficulties Field
        item {
            OutlinedTextField(
                value = difficulties,
                onValueChange = onDifficultiesChange,
                label = { Text("⚠️ Doubts, mistakes, or challenges faced:") },
                placeholder = { Text("e.g. Got stuck on thermodynamic Carnot engine efficiency derivations") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )
        }

        // Tomorrow's Plan Field
        item {
            OutlinedTextField(
                value = tomorrowPlan,
                onValueChange = onTomorrowPlanChange,
                label = { Text("🚀 Priority goals for tomorrow:") },
                placeholder = { Text("e.g. Timed mock test + Review mistake bookmarks") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )
        }

        // Freeform Notes Field
        item {
            OutlinedTextField(
                value = journalText,
                onValueChange = onJournalTextChange,
                label = { Text("💭 Personal reflections, mindset & ideas:") },
                placeholder = { Text("How do you feel about your consistency? Any adjustments needed?") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )
        }

        // Save Button
        item {
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isExistingEntry) "Update Journal Entry" else "Save Daily Reflection",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}
