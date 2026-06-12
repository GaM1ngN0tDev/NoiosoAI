package com.noioso.noiosoai.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noioso.noiosoai.data.remote.Message
import com.noioso.noiosoai.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isGenerating: Boolean = false,
    val error: String? = null,
    val model: String = "llama3",
    val systemPrompt: String = ""
)

class ChatViewModel(
    private val repository: ChatRepository,
    private val settingsManager: com.noioso.noiosoai.data.local.SettingsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var chatJob: Job? = null

    init {
        viewModelScope.launch {
            settingsManager.ollamaModel.collect { model ->
                _uiState.update { it.copy(model = model) }
            }
        }
        viewModelScope.launch {
            settingsManager.systemPrompt.collect { prompt ->
                _uiState.update { it.copy(systemPrompt = prompt) }
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank() || _uiState.value.isGenerating) return

        val userMessage = Message(role = "user", content = content)
        val currentMessages = _uiState.value.messages + userMessage
        
        _uiState.update { it.copy(messages = currentMessages, isGenerating = true, error = null) }

        chatJob = viewModelScope.launch {
            val assistantMessagePlaceholder = Message(role = "assistant", content = "")
            _uiState.update { it.copy(messages = it.messages + assistantMessagePlaceholder) }

            // Include system prompt at the beginning of the conversation
            val messagesWithSystem = if (_uiState.value.systemPrompt.isNotBlank()) {
                listOf(Message(role = "system", content = _uiState.value.systemPrompt)) + currentMessages
            } else {
                currentMessages
            }

            repository.chatStream(_uiState.value.model, messagesWithSystem)
                .onStart { /* Handle start if needed */ }
                .onCompletion { _uiState.update { it.copy(isGenerating = false) } }
                .catch { e ->
                    Log.e("ChatViewModel", "Error in chat stream", e)
                    _uiState.update { 
                        it.copy(
                            isGenerating = false, 
                            error = "Error: ${e.message ?: e.localizedMessage ?: "Unknown error"}"
                        ) 
                    }
                }
                .collect { response ->
                    response.message?.let { msg ->
                        updateLastAssistantMessage(msg.content)
                    }
                }
        }
    }

    private fun updateLastAssistantMessage(newContent: String) {
        _uiState.update { state ->
            val messages = state.messages.toMutableList()
            if (messages.isNotEmpty() && messages.last().role == "assistant") {
                val lastMessage = messages.last()
                messages[messages.size - 1] = lastMessage.copy(content = lastMessage.content + newContent)
            }
            state.copy(messages = messages)
        }
    }

    fun stopGeneration() {
        chatJob?.cancel()
        _uiState.update { it.copy(isGenerating = false) }
    }
    
    fun clearChat() {
        chatJob?.cancel()
        _uiState.update { it.copy(messages = emptyList(), isGenerating = false, error = null) }
    }
}
