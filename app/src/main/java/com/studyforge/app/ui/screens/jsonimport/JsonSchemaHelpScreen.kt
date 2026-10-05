package com.studyforge.app.ui.screens.jsonimport

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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studyforge.app.domain.json.CurriculumJsonParser
import com.studyforge.app.domain.json.TestJsonParser
import com.studyforge.app.ui.components.CodeBlockCard
import com.studyforge.app.ui.components.StudyCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JsonSchemaHelpScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Batch & Subjects, 1 = Tests & Questions

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("JSON Import Schemas", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Tab chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("Batch & Subjects Schema") },
                    leadingIcon = { Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                FilterChip(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("Tests & Questions Schema") },
                    leadingIcon = { Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            if (selectedTab == 0) {
                // Batch & Subjects Curriculum Schema
                Text(
                    text = "Batch & Subjects Curriculum Specification",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "You can import an entire Batch with its Subjects and Chapters (and even embedded Notes, Formulas, or Questions) in one clean JSON file.",
                    style = MaterialTheme.typography.bodyMedium
                )

                StudyCard(
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Top-Level Fields:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• batch (Object, optional): Details of the batch", style = MaterialTheme.typography.bodyMedium)
                        Text("   - name (String, required): e.g. 'JEE Advanced 2026', 'GATE CS'", style = MaterialTheme.typography.bodyMedium)
                        Text("   - courseOrClass (String, optional): e.g. 'Class 12', 'College'", style = MaterialTheme.typography.bodyMedium)
                        Text("   - session (String, optional): e.g. '2025-2026'", style = MaterialTheme.typography.bodyMedium)
                        Text("   - goal (String, optional): Target goal / rank", style = MaterialTheme.typography.bodyMedium)
                        Text("   - description (String, optional): Overview note", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("• subjects (Array of Objects, required): List of subjects", style = MaterialTheme.typography.bodyMedium)
                        Text("   - name (String, required): Subject name", style = MaterialTheme.typography.bodyMedium)
                        Text("   - iconName (String, optional): 'science', 'atom', 'dna', 'calculate', 'code', 'book', 'language'", style = MaterialTheme.typography.bodyMedium)
                        Text("   - colorHex (String, optional): Hex color code (e.g. '#3B82F6')", style = MaterialTheme.typography.bodyMedium)
                        Text("   - chapters (Array, required): List of chapters. Can be a simple string list [\"Ch 1\", \"Ch 2\"] OR objects with embedded notes and formulas.", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Copyable Batch & Subjects Template:", fontWeight = FontWeight.Bold)
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(CurriculumJsonParser.EXAMPLE_BATCH_SUBJECTS_JSON))
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Template")
                    }
                }

                CodeBlockCard(code = CurriculumJsonParser.EXAMPLE_BATCH_SUBJECTS_JSON)

            } else {
                // Test & Questions Schema
                Text(
                    text = "StudyForge JSON Test Specification",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "StudyForge supports importing tests and question banks directly from JSON strings or files. Both single correct (MCQ), multiple choice (MSQ), numerical, and text questions are supported.",
                    style = MaterialTheme.typography.bodyMedium
                )

                StudyCard(
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Top-Level Fields:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• title (String, required): Name of the test", style = MaterialTheme.typography.bodyMedium)
                        Text("• subject (String, optional): Subject name", style = MaterialTheme.typography.bodyMedium)
                        Text("• chapter (String, optional): Chapter name", style = MaterialTheme.typography.bodyMedium)
                        Text("• durationMinutes (Int, optional, default 30): Duration", style = MaterialTheme.typography.bodyMedium)
                        Text("• totalMarks (Double, optional): Calculated from questions if omitted", style = MaterialTheme.typography.bodyMedium)
                        Text("• questions (Array, required): List of question objects", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                StudyCard(
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Question Object Fields:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• id (String): Unique question identifier", style = MaterialTheme.typography.bodyMedium)
                        Text("• type (String): 'mcq', 'multiple_correct', 'true_false', 'numerical'", style = MaterialTheme.typography.bodyMedium)
                        Text("• question (String): Question text (Markdown & LaTeX supported)", style = MaterialTheme.typography.bodyMedium)
                        Text("• options (Array of Strings): Required for MCQ / MSQ", style = MaterialTheme.typography.bodyMedium)
                        Text("• correctAnswer: 0 (index for MCQ), [0, 2] (indices for MSQ), or '8' (string/number)", style = MaterialTheme.typography.bodyMedium)
                        Text("• marks (Double): Marks awarded on correct answer", style = MaterialTheme.typography.bodyMedium)
                        Text("• negativeMarks (Double): Penalty deducted on wrong answer", style = MaterialTheme.typography.bodyMedium)
                        Text("• explanation (String): Shown AFTER test submission", style = MaterialTheme.typography.bodyMedium)
                        Text("• difficulty (String): 'easy', 'medium', 'hard'", style = MaterialTheme.typography.bodyMedium)
                        Text("• topic (String): Subtopic name", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Example Test JSON Snippet:", fontWeight = FontWeight.Bold)
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(TestJsonParser.EXAMPLE_TEST_JSON))
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Example")
                    }
                }

                CodeBlockCard(code = TestJsonParser.EXAMPLE_TEST_JSON)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
