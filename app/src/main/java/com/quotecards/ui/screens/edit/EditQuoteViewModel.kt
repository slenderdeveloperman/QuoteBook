package com.quotecards.ui.screens.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quotecards.data.repository.QuoteRepository
import com.quotecards.domain.model.Quote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditQuoteUiState(
    val quoteId: Long = 0,
    val quoteText: String = "",
    val author: String = "",
    val createdAt: Long = 0,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val errorMessage: String? = null
) {
    val isValid: Boolean
        get() = quoteText.trim().isNotBlank()
}

@HiltViewModel
class EditQuoteViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val quoteId: Long = savedStateHandle.get<Long>("quoteId") ?: -1L

    private val _uiState = MutableStateFlow(EditQuoteUiState())
    val uiState: StateFlow<EditQuoteUiState> = _uiState.asStateFlow()

    init {
        if (quoteId > 0) {
            loadQuote()
        } else {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Invalid quote ID"
                )
            }
        }
    }

    private fun loadQuote() {
        viewModelScope.launch {
            try {
                val quote = quoteRepository.getQuoteById(quoteId)
                if (quote != null) {
                    _uiState.update {
                        it.copy(
                            quoteId = quote.id,
                            quoteText = quote.text,
                            author = quote.author,
                            createdAt = quote.createdAt,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Quote not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load quote: ${e.localizedMessage ?: "Please try again"}"
                    )
                }
            }
        }
    }

    fun updateQuoteText(text: String) {
        _uiState.update { it.copy(quoteText = text, errorMessage = null) }
    }

    fun updateAuthor(author: String) {
        _uiState.update { it.copy(author = author) }
    }

    fun saveQuote() {
        val currentState = _uiState.value

        // Prevent duplicate saves
        if (currentState.isLoading || currentState.isSaved) return

        if (!currentState.isValid) {
            _uiState.update { it.copy(errorMessage = "Please enter a quote") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val quote = Quote(
                    id = currentState.quoteId,
                    text = currentState.quoteText.trim(),
                    author = currentState.author.trim().ifBlank { "Unknown" },
                    createdAt = currentState.createdAt
                )
                quoteRepository.updateQuote(quote)
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

    fun showDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun deleteQuote() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showDeleteDialog = false) }

            try {
                quoteRepository.deleteQuoteById(_uiState.value.quoteId)
                _uiState.update { it.copy(isLoading = false, isDeleted = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to delete quote: ${e.localizedMessage ?: "Please try again"}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
