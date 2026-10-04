package com.studyforge.app.domain.model

import com.studyforge.app.data.local.entities.MistakeEntity
import com.studyforge.app.data.local.entities.QuestionEntity

data class SmartTestDistribution(
    val weakAreasPercent: Int = 40,
    val mediumPerformancePercent: Int = 30,
    val recentlyLearnedPercent: Int = 20,
    val previouslyIncorrectPercent: Int = 10
)

object SmartTestGenerator {

    /**
     * Generates a balanced question selection matching the target count and distribution.
     */
    fun selectQuestions(
        allQuestions: List<QuestionEntity>,
        allMistakes: List<MistakeEntity>,
        targetQuestionCount: Int,
        distribution: SmartTestDistribution = SmartTestDistribution()
    ): List<QuestionEntity> {
        if (allQuestions.isEmpty() || targetQuestionCount <= 0) return emptyList()

        if (allQuestions.size <= targetQuestionCount) {
            return allQuestions.shuffled()
        }

        val mistakeQuestionIds = allMistakes.map { it.questionId }.toSet()

        // 1. Previously Incorrect Pool (Questions currently or previously in mistakes)
        val previouslyIncorrectPool = allQuestions.filter { it.id in mistakeQuestionIds }.toMutableList()

        // 2. Weak Areas Pool (Attempted with low accuracy < 50% or Hard difficulty unsolved)
        val weakAreasPool = allQuestions.filter { q ->
            (q.timesAttempted > 0 && (q.timesCorrect.toDouble() / q.timesAttempted.toDouble()) < 0.5) ||
                (q.timesAttempted == 0 && q.difficulty == Difficulty.HARD)
        }.toMutableList()

        // 3. Medium Performance Pool (Accuracy between 50% and 80%, or Medium difficulty)
        val mediumPool = allQuestions.filter { q ->
            (q.timesAttempted > 0 && (q.timesCorrect.toDouble() / q.timesAttempted.toDouble()) in 0.5..0.8) ||
                (q.timesAttempted == 0 && q.difficulty == Difficulty.MEDIUM)
        }.toMutableList()

        // 4. Recently Learned Pool (Newest created questions or unattempted)
        val recentPool = allQuestions.sortedByDescending { it.createdAt }.toMutableList()

        val selectedIds = mutableSetOf<Long>()
        val selectedQuestions = mutableListOf<QuestionEntity>()

        fun takeFromPool(pool: List<QuestionEntity>, count: Int) {
            var taken = 0
            for (q in pool.shuffled()) {
                if (taken >= count) break
                if (q.id !in selectedIds) {
                    selectedIds.add(q.id)
                    selectedQuestions.add(q)
                    taken++
                }
            }
        }

        // Calculate counts
        val incorrectTarget = (targetQuestionCount * distribution.previouslyIncorrectPercent / 100).coerceAtLeast(1)
        val weakTarget = (targetQuestionCount * distribution.weakAreasPercent / 100).coerceAtLeast(1)
        val mediumTarget = (targetQuestionCount * distribution.mediumPerformancePercent / 100).coerceAtLeast(1)
        val recentTarget = (targetQuestionCount * distribution.recentlyLearnedPercent / 100).coerceAtLeast(1)

        takeFromPool(previouslyIncorrectPool, incorrectTarget)
        takeFromPool(weakAreasPool, weakTarget)
        takeFromPool(mediumPool, mediumTarget)
        takeFromPool(recentPool, recentTarget)

        // If we still need more questions, fill with any remaining questions
        if (selectedQuestions.size < targetQuestionCount) {
            val remainingNeeded = targetQuestionCount - selectedQuestions.size
            val remainingQuestions = allQuestions.filter { it.id !in selectedIds }.shuffled()
            takeFromPool(remainingQuestions, remainingNeeded)
        }

        return selectedQuestions.take(targetQuestionCount).shuffled()
    }
}
