package com.studyforge.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.studyforge.app.data.local.dao.BatchDao
import com.studyforge.app.data.local.dao.ChapterDao
import com.studyforge.app.data.local.dao.FlashcardDao
import com.studyforge.app.data.local.dao.FormulaDao
import com.studyforge.app.data.local.dao.JournalDao
import com.studyforge.app.data.local.dao.MistakeDao
import com.studyforge.app.data.local.dao.NoteDao
import com.studyforge.app.data.local.dao.QuestionDao
import com.studyforge.app.data.local.dao.RevisionDao
import com.studyforge.app.data.local.dao.StudySessionDao
import com.studyforge.app.data.local.dao.SubjectDao
import com.studyforge.app.data.local.dao.TestDao
import com.studyforge.app.data.local.dao.TimetableDao
import com.studyforge.app.data.local.entities.BatchEntity
import com.studyforge.app.data.local.entities.ChapterEntity
import com.studyforge.app.data.local.entities.FlashcardEntity
import com.studyforge.app.data.local.entities.FormulaEntity
import com.studyforge.app.data.local.entities.JournalEntity
import com.studyforge.app.data.local.entities.MistakeEntity
import com.studyforge.app.data.local.entities.NoteEntity
import com.studyforge.app.data.local.entities.QuestionEntity
import com.studyforge.app.data.local.entities.RevisionItemEntity
import com.studyforge.app.data.local.entities.StudySessionEntity
import com.studyforge.app.data.local.entities.SubjectEntity
import com.studyforge.app.data.local.entities.TestAnswerEntity
import com.studyforge.app.data.local.entities.TestAttemptEntity
import com.studyforge.app.data.local.entities.TestEntity
import com.studyforge.app.data.local.entities.TimetableEntity

@Database(
    entities = [
        BatchEntity::class,
        SubjectEntity::class,
        ChapterEntity::class,
        NoteEntity::class,
        FormulaEntity::class,
        QuestionEntity::class,
        TestEntity::class,
        TestAttemptEntity::class,
        TestAnswerEntity::class,
        MistakeEntity::class,
        RevisionItemEntity::class,
        FlashcardEntity::class,
        StudySessionEntity::class,
        TimetableEntity::class,
        JournalEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun batchDao(): BatchDao
    abstract fun subjectDao(): SubjectDao
    abstract fun chapterDao(): ChapterDao
    abstract fun noteDao(): NoteDao
    abstract fun formulaDao(): FormulaDao
    abstract fun questionDao(): QuestionDao
    abstract fun testDao(): TestDao
    abstract fun mistakeDao(): MistakeDao
    abstract fun revisionDao(): RevisionDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun timetableDao(): TimetableDao
    abstract fun journalDao(): JournalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "studyforge_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
