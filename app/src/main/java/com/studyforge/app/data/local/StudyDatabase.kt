package com.studyforge.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.studyforge.app.data.local.dao.BatchDao
import com.studyforge.app.data.local.dao.ChapterDao
import com.studyforge.app.data.local.dao.QuestionDao
import com.studyforge.app.data.local.dao.SubjectDao
import com.studyforge.app.data.local.entities.BatchEntity
import com.studyforge.app.data.local.entities.ChapterEntity
import com.studyforge.app.data.local.entities.QuestionEntity
import com.studyforge.app.data.local.entities.SubjectEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BatchEntity::class,
        SubjectEntity::class,
        ChapterEntity::class,
        QuestionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StudyDatabase : RoomDatabase() {
    abstract fun batchDao(): BatchDao
    abstract fun subjectDao(): SubjectDao
    abstract fun chapterDao(): ChapterDao
    abstract fun questionDao(): QuestionDao

    companion object {
        @Volatile
        private var INSTANCE: StudyDatabase? = null

        fun getDatabase(context: Context): StudyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyDatabase::class.java,
                    "studyforge_database"
                )
                    .addCallback(SeedCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class SeedCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(db: StudyDatabase) {
            val batchId = db.batchDao().insertBatch(
                BatchEntity(
                    name = "JEE & NEET Ultimate 2026",
                    description = "Target Batch for Complete Physics, Chemistry & Math Revision"
                )
            )

            // Physics Subject
            val physicsId = db.subjectDao().insertSubject(
                SubjectEntity(batchId = batchId, name = "Physics", icon = "bolt")
            )
            val kinematicsId = db.chapterDao().insertChapter(
                ChapterEntity(subjectId = physicsId, name = "Kinematics & Motion in 1D", orderIndex = 1)
            )
            val lawsOfMotionId = db.chapterDao().insertChapter(
                ChapterEntity(subjectId = physicsId, name = "Laws of Motion", orderIndex = 2)
            )

            // Mathematics Subject
            val mathId = db.subjectDao().insertSubject(
                SubjectEntity(batchId = batchId, name = "Mathematics", icon = "calculate")
            )
            val calculusId = db.chapterDao().insertChapter(
                ChapterEntity(subjectId = mathId, name = "Differential Calculus", orderIndex = 1)
            )

            // Initial Questions with LaTeX Math & Explanations
            val sampleQuestions = listOf(
                QuestionEntity(
                    chapterId = kinematicsId,
                    subjectId = physicsId,
                    batchId = batchId,
                    questionText = "A particle moves along a straight line such that its displacement is given by \$s(t) = 3t^2 - 4t + 5\$. What is its velocity at \$t = 2\\text{ s}\$?",
                    options = listOf("6 m/s", "8 m/s", "12 m/s", "16 m/s"),
                    correctOptionIndex = 1,
                    explanation = "**Step-by-step Solution:**\n\n1. Velocity is the first derivative of displacement with respect to time:\n   \$\$v(t) = \\frac{ds}{dt} = \\frac{d}{dt}(3t^2 - 4t + 5)\$\$\n2. Differentiating term by term:\n   \$\$v(t) = 6t - 4\$\$\n3. Substitute \$t = 2\\text{ s}\$:\n   \$\$v(2) = 6(2) - 4 = 12 - 4 = 8\\text{ m/s}\$\$\n\nHence, the correct velocity is **8 m/s**.",
                    topic = "Velocity and Differentiation",
                    difficulty = "Easy"
                ),
                QuestionEntity(
                    chapterId = kinematicsId,
                    subjectId = physicsId,
                    batchId = batchId,
                    questionText = "A projectile is fired from the ground with speed \$u\$ at an angle \$\\theta\$ to the horizontal. What is the maximum height \$H_{\\max}\$ reached?",
                    options = listOf(
                        "\$\$\\frac{u^2 \\sin(2\\theta)}{g}\$\$",
                        "\$\$\\frac{u^2 \\sin^2\\theta}{2g}\$\$",
                        "\$\$\\frac{u^2 \\cos^2\\theta}{2g}\$\$",
                        "\$\$\\frac{2u \\sin\\theta}{g}\$\$"
                    ),
                    correctOptionIndex = 1,
                    explanation = "**Standard Projectile Formula:**\n\nAt the peak of projectile trajectory, the vertical velocity component \$v_y = 0\$.\n\nUsing the 3rd equation of motion:\n\$\$v_y^2 = u_y^2 - 2g H_{\\max}\$\$\n\$\$0 = (u \\sin\\theta)^2 - 2g H_{\\max}\$\$\n\$\$H_{\\max} = \\frac{u^2 \\sin^2\\theta}{2g}\$\$\n\n| Parameter | Formula |\n| :--- | :--- |\n| Max Height (\$H_{\\max}\$) | \$\\frac{u^2 \\sin^2\\theta}{2g}\$ |\n| Time of Flight (\$T\$) | \$\\frac{2u \\sin\\theta}{g}\$ |\n| Horizontal Range (\$R\$) | \$\\frac{u^2 \\sin(2\\theta)}{g}\$ |",
                    topic = "Projectile Motion",
                    difficulty = "Medium"
                ),
                QuestionEntity(
                    chapterId = calculusId,
                    subjectId = mathId,
                    batchId = batchId,
                    questionText = "Evaluate the limit: \$\$\\lim_{x \\to 0} \\frac{\\sin(3x)}{x}\$\$",
                    options = listOf("0", "1", "3", "Does not exist"),
                    correctOptionIndex = 2,
                    explanation = "**Standard Limit:**\n\nRecall that \$\\lim_{\\theta \\to 0} \\frac{\\sin\\theta}{\\theta} = 1\$.\n\nMultiply and divide by 3:\n\$\$\\lim_{x \\to 0} 3 \\cdot \\frac{\\sin(3x)}{3x} = 3 \\cdot 1 = 3\$\$",
                    topic = "Limits and Continuity",
                    difficulty = "Easy"
                )
            )

            db.questionDao().insertQuestions(sampleQuestions)
        }
    }
}
