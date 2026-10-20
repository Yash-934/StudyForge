package com.studyforge.app.ui.screens.notes

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyforge.app.ui.components.MarkdownNode
import com.studyforge.app.ui.components.ReaderStyleConfig
import com.studyforge.app.ui.components.markdownNodeToSpokenText
import com.studyforge.app.ui.theme.JetBrainsMono
import com.studyforge.app.ui.theme.LoraSerifFont
import com.studyforge.app.ui.theme.OpenSansFont
import com.studyforge.app.ui.theme.PlusJakartaSans
import java.util.Locale

private const val PREFS_NAME = "studyforge_reader_prefs"
private const val KEY_THEME = "reader_theme"
private const val KEY_FONT = "reader_font"
private const val KEY_FONT_SCALE = "reader_font_scale"
private const val KEY_TTS_SPEED = "reader_tts_speed"

enum class ReaderTheme(
    val label: String,
    val badge: String,
    val sheetColor: Color,
    val backgroundColor: Color,
    val textColor: Color,
    val secondaryTextColor: Color,
    val accentColor: Color
) {
    PAPER_WHITE(
        label = "Paper White",
        badge = "📄",
        sheetColor = Color(0xFFFFFFFF),
        backgroundColor = Color(0xFFF2F5FA),
        textColor = Color(0xFF111827),
        secondaryTextColor = Color(0xFF4B5563),
        accentColor = Color(0xFF4F46E5)
    ),
    WARM_SEPIA(
        label = "Warm Sepia",
        badge = "📜",
        sheetColor = Color(0xFFFBF4E4),
        backgroundColor = Color(0xFFF1E5CD),
        textColor = Color(0xFF2B2118),
        secondaryTextColor = Color(0xFF5C4938),
        accentColor = Color(0xFFB45309)
    ),
    MINT_CARE(
        label = "Eye-Care Mint",
        badge = "🌿",
        sheetColor = Color(0xFFEEF7F1),
        backgroundColor = Color(0xFFDCECE2),
        textColor = Color(0xFF132A20),
        secondaryTextColor = Color(0xFF3B594B),
        accentColor = Color(0xFF15803D)
    ),
    NIGHT_DARK(
        label = "Night Dark",
        badge = "🌙",
        sheetColor = Color(0xFF151A26),
        backgroundColor = Color(0xFF0B0E17),
        textColor = Color(0xFFE5E7EB),
        secondaryTextColor = Color(0xFF9CA3AF),
        accentColor = Color(0xFF818CF8)
    )
}

enum class ReaderFontChoice(
    val label: String,
    val sample: String,
    val fontFamily: FontFamily
) {
    SANS("Modern Sans", "Aa", PlusJakartaSans),
    SERIF("Textbook Serif", "Aa", LoraSerifFont),
    HUMANIST("Open Sans", "Aa", OpenSansFont),
    MONO("Code Mono", "Aa", JetBrainsMono)
}

enum class AnnotationTool(
    val label: String,
    val openTag: String,
    val closeTag: String,
    val swatchColor: Color
) {
    NONE("Off", "", "", Color.Transparent),
    HIGHLIGHT_YELLOW("Yellow", "==", "==", Color(0xFFFACC15)),
    HIGHLIGHT_GREEN("Green", "<hl:green>", "</hl>", Color(0xFF4ADE80)),
    HIGHLIGHT_PINK("Pink", "<hl:pink>", "</hl>", Color(0xFFF472B6)),
    HIGHLIGHT_BLUE("Blue", "<hl:blue>", "</hl>", Color(0xFF38BDF8)),
    UNDERLINE("Underline", "++", "++", Color(0xFF6366F1)),
    ERASER("Clear", "", "", Color(0xFFEF4444))
}

