package com.quotecards.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quotecards.data.repository.QuoteRepository
import com.quotecards.domain.model.Quote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository
) : ViewModel() {

    val quotes: StateFlow<List<Quote>> = quoteRepository
        .getAllQuotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteQuote(quote: Quote) {
        viewModelScope.launch {
            quoteRepository.deleteQuote(quote)
        }
    }
}
