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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyforge.app.domain.json.TestJsonParser
import com.studyforge.app.ui.components.CodeBlockCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JsonSchemaHelpScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("JSON Test Schema", fontWeight = FontWeight.Bold) },
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
            Text(
                text = "StudyForge JSON Test Specification",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "StudyForge supports importing tests and question banks directly from JSON strings or files. Both single correct (MCQ), multiple choice (MSQ), numerical, and text questions are supported.",
                style = MaterialTheme.typography.bodyMedium
            )

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Top-Level Fields:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• title (String, required): Name of the test")
                    Text("• subject (String, optional): Subject name")
                    Text("• chapter (String, optional): Chapter name")
                    Text("• durationMinutes (Int, optional, default 30): Duration")
                    Text("• totalMarks (Double, optional): Calculated from questions if omitted")
                    Text("• questions (Array, required): List of question objects")
                }
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Question Object Fields:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• id (String): Unique question identifier")
                    Text("• type (String): 'mcq', 'multiple_correct', 'true_false', 'numerical'")
                    Text("• question (String): Question text (Markdown & LaTeX supported)")
                    Text("• options (Array of Strings): Required for MCQ / MSQ")
                    Text("• correctAnswer: 0 (index for MCQ), [0, 2] (indices for MSQ), or '8' (string/number)")
                    Text("• marks (Double): Marks awarded on correct answer")
                    Text("• negativeMarks (Double): Penalty deducted on wrong answer")
                    Text("• explanation (String): Shown AFTER test submission")
                    Text("• difficulty (String): 'easy', 'medium', 'hard'")
                    Text("• topic (String): Subtopic name")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Example JSON Snippet:", fontWeight = FontWeight.Bold)
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

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
