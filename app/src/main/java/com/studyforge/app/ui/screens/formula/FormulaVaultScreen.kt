package com.studyforge.app.ui.screens.formula

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyforge.app.data.local.entities.FormulaEntity
import com.studyforge.app.ui.components.BlockMathCard
import com.studyforge.app.ui.components.EmptyState
import com.studyforge.app.ui.components.MathEditorToolbar
import com.studyforge.app.ui.components.StudyCard
import com.studyforge.app.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormulaVaultScreen(
    chapterId: Long,
    subjectId: Long,
    viewModel: StudyViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formulas by viewModel.formulas.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val chapter = chapters.find { it.id == chapterId }

    val filteredList = remember(formulas, chapterId, subjectId) {
        when {
            chapterId > 0 -> formulas.filter { it.chapterId == chapterId }
            subjectId > 0 -> formulas.filter { it.subjectId == subjectId }
            else -> formulas
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var isRevisionMode by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    val displayedFormulas = remember(filteredList, searchQuery) {
        if (searchQuery.isBlank()) filteredList else filteredList.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                it.formulaLatex.contains(searchQuery, ignoreCase = true) ||
                it.explanation.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isRevisionMode) "Formula Revision Drill" else "Formula Vault",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isRevisionMode = !isRevisionMode }) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Revision Drill",
                            tint = if (isRevisionMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Formula")
            }
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
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search formulas, symbols, tags...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isRevisionMode) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🧠 Active Recall Mode: Formula expressions are hidden. Tap each card to reveal and verify your memory.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (displayedFormulas.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Calculate,
                    title = "Formula Vault is Empty",
                    message = "Store essential mathematical formulas, theorems, and identities here.",
                    actionLabel = "Add First Formula",
                    onAction = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedFormulas) { formula ->
                        FormulaVaultCard(
                            formula = formula,
                            isRevisionMode = isRevisionMode,
                            onToggleFavorite = {
                                viewModel.saveFormula(
                                    id = formula.id,
                                    chapterId = formula.chapterId,
                                    subjectId = formula.subjectId,
                                    batchId = formula.batchId,
                                    title = formula.title,
                                    formulaLatex = formula.formulaLatex,
                                    explanation = formula.explanation
                                )
                            },
                            onDelete = { viewModel.deleteFormula(formula) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }

    if (showAddDialog) {
        AddFormulaDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { fTitle, fLatex, fExplanation ->
                viewModel.saveFormula(
                    chapterId = if (chapterId > 0) chapterId else 1L,
                    subjectId = chapter?.subjectId ?: if (subjectId > 0) subjectId else 1L,
                    batchId = chapter?.batchId ?: 1L,
                    title = fTitle,
                    formulaLatex = fLatex,
                    explanation = fExplanation
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
fun FormulaVaultCard(
    formula: FormulaEntity,
    isRevisionMode: Boolean,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    var isRevealed by remember(isRevisionMode) { mutableStateOf(!isRevisionMode) }

    StudyCard(
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formula.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!isRevealed) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isRevealed = true }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tap to Reveal Formula",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                BlockMathCard(rawLatex = formula.formulaLatex)
            }

            if (formula.explanation.isNotBlank() && isRevealed) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formula.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddFormulaDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, latex: String, explanation: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var latex by remember { mutableStateOf("") }
    var explanation by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Formula", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Formula Name *") },
                    placeholder = { Text("e.g. Integration by Parts") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = latex,
                    onValueChange = { latex = it },
                    label = { Text("LaTeX Equation *") },
                    placeholder = { Text("e.g. \\Delta W_B = \\int_a^b f(x) dx") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (latex.isNotBlank()) {
                    Text("Formula Preview:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    BlockMathCard(rawLatex = latex)
                }

                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("Conditions / Explanation") },
                    placeholder = { Text("e.g. Use LIATE rule for choosing u.") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank() && latex.isNotBlank()) onConfirm(title, latex, explanation) },
                enabled = title.isNotBlank() && latex.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
