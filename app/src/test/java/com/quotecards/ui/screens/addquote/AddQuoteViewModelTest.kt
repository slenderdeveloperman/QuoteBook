package com.quotecards.ui.screens.addquote

import com.quotecards.domain.model.Quote
import com.quotecards.testutils.FakeQuoteRepository
import com.quotecards.testutils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddQuoteViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun saveQuote_withBlankText_setsValidationError() = runTest {
        val viewModel = AddQuoteViewModel(FakeQuoteRepository())

        viewModel.saveQuote()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSaved)
        assertEquals("Please enter a quote", state.errorMessage)
    }

    @Test
    fun saveQuote_withValidInput_savesAndDefaultsAuthor() = runTest {
        val repository = FakeQuoteRepository()
        val viewModel = AddQuoteViewModel(repository)

        viewModel.updateQuoteText("Fortune favors the prepared mind")
        viewModel.updateAuthor("   ")
        viewModel.saveQuote()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val savedQuotes = repository.getAllQuotes().first()

        assertTrue(state.isSaved)
        assertEquals(1, savedQuotes.size)
        assertEquals("Unknown", savedQuotes.first().author)
    }
}