class ReaderPreferencesState(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var theme by mutableStateOf(
        runCatching {
            ReaderTheme.valueOf(prefs.getString(KEY_THEME, ReaderTheme.PAPER_WHITE.name) ?: ReaderTheme.PAPER_WHITE.name)
        }.getOrDefault(ReaderTheme.PAPER_WHITE)
    )
        private set

    var fontChoice by mutableStateOf(
        runCatching {
            ReaderFontChoice.valueOf(prefs.getString(KEY_FONT, ReaderFontChoice.SANS.name) ?: ReaderFontChoice.SANS.name)
        }.getOrDefault(ReaderFontChoice.SANS)
    )
        private set

    var fontScale by mutableFloatStateOf(
        prefs.getFloat(KEY_FONT_SCALE, 1.0f).coerceIn(0.80f, 1.45f)
    )
        private set

    var ttsSpeed by mutableFloatStateOf(
        prefs.getFloat(KEY_TTS_SPEED, 1.0f).coerceIn(0.75f, 2.0f)
    )
        private set

    fun updateTheme(newTheme: ReaderTheme) {
        theme = newTheme
        prefs.edit().putString(KEY_THEME, newTheme.name).apply()
    }

    fun updateFont(newFont: ReaderFontChoice) {
        fontChoice = newFont
        prefs.edit().putString(KEY_FONT, newFont.name).apply()
    }

    fun updateFontScale(newScale: Float) {
        val clamped = newScale.coerceIn(0.80f, 1.45f)
        fontScale = clamped
        prefs.edit().putFloat(KEY_FONT_SCALE, clamped).apply()
    }

    fun updateTtsSpeed(newSpeed: Float) {
        val clamped = newSpeed.coerceIn(0.75f, 2.0f)
        ttsSpeed = clamped
        prefs.edit().putFloat(KEY_TTS_SPEED, clamped).apply()
    }

    fun toStyleConfig(): ReaderStyleConfig {
        return ReaderStyleConfig(
            fontScale = fontScale,
            fontFamily = fontChoice.fontFamily,
            textColor = theme.textColor,
            secondaryTextColor = theme.secondaryTextColor,
            accentColor = theme.accentColor,
            sheetColor = theme.sheetColor
        )
    }
}

@Composable
fun rememberReaderPreferences(context: Context): ReaderPreferencesState {
    return remember(context) { ReaderPreferencesState(context) }
}

/**
 * Collapsible Reader Appearance Panel (Reading Theme, Font Family, Text Size scale).
 */
