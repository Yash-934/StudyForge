package com.studyforge.app.domain.model

enum class QuestionType(val displayName: String) {
    MCQ("Multiple Choice"),
    MULTIPLE_CORRECT("Multiple Correct"),
    TRUE_FALSE("True / False"),
    FILL_BLANK("Fill in the Blank"),
    NUMERICAL("Numerical"),
    SHORT_ANSWER("Short Answer"),
    LONG_ANSWER("Long Answer"),
    ASSERTION_REASON("Assertion & Reason"),
    MATCH_FOLLOWING("Match the Following");

    companion object {
        fun fromString(value: String): QuestionType {
            return when (value.trim().lowercase()) {
                "mcq", "single_choice", "single_correct" -> MCQ
                "multiple_correct", "multi_choice", "multiple_choice", "msq" -> MULTIPLE_CORRECT
                "true_false", "tf", "boolean" -> TRUE_FALSE
                "fill_blank", "fill_in_the_blank", "blank" -> FILL_BLANK
                "numerical", "integer", "number" -> NUMERICAL
                "short_answer", "short" -> SHORT_ANSWER
                "long_answer", "long", "essay" -> LONG_ANSWER
                "assertion_reason", "ar" -> ASSERTION_REASON
                "match_following", "match", "matrix_match" -> MATCH_FOLLOWING
                else -> MCQ
            }
        }
    }
}

enum class Difficulty(val displayName: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard");

    companion object {
        fun fromString(value: String): Difficulty {
            return when (value.trim().lowercase()) {
                "easy", "basic" -> EASY
                "hard", "advanced", "difficult" -> HARD
                else -> MEDIUM
            }
        }
    }
}

enum class TestMode(val displayName: String) {
    PRACTICE("Practice Mode"),
    EXAM("Exam Mode"),
    REVISION("Revision Mode"),
    WEAK_AREA("Weak Area Mode"),
    RANDOM("Random Mode"),
    ADAPTIVE("Adaptive Mode");

    companion object {
        fun fromString(value: String): TestMode {
            return try {
                valueOf(value.uppercase())
            } catch (e: Exception) {
                PRACTICE
            }
        }
    }
}

enum class RevisionItemType(val displayName: String) {
    NOTE("Note"),
    FORMULA("Formula"),
    QUESTION("Question"),
    MISTAKE("Mistake"),
    FLASHCARD("Flashcard")
}

enum class Mood(val emoji: String, val label: String) {
    EXCELLENT("🔥", "Fired Up"),
    GOOD("😊", "Productive"),
    NEUTRAL("😐", "Steady"),
    TIRED("😴", "Exhausted"),
    STRESSED("🤯", "Overwhelmed")
}
