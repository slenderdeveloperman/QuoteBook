package com.quotecards.ui.screens.home

import com.quotecards.domain.model.Quote
import com.quotecards.testutils.FakeQuoteRepository
import com.quotecards.testutils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun deleteQuote_removesQuoteFromStateFlow() = runTest {
        val quote = Quote(id = 1, text = "Stay hungry, stay foolish", author = "Steve Jobs")
        val repository = FakeQuoteRepository(initialQuotes = listOf(quote))
        val viewModel = HomeViewModel(repository)

        val collector = backgroundScope.launch { viewModel.quotes.collect { } }
        advanceUntilIdle()
        assertEquals(1, viewModel.quotes.value.size)

        viewModel.deleteQuote(quote)
        advanceUntilIdle()

        assertTrue(viewModel.quotes.value.isEmpty())
        collector.cancel()
    }
}
