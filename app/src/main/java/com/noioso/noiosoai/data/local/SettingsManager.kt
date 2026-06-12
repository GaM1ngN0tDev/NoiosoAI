package com.noioso.noiosoai.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    companion object {
        private val OLLAMA_IP_KEY = stringPreferencesKey("ollama_ip")
        private val OLLAMA_MODEL_KEY = stringPreferencesKey("ollama_model")
        private val SYSTEM_PROMPT_KEY = stringPreferencesKey("system_prompt")
        private const val DEFAULT_IP = "http://10.0.2.2:11434" // Default for Android Emulator
        private const val DEFAULT_MODEL = "llama3.2"
        private const val DEFAULT_SYSTEM_PROMPT = "You are NoiosoAI, a helpful and friendly AI assistant."
    }

    val ollamaIp: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[OLLAMA_IP_KEY] ?: DEFAULT_IP
        }

    val ollamaModel: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[OLLAMA_MODEL_KEY] ?: DEFAULT_MODEL
        }

    val systemPrompt: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SYSTEM_PROMPT_KEY] ?: DEFAULT_SYSTEM_PROMPT
        }

    suspend fun saveOllamaIp(ip: String) {
        context.dataStore.edit { preferences ->
            preferences[OLLAMA_IP_KEY] = ip
        }
    }

    suspend fun saveOllamaModel(model: String) {
        context.dataStore.edit { preferences ->
            preferences[OLLAMA_MODEL_KEY] = model
        }
    }

    suspend fun saveSystemPrompt(prompt: String) {
        context.dataStore.edit { preferences ->
            preferences[SYSTEM_PROMPT_KEY] = prompt
        }
    }
}
