package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Assistant
import com.example.data.model.AssistantId
import com.example.data.model.ChatMessage
import com.example.data.model.DTFGAssistants
import com.example.data.repository.DTFGRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DTFGViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DTFGRepository(application)

    private val _selectedAssistant = MutableStateFlow(DTFGAssistants.all.first())
    val selectedAssistant: StateFlow<Assistant> = _selectedAssistant.asStateFlow()

    val assistants: List<Assistant> = repository.getAssistants()

    // Automatically switch message stream when selected assistant changes
    val messages: StateFlow<List<ChatMessage>> = _selectedAssistant
        .flatMapLatest { assistant ->
            repository.getMessages(assistant.id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allHistory: StateFlow<List<ChatMessage>> = repository.getAllHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<ChatMessage>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getAllHistory()
            else repository.searchHistory(query.trim())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun selectAssistant(id: AssistantId) {
        if (_selectedAssistant.value.id != id) {
            _selectedAssistant.value = repository.getAssistant(id)
            _errorMessage.value = null
        }
    }

    fun onInputChange(newText: String) {
        _inputText.value = newText
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun sendMessage(promptOverride: String? = null) {
        val textToSend = promptOverride ?: _inputText.value.trim()
        if (textToSend.isBlank() || _isGenerating.value) return

        if (promptOverride == null) {
            _inputText.value = ""
        }
        _isGenerating.value = true
        _errorMessage.value = null

        val currentAssistantId = _selectedAssistant.value.id
        val recentHistory = messages.value

        viewModelScope.launch {
            val result = repository.sendMessage(
                assistantId = currentAssistantId,
                userPrompt = textToSend,
                recentMessages = recentHistory
            )
            _isGenerating.value = false
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage
                    ?: "An unexpected error occurred while communicating with the assistant."
            }
        }
    }

    fun clearCurrentAssistantHistory() {
        viewModelScope.launch {
            repository.clearHistoryForAssistant(_selectedAssistant.value.id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch {
            repository.deleteMessage(id)
        }
    }

    fun dismissError() {
        _errorMessage.value = null
    }
}
