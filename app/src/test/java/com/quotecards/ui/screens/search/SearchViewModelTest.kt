package com.quotecards.ui.screens.search

import com.quotecards.domain.model.Quote
import com.quotecards.testutils.FakeQuoteRepository
import com.quotecards.testutils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun updateSearchQuery_afterDebounce_emitsMatchingResults() = runTest {
        val repository = FakeQuoteRepository(
            initialQuotes = listOf(
                Quote(id = 1, text = "Know thyself", author = "Socrates"),
                Quote(id = 2, text = "Amor fati", author = "Nietzsche")
            )
        )
        val viewModel = SearchViewModel(repository)
        val collector = backgroundScope.launch { viewModel.searchResults.collect { } }

        viewModel.updateSearchQuery("socr")
        advanceTimeBy(300)
        advanceUntilIdle()

        val results = viewModel.searchResults.value
        assertEquals(1, results.size)
        assertEquals("Socrates", results.first().author)
        collector.cancel()
    }

    @Test
    fun clearSearch_resetsQueryAndResults() = runTest {
        val repository = FakeQuoteRepository(
            initialQuotes = listOf(
                Quote(id = 1, text = "Meditations", author = "Marcus Aurelius")
            )
        )
        val viewModel = SearchViewModel(repository)
        val collector = backgroundScope.launch { viewModel.searchResults.collect { } }

        viewModel.updateSearchQuery("med")
        advanceTimeBy(300)
        advanceUntilIdle()

        viewModel.clearSearch()
        advanceTimeBy(300)
        advanceUntilIdle()

        assertTrue(viewModel.searchQuery.value.isEmpty())
        assertTrue(viewModel.searchResults.value.isEmpty())
        collector.cancel()
    }
}
