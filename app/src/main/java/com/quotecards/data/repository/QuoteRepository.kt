package com.quotecards.data.repository

import android.util.Log
import com.quotecards.data.local.QuoteDao
import com.quotecards.data.local.QuoteEntity
import com.quotecards.domain.model.Quote
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "QuoteRepository"
private const val DB_TIMEOUT_MS = 5000L

interface QuoteRepository {
    fun getAllQuotes(): Flow<List<Quote>>
    suspend fun getQuoteById(id: Long): Quote?
    suspend fun addQuote(quote: Quote): Long
    suspend fun updateQuote(quote: Quote)
    suspend fun deleteQuote(quote: Quote)
    suspend fun deleteQuoteById(id: Long)
    fun searchQuotes(query: String): Flow<List<Quote>>
}

@Singleton
class QuoteRepositoryImpl @Inject constructor(
    private val quoteDao: QuoteDao
) : QuoteRepository {

    override fun getAllQuotes(): Flow<List<Quote>> {
        return quoteDao.getAllQuotes()
            .map { entities ->
                entities.mapNotNull { entity ->
                    try {
                        entity.toDomainModel()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to map entity: ${entity.id}", e)
                        null // Skip corrupted entries
                    }
                }
            }
            .catch { e ->
                Log.e(TAG, "Database error in getAllQuotes", e)
                emit(emptyList())
            }
    }

    override suspend fun getQuoteById(id: Long): Quote? {
        return try {
            withTimeout(DB_TIMEOUT_MS) {
                quoteDao.getQuoteById(id)?.toDomainModel()
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Timeout getting quote by id: $id", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting quote by id: $id", e)
            null
        }
    }

    override suspend fun addQuote(quote: Quote): Long {
        return withTimeout(DB_TIMEOUT_MS) {
            quoteDao.insertQuote(QuoteEntity.fromDomainModel(quote))
        }
    }

    override suspend fun updateQuote(quote: Quote) {
        withTimeout(DB_TIMEOUT_MS) {
            quoteDao.updateQuote(QuoteEntity.fromDomainModel(quote))
        }
    }

    override suspend fun deleteQuote(quote: Quote) {
        withTimeout(DB_TIMEOUT_MS) {
            quoteDao.deleteQuote(QuoteEntity.fromDomainModel(quote))
        }
    }

    override suspend fun deleteQuoteById(id: Long) {
        withTimeout(DB_TIMEOUT_MS) {
            quoteDao.deleteQuoteById(id)
        }
    }

    override fun searchQuotes(query: String): Flow<List<Quote>> {
        return quoteDao.searchQuotes(query.trim())
            .map { entities ->
                entities.mapNotNull { entity ->
                    try {
                        entity.toDomainModel()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to map entity in search: ${entity.id}", e)
                        null
                    }
                }
            }
            .catch { e ->
                Log.e(TAG, "Database error in searchQuotes", e)
                emit(emptyList())
            }
    }
}
