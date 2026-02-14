package com.quotecards.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quotecards.data.repository.QuoteRepository
import com.quotecards.domain.model.Quote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val quotes: StateFlow<List<Quote>> = _selectedCategory
        .flatMapLatest { category ->
            quoteRepository.getQuotesByCategory(category)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categories: StateFlow<List<String>> = quoteRepository
        .getCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val uncategorizedQuotes: StateFlow<List<Quote>> = quoteRepository
        .getUncategorizedQuotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun createCategory(name: String, quoteIds: List<Long>) {
        viewModelScope.launch {
            quoteRepository.assignQuotesToCategory(quoteIds, name)
        }
    }

    fun deleteQuote(quote: Quote) {
        viewModelScope.launch {
            quoteRepository.deleteQuote(quote)
        }
    }
}
