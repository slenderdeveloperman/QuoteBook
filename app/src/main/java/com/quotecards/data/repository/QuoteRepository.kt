package com.quotecards.data.repository

import com.quotecards.data.local.QuoteDao
import com.quotecards.data.local.QuoteEntity
import com.quotecards.domain.model.Quote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface QuoteRepository {
    fun getAllQuotes(): Flow<List<Quote>>
    suspend fun getQuoteById(id: Long): Quote?
    suspend fun addQuote(quote: Quote): Long
    suspend fun updateQuote(quote: Quote)
    suspend fun deleteQuote(quote: Quote)
    suspend fun deleteQuoteById(id: Long)
}

@Singleton
class QuoteRepositoryImpl @Inject constructor(
    private val quoteDao: QuoteDao
) : QuoteRepository {

    override fun getAllQuotes(): Flow<List<Quote>> {
        return quoteDao.getAllQuotes().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getQuoteById(id: Long): Quote? {
        return quoteDao.getQuoteById(id)?.toDomainModel()
    }

    override suspend fun addQuote(quote: Quote): Long {
        return quoteDao.insertQuote(QuoteEntity.fromDomainModel(quote))
    }

    override suspend fun updateQuote(quote: Quote) {
        quoteDao.updateQuote(QuoteEntity.fromDomainModel(quote))
    }

    override suspend fun deleteQuote(quote: Quote) {
        quoteDao.deleteQuote(QuoteEntity.fromDomainModel(quote))
    }

    override suspend fun deleteQuoteById(id: Long) {
        quoteDao.deleteQuoteById(id)
    }
}
