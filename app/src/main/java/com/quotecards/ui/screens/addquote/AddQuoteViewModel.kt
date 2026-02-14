package com.quotecards.ui.screens.addquote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quotecards.data.repository.QuoteRepository
import com.quotecards.domain.model.Quote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddQuoteUiState(
    val quoteText: String = "",
    val author: String = "",
    val category: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
) {
    val isValid: Boolean
        get() = quoteText.trim().isNotBlank()
}

@HiltViewModel
class AddQuoteViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddQuoteUiState())
    val uiState: StateFlow<AddQuoteUiState> = _uiState.asStateFlow()

    val categories: StateFlow<List<String>> = quoteRepository
        .getCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateQuoteText(text: String) {
        _uiState.update { it.copy(quoteText = text, errorMessage = null) }
    }

    fun updateAuthor(author: String) {
        _uiState.update { it.copy(author = author) }
    }

    fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun saveQuote() {
        val currentState = _uiState.value

        // Prevent duplicate saves on rapid clicks
        if (currentState.isLoading || currentState.isSaved) return

        if (!currentState.isValid) {
            _uiState.update { it.copy(errorMessage = "Please enter a quote") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val quote = Quote(
                    text = currentState.quoteText.trim(),
                    author = currentState.author.trim().ifBlank { "Unknown" },
                    category = currentState.category
                )
                quoteRepository.addQuote(quote)
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to save quote: ${e.localizedMessage ?: "Please try again"}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
