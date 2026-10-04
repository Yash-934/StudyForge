package com.studyforge.app.ui.screens.search

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studyforge.app.ui.components.EmptyState
import com.studyforge.app.viewmodel.StudyViewModel

data class SearchResultItem(
    val title: String,
    val subtitle: String,
    val type: String,
    val icon: ImageVector,
    val destinationChapterId: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
    viewModel: StudyViewModel,
    onBack: () -> Unit,
    onNavigateToChapter: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val batches by viewModel.batches.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val formulas by viewModel.formulas.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val tests by viewModel.tests.collectAsState()

    var query by remember { mutableStateOf("") }

    val results = remember(query, batches, subjects, chapters, notes, formulas, questions, tests) {
        if (query.isBlank() || query.length < 2) emptyList()
        else {
            val list = mutableListOf<SearchResultItem>()
            val q = query.trim()

            chapters.filter { it.name.contains(q, ignoreCase = true) }.forEach {
                list.add(SearchResultItem(it.name, "Chapter", "Chapter", Icons.Default.Folder, it.id))
            }

            notes.filter { it.title.contains(q, ignoreCase = true) || it.contentMarkdown.contains(q, ignoreCase = true) }.forEach {
                list.add(SearchResultItem(it.title, it.contentMarkdown.take(60), "Note", Icons.AutoMirrored.Filled.MenuBook, it.chapterId))
            }

            formulas.filter { it.title.contains(q, ignoreCase = true) || it.formulaLatex.contains(q, ignoreCase = true) }.forEach {
                list.add(SearchResultItem(it.title, it.formulaLatex, "Formula", Icons.Default.Calculate, it.chapterId))
            }

            questions.filter { it.questionText.contains(q, ignoreCase = true) || it.topic.contains(q, ignoreCase = true) }.forEach {
                list.add(SearchResultItem(it.questionText.take(60), "${it.type.displayName} • ${it.topic}", "Question", Icons.Default.Quiz, it.chapterId))
            }

            list
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Global Knowledge Search", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search anything across notes, math, questions...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (query.isNotBlank() && results.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Search,
                    title = "No Matches Found",
                    message = "No notes, formulas, or questions matched '$query'."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(results) { res ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToChapter(res.destinationChapterId) }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = res.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = res.title,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = res.type,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = res.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(48.dp)) }
                }
            }
        }
    }
}
