package com.studyforge.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studyforge.app.data.local.entities.BatchEntity
import com.studyforge.app.data.local.entities.ChapterEntity
import com.studyforge.app.data.local.entities.QuestionEntity
import com.studyforge.app.data.local.entities.SubjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BatchDao {
    @Query("SELECT * FROM batches ORDER BY id ASC")
    fun getAllBatches(): Flow<List<BatchEntity>>

    @Query("SELECT * FROM batches WHERE id = :id LIMIT 1")
    suspend fun getBatchById(id: Long): BatchEntity?

    @Query("SELECT * FROM batches WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getBatchByName(name: String): BatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: BatchEntity): Long

    @Update
    suspend fun updateBatch(batch: BatchEntity)

    @Delete
    suspend fun deleteBatch(batch: BatchEntity)
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects WHERE batchId = :batchId ORDER BY id ASC")
    fun getSubjectsForBatch(batchId: Long): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects ORDER BY id ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE batchId = :batchId AND LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getSubjectByName(batchId: Long, name: String): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)
}

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId ORDER BY orderIndex ASC, id ASC")
    fun getChaptersForSubject(subjectId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters ORDER BY id ASC")
    fun getAllChapters(): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId AND LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getChapterByName(subjectId: Long, name: String): ChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity): Long

    @Delete
    suspend fun deleteChapter(chapter: ChapterEntity)
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions ORDER BY id DESC")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE chapterId = :chapterId ORDER BY id ASC")
    fun getQuestionsForChapter(chapterId: Long): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE batchId = :batchId ORDER BY id ASC")
    fun getQuestionsForBatch(batchId: Long): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE subjectId = :subjectId ORDER BY id ASC")
    fun getQuestionsForSubject(subjectId: Long): Flow<List<QuestionEntity>>

    @Query("SELECT COUNT(*) FROM questions WHERE chapterId = :chapterId")
    suspend fun getQuestionCountForChapter(chapterId: Long): Int

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getTotalQuestionCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>): List<Long>

    @Update
    suspend fun updateQuestion(question: QuestionEntity)

    @Delete
    suspend fun deleteQuestion(question: QuestionEntity)

    @Query("DELETE FROM questions WHERE chapterId = :chapterId")
    suspend fun deleteQuestionsByChapter(chapterId: Long)

    @Query("UPDATE questions SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun setBookmark(id: Long, bookmarked: Boolean)
}
