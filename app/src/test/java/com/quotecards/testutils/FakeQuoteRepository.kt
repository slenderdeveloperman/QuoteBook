package com.quotecards.testutils

import com.quotecards.data.repository.QuoteRepository
import com.quotecards.domain.model.Quote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeQuoteRepository(
    initialQuotes: List<Quote> = emptyList()
) : QuoteRepository {

    private val quotesFlow = MutableStateFlow(initialQuotes)
    private var nextId = (initialQuotes.maxOfOrNull { it.id } ?: 0L) + 1L

    override fun getAllQuotes(): Flow<List<Quote>> = quotesFlow

    override suspend fun getQuoteById(id: Long): Quote? {
        return quotesFlow.value.firstOrNull { it.id == id }
    }

    override suspend fun addQuote(quote: Quote): Long {
        val id = if (quote.id == 0L) nextId++ else quote.id
        val inserted = quote.copy(id = id)
        quotesFlow.value = listOf(inserted) + quotesFlow.value
        return id
    }

    override suspend fun updateQuote(quote: Quote) {
        quotesFlow.value = quotesFlow.value.map {
            if (it.id == quote.id) quote else it
        }
    }

    override suspend fun deleteQuote(quote: Quote) {
        quotesFlow.value = quotesFlow.value.filterNot { it.id == quote.id }
    }

    override suspend fun deleteQuoteById(id: Long) {
        quotesFlow.value = quotesFlow.value.filterNot { it.id == id }
    }

    override fun searchQuotes(query: String): Flow<List<Quote>> {
        val normalizedQuery = query.trim()
        return quotesFlow.map { quotes ->
            if (normalizedQuery.isBlank()) {
                emptyList()
            } else {
                quotes.filter {
                    it.text.contains(normalizedQuery, ignoreCase = true) ||
                        it.author.contains(normalizedQuery, ignoreCase = true)
                }
            }
        }
    }
}
