package com.quotecards.data.repository

import com.quotecards.data.local.QuoteDao
import com.quotecards.data.local.QuoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class QuoteRepositoryImplTest {

    private lateinit var quoteDao: FakeQuoteDao
    private lateinit var repository: QuoteRepositoryImpl

    @Before
    fun setUp() {
        quoteDao = FakeQuoteDao()
        repository = QuoteRepositoryImpl(quoteDao)
    }

    @Test
    fun addQuote_persistsAndReturnsGeneratedId() = runTest {
        val id = repository.addQuote(
            com.quotecards.domain.model.Quote(
                text = "The obstacle is the way",
                author = "Marcus Aurelius"
            )
        )

        val all = repository.getAllQuotes().first()

        assertTrue(id > 0)
        assertEquals(1, all.size)
        assertEquals("The obstacle is the way", all.first().text)
        assertEquals("Marcus Aurelius", all.first().author)
    }

    @Test
    fun searchQuotes_trimsInputAndMatchesTextOrAuthor() = runTest {
        repository.addQuote(
            com.quotecards.domain.model.Quote(
                text = "Stillness is the key",
                author = "Ryan Holiday"
            )
        )
        repository.addQuote(
            com.quotecards.domain.model.Quote(
                text = "No great thing is created suddenly",
                author = "Epictetus"
            )
        )

        val byText = repository.searchQuotes("  stillness ").first()
        val byAuthor = repository.searchQuotes("epictetus").first()

        assertEquals(1, byText.size)
        assertEquals("Stillness is the key", byText.first().text)
        assertEquals(1, byAuthor.size)
        assertEquals("Epictetus", byAuthor.first().author)
    }

    @Test
    fun getQuoteById_returnsNullForMissingId() = runTest {
        val missing = repository.getQuoteById(999L)
        assertEquals(null, missing)
    }

    private class FakeQuoteDao : QuoteDao {
        private val entitiesFlow = MutableStateFlow<List<QuoteEntity>>(emptyList())
        private var nextId = 1L

        override fun getAllQuotes(): Flow<List<QuoteEntity>> = entitiesFlow

        override suspend fun getQuoteById(id: Long): QuoteEntity? {
            return entitiesFlow.value.firstOrNull { it.id == id }
        }

        override suspend fun insertQuote(quote: QuoteEntity): Long {
            val id = if (quote.id == 0L) nextId++ else quote.id
            val inserted = quote.copy(id = id)
            entitiesFlow.value = listOf(inserted) + entitiesFlow.value
            return id
        }

        override suspend fun updateQuote(quote: QuoteEntity) {
            entitiesFlow.value = entitiesFlow.value.map { existing ->
                if (existing.id == quote.id) quote else existing
            }
        }

        override suspend fun deleteQuote(quote: QuoteEntity) {
            deleteQuoteById(quote.id)
        }

        override suspend fun deleteQuoteById(id: Long) {
            entitiesFlow.value = entitiesFlow.value.filterNot { it.id == id }
        }

        override suspend fun getQuoteCount(): Int = entitiesFlow.value.size

        override fun searchQuotes(query: String): Flow<List<QuoteEntity>> {
            val normalized = query.trim()
            val filtered = entitiesFlow.value.filter {
                it.text.contains(normalized, ignoreCase = true) ||
                    it.author.contains(normalized, ignoreCase = true)
            }
            return MutableStateFlow(filtered)
        }
    }
}
