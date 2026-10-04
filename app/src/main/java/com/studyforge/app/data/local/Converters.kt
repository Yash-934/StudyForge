package com.studyforge.app.data.local

import androidx.room.TypeConverter
import com.studyforge.app.domain.model.Difficulty
import com.studyforge.app.domain.model.Mood
import com.studyforge.app.domain.model.QuestionType
import com.studyforge.app.domain.model.RevisionItemType
import com.studyforge.app.domain.model.TestMode
import org.json.JSONArray

class Converters {
    @TypeConverter
    fun fromQuestionType(type: QuestionType?): String = type?.name ?: QuestionType.MCQ.name

    @TypeConverter
    fun toQuestionType(name: String?): QuestionType =
        name?.let { runCatching { QuestionType.valueOf(it) }.getOrNull() } ?: QuestionType.MCQ

    @TypeConverter
    fun fromDifficulty(diff: Difficulty?): String = diff?.name ?: Difficulty.MEDIUM.name

    @TypeConverter
    fun toDifficulty(name: String?): Difficulty =
        name?.let { runCatching { Difficulty.valueOf(it) }.getOrNull() } ?: Difficulty.MEDIUM

    @TypeConverter
    fun fromTestMode(mode: TestMode?): String = mode?.name ?: TestMode.PRACTICE.name

    @TypeConverter
    fun toTestMode(name: String?): TestMode =
        name?.let { runCatching { TestMode.valueOf(it) }.getOrNull() } ?: TestMode.PRACTICE

    @TypeConverter
    fun fromRevisionItemType(type: RevisionItemType?): String = type?.name ?: RevisionItemType.NOTE.name

    @TypeConverter
    fun toRevisionItemType(name: String?): RevisionItemType =
        name?.let { runCatching { RevisionItemType.valueOf(it) }.getOrNull() } ?: RevisionItemType.NOTE

    @TypeConverter
    fun fromMood(mood: Mood?): String = mood?.name ?: Mood.GOOD.name

    @TypeConverter
    fun toMood(name: String?): Mood =
        name?.let { runCatching { Mood.valueOf(it) }.getOrNull() } ?: Mood.GOOD

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        if (list == null) return "[]"
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        return arr.toString()
    }

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            val result = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                result.add(arr.optString(i, ""))
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromLongList(list: List<Long>?): String {
        if (list == null) return "[]"
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        return arr.toString()
    }

    @TypeConverter
    fun toLongList(json: String?): List<Long> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            val result = mutableListOf<Long>()
            for (i in 0 until arr.length()) {
                result.add(arr.optLong(i))
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }
}
