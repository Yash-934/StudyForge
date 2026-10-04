package com.studyforge.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "studyforge_prefs")

class PreferencesRepository(private val context: Context) {
    companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // "SYSTEM", "LIGHT", "DARK"
        val KEY_DEFAULT_TEST_DURATION = intPreferencesKey("default_test_duration")
        val KEY_DEFAULT_NEGATIVE_MARKS = doublePreferencesKey("default_negative_marks")
        val KEY_GEMINI_API_KEY_OVERRIDE = stringPreferencesKey("gemini_api_key_override")
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME_MODE] ?: "SYSTEM"
    }

    val defaultTestDuration: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_TEST_DURATION] ?: 30
    }

    val defaultNegativeMarks: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_NEGATIVE_MARKS] ?: 0.25
    }

    val geminiApiKeyOverride: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_GEMINI_API_KEY_OVERRIDE] ?: ""
    }

    val isOnboardingDone: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_DONE] ?: false
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[KEY_THEME_MODE] = mode }
    }

    suspend fun setDefaultTestDuration(minutes: Int) {
        context.dataStore.edit { it[KEY_DEFAULT_TEST_DURATION] = minutes }
    }

    suspend fun setDefaultNegativeMarks(negativeMarks: Double) {
        context.dataStore.edit { it[KEY_DEFAULT_NEGATIVE_MARKS] = negativeMarks }
    }

    suspend fun setGeminiApiKeyOverride(key: String) {
        context.dataStore.edit { it[KEY_GEMINI_API_KEY_OVERRIDE] = key }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_DONE] = done }
    }
}