@Composable
fun ReaderAppearancePanel(
    visible: Boolean,
    readerPrefs: ReaderPreferencesState,
    onClose: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(18.dp),
            tonalElevation = 4.dp,
            shadowElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reading Theme, Font & Text Size",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Appearance Panel",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 1. Reading Theme Selector
                Text(
                    text = "Reading Theme",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReaderTheme.entries.forEach { item ->
                        val isSelected = readerPrefs.theme == item
                        Surface(
                            onClick = { readerPrefs.updateTheme(item) },
                            shape = RoundedCornerShape(12.dp),
                            color = item.sheetColor,
                            modifier = Modifier.border(
                                width = if (isSelected) 2.2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = item.badge, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                    ),
                                    color = item.textColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Font Family Selector
                Text(
                    text = "Font Style",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReaderFontChoice.entries.forEach { fontItem ->
                        val isSelected = readerPrefs.fontChoice == fontItem
                        FilterChip(
                            selected = isSelected,
                            onClick = { readerPrefs.updateFont(fontItem) },
                            label = {
                                Text(
                                    text = fontItem.label,
                                    fontFamily = fontItem.fontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Text Size Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Text Size: ${(readerPrefs.fontScale * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (readerPrefs.fontScale != 1.0f) {
                        Text(
                            text = "Reset 100%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { readerPrefs.updateFontScale(1.0f) }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = { readerPrefs.updateFontScale(readerPrefs.fontScale - 0.05f) },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "A−",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    Slider(
                        value = readerPrefs.fontScale,
                        onValueChange = { readerPrefs.updateFontScale(it) },
                        valueRange = 0.80f..1.45f,
                        steps = 12,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                    Surface(
                        onClick = { readerPrefs.updateFontScale(readerPrefs.fontScale + 0.05f) },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "A+",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive Annotation Toolbar Dock for Reader Mode (Highlight Colors, Underline, Word Picker vs Line Tap).
 */
@Composable
fun ReaderAnnotationBar(
    visible: Boolean,
    activeTool: AnnotationTool,
    onSelectTool: (AnnotationTool) -> Unit,
    wordSelectionMode: Boolean,
    onToggleWordSelectionMode: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 4.dp,
            shadowElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp)
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (activeTool == AnnotationTool.NONE) {
                            "🖍️ Select a Highlighter or Underline tool, then tap any note line"
                        } else if (wordSelectionMode) {
                            "✨ Tap any paragraph/point to select specific words to ${activeTool.label}"
                        } else {
                            "✨ Tap any paragraph/point to apply ${activeTool.label} (saved automatically)"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Annotation Bar",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tools = listOf(
                        AnnotationTool.HIGHLIGHT_YELLOW,
                        AnnotationTool.HIGHLIGHT_GREEN,
                        AnnotationTool.HIGHLIGHT_PINK,
                        AnnotationTool.HIGHLIGHT_BLUE,
                        AnnotationTool.UNDERLINE,
                        AnnotationTool.ERASER
                    )
                    tools.forEach { tool ->
                        val selected = activeTool == tool
                        Surface(
                            onClick = {
                                onSelectTool(if (selected) AnnotationTool.NONE else tool)
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            },
                            modifier = Modifier.border(
                                width = if (selected) 1.8.dp else 0.8.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (tool == AnnotationTool.UNDERLINE) {
                                    Icon(
                                        imageVector = Icons.Default.FormatUnderlined,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(13.dp)
                                            .clip(CircleShape)
                                            .background(tool.swatchColor)
                                            .border(1.dp, Color.Black.copy(alpha = 0.25f), CircleShape)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tool.label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                                        textDecoration = if (tool == AnnotationTool.UNDERLINE) TextDecoration.Underline else TextDecoration.None
                                    )
                                )
                            }
                        }
                    }

                    // Mode toggle: Full Line vs Select Words
                    FilterChip(
                        selected = wordSelectionMode,
                        onClick = { onToggleWordSelectionMode(!wordSelectionMode) },
                        label = {
                            Text(
                                text = if (wordSelectionMode) "🎯 Select Words" else "📏 Full Line",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Dialog allowing the user to tap specific words/phrases in a paragraph to highlight or underline,
 * or apply to the entire block in 1 tap.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordAnnotationPickerDialog(
    rawBlockText: String,
    activeTool: AnnotationTool,
    onApplyFullLine: () -> Unit,
    onApplyWordRange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val cleanDisplay = remember(rawBlockText) {
        stripAnnotationTags(rawBlockText)
    }
    val words = remember(cleanDisplay) {
        cleanDisplay.split(Regex("""\s+""")).filter { it.isNotBlank() }
    }
    var startWordIdx by remember { mutableIntStateOf(-1) }
    var endWordIdx by remember { mutableIntStateOf(-1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Annotate (${activeTool.label})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Tap the first and last word of the phrase you want to ${activeTool.label.lowercase()}, or apply to the entire line:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    words.forEachIndexed { idx, w ->
                        val inRange = startWordIdx != -1 && endWordIdx != -1 &&
                            idx in minOf(startWordIdx, endWordIdx)..maxOf(startWordIdx, endWordIdx)
                        Surface(
                            onClick = {
                                if (startWordIdx == -1 || (startWordIdx != -1 && endWordIdx != startWordIdx)) {
                                    startWordIdx = idx
                                    endWordIdx = idx
                                } else {
                                    endWordIdx = idx
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (inRange) {
                                activeTool.swatchColor.copy(alpha = 0.45f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            },
                            modifier = Modifier.border(
                                width = if (inRange) 1.5.dp else 0.5.dp,
                                color = if (inRange) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                        ) {
                            Text(
                                text = w,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (inRange) FontWeight.Bold else FontWeight.Normal,
                                    textDecoration = if (inRange && activeTool == AnnotationTool.UNDERLINE) {
                                        TextDecoration.Underline
                                    } else {
                                        TextDecoration.None
                                    }
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onApplyFullLine) {
                    Text("Entire Line", fontWeight = FontWeight.Bold)
                }
                Button(
                    enabled = startWordIdx != -1 && endWordIdx != -1,
                    onClick = {
                        val s = minOf(startWordIdx, endWordIdx)
                        val e = maxOf(startWordIdx, endWordIdx)
                        val phrase = words.subList(s, e + 1).joinToString(" ")
                        onApplyWordRange(phrase)
                    }
                ) {
                    Text("Apply Selection")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Listen Note (Audio Text-to-Speech) Player Bar with Hindi & English Math Speech support,
 * section navigation, speed control, and auto-advance.
 */
@Composable
fun ListenNotePlayerBar(
    visible: Boolean,
    context: Context,
    parsedNodes: List<MarkdownNode>,
    noteTitle: String,
    readerPrefs: ReaderPreferencesState,
    currentSpokenIndex: Int,
    onSpokenIndexChange: (Int) -> Unit,
    onClose: () -> Unit
) {
    val spokenSections = remember(parsedNodes, noteTitle) {
        buildList {
            if (noteTitle.isNotBlank()) {
                add(Pair(-1, noteTitle))
            }
            parsedNodes.forEachIndexed { idx, node ->
                val spoken = markdownNodeToSpokenText(node)
                if (spoken.isNotBlank()) {
                    add(Pair(idx, spoken))
                }
            }
        }
    }

    var isPlaying by remember { mutableStateOf(false) }
    var isTtsReady by remember { mutableStateOf(false) }
    var sectionCursor by remember(spokenSections) { mutableIntStateOf(0) }
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }

    fun speakSectionAt(cursorIdx: Int) {
        val tts = ttsInstance ?: return
        if (!isTtsReady || spokenSections.isEmpty()) return
        val safeCursor = cursorIdx.coerceIn(0, spokenSections.lastIndex)
        sectionCursor = safeCursor
        val (nodeIdx, textToSpeak) = spokenSections[safeCursor]
        onSpokenIndexChange(nodeIdx)

        // Choose Hindi locale if section contains Devanagari characters
        val hasHindi = textToSpeak.any { it in '\u0900'..'\u097F' }
        val targetLocale = if (hasHindi) Locale("hi", "IN") else Locale("en", "IN")
        val langRes = tts.isLanguageAvailable(targetLocale)
        if (langRes >= TextToSpeech.LANG_AVAILABLE) {
            tts.language = targetLocale
        } else if (!hasHindi) {
            tts.language = Locale.US
        }
        tts.setSpeechRate(readerPrefs.ttsSpeed)
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utt_$safeCursor")
        }
        tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params, "utt_$safeCursor")
        isPlaying = true
    }

    DisposableEffect(context) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        val completedIdx = utteranceId?.removePrefix("utt_")?.toIntOrNull() ?: return
                        if (completedIdx + 1 < spokenSections.size) {
                            speakSectionAt(completedIdx + 1)
                        } else {
                            isPlaying = false
                            onSpokenIndexChange(-1)
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        isPlaying = false
                    }
                })
                isTtsReady = true
            }
        }
        ttsInstance = engine

        onDispose {
            engine?.stop()
            engine?.shutdown()
            ttsInstance = null
            onSpokenIndexChange(-1)
        }
    }

    // Auto-start playback when the user opens the Listen Note bar
    LaunchedEffect(visible, isTtsReady) {
        if (visible && isTtsReady && !isPlaying && spokenSections.isNotEmpty()) {
            speakSectionAt(sectionCursor.coerceIn(0, spokenSections.lastIndex))
        } else if (!visible) {
            ttsInstance?.stop()
            isPlaying = false
            onSpokenIndexChange(-1)
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(18.dp),
            tonalElevation = 6.dp,
            shadowElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp)
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Listen Note",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val currentPreview = spokenSections.getOrNull(sectionCursor)?.second ?: "Ready to read note"
                        Text(
                            text = currentPreview,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (spokenSections.isNotEmpty()) "${sectionCursor + 1}/${spokenSections.size}" else "0/0",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Speed cycle chip
                    Surface(
                        onClick = {
                            val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                            val nextIdx = (speeds.indexOfFirst { it == readerPrefs.ttsSpeed } + 1) % speeds.size
                            val newSpeed = speeds[nextIdx]
                            readerPrefs.updateTtsSpeed(newSpeed)
                            if (isPlaying) {
                                speakSectionAt(sectionCursor)
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "${readerPrefs.ttsSpeed}x",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (sectionCursor > 0) {
                                    speakSectionAt(sectionCursor - 1)
                                }
                            },
                            enabled = sectionCursor > 0,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous Section"
                            )
                        }

                        Surface(
                            onClick = {
                                if (isPlaying) {
                                    ttsInstance?.stop()
                                    isPlaying = false
                                } else {
                                    speakSectionAt(sectionCursor)
                                }
                            },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                if (sectionCursor + 1 < spokenSections.size) {
                                    speakSectionAt(sectionCursor + 1)
                                }
                            },
                            enabled = sectionCursor + 1 < spokenSections.size,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next Section"
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            ttsInstance?.stop()
                            isPlaying = false
                            onSpokenIndexChange(-1)
                            onClose()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Listening",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * Extracts the plain text snippet of a `MarkdownNode` so we can locate its line in `contentMarkdown`
 * when the user taps a node to highlight/underline it in Reader Mode.
 */
fun extractNodeAnchorText(node: MarkdownNode): String {
    return when (node) {
        is MarkdownNode.Heading -> node.text.text.trim()
        is MarkdownNode.Paragraph -> node.text.text.trim()
        is MarkdownNode.ListItem -> node.text.text.trim()
        is MarkdownNode.Checklist -> node.text.text.trim()
        is MarkdownNode.Blockquote -> node.text.text.trim()
        is MarkdownNode.Callout -> node.content.text.trim()
        else -> ""
    }
}

fun stripAnnotationTags(input: String): String {
    return input
        .replace("==", "")
        .replace("++", "")
        .replace(Regex("""<hl:[a-zA-Z]+>"""), "")
        .replace("</hl>", "")
        .replace("<mark>", "")
        .replace("</mark>", "")
        .replace("<u>", "")
        .replace("</u>", "")
}

/**
 * Applies or clears an annotation (`Highlight` or `Underline`) on the matching markdown line
 * or selected word phrase inside `contentMarkdown`.
 */
fun applyAnnotationToMarkdown(
    contentMarkdown: String,
    nodeIndex: Int,
    parsedNodes: List<MarkdownNode>,
    tool: AnnotationTool,
    selectedPhrase: String? = null
): String {
    if (tool == AnnotationTool.NONE) return contentMarkdown
    val node = parsedNodes.getOrNull(nodeIndex) ?: return contentMarkdown
    val anchorWords = extractNodeAnchorText(node)
        .split(Regex("""\s+"""))
        .map { it.replace(Regex("""[^a-zA-Z0-9\u0900-\u097F]"""), "") }
        .filter { it.length >= 2 }
        .take(5)

    val lines = contentMarkdown.lines().toMutableList()
    // Find the best matching line in contentMarkdown
    var bestLineIdx = -1
    var bestScore = 0

    for (i in lines.indices) {
        val rawLine = lines[i]
        val trimmed = rawLine.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("$$") || trimmed.startsWith("|")) continue
        val strippedLine = stripAnnotationTags(trimmed)
        var score = 0
        for (w in anchorWords) {
            if (w.isNotEmpty() && strippedLine.contains(w)) score++
        }
        if (score > bestScore) {
            bestScore = score
            bestLineIdx = i
        }
    }

    if (bestLineIdx == -1) return contentMarkdown
    val targetLine = lines[bestLineIdx]

    if (tool == AnnotationTool.ERASER) {
        lines[bestLineIdx] = stripAnnotationTags(targetLine)
        return lines.joinToString("\n")
    }

    if (!selectedPhrase.isNullOrBlank()) {
        val cleanTarget = stripAnnotationTags(targetLine)
        val idx = cleanTarget.indexOf(selectedPhrase)
        if (idx != -1) {
            lines[bestLineIdx] = cleanTarget.substring(0, idx) +
                tool.openTag + selectedPhrase + tool.closeTag +
                cleanTarget.substring(idx + selectedPhrase.length)
            return lines.joinToString("\n")
        }
    }

    // Full-line toggle: preserve markdown prefix (#, *, -, 1., >) and wrap the content body
    val prefixRegex = Regex("""^(\s*(?:#{1,6}\s+|[-*•]\s+\[[ xX]\]\s+|[-*•]\s+|\d+\.\s+|>\s+)?)([\s\S]*)$""")
    val match = prefixRegex.find(targetLine) ?: return contentMarkdown
    val prefix = match.groupValues[1]
    val body = match.groupValues[2].trim()
    if (body.isEmpty()) return contentMarkdown

    // If already wrapped with this exact tool, unwrap it (toggle off); otherwise strip old wrapper and apply new
    val updatedBody = if (body.startsWith(tool.openTag) && body.endsWith(tool.closeTag) &&
        body.length > tool.openTag.length + tool.closeTag.length
    ) {
        body.substring(tool.openTag.length, body.length - tool.closeTag.length)
    } else {
        val cleanBody = stripAnnotationTags(body)
        "${tool.openTag}$cleanBody${tool.closeTag}"
    }

    lines[bestLineIdx] = "$prefix$updatedBody"
    return lines.joinToString("\n")
}
