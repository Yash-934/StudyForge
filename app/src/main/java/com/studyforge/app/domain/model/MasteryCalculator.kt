package com.studyforge.app.domain.model

import com.studyforge.app.data.local.entities.ChapterEntity
import com.studyforge.app.data.local.entities.MistakeEntity
import com.studyforge.app.data.local.entities.QuestionEntity
import com.studyforge.app.data.local.entities.RevisionItemEntity
import com.studyforge.app.data.local.entities.TestAttemptEntity
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Deterministic Mastery Score Engine for StudyForge.
 *
 * Factors:
 * 1. Test Accuracy (40% weight): Average score percentage across tests for this chapter.
 * 2. Question Practice (25% weight): Ratio of timesCorrect to timesAttempted across questions.
 * 3. Mistake Resolution (15% weight): Ratio of resolved mistakes vs total mistakes.
 * 4. Revision Discipline (10% weight): Repetition levels and on-time completion of revisions.
 * 5. Learning Momentum (10% weight): Coverage of questions attempted relative to total chapter bank.
 */
object MasteryCalculator {

    fun calculateChapterMastery(
        questions: List<QuestionEntity>,
        attempts: List<TestAttemptEntity>,
        mistakes: List<MistakeEntity>,
        revisions: List<RevisionItemEntity>
    ): Int {
        if (questions.isEmpty() && attempts.isEmpty()) {
            return 0
        }

        // 1. Test Performance (Weight: 40)
        val testScore: Double = if (attempts.isNotEmpty()) {
            val totalPct = attempts.sumOf { it.accuracyPercentage }
            (totalPct / attempts.size).coerceIn(0.0, 100.0)
        } else {
            0.0
        }

        // 2. Question Practice Accuracy (Weight: 25)
        var totalAttempts = 0
        var totalCorrect = 0
        questions.forEach {
            totalAttempts += it.timesAttempted
            totalCorrect += it.timesCorrect
        }
        val practiceScore: Double = if (totalAttempts > 0) {
            (totalCorrect.toDouble() / totalAttempts.toDouble() * 100.0).coerceIn(0.0, 100.0)
        } else {
            0.0
        }

        // 3. Mistake Resolution Rate (Weight: 15)
        val mistakeScore: Double = if (mistakes.isNotEmpty()) {
            val resolvedCount = mistakes.count { it.isResolved }
            (resolvedCount.toDouble() / mistakes.size.toDouble() * 100.0).coerceIn(0.0, 100.0)
        } else {
            // No mistakes means 100% on this factor if questions attempted, else 50%
            if (totalAttempts > 0) 100.0 else 50.0
        }

        // 4. Revision Discipline (Weight: 10)
        val revisionScore: Double = if (revisions.isNotEmpty()) {
            val avgInterval = revisions.sumOf { it.intervalLevel }.toDouble() / revisions.size.toDouble()
            // Interval level 4 (30 days) is master level
            (avgInterval / 4.0 * 100.0).coerceIn(0.0, 100.0)
        } else {
            if (questions.isNotEmpty()) 20.0 else 0.0
        }

        // 5. Question Bank Coverage (Weight: 10)
        val attemptedQuestionsCount = questions.count { it.timesAttempted > 0 }
        val coverageScore: Double = if (questions.isNotEmpty()) {
            (attemptedQuestionsCount.toDouble() / questions.size.toDouble() * 100.0).coerceIn(0.0, 100.0)
        } else {
            0.0
        }

        // Combine weights:
        val composite = if (attempts.isEmpty()) {
            // If no full tests taken yet, rebalance weights:
            // Practice: 50%, Mistake: 25%, Revision: 15%, Coverage: 10%
            (practiceScore * 0.50) + (mistakeScore * 0.25) + (revisionScore * 0.15) + (coverageScore * 0.10)
        } else {
            (testScore * 0.40) + (practiceScore * 0.25) + (mistakeScore * 0.15) + (revisionScore * 0.10) + (coverageScore * 0.10)
        }

        return min(100, composite.roundToInt().coerceAtLeast(0))
    }
}
