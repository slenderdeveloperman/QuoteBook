package com.quotecards.ui.screens.edit

import androidx.lifecycle.SavedStateHandle
import com.quotecards.domain.model.Quote
import com.quotecards.testutils.FakeQuoteRepository
import com.quotecards.testutils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditQuoteViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun init_withValidQuoteId_loadsExistingQuote() = runTest {
        val repository = FakeQuoteRepository(
            initialQuotes = listOf(
                Quote(id = 5L, text = "Begin at once to live", author = "Seneca")
            )
        )

        val viewModel = EditQuoteViewModel(
            quoteRepository = repository,
            savedStateHandle = SavedStateHandle(mapOf("quoteId" to 5L))
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(5L, state.quoteId)
        assertEquals("Seneca", state.author)
    }

    @Test
    fun saveQuote_withUpdatedValues_persistsAndSetsSavedFlag() = runTest {
        val repository = FakeQuoteRepository(
            initialQuotes = listOf(
                Quote(id = 7L, text = "Original", author = "Unknown")
            )
        )

        val viewModel = EditQuoteViewModel(
            quoteRepository = repository,
            savedStateHandle = SavedStateHandle(mapOf("quoteId" to 7L))
        )
        advanceUntilIdle()

        viewModel.updateQuoteText("Updated text")
        viewModel.updateAuthor("Updated author")
        viewModel.saveQuote()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val saved = repository.getQuoteById(7L)

        assertTrue(state.isSaved)
        assertEquals("Updated text", saved?.text)
        assertEquals("Updated author", saved?.author)
    }

    @Test
    fun deleteQuote_removesQuoteAndSetsDeletedFlag() = runTest {
        val repository = FakeQuoteRepository(
            initialQuotes = listOf(
                Quote(id = 8L, text = "Delete me", author = "Author")
            )
        )

        val viewModel = EditQuoteViewModel(
            quoteRepository = repository,
            savedStateHandle = SavedStateHandle(mapOf("quoteId" to 8L))
        )
        advanceUntilIdle()

        viewModel.deleteQuote()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isDeleted)
        assertEquals(null, repository.getQuoteById(8L))
    }

    @Test
    fun init_withMissingQuote_setsNotFoundError() = runTest {
        val viewModel = EditQuoteViewModel(
            quoteRepository = FakeQuoteRepository(),
            savedStateHandle = SavedStateHandle(mapOf("quoteId" to 999L))
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Quote not found", state.errorMessage)
    }
}
