package com.studyforge.app.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.studyforge.app.ui.components.SquircleIconBadge
import com.studyforge.app.ui.components.StudyCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowToUseScreen(
    onBack: () -> Unit,
    onNavigateToJsonImport: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("How to Use StudyForge", fontWeight = FontWeight.Bold) },
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
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("App Guide & Workflows") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("JSON Import Examples") }
                )
            }

            if (selectedTab == 0) {
                // APP WORKFLOW GUIDE
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GuideHeaderCard()

                    GuideStepItem(
                        icon = Icons.Default.Folder,
                        stepNumber = "1",
                        title = "Batches, Subjects & Chapters",
                        description = "Organize learning hierarchically. Create a Batch (e.g. 'Class 12 Prep' or 'Python Mastery'), add Subjects (e.g. 'Mathematics'), and divide into Chapters (e.g. 'Limits & Continuity'). The Chapter is your central learning unit containing all notes, formulas, questions, tests, and analytics."
                    )

                    GuideStepItem(
                        icon = Icons.Default.MenuBook,
                        stepNumber = "2",
                        title = "Distraction-Free Notes & LaTeX Math",
                        description = "Opening any note displays a clean full-screen preview with rendered LaTeX math (e.g. integrals \\int, fractions \\frac{a}{b}) and Markdown formatting. Tap 'Edit Note' whenever you want to edit text or insert calculus symbols via the Math Toolbar."
                    )

                    GuideStepItem(
                        icon = Icons.Default.Functions,
                        stepNumber = "3",
                        title = "Formula Vault",
                        description = "Save high-yield mathematical formulas, physics laws, and chemical equations. Tag by category and pin favorites for instant revision before exams."
                    )

                    GuideStepItem(
                        icon = Icons.Default.Quiz,
                        stepNumber = "4",
                        title = "Question Bank & Multi-Format Questions",
                        description = "Create or import unlimited questions: Single Choice (MCQ), Multiple Correct, True / False, Fill in the Blank, and Numerical. Every question can include topic tags, hints, and detailed step-by-step solutions."
                    )

                    GuideStepItem(
                        icon = Icons.Default.FitnessCenter,
                        stepNumber = "5",
                        title = "Tests, Smart Diagnostic Tests & Scoring",
                        description = "Run interactive timed tests with live countdown timers, question review markers, and automatic score calculations with negative marking. Use 'Smart Test' to automatically target your weakest concepts and past mistakes."
                    )

                    GuideStepItem(
                        icon = Icons.Default.School,
                        stepNumber = "6",
                        title = "Mistake Notebook & Spaced Repetition",
                        description = "Questions you get wrong are automatically saved to your Mistake Notebook. Write student reflection notes on why you slipped, retry problems directly, and review SM-2 flashcard decks scheduled by spaced repetition."
                    )

                    GuideStepItem(
                        icon = Icons.Default.Alarm,
                        stepNumber = "7",
                        title = "Timetable Alarms & Study Journal",
                        description = "Set recurring weekly study slots and toggle study alarms that ring on schedule. Log your daily accomplishments, challenges, and mood in the Daily Study Journal to build consistent study streaks."
                    )

                    GuideStepItem(
                        icon = Icons.Default.AutoAwesome,
                        stepNumber = "8",
                        title = "AI Study Assistant (Gemini)",
                        description = "Get instant explanations for complex concepts, generate custom practice questions, or analyze why an answer was wrong using built-in AI assistance."
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            } else {
                // JSON FORMATS & LIVE EXAMPLES
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StudyCard(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SquircleIconBadge(
                                    icon = Icons.Default.UploadFile,
                                    tint = MaterialTheme.colorScheme.primary,
                                    size = 44.dp,
                                    iconSize = 24.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("JSON Import Guide", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("Pre-formatted templates ready to copy & test", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "StudyForge accepts full test papers or single question banks in JSON format. Click 'Copy JSON' on any format below, then open the JSON Importer to paste or save as a .json file.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onNavigateToJsonImport,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open JSON Importer")
                            }
                        }
                    }

                    // Format 1: Single Choice MCQ
                    JsonExampleCard(
                        title = "1. Single Choice (MCQ)",
                        description = "Standard 4-option multiple choice question with single correct answer index (0-based: 0 for A, 1 for B, 2 for C, 3 for D).",
                        jsonSnippet = """{
  "type": "mcq",
  "question": "What is the derivative of sin(x) with respect to x?",
  "options": [
    "cos(x)",
    "-cos(x)",
    "tan(x)",
    "-sin(x)"
  ],
  "correctAnswer": 0,
  "marks": 2.0,
  "negativeMarks": 0.5,
  "explanation": "Standard derivative formula: d/dx(sin x) = cos x.",
  "topic": "Calculus",
  "difficulty": "easy"
}""",
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(it))
                            Toast.makeText(context, "Copied MCQ JSON to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // Format 2: Multiple Correct Answers
                    JsonExampleCard(
                        title = "2. Multiple Correct Answers",
                        description = "Questions where one or more options are correct. 'correctAnswers' contains an array of zero-based option indices.",
                        jsonSnippet = """{
  "type": "multiple_correct",
  "question": "Which of the following functions are continuous at x = 0?",
  "options": [
    "f(x) = x^2",
    "f(x) = cos(x)",
    "f(x) = 1/x",
    "f(x) = e^x"
  ],
  "correctAnswers": [0, 1, 3],
  "marks": 4.0,
  "negativeMarks": 1.0,
  "explanation": "1/x is discontinuous with a vertical asymptote at x = 0.",
  "topic": "Continuity",
  "difficulty": "medium"
}""",
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(it))
                            Toast.makeText(context, "Copied Multiple Correct JSON to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // Format 3: True / False
                    JsonExampleCard(
                        title = "3. True / False",
                        description = "Binary true or false questions. 'correctAnswer' can be true, false, or 0/1.",
                        jsonSnippet = """{
  "type": "true_false",
  "question": "Every differentiable function is necessarily continuous.",
  "options": ["True", "False"],
  "correctAnswer": "true",
  "marks": 1.0,
  "negativeMarks": 0.25,
  "explanation": "Differentiability implies continuity, but continuity does not imply differentiability.",
  "topic": "Calculus Theorems",
  "difficulty": "easy"
}""",
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(it))
                            Toast.makeText(context, "Copied True/False JSON to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // Format 4: Fill in the Blank
                    JsonExampleCard(
                        title = "4. Fill in the Blank",
                        description = "Question contains a blank (e.g. _______) and 'correctAnswer' specifies the expected keyword.",
                        jsonSnippet = """{
  "type": "fill_blank",
  "question": "The powerhouse of the cellular structure is called the _______.",
  "correctAnswer": "mitochondria",
  "marks": 2.0,
  "negativeMarks": 0.0,
  "explanation": "Mitochondria generate most of the chemical energy needed to power the cell.",
  "topic": "Cell Biology",
  "difficulty": "easy"
}""",
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(it))
                            Toast.makeText(context, "Copied Fill in Blank JSON to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // Format 5: Numerical Value
                    JsonExampleCard(
                        title = "5. Numerical Answer",
                        description = "Questions requiring exact numeric input. 'correctAnswer' is the numeric value.",
                        jsonSnippet = """{
  "type": "numerical",
  "question": "Evaluate the limit of sin(8x)/x as x approaches 0:",
  "correctAnswer": "8",
  "marks": 3.0,
  "negativeMarks": 0.0,
  "explanation": "Using standard limit: lim_{x->0} sin(kx)/x = k, here k = 8.",
  "topic": "Limits",
  "difficulty": "easy"
}""",
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(it))
                            Toast.makeText(context, "Copied Numerical JSON to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // Format 6: Short Answer / Definition
                    JsonExampleCard(
                        title = "6. Short Answer",
                        description = "Short answer or definition question with expected key phrase.",
                        jsonSnippet = """{
  "type": "short_answer",
  "question": "State the principle that energy can neither be created nor destroyed.",
  "correctAnswer": "Law of Conservation of Energy",
  "marks": 2.0,
  "negativeMarks": 0.0,
  "explanation": "First law of thermodynamics / Conservation of energy.",
  "topic": "Thermodynamics",
  "difficulty": "easy"
}""",
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(it))
                            Toast.makeText(context, "Copied Short Answer JSON to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // Format 7: Complete Test Paper
                    JsonExampleCard(
                        title = "7. Full Test Paper (Multiple Mixed Questions)",
                        description = "A complete test package with title, duration in minutes, total marks, and questions array containing any question types.",
                        jsonSnippet = """{
  "title": "Comprehensive Physics Drill 01",
  "subject": "Physics",
  "chapter": "Kinematics",
  "durationMinutes": 45,
  "totalMarks": 15,
  "questions": [
    {
      "type": "mcq",
      "question": "A particle moves with constant acceleration a. Its velocity after time t is:",
      "options": ["v = u + at", "v = u + 1/2 at^2", "v^2 = u^2 + at", "v = u - at"],
      "correctAnswer": 0,
      "marks": 3.0,
      "negativeMarks": 1.0,
      "explanation": "First equation of motion: v = u + at."
    },
    {
      "type": "numerical",
      "question": "What is acceleration due to gravity on earth surface in m/s^2 (round to 1 decimal)?",
      "correctAnswer": "9.8",
      "marks": 3.0,
      "negativeMarks": 0.0,
      "explanation": "g = 9.8 m/s^2."
    },
    {
      "type": "true_false",
      "question": "Distance can be negative in one dimensional motion.",
      "options": ["True", "False"],
      "correctAnswer": "false",
      "marks": 2.0,
      "negativeMarks": 0.5,
      "explanation": "Distance is scalar and always non-negative. Displacement can be negative."
    }
  ]
}""",
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(it))
                            Toast.makeText(context, "Copied Full Test JSON to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun GuideHeaderCard() {
    StudyCard(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            SquircleIconBadge(
                icon = Icons.Default.MenuBook,
                tint = MaterialTheme.colorScheme.primary,
                size = 48.dp,
                iconSize = 24.dp
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text("StudyForge Learning Architecture", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "Batch → Subject → Chapter → Notes / Formulas / Questions / Tests / Revisions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun GuideStepItem(
    icon: ImageVector,
    stepNumber: String,
    title: String,
    description: String
) {
    StudyCard(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(stepNumber, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SquircleIconBadge(
                        icon = icon,
                        tint = MaterialTheme.colorScheme.primary,
                        size = 32.dp,
                        iconSize = 16.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun JsonExampleCard(
    title: String,
    description: String,
    jsonSnippet: String,
    onCopy: (String) -> Unit
) {
    StudyCard(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                OutlinedButton(
                    onClick = { onCopy(jsonSnippet) },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy JSON", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    Text(
                        text = jsonSnippet,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
